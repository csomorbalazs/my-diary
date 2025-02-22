package hu.csomorbalazs.mydiary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DiaryEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryEntryDao(): DiaryEntryDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "diary.db"
                ).fallbackToDestructiveMigration().build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}