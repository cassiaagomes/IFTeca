package com.example.myapplication.data

data class SalaInfo(
    val nome: String = "",
    val vagasMaximas: Int = 0,
    val duracao: String = ""
)

data class Sala(
    val id: String = "",
    val nome: String = "",
    val vagasMaximas: Int = 0,
    val duracao: String = "",
    val vagasOcupadas: Int = 0
)