package com.example.myapplication.services.Email

interface EmailService {
    fun sendEmail(to: String, subject: String, body: String)
}