package com.example.myapplication.services.Email


class GmailService(
    private val gmailApiClient: GmailApiClient
) : EmailService {
    override fun sendEmail(to: String, subject: String, body: String) {
        gmailApiClient.send(to, subject, body)
    }
}