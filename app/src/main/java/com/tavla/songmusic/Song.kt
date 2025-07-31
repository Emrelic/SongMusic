package com.tavla.songmusic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Int,
    val albumName: String,
    val songName: String,
    var totalScore: Int = 0,
    var matchesPlayed: Int = 0
) {
    val winRate: Float
        get() = if (matchesPlayed > 0) totalScore.toFloat() / matchesPlayed else 0f
}