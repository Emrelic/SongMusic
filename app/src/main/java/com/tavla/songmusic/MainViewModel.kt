package com.tavla.songmusic.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tavla.songmusic.data.database.AppDatabase
import com.tavla.songmusic.data.entity.Match
import com.tavla.songmusic.data.entity.Song
import com.tavla.songmusic.data.repository.SongRepository
import com.tavla.songmusic.utils.CsvUtils
import com.tavla.songmusic.utils.CsvValidationResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SongRepository(database.songDao(), database.matchDao())

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val allSongs = repository.getAllSongs()
    val songsByRanking = repository.getSongsByRanking()
    val allMatches = repository.getAllMatches()

    fun validateCsv(uri: Uri): CsvValidationResult {
        return CsvUtils.validateCsvFormat(getApplication(), uri)
    }

    fun loadSongsFromCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val songs = CsvUtils.readSongsFromCsv(getApplication(), uri)
                if (songs.isNotEmpty()) {
                    repository.clearAllData()
                    repository.insertSongs(songs)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "${songs.size} şarkı başarıyla yüklendi"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "CSV dosyasından şarkı okunamadı"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Hata: ${e.message}"
                )
            }
        }
    }

    fun getNextMatch() {
        viewModelScope.launch {
            val match = repository.getNextIncompleteMatch()
            if (match != null) {
                val song1 = repository.getSongById(match.song1Id)
                val song2 = repository.getSongById(match.song2Id)

                if (song1 != null && song2 != null) {
                    _uiState.value = _uiState.value.copy(
                        currentMatch = match,
                        currentSong1 = song1,
                        currentSong2 = song2
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    currentMatch = null,
                    currentSong1 = null,
                    currentSong2 = null,
                    message = "Tüm eşleşmeler tamamlandı!"
                )
            }
        }
    }

    fun completeMatch(winnerId: Int) {
        viewModelScope.launch {
            _uiState.value.currentMatch?.let { match ->
                repository.completeMatch(match.id, winnerId)
                getNextMatch() // Bir sonraki eşleşmeyi getir
            }
        }
    }

    fun getMatchProgress() {
        viewModelScope.launch {
            val total = repository.getTotalMatchCount()
            val incomplete = repository.getIncompleteMatchCount()
            val completed = total - incomplete

            _uiState.value = _uiState.value.copy(
                totalMatches = total,
                completedMatches = completed
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val currentMatch: Match? = null,
    val currentSong1: Song? = null,
    val currentSong2: Song? = null,
    val totalMatches: Int = 0,
    val completedMatches: Int = 0,
    val message: String? = null,
    val error: String? = null
)