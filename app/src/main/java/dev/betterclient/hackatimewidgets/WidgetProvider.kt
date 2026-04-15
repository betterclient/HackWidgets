package dev.betterclient.hackatimewidgets

import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.betterclient.hackatimewidgets.api.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class HackWidgetProvider : GlanceAppWidgetReceiver() {
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
                maxWidth = widgetWidth - 8.dp,
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
                LinearProgressIndicator(
                    modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                    progress = if (statsToday == 0) 0f else (statsToday / 3600f) / 24f,
                    color = GlanceTheme.colors.primary,
                    backgroundColor = GlanceTheme.colors.onPrimary
                )
            }
        }
    }
}

@Composable
fun calculateFittingFontSize(
    text: String,
    maxWidth: Dp,
    maxFontSizeSp: Float = 32f,
    minFontSizeSp: Float = 10f
): TextUnit {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics

    val maxWidthPx = maxWidth.value * displayMetrics.density

    val paint = Paint()

    var low = minFontSizeSp
    var high = maxFontSizeSp

    while (high - low > 0.1f) {
        val mid = low + (high - low) / 2f

        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            mid,
            displayMetrics
        )

        val textWidthPx = paint.measureText(text)

        if (textWidthPx <= maxWidthPx) {
            low = mid
        } else {
            high = mid
        }
    }

    return low.sp
}