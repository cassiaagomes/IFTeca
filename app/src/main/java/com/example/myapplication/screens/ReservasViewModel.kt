package com.example.myapplication.screens

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
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

class ReservasViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    val userId = auth.currentUser?.uid

    private val _minhasReservas = MutableStateFlow<List<MinhaReserva>>(emptyList())
    val minhasReservas: StateFlow<List<MinhaReserva>> = _minhasReservas

    init {
        fetchMinhasReservas()
    }

    private fun fetchMinhasReservas() {
        if (userId == null) return

        val userReservasRef = db.getReference("reservas_por_usuario/$userId")

        userReservasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<MinhaReserva>()
                snapshot.children.forEach { reservaSnapshot ->
                    val reserva = reservaSnapshot.getValue(MinhaReserva::class.java)
                    reserva?.let {
                        lista.add(it)
                    }
                }
                _minhasReservas.value = lista.sortedWith(compareBy<MinhaReserva> {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.data)
                }.thenBy {
                    val parts = it.horarioInicio.split(":")
                    if (parts.size == 2) parts[0].toInt() * 60 + parts[1].toInt() else 0
                })
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao buscar reservas: ${error.message}")
            }
        })
    }

    fun cancelarReserva(reserva: MinhaReserva) {
        if (userId == null) return

        val updates = mapOf(
            "/reservas_por_usuario/$userId/${reserva.id}" to null
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener {
                println("Reserva cancelada com sucesso: ${reserva.id}")
            }
            .addOnFailureListener { e ->
                println("Erro ao cancelar reserva: ${e.message}")
            }
    }

    @SuppressLint("RestrictedApi")
    fun salvarNovaReserva(novaReserva: MinhaReserva, onComplete: (Boolean) -> Unit) {
        if (userId == null) {
            onComplete(false)
            return
        }

        val userReservaRef = db.getReference("reservas_por_usuario/$userId").child(novaReserva.id)

        val dataParaFirebase = try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(novaReserva.data)
            outputFormat.format(date)
        } catch (e: Exception) {
            novaReserva.data
        }
        val salaReservaRef = db.getReference("reservas_por_sala/${novaReserva.idSala}/$dataParaFirebase").child(novaReserva.id)

        val updates = hashMapOf<String, Any>(
            userReservaRef.path.toString() to novaReserva,
            salaReservaRef.path.toString() to novaReserva
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                println("Erro ao salvar nova reserva: ${e.message}")
                onComplete(false)
            }
    }
}