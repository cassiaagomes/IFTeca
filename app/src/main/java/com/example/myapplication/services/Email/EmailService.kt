package com.example.myapplication.services.Email

import com.example.myapplication.data.local.data.MinhaReserva

// A Interface (o "contrato")
interface EmailService {
    suspend fun enviarEmailConfirmacao(reserva: MinhaReserva, destinatario: String)
    suspend fun enviarEmailCancelamento(reserva: MinhaReserva, destinatario: String)
}
