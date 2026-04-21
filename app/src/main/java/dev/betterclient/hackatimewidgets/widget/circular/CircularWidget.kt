package dev.betterclient.hackatimewidgets.widget.circular

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.betterclient.hackatimewidgets.api.now
import dev.betterclient.hackatimewidgets.getApi
import dev.betterclient.hackatimewidgets.widget.calculateFittingFontSize
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CircularWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CircularWidget()
}

object CircularWidgetPreferences {
    val hourTarget = intPreferencesKey("HOUR_TARGET")
    val mode = intPreferencesKey("MODE") //0: today, 1: current week, 2: last 7 days, 3: current month, 4: last 30 days
    val background = booleanPreferencesKey("BACKGROUND")
}

enum class CircularMode(val text: String) {
    TODAY("Today"),
    CURRENT_WEEK("Current week"),
    LAST_7("Last 7 days"),
    CURRENT_MONTH("Current month"),
    LAST_30("Last 30 days")
}

class CircularWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val (text, hours) = getStatsText(context, id)

        provideContent {
            val prefs = currentState<Preferences>()
            val hourTarget = prefs[CircularWidgetPreferences.hourTarget]?: 8
            val background = prefs[CircularWidgetPreferences.background]?: false

            val widgetWidth = LocalSize.current.width
            val fittingSp = calculateFittingFontSize(
                text = text,
                maxWidth = widgetWidth - ((32 * 2) + 16).dp,
                maxFontSizeSp = 9999f
            )

            Box(
                GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clickable(actionRunCallback<RefreshCircularAction>())
                    .then(
                        if (background) {
                            GlanceModifier
                                .background(GlanceTheme.colors.background)
                                .cornerRadius(32.dp)
                        } else {
                            GlanceModifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val prog = if (hourTarget > 0) (((hours / hourTarget.toFloat()) * 100)) else 0f
                val density = context.resources.displayMetrics.density
                val bitmap = createCircularProgressBitmap(
                    width = (LocalSize.current.width.value * density).toInt(),
                    height = (LocalSize.current.height.value * density).toInt(),
                    progress = if (prog > 100) 100f else prog,
                    gapAngle = 75f
                )

                Image(
                    provider = ImageProvider(bitmap),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize()
                )

                Text(
                    text,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = fittingSp
                    )
                )
            }
        }
    }

    suspend fun getStatsText(context: Context, id: GlanceId): Pair<String, Int> {
        val prefs = getAppWidgetState<Preferences>(context, id)
        val mode = prefs[CircularWidgetPreferences.mode] ?: 0

        val api = getApi(context)
        val stats = api?.getCodeTime(
            start = now().let {
                when(mode) {
                    0 -> it
                    1 -> it.minus(it.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
                    2 -> it.minus(7, DateTimeUnit.DAY)
                    3 -> it.minus(it.day - 1, DateTimeUnit.DAY)
                    4 -> it.minus(30, DateTimeUnit.DAY)
                    else -> it
                }
            }
        )?.total_seconds ?: -1

        return (if (stats == -1) {
            "Please login in the app"
        } else {
            stats
                .toDuration(DurationUnit.SECONDS)
                .toComponents { hours, minutes, _, _ ->
                    "%02d:%02d".format(hours, minutes)
                }
        }) to (stats / 3600f).toInt()
    }


    fun createCircularProgressBitmap(
        width: Int,
        height: Int,
        progress: Float,
        trackColor: Int = "#2A2A2A".toColorInt(),
        progressColor: Int = "#00E5FF".toColorInt(),
        strokeWidthFraction: Float = 0.08f,
        gapAngle: Float = 60f
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        val strokeWidth = min(width, height) * strokeWidthFraction
        val halfStroke = strokeWidth / 2f
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - halfStroke
        val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        val sweepAngle = 360f - gapAngle
        val startAngle = 90f + gapAngle / 2f

        canvas.drawArc(oval, startAngle, sweepAngle, false, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = trackColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        })

        canvas.drawArc(oval, startAngle, sweepAngle * (progress.coerceIn(0f, 100f) / 100f), false, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = progressColor
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = Paint.Cap.ROUND
        })

        return bitmap
    }
}

class RefreshCircularAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        CircularWidget().update(context, glanceId)
    }
}