package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.navigation.AppRoutes

@Composable
fun MenuScreen(navController: NavController, onLogout: () -> Unit) {
    val verdeEscuro = Color(0xFF1B5E20)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(70.dp).background(verdeEscuro).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Menu", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo IFTECA",
                    modifier = Modifier
                        .height(40.dp)
                        .padding(end = 8.dp)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuItem(
                text = "Visualizar Salas",
                icon = Icons.Default.MeetingRoom,
                onClick = { navController.navigate(AppRoutes.SALAS) }
            )
            MenuItem(
                text = "Minhas Reservas",
                icon = Icons.Default.CalendarToday,
                onClick = { navController.navigate(AppRoutes.RESERVAS) }
            )
            MenuItem(
                text = "Sair",
                icon = Icons.Default.Logout,
                onClick = onLogout
            )
        }
    }
}

@Composable
fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(40.dp), tint = Color(0xFF1B5E20))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}