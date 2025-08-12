package com.example.hangout.models

data class LoginResponse(
    val acceso: Boolean,
    val token: String,
    val rol: String
)
