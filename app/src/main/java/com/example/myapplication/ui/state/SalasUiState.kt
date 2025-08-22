package com.example.myapplication.ui.state

import com.example.myapplication.data.local.data.Sala

data class SalasUiState(
    val isLoading: Boolean = false, // A tela está carregando dados?
    val salas: List<Sala> = emptyList(), // A lista de salas em caso de sucesso
    val error: String? = null // Uma mensagem de erro, se houver
)