package com.example.myapplication.services.Email

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailApiClient {

    private val remetente = "nillocoelho@gmail.com"
    private val senhaApp = "mtvm nvgc ryol tfmx" // gerada no Google

    fun send(to: String, subject: String, body: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(remetente, senhaApp)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(remetente))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
            println("📧 Email enviado para $to")

        } catch (e: MessagingException) {
            e.printStackTrace()
            println("❌ Erro ao enviar email: ${e.message}")
        }
    }
}
