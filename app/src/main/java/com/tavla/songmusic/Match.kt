package com.tavla.songmusic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val song1Id: Int,
    val song2Id: Int,
    val winnerId: Int? = null, // null = henüz oynanmamış, value = kazanan şarkı ID'si
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)