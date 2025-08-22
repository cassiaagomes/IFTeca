package com.example.myapplication.ui.screens

import android.widget.Toast
import org.koin.compose.koinInject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// CORREÇÃO 1: Import necessário para collectAsStateWithLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.services.Email.EmailService
import com.example.myapplication.data.local.data.MinhaReserva // <-- Usaremos MinhaReserva
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.viewmodel.ReservasViewModelFactory
import com.example.myapplication.viewmodel.ReservasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val reservaDao = db.reservaDao()
    val emailService: EmailService = koinInject()
    val viewModel: ReservasViewModel = viewModel(
        factory = ReservasViewModelFactory(reservaDao, emailService)
    )

    // CORREÇÃO 2: Usar collectAsStateWithLifecycle para coletar um StateFlow
    val minhasReservas by viewModel.minhasReservas.collectAsStateWithLifecycle()
    // CORREÇÃO 3: O estado para cancelar deve ser do tipo MinhaReserva
    var reservaParaCancelar by remember { mutableStateOf<MinhaReserva?>(null) }
    val verdeEscuro = Color(0xFF1B5E20)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Reservas", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
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
        ) {
            if (minhasReservas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Você não possui reservas no momento.", color = Color.Black)
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Suas reservas ativas:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // A chave agora pode ser o ID local ou o do Firebase, ambos devem ser únicos
                        items(minhasReservas, key = { it.id }) { reserva ->
                            ReservaItem(reserva = reserva, onCancelClick = { reservaParaCancelar = reserva })
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmação
    if (reservaParaCancelar != null) {
        val reserva = reservaParaCancelar!!
        AlertDialog(
            onDismissRequest = { reservaParaCancelar = null },
            title = { Text("Cancelar Reserva") },
            text = {
                Text("Tem certeza que deseja cancelar a reserva da ${reserva.nomeSala} no dia ${reserva.data} das ${reserva.horarioInicio} às ${reserva.horarioFim}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // CORREÇÃO 4: Chamar a nova função 'cancelarReserva' com o callback de resultado
                        viewModel.cancelarReserva(reserva) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, "Reserva cancelada.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Falha ao cancelar.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        reservaParaCancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Confirmar", color = Color.White) }
            },
            dismissButton = {
                Button(
                    onClick = { reservaParaCancelar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Voltar", color = Color.White) }
            }
        )
    }
}

@Composable
// CORREÇÃO 5: O item da lista agora recebe um MinhaReserva
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
                Text("Sala", color = Color.Black, fontSize = 12.sp)
                Text(reserva.nomeSala, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
            }

            Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Data e Hora", color = Color.Black, fontSize = 12.sp)
                Text(reserva.data, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                Text("${reserva.horarioInicio} - ${reserva.horarioFim}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
            }

            IconButton(onClick = onCancelClick) {
                Icon(Icons.Default.Delete, contentDescription = "Cancelar Reserva", tint = Color.Red)
            }
        }
    }
}