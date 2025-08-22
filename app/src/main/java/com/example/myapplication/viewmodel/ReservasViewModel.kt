package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.ReservaDao
import com.example.myapplication.data.local.data.MinhaReserva
import com.example.myapplication.data.local.mappers.toMinhaReserva
import com.example.myapplication.data.local.mappers.toReservaEntity
import com.example.myapplication.services.Email.EmailService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// O ViewModel agora recebe o DAO
class ReservasViewModel(private val reservaDao: ReservaDao, private val emailService: EmailService) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    private val _minhasReservas = MutableStateFlow<List<MinhaReserva>>(emptyList())
    val minhasReservas: StateFlow<List<MinhaReserva>> = _minhasReservas

    private val emailDeTeste = "coelho.danillo@academico.ifpb.edu.br"

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
                // Salva no banco de dados local primeiro
                val reservaEntity = novaReserva.toReservaEntity()
                reservaDao.salvar(reservaEntity)
                Log.d("ReservaDebug", "Reserva salva no SQLite com sucesso!")

                // Após salvar com sucesso, envia o e-mail
                try {
                    emailService.enviarEmailConfirmacao(novaReserva, emailDeTeste)
                    Log.d("EmailDebug", "E-mail de confirmação enviado.")
                } catch (e: Exception) {
                    Log.e("EmailDebug", "Falha ao enviar e-mail de confirmação", e)
                    // Nota: A reserva foi salva mesmo se o e-mail falhar.
                    // Você pode decidir como lidar com isso (ex: tentar reenviar depois).
                }

                onResult(true)

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
                // Cancela no banco de dados local primeiro
                val reservaEntity = reserva.toReservaEntity()
                reservaDao.deletar(reservaEntity)
                Log.d("ReservaDebug", "Reserva cancelada no SQLite com sucesso!")

                // Após cancelar com sucesso, envia o e-mail
                try {
                    emailService.enviarEmailCancelamento(reserva, emailDeTeste)
                    Log.d("EmailDebug", "E-mail de cancelamento enviado.")
                } catch (e: Exception) {
                    Log.e("EmailDebug", "Falha ao enviar e-mail de cancelamento", e)
                }

                onResult(true)

            } catch (e: Exception) {
                Log.e("ReservaDebug", "ERRO ao cancelar reserva no SQLite!", e)
                onResult(false)
            }
        }
    }
}