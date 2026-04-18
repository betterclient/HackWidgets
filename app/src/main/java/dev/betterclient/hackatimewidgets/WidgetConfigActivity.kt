package dev.betterclient.hackatimewidgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import dev.betterclient.hackatimewidgets.ui.setContent0
import kotlinx.coroutines.launch

abstract class WidgetConfigActivity() : ComponentActivity() {
    protected var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
        private set

    abstract val uselessWidget: GlanceAppWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        setContent0 {
            ConfigScreen()
        }
    }

    @Composable
    abstract fun ConfigScreen()

    protected fun saveAndFinish(data: List<Preferences.Pair<*>>) {
        val context = this
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

            updateAppWidgetState(context, glanceId) { prefs ->
                data.forEach { pair ->
                    prefs.plusAssign(pair)
                }
            }

            uselessWidget.update(context, glanceId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    //utility
    @Composable
    fun <T> getData(context: Context, appWidgetId: Int, key: Preferences.Key<T>, defaultValue: T): MutableState<T> {
        val data = remember(key) { mutableStateOf(defaultValue) }

        LaunchedEffect(key) {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

            val prefs = getAppWidgetState(
                context,
                PreferencesGlanceStateDefinition,
                glanceId
            )
            data.value = prefs[key]?: defaultValue
        }

        return data
    }
}