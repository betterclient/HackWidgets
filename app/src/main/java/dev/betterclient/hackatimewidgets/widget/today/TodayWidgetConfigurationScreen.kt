package dev.betterclient.hackatimewidgets.widget.today

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dev.betterclient.hackatimewidgets.WidgetConfigActivity
import kotlin.math.roundToInt

class TodayWidgetConfigureScreen() : WidgetConfigActivity() {
    override val uselessWidget = TodayWidget()

    @Composable
    override fun ConfigScreen() {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))
            Text("Configure today widget")

            var value by getData(
                LocalContext.current,
                appWidgetId,
                TodayWidgetPreferences.hourTarget,
                8
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hour target: ")
                Spacer(Modifier.width(8.dp))

                Slider(
                    value = value.toFloat(),
                    valueRange = 1f..23f,
                    onValueChange = { it ->
                        value = it.roundToInt()
                    },
                    modifier = Modifier.width(120.dp),
                )
                Spacer(Modifier.weight(1f))

                Text("$value hour${if(value == 1) "" else "s"}")
            }

            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    finish()
                }) {
                    Text("Cancel")
                }

                Spacer(Modifier.weight(1f))

                Button(onClick = {
                    saveAndFinish(
                        listOf(
                            TodayWidgetPreferences.hourTarget to value
                        )
                    )
                }) {
                    Text("Save")
                }
            }
        }
    }
}