package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.ReservaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(reserva: ReservaEntity)

    @Delete
    suspend fun deletar(reserva: ReservaEntity)

    @Query("SELECT * FROM reservas ORDER BY data ASC")
    fun listarTodas(): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE idUsuario = :usuarioId ORDER BY data ASC")
    fun listarPorUsuarioFlow(usuarioId: String): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE idSala = :salaId AND data = :data")
    fun getReservasPorSalaEData(salaId: String, data: String): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE data = :data")
    suspend fun getReservasDoDia(data: String): List<ReservaEntity>

    // Deleta todas as reservas de um usuário específico
    @Query("DELETE FROM reservas WHERE idUsuario = :usuarioId")
    suspend fun deletarReservasDoUsuario(usuarioId: String)

    // Insere uma lista de reservas de uma vez (mais eficiente)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarLista(reservas: List<ReservaEntity>)
}
