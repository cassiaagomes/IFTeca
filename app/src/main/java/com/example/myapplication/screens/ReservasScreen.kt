package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.data.MinhaReserva

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(navController: NavController, viewModel: ReservasViewModel = viewModel()) {
    val minhasReservas by viewModel.minhasReservas.collectAsStateWithLifecycle()
    var reservaParaCancelar by remember { mutableStateOf<MinhaReserva?>(null) }
    val verdeEscuro = Color(0xFF1B5E20)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Reservas", color = Color.White) },
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
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF5F5F5))
        ) {
            if (minhasReservas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Você não possui reservas no momento.")
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Suas reservas ativas:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(minhasReservas, key = { it.id }) { reserva ->
                            ReservaItem(reserva = reserva, onCancelClick = { reservaParaCancelar = reserva })
                        }
                    }
                }
            }
        }
    }

    if (reservaParaCancelar != null) {
        val reserva = reservaParaCancelar!!
        AlertDialog(
            onDismissRequest = { reservaParaCancelar = null },
            title = { Text("Cancelar Reserva") },
            text = { Text("Tem certeza que deseja cancelar a reserva da ${reserva.nomeSala} no dia ${reserva.data} das ${reserva.horarioInicio} às ${reserva.horarioFim}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelarReserva(reserva)
                        reservaParaCancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                Button(
                    onClick = { reservaParaCancelar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Voltar") }
            }
        )
    }
}

@Composable
fun ReservaItem(reserva: MinhaReserva, onCancelClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sala", color = Color.Gray, fontSize = 12.sp)
                Text(reserva.nomeSala, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Data e Hora", color = Color.Gray, fontSize = 12.sp)
                Text("${reserva.data}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${reserva.horarioInicio} - ${reserva.horarioFim}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            IconButton(onClick = onCancelClick) {
                Icon(Icons.Default.Delete, contentDescription = "Cancelar Reserva", tint = Color.Red)
            }
        }
    }
}