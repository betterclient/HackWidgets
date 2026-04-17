package dev.betterclient.hackatimewidgets.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.betterclient.hackatimewidgets.api.now
import dev.betterclient.hackatimewidgets.getApi
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TodayWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TodayWidget()
}

class TodayWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val api = getApi(context)
        val statsToday = api?.getCodeTime(
            start = now() //today
        )?.total_seconds ?: -1

        val text = if (statsToday == -1) {
            "Please login in the app"
        } else {
            statsToday
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
                val prog = if (statsToday == 0) 0f else (statsToday / 3600f) / 24f
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