package dev.betterclient.hackatimewidgets.widget.circular

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.betterclient.hackatimewidgets.WidgetConfigActivity
import dev.betterclient.hackatimewidgets.widget.weekly.WeeklyWidgetPreferences

class CircularWidgetConfigurationScreen() : WidgetConfigActivity() {
    override val uselessWidget = CircularWidget()

    @Composable
    override fun ConfigScreen() {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Text("Configure circular widget")

            var hourTarget by getData(
                LocalContext.current,
                appWidgetId,
                CircularWidgetPreferences.hourTarget,
                8
            )

            val mode0 by getData(
                LocalContext.current,
                appWidgetId,
                CircularWidgetPreferences.mode,
                0
            )

            var mode by remember(mode0) { mutableStateOf(
                CircularMode.entries[mode0]
            ) }

            var background by getData(
                LocalContext.current,
                appWidgetId,
                CircularWidgetPreferences.background,
                false
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hour target: ")
                Spacer(Modifier.width(8.dp))

                TextField(
                    value = hourTarget.toString(),
                    onValueChange = { it ->
                        hourTarget = it.filter { it.isDigit() }.toIntOrNull() ?: hourTarget
                    },
                    modifier = Modifier.width(120.dp)
                )
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
                    Text(mode.text)
                }
                DropdownMenu(
                    expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(120.dp),
                ) {
                    CircularMode.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(it.text) },
                            onClick = { mode = it }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = background,
                    onCheckedChange = { background = !background }
                )
                Spacer(Modifier.width(8.dp))
                Text("Background")
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
                            CircularWidgetPreferences.hourTarget to hourTarget,
                            CircularWidgetPreferences.mode to mode.ordinal,
                            CircularWidgetPreferences.background to background
                        )
                    )
                }) {
                    Text("Save")
                }
            }
        }
    }
}