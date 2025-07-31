package com.tavla.songmusic.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.tavla.songmusic.data.dao.MatchDao
import com.tavla.songmusic.data.dao.SongDao
import com.tavla.songmusic.data.entity.Match
import com.tavla.songmusic.data.entity.Song

@Database(
    entities = [Song::class, Match::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "song_music_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}