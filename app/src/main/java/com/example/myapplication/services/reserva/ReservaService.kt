package com.example.myapplication.reserva.ReservaService

import com.example.myapplication.data.local.data.MinhaReserva // Importe sua classe de dados
import com.example.myapplication.services.Email.EmailService

class ReservaService(
    private val emailService: EmailService
) {
    // 1. A função agora é 'suspend' e recebe o objeto 'MinhaReserva' completo
    suspend fun confirmarReserva(reserva: MinhaReserva, usuarioEmail: String) {
        println("✅ Reserva confirmada no sistema.")
        // 2. Chama o método correto e específico do EmailService
        emailService.enviarEmailConfirmacao(reserva, usuarioEmail)
    }

    // 1. A função agora é 'suspend' e recebe o objeto 'MinhaReserva' completo
    suspend fun cancelarReserva(reserva: MinhaReserva, usuarioEmail: String) {
        println("❌ Reserva cancelada no sistema.")
        // 2. Chama o método correto e específico do EmailService
        emailService.enviarEmailCancelamento(reserva, usuarioEmail)
    }
}