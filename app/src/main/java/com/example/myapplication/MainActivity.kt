package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.MenuScreen
import com.example.myapplication.screens.SalasScreen
import com.example.myapplication.screens.ReservasScreen
import com.example.myapplication.screens.HorariosSalaScreen
import com.example.myapplication.navigation.AppRoutes

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {
                    composable(AppRoutes.LOGIN) {
                        var isLoginLoading by remember { mutableStateOf(false) }
                        var isRegisterLoading by remember { mutableStateOf(false) }

                        LoginScreen(
                            isLoginLoading = isLoginLoading,
                            isRegisterLoading = isRegisterLoading,
                            onLoginClicked = { matricula, senha ->
                                if (matricula.isBlank() || senha.isBlank()) {
                                    Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                                    return@LoginScreen
                                }
                                isLoginLoading = true
                                val email = "$matricula@aluno.com"
                                auth.signInWithEmailAndPassword(email, senha)
                                    .addOnCompleteListener { task ->
                                        isLoginLoading = false
                                        if (task.isSuccessful) {
                                            Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                            navController.navigate(AppRoutes.MENU) {
                                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                                            }
                                        } else {
                                            val error = when (task.exception) {
                                                is FirebaseAuthInvalidCredentialsException -> "Matrícula ou senha inválida."
                                                else -> "Falha no login: Verifique sua conexão."
                                            }
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    }
                            },
                            onRegisterClicked = { matricula, senha ->
                                if (matricula.isBlank() || senha.isBlank()) {
                                    Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                                    return@LoginScreen
                                }
                                isRegisterLoading = true
                                val email = "$matricula@aluno.com"
                                auth.createUserWithEmailAndPassword(email, senha)
                                    .addOnCompleteListener { task ->
                                        isRegisterLoading = false
                                        if (task.isSuccessful) {
                                            Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val error = when (task.exception) {
                                                is FirebaseAuthUserCollisionException -> "Esta matrícula já está cadastrada."
                                                else -> "Falha no cadastro: Verifique sua conexão e a senha (mínimo 6 caracteres)."
                                            }
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        )
                    }

                    composable(AppRoutes.MENU) {
                        MenuScreen(
                            navController = navController,
                            onLogout = {
                                auth.signOut()
                                navController.navigate(AppRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(AppRoutes.SALAS) { SalasScreen(navController) }
                    composable(AppRoutes.RESERVAS) { ReservasScreen(navController) }

                    composable(
                        route = "${AppRoutes.HORARIOS_SALA}/{salaId}/{turno}",
                        arguments = listOf(
                            navArgument("salaId") { type = NavType.StringType },
                            navArgument("turno") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val salaId = backStackEntry.arguments?.getString("salaId")
                        val turno = backStackEntry.arguments?.getString("turno")
                        if (salaId != null && turno != null) {
                            HorariosSalaScreen(
                                navController = navController,
                                salaId = salaId,
                                turnoSelecionado = turno
                            )
                        } else {
                            Toast.makeText(context, "Erro: Detalhes da sala ou turno ausentes.", Toast.LENGTH_SHORT).show()
                            navController.navigateUp() // Volta para a tela anterior
                        }
                    }
                }
            }
        }
    }
}