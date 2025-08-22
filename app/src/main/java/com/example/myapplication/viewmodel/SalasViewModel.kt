package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.ReservaDao
import com.example.myapplication.data.local.data.Sala
import com.example.myapplication.data.local.entity.ReservaEntity
import com.example.myapplication.ui.state.SalasUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalasViewModel(
    private val reservaDao: ReservaDao
) : ViewModel() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _salasUiState = MutableStateFlow(SalasUiState())
    val salasUiState: StateFlow<SalasUiState> = _salasUiState

    private val _reservasDaSala = MutableStateFlow<List<ReservaEntity>>(emptyList())
    val reservasDaSala: StateFlow<List<ReservaEntity>> = _reservasDaSala


    private val _salaSelecionada = MutableStateFlow<Sala?>(null)
    val salaSelecionada: StateFlow<Sala?> = _salaSelecionada

    fun fetchSalas(turno: String) {
        _salasUiState.value = SalasUiState(isLoading = true)

        val hojeFormatado = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        db.getReference("salas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshotSalas: DataSnapshot) {
                val salasBase = snapshotSalas.children.mapNotNull { salaSnapshot ->
                    salaSnapshot.getValue(Sala::class.java)?.copy(id = salaSnapshot.key ?: "")
                }

                val salasDoTurno = salasBase.filter {
                    it.turnosDisponiveis.contains(turno)
                }

                if (salasDoTurno.isEmpty()) {
                    _salasUiState.value = SalasUiState(isLoading = false, salas = emptyList())
                    return
                }

                viewModelScope.launch(Dispatchers.IO) {
                    val reservasDoDia = reservaDao.getReservasDoDia(hojeFormatado)

                    val salasComVagas = salasDoTurno.map { sala ->
                        val vagasOcupadas = reservasDoDia.count { reserva ->
                            val horaInicio = reserva.horarioInicio.split(":")[0].toIntOrNull() ?: 0
                            val isMesmaSala = reserva.idSala == sala.id
                            val isNoTurno = when (turno) {
                                "Manhã" -> horaInicio in 8..11
                                "Tarde" -> horaInicio in 13..16
                                "Noite" -> horaInicio in 18..21
                                else -> false
                            }
                            isMesmaSala && isNoTurno
                        }
                        sala.copy(vagasOcupadas = vagasOcupadas)
                    }

                    launch(Dispatchers.Main) {
                        _salasUiState.value = SalasUiState(
                            isLoading = false,
                            salas = salasComVagas.sortedBy { it.nome }
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _salasUiState.value = SalasUiState(
                    isLoading = false,
                    error = "Falha ao carregar salas: ${error.message}"
                )
            }
        })
    }

    private fun fetchReservasParaSalaEData(
        salaId: String,
        data: String,
        callback: (List<ReservaEntity>) -> Unit
    ) {
        val reservasRef = db.getReference("reservas")

        reservasRef.orderByChild("idSala").equalTo(salaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reservas = snapshot.children.mapNotNull { reservaSnapshot ->
                        val reserva = reservaSnapshot.getValue(ReservaEntity::class.java)
                        if (reserva != null && reserva.data == data) reserva else null
                    }
                    callback(reservas)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }



    fun carregarReservasDaSala(salaId: String, dataFirebaseFormat: String) {
        fetchReservasParaSalaEData(salaId, dataFirebaseFormat) { reservas ->
            _reservasDaSala.value = reservas
        }
    }

    fun fetchSalaById(salaId: String) {
        db.getReference("salas/$salaId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nomeSala = snapshot.child("nome").getValue(String::class.java) ?: "Sala Desconhecida"
                val vagasMaximas = snapshot.child("vagasMaximas").getValue(Int::class.java) ?: 0
                val duracaoPadrao = snapshot.child("duracaoPadraoMinutos").getValue(Int::class.java) ?: 60
                val turnos = snapshot.child("turnosDisponiveis").children.mapNotNull { it.getValue(String::class.java) }
                _salaSelecionada.value = Sala(
                    id = salaId,
                    nome = nomeSala,
                    vagasMaximas = vagasMaximas,
                    duracaoPadraoMinutos = duracaoPadrao,
                    turnosDisponiveis = turnos
                )
            }

            override fun onCancelled(error: DatabaseError) {
                _salaSelecionada.value = null
            }
        })
    }
}
