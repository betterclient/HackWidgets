package dev.betterclient.hackatimewidgets.widget.weekly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.betterclient.hackatimewidgets.WidgetConfigActivity
import dev.betterclient.hackatimewidgets.widget.today.TodayWidgetPreferences
import kotlin.math.roundToInt

class WeeklyWidgetConfigurationScreen() : WidgetConfigActivity() {
    override val uselessWidget = WeeklyWidget()

    @Composable
    override fun ConfigScreen() {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Text("Configure weekly widget")

            var hourTarget by getData(
                LocalContext.current,
                appWidgetId,
                WeeklyWidgetPreferences.hourTarget,
                8
            )

            var mode by getData(
                LocalContext.current,
                appWidgetId,
                WeeklyWidgetPreferences.mode,
                0
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hour target: ")
                Spacer(Modifier.width(8.dp))

                Slider(
                    value = hourTarget.toFloat(),
                    valueRange = 1f..168f,
                    onValueChange = { it ->
                        hourTarget = it.roundToInt()
                    },
                    modifier = Modifier.width(120.dp),
                )
                Spacer(Modifier.weight(1f))

                Text("$hourTarget hour${if(hourTarget == 1) "" else "s"}")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mode: ")
                Spacer(Modifier.width(8.dp))

                var expanded by remember { mutableStateOf(false) }
                Button(onClick = {
                    expanded = true
                }) {
                    Text(if (mode == 0) "Current week" else "Last 7 days")
                }
                DropdownMenu(
                    expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(120.dp),
                ) {
                    DropdownMenuItem(
                        text = { Text("Current Week") },
                        onClick = { mode = 0 }
                    )
                    DropdownMenuItem(
                        text = { Text("Last 7 days") },
                        onClick = { mode = 1 }
                    )
                }
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
                            WeeklyWidgetPreferences.hourTarget to hourTarget,
                            WeeklyWidgetPreferences.mode to mode
                        )
                    )
                }) {
                    Text("Save")
                }
            }
        }
    }
}