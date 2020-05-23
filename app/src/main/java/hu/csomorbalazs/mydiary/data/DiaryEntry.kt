package hu.csomorbalazs.mydiary.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) var diaryId: Long?,
    @ColumnInfo(name = "creation_date") var creationDate: Long,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "is_personal") var isPersonal: Boolean = true,
    @ColumnInfo(name = "place") var place: String? = null
)