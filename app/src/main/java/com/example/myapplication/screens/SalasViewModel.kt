package com.example.myapplication.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Sala
import com.example.myapplication.data.SalaInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SalasViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas: StateFlow<List<Sala>> = _salas

    private val _reservationResult = MutableStateFlow<Result<Unit>?>(null)
    val reservationResult: StateFlow<Result<Unit>?> = _reservationResult

    fun fetchSalas(turno: String) {
        val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val salasRef = db.getReference("salas/$turno")

        salasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshotSalas: DataSnapshot) {
                val salasInfoMap = mutableMapOf<String, SalaInfo>()
                snapshotSalas.children.forEach { salaSnapshot ->
                    val salaId = salaSnapshot.key!!
                    val salaInfo = salaSnapshot.getValue(SalaInfo::class.java)
                    if (salaInfo != null) {
                        salasInfoMap[salaId] = salaInfo
                    }
                }

                val reservasRef = db.getReference("reservas/$hoje/$turno")
                reservasRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshotReservas: DataSnapshot) {
                        val listaDeSalas = salasInfoMap.map { (salaId, salaInfo) ->
                            val vagasOcupadas = snapshotReservas.child(salaId).childrenCount.toInt()
                            Sala(
                                id = salaId,
                                nome = salaInfo.nome,
                                vagasMaximas = salaInfo.vagasMaximas,
                                duracao = salaInfo.duracao,
                                vagasOcupadas = vagasOcupadas
                            )
                        }
                        _salas.value = listaDeSalas.sortedBy { it.id }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun reservarSala(salaId: String, turno: String) {
        val userId = auth.currentUser?.uid ?: return
        val hoje = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val salaInfoRef = db.getReference("salas/$turno/$salaId")

        viewModelScope.launch {
            salaInfoRef.get().addOnSuccessListener { salaSnapshot ->
                val salaInfo = salaSnapshot.getValue(SalaInfo::class.java)
                val vagasMaximas = salaInfo?.vagasMaximas ?: 0
                val reservaRef = db.getReference("reservas/$hoje/$turno/$salaId")

                reservaRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        // Usar childrenCount é mais simples e direto
                        val numReservas = currentData.childrenCount

                        if (currentData.hasChild(userId)) {
                            return Transaction.abort() // Usuário já reservou
                        }

                        if (numReservas >= vagasMaximas) {
                            return Transaction.abort() // Sala cheia
                        }

                        currentData.child(userId).value = true
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            _reservationResult.value = Result.failure(error.toException())
                        }
                        else if (committed) {
                            val userReservaPath = "/usuarios_reservas/$userId/$hoje/$turno/$salaId"
                            db.reference.child(userReservaPath).setValue(true)
                                .addOnSuccessListener {
                                    _reservationResult.value = Result.success(Unit)
                                }
                                .addOnFailureListener {
                                    _reservationResult.value = Result.failure(it)
                                }
                        } else {
                            val exception = Exception("Não foi possível reservar. A sala pode estar cheia ou você já possui uma reserva.")
                            _reservationResult.value = Result.failure(exception)
                        }
                    }
                })
            }
        }
    }

    fun clearReservationResult() {
        _reservationResult.value = null
    }
}