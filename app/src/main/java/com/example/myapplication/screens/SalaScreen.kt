package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.Sala
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalasScreen(navController: NavController, salasViewModel: SalasViewModel = viewModel()) {
    val context = LocalContext.current
    val verdeEscuro = Color(0xFF1B5E20)
    val vermelhoClaro= Color(0xFFFF6666)

    val salas by salasViewModel.salas.collectAsStateWithLifecycle()
    val reservationResult by salasViewModel.reservationResult.collectAsStateWithLifecycle()

    var turnoSelecionado by remember { mutableStateOf("Manhã") }
    val turnos = listOf("Manhã", "Tarde", "Noite")
    var dropdownAberto by remember { mutableStateOf(false) }
    var salaParaConfirmar by remember { mutableStateOf<Sala?>(null) }

    LaunchedEffect(turnoSelecionado) {
        salasViewModel.fetchSalas(turnoSelecionado)
    }

    LaunchedEffect(reservationResult) {
        reservationResult?.let { result ->
            val message = if(result.isSuccess) "Reservado com sucesso!" else result.exceptionOrNull()?.message ?: "Erro desconhecido"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            salasViewModel.clearReservationResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salas", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = verdeEscuro)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF5F5F5))
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Olá,", fontSize = 18.sp, color= Color.Black)
                Spacer(modifier = Modifier.weight(1f))
                ExposedDropdownMenuBox(expanded = dropdownAberto, onExpandedChange = { dropdownAberto = !dropdownAberto }) {
                    TextField(
                        value = turnoSelecionado, onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAberto) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    ExposedDropdownMenu(expanded = dropdownAberto, onDismissRequest = { dropdownAberto = false },  modifier = Modifier.background(Color.White)) {
                        turnos.forEach { turno ->
                            DropdownMenuItem(text = { Text(turno, color = Color.Black) }, onClick = {
                                turnoSelecionado = turno
                                dropdownAberto = false

                            })
                        }
                    }
                }
            }

            if (salas.isEmpty()){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text(text = "Nenhuma sala encontrada para este turno.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(salas, key = { it.id }) { sala ->
                        SalaCard(sala = sala, onClick = {
                            if (sala.vagasOcupadas < sala.vagasMaximas) {
                                salaParaConfirmar = sala
                            } else {
                                Toast.makeText(context, "Não há vagas para esta sala.", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
        }
    }

    if (salaParaConfirmar != null) {
        val sala = salaParaConfirmar!!
        val hojeFormatado = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        AlertDialog(
            onDismissRequest = { salaParaConfirmar = null },
            title = { Text("Quer confirmar a reserva?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Sala: ${sala.nome}", fontWeight = FontWeight.Bold)
                    Text("Vagas: ${sala.vagasOcupadas}/${sala.vagasMaximas}")
                    Text("Data: $hojeFormatado")
                }
            },
            confirmButton = {
                Button(onClick = {
                    salasViewModel.reservarSala(sala.id, turnoSelecionado)
                    salaParaConfirmar = null
                }, colors = ButtonDefaults.buttonColors(containerColor = verdeEscuro)) { Text("Confirmar", color= Color.White) }
            },
            dismissButton = {
                Button(onClick = { salaParaConfirmar = null }, colors = ButtonDefaults.buttonColors(containerColor = vermelhoClaro)) { Text("Cancelar", color= Color.White) }
            },
            containerColor = Color.DarkGray
        )
    }
}

@Composable
fun SalaCard(sala: Sala, onClick: () -> Unit) {
    val corVagas = if (sala.vagasOcupadas < sala.vagasMaximas) Color(0xFF1B5E20) else Color.Red
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(sala.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text("Vagas", fontSize = 14.sp, color = Color.Black)
            Text(
                text = "${sala.vagasOcupadas}/${sala.vagasMaximas}",
                color = corVagas,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = "Duração", modifier = Modifier.size(16.dp), tint = Color.DarkGray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(sala.duracao, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
