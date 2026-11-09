package com.eclassports.football.model

data class UserTip(
    val id: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val league: String = "",
    val tipPick: String = "",
    val odd: Double = 1.0,
    val status: String = "PENDING",
    val category: String = "FREE_TIPS",
    val categoryName: String = "Free Tips",
    val ticketDate: String = "",
    val kickoffTime: String = "",
    val createdAt: Long = 0L
)
