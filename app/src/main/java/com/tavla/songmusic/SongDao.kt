package com.tavla.songmusic.data.dao

import androidx.room.*
import com.tavla.songmusic.data.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY id ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs ORDER BY totalScore DESC, winRate DESC")
    fun getSongsByRanking(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Int): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Update
    suspend fun updateSong(song: Song)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("UPDATE songs SET totalScore = :score, matchesPlayed = :matches WHERE id = :id")
    suspend fun updateSongStats(id: Int, score: Int, matches: Int)
}