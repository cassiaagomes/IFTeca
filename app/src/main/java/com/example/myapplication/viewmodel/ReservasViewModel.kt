package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.ReservaDao
import com.example.myapplication.data.local.data.MinhaReserva
import com.example.myapplication.data.local.mappers.toMinhaReserva
import com.example.myapplication.data.local.mappers.toReservaEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// O ViewModel agora recebe o DAO
class ReservasViewModel(private val reservaDao: ReservaDao) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    private val _minhasReservas = MutableStateFlow<List<MinhaReserva>>(emptyList())
    val minhasReservas: StateFlow<List<MinhaReserva>> = _minhasReservas

    init {
        fetchMinhasReservas()
    }

    // AGORA BUSCA DO SQLITE
    private fun fetchMinhasReservas() {
        if (userId == null) return
        viewModelScope.launch {
            reservaDao.listarPorUsuarioFlow(userId).collect { listaDeEntities ->
                // Converte a lista de Entity para a lista de MinhaReserva para a UI
                _minhasReservas.value = listaDeEntities.map { it.toMinhaReserva() }
            }
        }
    }

    // AGORA SALVA NO SQLITE
    fun salvarNovaReserva(novaReserva: MinhaReserva, onResult: (Boolean) -> Unit) {
        Log.d("ReservaDebug", "Função 'salvarNovaReserva' iniciada com: $novaReserva")
        viewModelScope.launch {
            try {
                val reservaEntity = novaReserva.toReservaEntity()
                reservaDao.salvar(reservaEntity)
                Log.d("ReservaDebug", "Reserva salva no SQLite com sucesso!")
                onResult(true)
                // Opcional: Você pode salvar no Firebase também se precisar de um backup na nuvem
                // salvarReservaNoFirebase(novaReserva)
            } catch (e: Exception) {
                Log.e("ReservaDebug", "ERRO ao salvar reserva no SQLite!", e)
                onResult(false)
            }
        }
    }

    // AGORA CANCELA NO SQLITE
    fun cancelarReserva(reserva: MinhaReserva, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val reservaEntity = reserva.toReservaEntity()
                reservaDao.deletar(reservaEntity)
                onResult(true)
                // Opcional: Deletar do Firebase também
                // deletarReservaDoFirebase(reserva)
            } catch (e: Exception) {
                Log.e("ReservaDebug", "ERRO ao cancelar reserva no SQLite!", e)
                onResult(false)
            }
        }
    }
}