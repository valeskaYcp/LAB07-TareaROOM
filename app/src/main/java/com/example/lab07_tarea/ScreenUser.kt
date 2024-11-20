package com.example.lab07_tarea

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.security.MessageDigest


fun cifrarContrasena(contrasena: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(contrasena.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db: UserDatabase = crearDatabase(context)
    val dao = db.userDao()

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var userList by remember { mutableStateOf(listOf<User>()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            userList = dao.getAll()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Registrar Usuario",
                fontSize = 24.sp,
                color = Color(0xFF388E3C),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                colors = customTextFieldColors(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                colors = customTextFieldColors(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                colors = customTextFieldColors(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo") },
                colors = customTextFieldColors(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                colors = customTextFieldColors(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = confirmarContrasena,
                onValueChange = { confirmarContrasena = it },
                label = { Text("Confirmar Contraseña") },
                colors = customTextFieldColors(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    when {
                        contrasena != confirmarContrasena -> mensajeError = "Las contraseñas no coinciden."
                        nombre.isEmpty() || apellidos.isEmpty() || username.isEmpty() || correo.isEmpty() || contrasena.isEmpty() -> mensajeError = "Por favor completa todos los campos."
                        else -> {
                            val contrasenaCifrada = cifrarContrasena(contrasena)
                            val user = User(
                                nombre = nombre,
                                apellidos = apellidos,
                                username = username,
                                correo = correo,
                                contrasena = contrasenaCifrada
                            )

                            coroutineScope.launch {
                                AgregarUsuario(user, dao)
                                userList = dao.getAll()
                                nombre = ""
                                apellidos = ""
                                username = ""
                                correo = ""
                                contrasena = ""
                                confirmarContrasena = ""
                                mensajeError = "Usuario agregado exitosamente."
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Registrarse", fontSize = 16.sp)
            }

            if (mensajeError.isNotEmpty()) {
                Text(
                    text = mensajeError,
                    color = if (mensajeError == "Usuario agregado exitosamente.") Color(0xFF388E3C) else Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun customTextFieldColors() = androidx.compose.material3.TextFieldDefaults.textFieldColors(
    containerColor = Color(0xFFF1F8E9),
    focusedIndicatorColor = Color(0xFF388E3C),
    unfocusedIndicatorColor = Color.Gray,
    cursorColor = Color(0xFF388E3C),
    focusedLabelColor = Color(0xFF388E3C),
    unfocusedLabelColor = Color.Gray
)

@Composable
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("UserDatabase", "Base de datos creada.")
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d("UserDatabase", "Base de datos abierta.")
            }
        })
        .build()
}


// Función para agregar usuario
suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
        Log.d("User", "Usuario agregado: ${user.nombre} ${user.apellidos}")
    } catch (e: Exception) {
        Log.e("User", "Error al agregar usuario: ${e.message}")
    }
}

