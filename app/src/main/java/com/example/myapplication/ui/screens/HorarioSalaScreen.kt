package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.data.local.data.MinhaReserva
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.viewmodel.ReservasViewModel
import com.example.myapplication.viewmodel.ReservasViewModelFactory
import com.example.myapplication.viewmodel.SalasViewModel
import com.example.myapplication.viewmodel.SalasViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosSalaScreen(
    navController: NavController,
    salaId: String,
    turnoSelecionado: String,
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val reservaDao = db.reservaDao()
    val salasViewModel: SalasViewModel = viewModel(
        factory = SalasViewModelFactory(reservaDao)
    )
    val reservaViewModel: ReservasViewModel = viewModel(
        factory = ReservasViewModelFactory(reservaDao)
    )
    val verdeEscuro = Color(0xFF1B5E20)

    val sala by salasViewModel.salaSelecionada.collectAsStateWithLifecycle()
    val reservasOcupadas by salasViewModel.reservasDaSala.collectAsStateWithLifecycle() // <-- NOVO

    var horarioSelecionado by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val hoje = remember { Calendar.getInstance() }
    val dataAtualFormatadaFirebase = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(hoje.time) }
    val dataAtualFormatadaExibicao = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(hoje.time) }

    LaunchedEffect(salaId) {
        salasViewModel.fetchSalaById(salaId)
    }

    LaunchedEffect(sala) {
        sala?.let {
            salasViewModel.carregarReservasDaSala(it.id, dataAtualFormatadaExibicao)
        }
    }
    val horariosDisponiveis = remember(sala, reservasOcupadas) {
        val currentSala = sala
        if (currentSala == null) {
            emptyList()
        } else {
            val occupiedSlots = reservasOcupadas.map { it.horarioInicio to it.horarioFim }.toSet()
            val allSlots = generateTimeSlots(turnoSelecionado, currentSala.duracaoPadraoMinutos)

            allSlots.filter { slot ->
                val (slotInicio, slotFim) = slot.split(" - ")
                !occupiedSlots.any { (reservaInicio, reservaFim) ->
                    val slotStartCal = getTimeCalendar(slotInicio)
                    val slotEndCal = getTimeCalendar(slotFim)
                    val reservaStartCal = getTimeCalendar(reservaInicio)
                    val reservaEndCal = getTimeCalendar(reservaFim)
                    (slotStartCal.before(reservaEndCal) && slotEndCal.after(reservaStartCal))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sala?.nome ?: "Carregando...", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo IFTECA",
                        modifier = Modifier
                            .height(40.dp)
                            .padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = verdeEscuro)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            Text(
                "Horários disponíveis para ${sala?.nome ?: "a sala"} em $dataAtualFormatadaExibicao:",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (sala == null) {
                // Adicionado um estado de carregamento para a sala
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }else if (horariosDisponiveis.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum horário disponível para este turno.", color = Color.Black)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(horariosDisponiveis) { horarioSlot ->
                        HorarioItem(
                            horario = horarioSlot,
                            isOcupado = false, // <-- A lista já é de disponíveis, então nunca está ocupado
                            onClick = {
                                horarioSelecionado = horarioSlot
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog && horarioSelecionado != null && sala != null) {
        val (inicio, fim) = horarioSelecionado!!.split(" - ")
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false; horarioSelecionado = null },
            title = { Text("Confirmar Reserva") },
            text = {
                Column {
                    Text("Sala: ${sala!!.nome}", fontWeight = FontWeight.Bold)
                    Text("Data: $dataAtualFormatadaExibicao", fontWeight = FontWeight.Bold)
                    Text("Horário: $inicio - $fim", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val novaReserva = MinhaReserva(
                            id = UUID.randomUUID().toString(),
                            idSala = sala!!.id,
                            nomeSala = sala!!.nome,
                            data = dataAtualFormatadaExibicao,
                            horarioInicio = inicio,
                            horarioFim = fim,
                            idUsuario = reservaViewModel.userId ?: ""
                        )
                        reservaViewModel.salvarNovaReserva(novaReserva) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, "Reserva realizada com sucesso!", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            } else {
                                Toast.makeText(context, "Falha ao realizar reserva. Tente novamente.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showConfirmDialog = false
                        horarioSelecionado = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = verdeEscuro)
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmDialog = false; horarioSelecionado = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun HorarioItem(horario: String, isOcupado: Boolean, onClick: () -> Unit) {
    val corFundo = if (isOcupado) Color(0xFFE0E0E0) else Color.White
    val corTexto = if (isOcupado) Color.Gray else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isOcupado, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = horario,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = corTexto
            )
            if (isOcupado) {
                Text(
                    text = "Ocupado",
                    fontSize = 14.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Disponível",
                    fontSize = 14.sp,
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun generateTimeSlots(turno: String, duracaoMinutos: Int): List<String> {
    val slots = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())

    val (startHour, endHour) = when (turno) {
        "Manhã" -> 8 to 12
        "Tarde" -> 13 to 17
        "Noite" -> 18 to 22
        else -> 0 to 24
    }

    calendar.set(Calendar.HOUR_OF_DAY, startHour)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val endCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, endHour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    while (calendar.time.before(endCalendar.time)) {
        val startTime = format.format(calendar.time)
        calendar.add(Calendar.MINUTE, duracaoMinutos)
        val endTime = format.format(calendar.time)
        if (calendar.time.after(endCalendar.time)) break
        slots.add("$startTime - $endTime")
    }
    return slots
}

fun getTimeCalendar(timeString: String): Calendar {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = format.parse(timeString) ?: Date(0)
    return calendar
}