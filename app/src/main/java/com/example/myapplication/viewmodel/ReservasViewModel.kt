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

class ReservasViewModel(
    private val reservaDao: ReservaDao,
    private val emailService: EmailService
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    // ADICIONE ESTA LINHA QUE ESTÁ FALTANDO
    private val db = FirebaseDatabase.getInstance()

    val userId = auth.currentUser?.uid

    private val _minhasReservas = MutableStateFlow<List<MinhaReserva>>(emptyList())
    val minhasReservas: StateFlow<List<MinhaReserva>> = _minhasReservas

    private val emailDeTeste = "coelho.danillo@academico.ifpb.edu.br"

    init {
        fetchMinhasReservas()
    }

    private fun fetchMinhasReservas() {
        if (userId == null) return

        val userReservasRef = db.getReference("reservas_por_usuario/$userId")

        userReservasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ReservaSync", "Dados de reserva do usuário alterados no Firebase. Sincronizando...")
                val listaFirebase = snapshot.children.mapNotNull {
                    it.getValue(MinhaReserva::class.java)
                }

                _minhasReservas.value = listaFirebase.sortedBy { it.data } // Simplificado

                viewModelScope.launch(Dispatchers.IO) {
                    reservaDao.deletarReservasDoUsuario(userId)
                    val entities = listaFirebase.map { it.toReservaEntity() }
                    reservaDao.salvarLista(entities)
                    Log.d("ReservaSync", "${entities.size} reservas sincronizadas para o SQLite.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReservaSync", "Erro ao ouvir reservas do Firebase: ${error.message}")
            }
        })
    }

    fun salvarNovaReserva(novaReserva: MinhaReserva, onResult: (Boolean) -> Unit) {
        if (userId == null) {
            onResult(false)
            return
        }

        Log.d("ReservaSync", "Iniciando salvamento no Firebase...")
        // Use um formato de data consistente para o Firebase (yyyy-MM-dd é bom para ordenação)
        val dataParaFirebase = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(novaReserva.data)!!
            )
        } catch (e: Exception) {
            // Fallback em caso de erro de parse, embora improvável
            novaReserva.data.split("/").reversed().joinToString("-")
        }

        val updates = mapOf(
            "/reservas_por_usuario/$userId/${novaReserva.id}" to novaReserva,
            "/reservas_por_sala/${novaReserva.idSala}/$dataParaFirebase/${novaReserva.id}" to novaReserva
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("ReservaSync", "Sucesso ao salvar no Firebase. Agora, salvando no cache SQLite...")
                viewModelScope.launch {
                    try {
                        reservaDao.salvar(novaReserva.toReservaEntity())
                        emailService.enviarEmailConfirmacao(novaReserva, emailDeTeste) // Envia o e-mail
                        Log.d("ReservaSync", "Sucesso ao salvar no cache SQLite e enviar e-mail.")
                        onResult(true)
                    } catch (e: Exception) {
                        Log.e("ReservaSync", "Firebase OK, mas falha ao salvar no SQLite ou enviar e-mail!", e)
                        onResult(false)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ReservaSync", "ERRO ao salvar no Firebase!", e)
                onResult(false)
            }
    }

    fun cancelarReserva(reserva: MinhaReserva, onResult: (Boolean) -> Unit) {
        if (userId == null) {
            onResult(false)
            return
        }

        // Deleta do Firebase primeiro
        val dataParaFirebase = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(reserva.data)!!
            )
        } catch (e: Exception) {
            reserva.data.split("/").reversed().joinToString("-")
        }

        val updates = mapOf<String, Any?>(
            "/reservas_por_usuario/$userId/${reserva.id}" to null,
            "/reservas_por_sala/${reserva.idSala}/$dataParaFirebase/${reserva.id}" to null
        )

        db.reference.updateChildren(updates).addOnSuccessListener {
            Log.d("ReservaSync", "Reserva deletada do Firebase. Agora, deletando do SQLite...")
            viewModelScope.launch {
                try {
                    reservaDao.deletar(reserva.toReservaEntity())
                    emailService.enviarEmailCancelamento(reserva, emailDeTeste) // Envia o e-mail
                    Log.d("ReservaDebug", "Reserva cancelada no SQLite e e-mail enviado com sucesso!")
                    onResult(true)
                } catch (e: Exception) {
                    Log.e("ReservaDebug", "ERRO ao cancelar reserva no SQLite ou enviar e-mail!", e)
                    onResult(false)
                }
            }
        }.addOnFailureListener { e ->
            Log.e("ReservaSync", "ERRO ao deletar do Firebase!", e)
            onResult(false)
        }
    }
}