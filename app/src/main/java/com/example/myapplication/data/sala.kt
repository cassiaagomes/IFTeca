package com.example.myapplication.data

data class Sala(
    val id: String = "",
    val nome: String = "",
    val vagasMaximas: Int = 0,
    val vagasOcupadas: Int = 0,
    val duracaoPadraoMinutos: Int = 60,
    val turnosDisponiveis: List<String> = emptyList()
)