package com.dev.smsspamdetector

data class Result(
    val hamProba: Double,
    val result: String,
    val spamProba: Double,
    val acc : Double
)