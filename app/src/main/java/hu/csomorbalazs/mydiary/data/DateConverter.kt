package hu.csomorbalazs.mydiary.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateConverter {
    private val formatter = DateTimeFormatter.ISO_DATE

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let {
            return formatter.parse(value, LocalDate::from)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }
}
