package com.example.userapp.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class TipModel(
    val id: String = "",
    val category: String = "FREE_TIPS", // FREE_TIPS, VIP_2_PLUS, VIP_3_PLUS, VIP_5_PLUS, SPECIAL_VIP
    val categoryName: String = "",
    val ticketDate: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val match: String = "",
    val kickoffTime: String = "",
    val league: String = "",
    val tipPick: String = "",
    val odd: Double = 1.0,
    val status: String = "PENDING", // PENDING, WON, LOST
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
