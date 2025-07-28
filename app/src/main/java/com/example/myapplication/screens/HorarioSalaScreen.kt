package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.myapplication.data.MinhaReserva
import com.example.myapplication.data.Sala
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosSalaScreen(
    navController: NavController,
    salaId: String,
    turnoSelecionado: String,
    salasViewModel: SalasViewModel = viewModel(),
    reservaViewModel: ReservasViewModel = viewModel()
) {
    val context = LocalContext.current
    val verdeEscuro = Color(0xFF1B5E20)

    val sala by salasViewModel.salaSelecionada.collectAsStateWithLifecycle()
    var horariosDisponiveis by remember { mutableStateOf<List<String>>(emptyList()) }
    var horariosOcupados by remember { mutableStateOf<List<String>>(emptyList()) }
    var horarioSelecionado by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val hoje = remember { Calendar.getInstance() }
    val dataAtualFormatadaFirebase = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(hoje.time) }
    val dataAtualFormatadaExibicao = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(hoje.time) }

    LaunchedEffect(salaId) {
        salasViewModel.fetchSalaById(salaId)
    }

    LaunchedEffect(sala, turnoSelecionado) {
        val currentSala = sala
        if (currentSala != null) {
            salasViewModel.fetchReservasParaSalaEData(currentSala.id, dataAtualFormatadaFirebase) { reservasOcupadasList ->
                val occupiedSlots = reservasOcupadasList.map { it.horarioInicio to it.horarioFim }.toSet()
                horariosOcupados = occupiedSlots.map { "${it.first} - ${it.second}" }

                val allSlots = generateTimeSlots(turnoSelecionado, currentSala.duracaoPadraoMinutos)
                val availableSlots = allSlots.filter { slot ->
                    val (slotInicio, slotFim) = slot.split(" - ")
                    !occupiedSlots.any { (reservaInicio, reservaFim) ->
                        val slotStartCal = getTimeCalendar(slotInicio)
                        val slotEndCal = getTimeCalendar(slotFim)
                        val reservaStartCal = getTimeCalendar(reservaInicio)
                        val reservaEndCal = getTimeCalendar(reservaFim)

                        (slotStartCal.before(reservaEndCal) && slotEndCal.after(reservaStartCal))
                    }
                }
                horariosDisponiveis = availableSlots
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

            if (horariosDisponiveis.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum horário disponível para este turno.", color = Color.Black)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(horariosDisponiveis) { horarioSlot ->
                        val isOcupado = horariosOcupados.contains(horarioSlot)
                        HorarioItem(
                            horario = horarioSlot,
                            isOcupado = isOcupado,
                            onClick = {
                                if (!isOcupado) {
                                    horarioSelecionado = horarioSlot
                                    showConfirmDialog = true
                                } else {
                                    Toast.makeText(context, "Este horário já está ocupado.", Toast.LENGTH_SHORT).show()
                                }
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