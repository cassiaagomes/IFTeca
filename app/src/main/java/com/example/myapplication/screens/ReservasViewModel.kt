package com.example.myapplication.screens

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
    private val userId = auth.currentUser?.uid

    private val _minhasReservas = MutableStateFlow<List<MinhaReserva>>(emptyList())
    val minhasReservas: StateFlow<List<MinhaReserva>> = _minhasReservas

    init {
        fetchMinhasReservas()
    }

    private fun fetchMinhasReservas() {
        if (userId == null) return
        val userReservasRef = db.getReference("usuarios_reservas/$userId")

        userReservasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<MinhaReserva>()
                snapshot.children.forEach { dataSnapshot ->
                    val data = dataSnapshot.key ?: ""
                    dataSnapshot.children.forEach { turnoSnapshot ->
                        val turno = turnoSnapshot.key ?: ""
                        turnoSnapshot.children.forEach { salaSnapshot ->
                            val salaId = salaSnapshot.key ?: ""
                            val dataFormatada = try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val date = inputFormat.parse(data)
                                outputFormat.format(date)
                            } catch (e: Exception) { data }

                            lista.add(
                                MinhaReserva(
                                    id = "$data-$turno-$salaId",
                                    salaId = salaId,
                                    nomeSala = "Sala ${salaId.takeLast(2)}",
                                    data = dataFormatada,
                                    turno = turno,
                                    horario = getHorarioPorTurno(turno)
                                )
                            )
                        }
                    }
                }
                _minhasReservas.value = lista.sortedByDescending { it.data }
            }

            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    fun cancelarReserva(reserva: MinhaReserva) {
        if (userId == null) return
        val dataOriginal = try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(reserva.data)
            outputFormat.format(date)
        } catch (e: Exception) { reserva.data }

        val updates = mapOf(
            "/reservas/$dataOriginal/${reserva.turno}/${reserva.salaId}/$userId" to null,
            "/usuarios_reservas/$userId/$dataOriginal/${reserva.turno}/${reserva.salaId}" to null
        )

        db.reference.updateChildren(updates)
    }

    private fun getHorarioPorTurno(turno: String): String {
        return when (turno) {
            "Manhã" -> "10:00/11:00"
            "Tarde" -> "15:00/16:00"
            "Noite" -> "19:00/20:00"
            else -> ""
        }
    }
}