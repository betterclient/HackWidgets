package dev.betterclient.hackatimewidgets.heatmap

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.betterclient.hackatimewidgets.api.Api
import dev.betterclient.hackatimewidgets.api.now
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "heatmap_store")
val HEATMAP_KEY = stringPreferencesKey("heatmap_json")

suspend fun recordHeatmap(api: Api, context: Context, updateProgress: suspend (Int) -> Unit) {
    val existing = loadHeatMap(context)

    val now = now()
    val oneYearAgo = now.minus(1, DateTimeUnit.YEAR)

    val map = existing?.usage
        ?.associateBy { it.day }
        ?.toMutableMap()
        ?: mutableMapOf()

    val lastStored = map.keys.maxOrNull()

    val startDate = when {
        lastStored == null -> oneYearAgo
        else -> lastStored.plus(1, DateTimeUnit.DAY)
    }

    var current = startDate
    var prog = 0
    while (current <= now) {
        val seconds = api.getCodeTime(
            start = current,
            end = current
        ).total_seconds.toLong()

        map[current] = HeatMapEntry(current, seconds)
        current = current.plus(1, DateTimeUnit.DAY)

        updateProgress(prog++)

        delay(250)
    }

    val todaySeconds = api.getCodeTime(
        start = now,
        end = now
    ).total_seconds.toLong()

    map[now] = HeatMapEntry(now, todaySeconds)

    val filtered = map.values
        .filter { it.day >= oneYearAgo }
        .sortedBy { it.day }

    saveHeatMap(context, HeatMap(filtered))
}

suspend fun loadHeatMap(context: Context): HeatMap? {
    val prefs = context.dataStore.data.first()
    val json = prefs[HEATMAP_KEY] ?: return null

    return Json.decodeFromString(json)
}

suspend fun saveHeatMap(context: Context, heatMap: HeatMap) {
    val json = Json.encodeToString(heatMap)

    context.dataStore.edit { prefs ->
        prefs[HEATMAP_KEY] = json
    }
}

suspend fun getMissingDaysCount(context: Context): Int {
    val heatmap = loadHeatMap(context) ?: return -1
    val usage = heatmap.usage
    if (usage.isEmpty()) return -1

    val now = now()
    val oneYearAgo = now.minus(1, DateTimeUnit.YEAR)
    val expectedDays = oneYearAgo.daysUntil(now) + 1

    val missing = expectedDays - usage.size
    return if (missing < 0) 0 else missing
}

fun enqueueHeatmapJob(apiToken: String, context: Context) {
    val workRequest = OneTimeWorkRequestBuilder<HeatmapWorker>()
        .setInputData(workDataOf("token" to apiToken))
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "widget_heatmap_update",
        ExistingWorkPolicy.KEEP,
        workRequest
    )
}

@Serializable
class HeatMap(
    val usage: List<HeatMapEntry>
)

@Serializable
class HeatMapEntry(
    val day: LocalDate,
    val seconds: Long
) {
    @Transient
    val color: Color = when(seconds) {
        in 0..100 -> Color.Gray
        in 0..3600 -> Color.Green.copy(alpha = 0.1f)
        in 3601..7200 -> Color.Green.copy(alpha = 0.5f)
        else -> Color.Green
    }
}