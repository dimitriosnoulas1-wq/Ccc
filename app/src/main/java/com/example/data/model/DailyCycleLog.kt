package com.example.data.model

data class DailyCycleLogEntry(
    val dateKey: String,
    val displayDate: String,
    val cycleDay: Int,
    val axisDays: Int,
    val multiple: Double?,
    val multiple2016: Double?,
    val multiple2020: Double?,
    val priceUsd: Double
)
