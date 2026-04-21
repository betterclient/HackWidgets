package dev.betterclient.hackatimewidgets.widget.streak

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.betterclient.hackatimewidgets.R
import dev.betterclient.hackatimewidgets.api.now
import dev.betterclient.hackatimewidgets.getApi

class StreakWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = StreakWidget()
}

class StreakWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val api = getApi(context)
        if (api == null) {
            provideContent {
                Text("Login in the app")
            }
        }

        var streak = loadStreak(context) ?: generateStreak(api, context)
        val today = now()
        val codedToday = api.getCodeTime(today, today).total_seconds > 1800
        streak = updateStreakPartially(api, context, streak, today, codedToday)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(32.dp)
                    .background(GlanceTheme.colors.background)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(GlanceModifier.defaultWeight())
                Content(streak, codedToday)
                Spacer(GlanceModifier.defaultWeight())
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun Content(streak: StreakData, today: Boolean) {
        val size = LocalSize.current
        if (today) {
            Image(
                provider = ImageProvider(R.drawable.flame),
                contentDescription = null,
                modifier = GlanceModifier
                    .width(
                        size.width - 32.dp
                    ).height(
                        size.height - 64.dp
                    )
            )
        } else {
            Image(
                provider = ImageProvider(R.drawable.flame),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ColorProvider(Color.Gray)),
                modifier = GlanceModifier
                    .width(
                        size.width - 32.dp
                    ).height(
                        size.height - 64.dp
                    )
            )
        }

        Spacer(GlanceModifier.height(8.dp))
        Text(
            "${streak.streak} day${if (streak.streak != 1) "s" else ""}",
            style = TextStyle(
                color = GlanceTheme.colors.onBackground
            )
        )
    }
}