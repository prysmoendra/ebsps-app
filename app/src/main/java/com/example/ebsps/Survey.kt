package com.example.ebsps

import com.google.firebase.Timestamp

data class Survey(
    val id: String = "",
    val ownerName: String = "",
    val address: String = "",
    val mainPhotoUrl: String = "",
    val roofCondition: String = "",
    val wallCondition: String = "",
    val floorCondition: String = "",
    val sanitationCondition: String = "",
    val timestamp: Timestamp = Timestamp.now()
)


