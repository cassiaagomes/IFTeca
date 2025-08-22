package com.example.myapplication.services.Email

import com.example.myapplication.data.local.data.MinhaReserva
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GmailService(private val apiClient: GmailApiClient) : EmailService {

    override suspend fun enviarEmailConfirmacao(reserva: MinhaReserva, destinatario: String) {
        val subject = "Confirmação de Reserva: Sala ${reserva.nomeSala}"
        val body = """
            Olá,
            
            Sua reserva para a sala "${reserva.nomeSala}" foi confirmada com sucesso!
            
            Detalhes:
            - Data: ${reserva.data}
            - Horário: ${reserva.horarioInicio} - ${reserva.horarioFim}
            
            Obrigado por usar o sistema de agendamento.
        """.trimIndent()

        // Executa o envio em uma thread de I/O para não bloquear a principal
        withContext(Dispatchers.IO) {
            apiClient.send(destinatario, subject, body)
        }
    }

    override suspend fun enviarEmailCancelamento(reserva: MinhaReserva, destinatario: String) {
        val subject = "Cancelamento de Reserva: Sala ${reserva.nomeSala}"
        val body = """
            Olá,
            
            Sua reserva para a sala "${reserva.nomeSala}" foi cancelada.
            
            Detalhes da reserva cancelada:
            - Data: ${reserva.data}
            - Horário: ${reserva.horarioInicio} - ${reserva.horarioFim}
            
            O horário agora está disponível para outros usuários.
        """.trimIndent()

        withContext(Dispatchers.IO) {
            apiClient.send(destinatario, subject, body)
        }
    }
}