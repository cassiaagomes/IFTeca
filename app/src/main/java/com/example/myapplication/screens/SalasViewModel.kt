package com.example.myapplication.screens

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.Sala
import com.example.myapplication.data.MinhaReserva
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

class SalasViewModel : ViewModel() {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas: StateFlow<List<Sala>> = _salas

    private val _salaSelecionada = MutableStateFlow<Sala?>(null)
    val salaSelecionada: StateFlow<Sala?> = _salaSelecionada

    fun fetchSalas(turno: String) {
        val hojeFormatadoFirebase = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.getReference("salas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshotSalas: DataSnapshot) {
                val salasBase = mutableListOf<Sala>()
                snapshotSalas.children.forEach { salaSnapshot ->
                    val salaId = salaSnapshot.key ?: return@forEach
                    val nomeSala = salaSnapshot.child("nome").getValue(String::class.java) ?: "Sala $salaId"
                    val vagasMaximas = salaSnapshot.child("vagasMaximas").getValue(Int::class.java) ?: 0
                    val duracaoPadrao = salaSnapshot.child("duracaoPadraoMinutos").getValue(Int::class.java) ?: 60
                    val turnos = salaSnapshot.child("turnosDisponiveis").children.mapNotNull { it.getValue(String::class.java) } // BUSCA OS TURNOS

                    val sala = Sala(
                        id = salaId,
                        nome = nomeSala,
                        vagasMaximas = vagasMaximas,
                        duracaoPadraoMinutos = duracaoPadrao,
                        turnosDisponiveis = turnos // ATRIBUI OS TURNOS
                    )
                    // Filtra as salas pelo turno selecionado aqui
                    if (sala.turnosDisponiveis.contains(turno)) {
                        salasBase.add(sala)
                    }
                }

                if (salasBase.isEmpty()) {
                    _salas.value = emptyList()
                    return
                }

                var remainingFetches = salasBase.size
                val finalSalasList = mutableListOf<Sala>()

                salasBase.forEach { salaBase ->
                    db.getReference("reservas_por_sala/${salaBase.id}/$hojeFormatadoFirebase")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(reservasSnapshot: DataSnapshot) {
                                var vagasOcupadasHoje = 0
                                reservasSnapshot.children.forEach { reservaDataSnapshot ->
                                    val reserva = reservaDataSnapshot.getValue(MinhaReserva::class.java)
                                    if (reserva != null) {
                                        val horaInicioReserva = reserva.horarioInicio.split(":")[0].toIntOrNull() ?: 0
                                        // A lógica de turno para contagem de vagas ocupadas permanece
                                        val seEncaixaNoTurno = when (turno) {
                                            "Manhã" -> horaInicioReserva >= 8 && horaInicioReserva < 12
                                            "Tarde" -> horaInicioReserva >= 13 && horaInicioReserva < 17
                                            "Noite" -> horaInicioReserva >= 18 && horaInicioReserva < 22
                                            else -> false
                                        }
                                        if (seEncaixaNoTurno) {
                                            vagasOcupadasHoje++
                                        }
                                    }
                                }

                                finalSalasList.add(
                                    salaBase.copy(vagasOcupadas = vagasOcupadasHoje)
                                )

                                remainingFetches--
                                if (remainingFetches == 0) {
                                    _salas.value = finalSalasList.sortedBy { it.nome }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                remainingFetches--
                                println("Erro ao buscar reservas para sala ${salaBase.id}: ${error.message}")
                                // Se der erro na reserva, assuma que está cheia ou ignore, dependendo da UX desejada
                                finalSalasList.add(salaBase.copy(vagasOcupadas = salaBase.vagasMaximas))
                                if (remainingFetches == 0) {
                                    _salas.value = finalSalasList.sortedBy { it.nome }
                                }
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao buscar salas: ${error.message}")
            }
        })
    }

    fun fetchSalaById(salaId: String) {
        db.getReference("salas/$salaId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nomeSala = snapshot.child("nome").getValue(String::class.java) ?: "Sala Desconhecida"
                val vagasMaximas = snapshot.child("vagasMaximas").getValue(Int::class.java) ?: 0
                val duracaoPadrao = snapshot.child("duracaoPadraoMinutos").getValue(Int::class.java) ?: 60
                val turnos = snapshot.child("turnosDisponiveis").children.mapNotNull { it.getValue(String::class.java) }
                _salaSelecionada.value = Sala(id = salaId, nome = nomeSala, vagasMaximas = vagasMaximas, duracaoPadraoMinutos = duracaoPadrao, turnosDisponiveis = turnos)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao buscar sala por ID: ${error.message}")
                _salaSelecionada.value = null
            }
        })
    }

    fun fetchReservasParaSalaEData(salaId: String, dataFirebaseFormat: String, callback: (List<MinhaReserva>) -> Unit) {
        db.getReference("reservas_por_sala/$salaId/$dataFirebaseFormat")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reservas = mutableListOf<MinhaReserva>()
                    snapshot.children.forEach { reservaSnapshot ->
                        val reserva = reservaSnapshot.getValue(MinhaReserva::class.java)
                        reserva?.let { reservas.add(it) }
                    }
                    callback(reservas)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Erro ao buscar reservas para sala $salaId e data $dataFirebaseFormat: ${error.message}")
                    callback(emptyList())
                }
            })
    }
}