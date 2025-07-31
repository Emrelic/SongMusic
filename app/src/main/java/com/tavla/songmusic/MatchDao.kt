package com.tavla.songmusic.data.dao

import androidx.room.*
import com.tavla.songmusic.data.entity.Match
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE isCompleted = 0 ORDER BY id ASC LIMIT 1")
    suspend fun getNextIncompleteMatch(): Match?

    @Query("SELECT * FROM matches WHERE song1Id = :songId OR song2Id = :songId")
    fun getMatchesForSong(songId: Int): Flow<List<Match>>

    @Query("SELECT COUNT(*) FROM matches WHERE isCompleted = 0")
    suspend fun getIncompleteMatchCount(): Int

    @Query("SELECT COUNT(*) FROM matches")
    suspend fun getTotalMatchCount(): Int

    @Insert
    suspend fun insertMatch(match: Match): Long

    @Insert
    suspend fun insertMatches(matches: List<Match>)

    @Update
    suspend fun updateMatch(match: Match)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()

    @Query("UPDATE matches SET winnerId = :winnerId, isCompleted = 1 WHERE id = :matchId")
    suspend fun completeMatch(matchId: Long, winnerId: Int)
}