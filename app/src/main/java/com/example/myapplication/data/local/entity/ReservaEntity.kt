package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservas")
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0, // chave local
    val id: String = "", // id do Firebase (se existir)
    val idSala: String = "",
    val nomeSala: String = "",
    val data: String = "",
    val horarioInicio: String = "",
    val horarioFim: String = "",
    val idUsuario: String = ""
)
