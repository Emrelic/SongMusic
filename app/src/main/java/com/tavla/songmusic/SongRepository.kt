package com.tavla.songmusic.data.repository

import com.tavla.songmusic.data.dao.MatchDao
import com.tavla.songmusic.data.dao.SongDao
import com.tavla.songmusic.data.entity.Match
import com.tavla.songmusic.data.entity.Song
import kotlinx.coroutines.flow.Flow

class SongRepository(
    private val songDao: SongDao,
    private val matchDao: MatchDao
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun getSongsByRanking(): Flow<List<Song>> = songDao.getSongsByRanking()

    suspend fun getSongById(id: Int): Song? = songDao.getSongById(id)

    suspend fun insertSongs(songs: List<Song>) {
        songDao.insertSongs(songs)
        generateMatches(songs)
    }

    suspend fun clearAllData() {
        matchDao.deleteAllMatches()
        songDao.deleteAllSongs()
    }

    private suspend fun generateMatches(songs: List<Song>) {
        val matches = mutableListOf<Match>()

        // Her şarkıyı diğer şarkılarla eşleştir
        for (i in songs.indices) {
            for (j in i + 1 until songs.size) {
                matches.add(
                    Match(
                        song1Id = songs[i].id,
                        song2Id = songs[j].id
                    )
                )
            }
        }

        matchDao.insertMatches(matches)
    }

    // Match operations
    fun getAllMatches(): Flow<List<Match>> = matchDao.getAllMatches()

    suspend fun getNextIncompleteMatch(): Match? = matchDao.getNextIncompleteMatch()

    suspend fun completeMatch(matchId: Long, winnerId: Int) {
        matchDao.completeMatch(matchId, winnerId)
        updateSongStats()
    }

    suspend fun getIncompleteMatchCount(): Int = matchDao.getIncompleteMatchCount()

    suspend fun getTotalMatchCount(): Int = matchDao.getTotalMatchCount()

    private suspend fun updateSongStats() {
        val songs = songDao.getAllSongs()
        val matches = matchDao.getAllMatches()

        // Her şarkı için istatistikleri hesapla
        songs.collect { songList ->
            matches.collect { matchList ->
                for (song in songList) {
                    val songMatches = matchList.filter {
                        (it.song1Id == song.id || it.song2Id == song.id) && it.isCompleted
                    }

                    val totalMatches = songMatches.size
                    val wins = songMatches.count { it.winnerId == song.id }

                    songDao.updateSongStats(song.id, wins, totalMatches)
                }
            }
        }
    }
}