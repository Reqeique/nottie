package com.ultraone.nottie.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippie_")
data class Snippie(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val mainNote: String,
    val dateTime: String
)