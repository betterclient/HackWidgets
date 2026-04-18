package dev.betterclient.hackatimewidgets.widget.weekly

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.betterclient.hackatimewidgets.api.now
import dev.betterclient.hackatimewidgets.getApi
import dev.betterclient.hackatimewidgets.widget.calculateFittingFontSize
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class WeeklyWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WeeklyWidget()
}

object WeeklyWidgetPreferences {
    val hourTarget = intPreferencesKey("HOUR_TARGET")
    val mode = intPreferencesKey("MODE") //0 for current week, 1 for last week
}

class WeeklyWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val prefs = getAppWidgetState<Preferences>(context, id)
        val mode = prefs[WeeklyWidgetPreferences.mode] ?: 0
        val hourTarget = prefs[WeeklyWidgetPreferences.hourTarget] ?: 24

        val api = getApi(context)
        val statsWeek = api?.getCodeTime(
            start = now().let {
                if (mode == 0) {
                    it.minus(it.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
                } else {
                    it.minus(7, DateTimeUnit.DAY)
                }
            }
        )?.total_seconds ?: -1

        val text = if (statsWeek == -1) {
            "Please login in the app"
        } else {
            statsWeek
                .toDuration(DurationUnit.SECONDS)
                .toComponents { hours, minutes, _, _ ->
                    "%02d:%02d".format(hours, minutes)
                }
        }

        provideContent {
            val widgetWidth = LocalSize.current.width
            val fittingSp = calculateFittingFontSize(
                text = text,
                maxWidth = widgetWidth - ((16 * 2) + 8).dp,
            )

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.background)
                    .cornerRadius(32.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(GlanceModifier.defaultWeight())

                Text(
                    text,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = fittingSp,
                    )
                )

                Spacer(GlanceModifier.defaultWeight())
                val prog = if (statsWeek == 0) 0f else (statsWeek / 3600f) / hourTarget
                val progPercent = (prog * 100).toInt()
                Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(8.dp),
                        progress = prog,
                        color = GlanceTheme.colors.primary,
                        backgroundColor = GlanceTheme.colors.onPrimary
                    )

                    Spacer(modifier = GlanceModifier.width(4.dp))

                    Text(
                        text = "$progPercent%",
                        style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onBackground)
                    )
                }
            }
        }
    }
}