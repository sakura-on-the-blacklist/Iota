package edu.ph.iota.utilities

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


object CalendarUtils {


    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeekDays(referenceDate: LocalDate = LocalDate.now()): List<LocalDate> {
        return (6 downTo 0).map { referenceDate.minusDays(it.toLong()) }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.toDayName(): String =
        this.format(DateTimeFormatter.ofPattern("EEE"))   // "Mon"


    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.toDayNumber(): String =
        this.format(DateTimeFormatter.ofPattern("dd"))    // "09"

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.toMonthYear(): String =
        this.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.toFirestoreDate(): String =
        this.format(DateTimeFormatter.ISO_LOCAL_DATE)     // "2025-04-23"


    @RequiresApi(Build.VERSION_CODES.O)
    fun String.toLocalDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)



    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.isToday(): Boolean = this == LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.isPast(): Boolean = this.isBefore(LocalDate.now())

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.isEditableWindow(): Boolean {
        val today = LocalDate.now()
        return !this.isAfter(today) && this.isAfter(today.minusDays(7))
    }
}