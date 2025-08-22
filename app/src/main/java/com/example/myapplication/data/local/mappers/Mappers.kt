package com.example.myapplication.data.local.mappers

import com.example.myapplication.data.local.data.MinhaReserva
import com.example.myapplication.data.local.entity.ReservaEntity

// Esta função ensina como transformar um MinhaReserva em um ReservaEntity
fun MinhaReserva.toReservaEntity(): ReservaEntity {
    return ReservaEntity(
        localId = this.localId, // Se for uma nova reserva, o Room ignora o 0 e gera um novo ID
        id = this.id,
        idSala = this.idSala,
        nomeSala = this.nomeSala,
        data = this.data,
        horarioInicio = this.horarioInicio,
        horarioFim = this.horarioFim,
        idUsuario = this.idUsuario
    )
}

fun ReservaEntity.toMinhaReserva(): MinhaReserva {
    return MinhaReserva(
        localId = this.localId,
        id = this.id,
        idSala = this.idSala,
        nomeSala = this.nomeSala,
        data = this.data,
        horarioInicio = this.horarioInicio,
        horarioFim = this.horarioFim,
        idUsuario = this.idUsuario
        // Garanta que todos os campos necessários de MinhaReserva estão aqui
    )
}