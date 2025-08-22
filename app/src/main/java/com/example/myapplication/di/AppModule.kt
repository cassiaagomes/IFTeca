package com.example.myapplication.di

import com.example.myapplication.reserva.ReservaService.ReservaService
import com.example.myapplication.services.Email.EmailService
import com.example.myapplication.services.Email.GmailApiClient
import com.example.myapplication.services.Email.GmailService
import org.koin.dsl.module

val appModule = module {
    single { GmailApiClient() }
    single<EmailService> { GmailService(get()) }
    single { ReservaService(get()) }
}
