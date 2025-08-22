package com.example.myapplication.viewmodel

// Em um novo arquivo ReservasViewModelFactory.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.dao.ReservaDao
import com.example.myapplication.services.Email.EmailService

class ReservasViewModelFactory(private val reservaDao: ReservaDao,  private val emailService: EmailService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReservasViewModel(reservaDao, emailService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}