package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
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
import com.example.myapplication.data.Sala
import com.example.myapplication.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalasScreen(navController: NavController, salasViewModel: SalasViewModel = viewModel()) {
    val context = LocalContext.current
    val verdeEscuro = Color(0xFF1B5E20)

    val salas by salasViewModel.salas.collectAsStateWithLifecycle()

    var turnoSelecionado by remember { mutableStateOf("Manhã") }
    val turnos = listOf("Manhã", "Tarde", "Noite")
    var dropdownAberto by remember { mutableStateOf(false) }

    LaunchedEffect(turnoSelecionado) {
        salasViewModel.fetchSalas(turnoSelecionado)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salas", color = Color.White) },
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
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Olá,", fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                ExposedDropdownMenuBox(expanded = dropdownAberto, onExpandedChange = { dropdownAberto = !dropdownAberto }) {
                    TextField(
                        value = turnoSelecionado, onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAberto) },
                        modifier = Modifier.menuAnchor(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(expanded = dropdownAberto, onDismissRequest = { dropdownAberto = false }) {
                        turnos.forEach { turno ->
                            DropdownMenuItem(text = { Text(turno) }, onClick = {
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
                                navController.navigate("${AppRoutes.HORARIOS_SALA}/${sala.id}/${turnoSelecionado}")
                            } else {
                                Toast.makeText(context, "Não há vagas para esta sala.", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
        }
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
            Text(sala.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Vagas", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = "${sala.vagasOcupadas}/${sala.vagasMaximas}",
                color = corVagas,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = "Duração", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${sala.duracaoPadraoMinutos}min", fontSize = 14.sp)
            }
        }
    }
}
