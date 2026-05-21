package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val secondsSurvived: Int,
    val particlesCollected: Int,
    val timestamp: Long = System.currentTimeMillis()
)
