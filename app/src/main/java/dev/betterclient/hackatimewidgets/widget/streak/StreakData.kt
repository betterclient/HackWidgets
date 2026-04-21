package dev.betterclient.hackatimewidgets.widget.streak

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.betterclient.hackatimewidgets.api.Api
import dev.betterclient.hackatimewidgets.api.now
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


val Context.dataStore by preferencesDataStore(name = "streak_store")
val STREAK_KEY = stringPreferencesKey("STREAK")

@Serializable
class StreakData(
    val streak: Int,
    val updateDate: LocalDate
)

suspend fun loadStreak(context: Context): StreakData? {
    val prefs = context.dataStore.data.first()
    val json = prefs[STREAK_KEY] ?: return null

    return Json.decodeFromString(json)
}

suspend fun saveStreak(context: Context, streak: StreakData) {
    val json = Json.encodeToString(streak)

    context.dataStore.edit { prefs ->
        prefs[STREAK_KEY] = json
    }
}

suspend fun generateStreak(
    api: Api,
    context: Context
): StreakData {
    var streak = 0
    var date = now().minus(1, DateTimeUnit.DAY)
    var codeTime = api.getCodeTime(start = date, end = date).total_seconds

    while (codeTime > 1800) {
        streak++
        date = date.minus(1, DateTimeUnit.DAY)
        codeTime = api.getCodeTime(start = date, end = date).total_seconds
    }

    val today = now()
    val codedToday = api.getCodeTime(today, today).total_seconds > 1800

    if (codedToday) {
        streak++
    }

    return StreakData(
        streak = streak,
        updateDate = if (codedToday) today else today.minus(1, DateTimeUnit.DAY)
    ).also {
        saveStreak(context, it)
    }
}

suspend fun updateStreakPartially(
    api: Api,
    context: Context,
    streak: StreakData,
    today: LocalDate,
    codedToday: Boolean
): StreakData {
    val days = streak.updateDate.daysUntil(today)

    if (days < 1) {
        return streak
    }

    var currentStreak = streak.streak
    var lastUpdate: LocalDate

    for (i in 1..days) {
        val date = streak.updateDate.plus(i, DateTimeUnit.DAY)
        if (date == today) break

        val seconds = api.getCodeTime(date, date).total_seconds

        if (seconds > 1800) {
            currentStreak++
        } else {
            currentStreak = 0
        }
    }

    lastUpdate = today.minus(1, DateTimeUnit.DAY)

    if (codedToday) {
        currentStreak++
        lastUpdate = today
    }

    return StreakData(currentStreak, lastUpdate).also {
        saveStreak(context, it)
    }
}
