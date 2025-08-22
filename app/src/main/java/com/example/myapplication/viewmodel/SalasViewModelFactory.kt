package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.dao.ReservaDao

class SalasViewModelFactory(
    private val reservaDao: ReservaDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalasViewModel::class.java)) {
            return SalasViewModel(reservaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
