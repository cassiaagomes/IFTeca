package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.ReservaDao
import com.example.myapplication.data.local.data.MinhaReserva
import com.example.myapplication.data.local.data.Sala
import com.example.myapplication.data.local.mappers.toMinhaReserva
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
    private val reservaDao: ReservaDao // injetar DAO do Room
) : ViewModel() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _reservasDaSala = MutableStateFlow<List<MinhaReserva>>(emptyList())
    val reservasDaSala: StateFlow<List<MinhaReserva>> = _reservasDaSala

    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    val salas: StateFlow<List<Sala>> = _salas

    private val _salaSelecionada = MutableStateFlow<Sala?>(null)
    val salaSelecionada: StateFlow<Sala?> = _salaSelecionada

    fun fetchSalas(turno: String) {
        val hojeFormatado = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        Log.d("FirebaseDebug", "Iniciando fetchSalas para o turno: '$turno'")

        db.getReference("salas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshotSalas: DataSnapshot) {
                // LOG 1: Aconteceu a conexão? O Firebase retornou algo?
                Log.d("FirebaseDebug", "onDataChange foi chamado. Snapshot existe: ${snapshotSalas.exists()}. Filhos: ${snapshotSalas.childrenCount}")

                val salasBase = snapshotSalas.children.mapNotNull { salaSnapshot ->
                    salaSnapshot.getValue(Sala::class.java)?.copy(id = salaSnapshot.key ?: "")
                }
                // LOG 2: Quantas salas foram convertidas do JSON com sucesso?
                Log.d("FirebaseDebug", "Número de salas ANTES do filtro de turno: ${salasBase.size}")

                val salasDoTurno = salasBase.filter {
                    // LOG 3: Verificando cada sala
                    Log.d("FirebaseDebug", "Verificando sala '${it.nome}' com turnos: ${it.turnosDisponiveis}. Contém '$turno'? ${it.turnosDisponiveis.contains(turno)}")
                    it.turnosDisponiveis.contains(turno)
                }
                // LOG 4: Quantas salas sobraram DEPOIS do filtro?
                Log.d("FirebaseDebug", "Número de salas DEPOIS do filtro de turno: ${salasDoTurno.size}")

                if (salasDoTurno.isEmpty()) {
                    _salas.value = emptyList()
                    Log.w("FirebaseDebug", "Nenhuma sala encontrada para o turno. A lista ficará vazia.")
                    return
                }

                viewModelScope.launch(Dispatchers.IO) {
                    val reservasDoDia = reservaDao.getReservasDoDia(hojeFormatado)
                    // LOG 5: Encontrou reservas no SQLite?
                    Log.d("FirebaseDebug", "Encontradas ${reservasDoDia.size} reservas no SQLite para a data $hojeFormatado")

                    // ... o resto da sua lógica de contagem de vagas ...
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
                        // LOG 6: A lista final está sendo enviada para a UI.
                        Log.d("FirebaseDebug", "Atualizando a UI com ${salasComVagas.size} salas.")
                        _salas.value = salasComVagas.sortedBy { it.nome }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // LOG 7: O Firebase retornou um erro?
                Log.e("FirebaseDebug", "ERRO GRAVE ao buscar salas: ${error.message}")
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
                _salaSelecionada.value = Sala(
                    id = salaId,
                    nome = nomeSala,
                    vagasMaximas = vagasMaximas,
                    duracaoPadraoMinutos = duracaoPadrao,
                    turnosDisponiveis = turnos
                )
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao buscar sala por ID: ${error.message}")
                _salaSelecionada.value = null
            }
        })
    }
    fun carregarReservasDaSala(salaId: String, data: String) {
        viewModelScope.launch {
            reservaDao.getReservasPorSalaEData(salaId, data).collect { listaDeEntities ->
                // Usamos .map para converter cada item da lista
                val listaDeMinhasReservas = listaDeEntities.map { entity ->
                    entity.toMinhaReserva()
                }
                // Agora o tipo da lista é List<MinhaReserva>, que é o tipo correto!
                _reservasDaSala.value = listaDeMinhasReservas
            }
        }
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

