package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun LoginScreen(
    isLoginLoading: Boolean,
    isRegisterLoading: Boolean,
    onLoginClicked: (String, String) -> Unit,
    onRegisterClicked: (String, String) -> Unit,
) {
    val verdeEscuro = Color(0xFF1B5E20)
    val cinzaClaroFundo = Color(0xFFF5F5F5)
    var matricula by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var salvarDados by remember { mutableStateOf(false) }
    var senhaVisivel by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(cinzaClaroFundo)) {
        Box(modifier = Modifier.fillMaxWidth().height(70.dp).background(verdeEscuro).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            Text(text = "Login", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Matrícula", modifier = Modifier.fillMaxWidth(), color = Color.DarkGray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = matricula, onValueChange = { matricula = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, focusedBorderColor = verdeEscuro, unfocusedBorderColor = Color.Transparent, focusedTextColor = verdeEscuro, unfocusedTextColor = verdeEscuro, cursorColor = verdeEscuro),
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Ícone de usuário") }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Senha", modifier = Modifier.fillMaxWidth(), color = Color.DarkGray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = senha, onValueChange = { senha = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, focusedBorderColor = verdeEscuro, unfocusedBorderColor = Color.Transparent, focusedTextColor = verdeEscuro, unfocusedTextColor = verdeEscuro, cursorColor = verdeEscuro),
                singleLine = true, visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (senhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) { Icon(imageVector = image, contentDescription = "Mudar visibilidade") }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = salvarDados, onCheckedChange = { salvarDados = it }, colors = CheckboxDefaults.colors(checkedColor = verdeEscuro, uncheckedColor = Color.Gray))
                Text("Salvar", fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { /* TODO: Ação de esquecer senha */ }) {
                    Text("Esqueceu a senha?", color = verdeEscuro, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onLoginClicked(matricula, senha) },
                enabled = !isLoginLoading && !isRegisterLoading,
                colors = ButtonDefaults.buttonColors(containerColor = verdeEscuro), shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoginLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Entrar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onRegisterClicked(matricula, senha) },
                enabled = !isLoginLoading && !isRegisterLoading,
                colors = ButtonDefaults.buttonColors(containerColor = verdeEscuro),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isRegisterLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Cadastrar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        LoginScreen(
            isLoginLoading = false,
            isRegisterLoading = false,
            onLoginClicked = { _, _ -> },
            onRegisterClicked = { _, _ -> }
        )
    }
}