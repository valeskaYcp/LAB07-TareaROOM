package com.example.lab07_tarea

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "nombre") val nombre: String?,
    @ColumnInfo(name = "apellidos") val apellidos: String?,
    @ColumnInfo(name = "username") val username: String?,
    @ColumnInfo(name = "correo") val correo: String?,
    @ColumnInfo(name = "contrasena") val contrasena: String?
)
