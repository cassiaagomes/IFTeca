package com.example.myapplication.reserva.ReservaService

import com.example.myapplication.services.Email.EmailService

class ReservaService(
    private val emailService: EmailService
) {
    fun confirmarReserva(usuarioEmail: String) {
        println("✅ Reserva confirmada no sistema.")
        emailService.sendEmail(
            usuarioEmail,
            "Reserva Confirmada",
            "Sua reserva da sala foi confirmada com sucesso!"
        )
    }

    fun cancelarReserva(usuarioEmail: String) {
        println("❌ Reserva cancelada no sistema.")
        emailService.sendEmail(
            usuarioEmail,
            "Reserva Cancelada",
            "Sua reserva da sala foi cancelada."
        )
    }
}
