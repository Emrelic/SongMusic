package com.tavla.songmusic.utils

import android.content.Context
import android.net.Uri
import com.opencsv.CSVReader
import com.tavla.songmusic.data.entity.Song
import java.io.InputStreamReader

object CsvUtils {

    fun readSongsFromCsv(context: Context, uri: Uri): List<Song> {
        val songs = mutableListOf<Song>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = CSVReader(InputStreamReader(inputStream))
                val lines = reader.readAll()

                // İlk satır başlık olabilir, kontrol et
                val startIndex = if (lines.isNotEmpty() &&
                    (lines[0][0].lowercase().contains("id") ||
                            lines[0][0].lowercase().contains("numara") ||
                            !lines[0][0].matches(Regex("\\d+")))) 1 else 0

                for (i in startIndex until lines.size) {
                    val line = lines[i]
                    if (line.size >= 3) {
                        try {
                            val id = line[0].trim().toInt()
                            val albumName = line[1].trim()
                            val songName = line[2].trim()

                            if (albumName.isNotEmpty() && songName.isNotEmpty()) {
                                songs.add(Song(id, albumName, songName))
                            }
                        } catch (e: NumberFormatException) {
                            // Geçersiz ID, bu satırı atla
                            continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return songs
    }

    fun validateCsvFormat(context: Context, uri: Uri): CsvValidationResult {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = CSVReader(InputStreamReader(inputStream))
                val lines = reader.readAll()

                if (lines.isEmpty()) {
                    return CsvValidationResult(false, "CSV dosyası boş")
                }

                val firstLine = lines[0]
                if (firstLine.size < 3) {
                    return CsvValidationResult(false, "CSV dosyası en az 3 sütun içermelidir (ID, Albüm, Şarkı)")
                }

                // İlk veri satırını kontrol et
                val dataStartIndex = if (firstLine[0].lowercase().contains("id") ||
                    firstLine[0].lowercase().contains("numara") ||
                    !firstLine[0].matches(Regex("\\d+"))) 1 else 0

                if (dataStartIndex >= lines.size) {
                    return CsvValidationResult(false, "CSV dosyasında veri bulunamadı")
                }

                val firstDataLine = lines[dataStartIndex]
                try {
                    firstDataLine[0].trim().toInt()
                } catch (e: NumberFormatException) {
                    return CsvValidationResult(false, "İlk sütun sayısal ID içermelidir")
                }

                val songCount = lines.size - dataStartIndex
                return CsvValidationResult(true, "$songCount şarkı bulundu")
            }
        } catch (e: Exception) {
            return CsvValidationResult(false, "Dosya okuma hatası: ${e.message}")
        }

        return CsvValidationResult(false, "Bilinmeyen hata")
    }
}

data class CsvValidationResult(
    val isValid: Boolean,
    val message: String
)