package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ReservaDao
import com.example.myapplication.data.local.entity.ReservaEntity

class ReservaRepository(private val dao: ReservaDao) {

    suspend fun salvarLocal(reserva: ReservaEntity) {
        dao.salvar(reserva)
    }

    suspend fun deletarReserva(reserva: ReservaEntity) {
        dao.deletar(reserva)
    }

    fun listarReservas(): kotlinx.coroutines.flow.Flow<List<ReservaEntity>> {
        return dao.listarTodas() // retorna Flow para poder usar collectAsStateWithLifecycle
    }

    fun listarReservasPorUsuario(usuarioId: String): kotlinx.coroutines.flow.Flow<List<ReservaEntity>> {
        return dao.listarPorUsuarioFlow(usuarioId) // caso queira filtrar por usuário
    }
}

