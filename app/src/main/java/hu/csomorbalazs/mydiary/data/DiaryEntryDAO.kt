package hu.csomorbalazs.mydiary.data

import androidx.room.*

@Dao
interface DiaryEntryDAO {

    @Query("SELECT * FROM diary")
    fun getAllDiaryEntries(): List<DiaryEntry>

    @Insert
    fun addDiaryEntry(entry: DiaryEntry): Long

    @Delete
    fun deleteDiaryEntry(entry: DiaryEntry)

    @Update
    fun updateDiaryEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary")
    fun deleteAllDiaryEntries();
}