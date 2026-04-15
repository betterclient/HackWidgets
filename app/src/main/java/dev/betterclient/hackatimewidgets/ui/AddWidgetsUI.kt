package dev.betterclient.hackatimewidgets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.lifecycle.coroutineScope
import dev.betterclient.hackatimewidgets.MainActivity
import dev.betterclient.hackatimewidgets.api.Api
import dev.betterclient.hackatimewidgets.api.now
import dev.betterclient.hackatimewidgets.clearToken
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

@Composable
fun AddWidgetsUI(api: Api, activity: MainActivity) {
    Column(
        Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Add a widget to start!")

        var codeTime by remember { mutableStateOf("") }
        Text("You did $codeTime hours of coding yesterday")

        LaunchedEffect(Unit) {
            codeTime = (api.getCodeTime(
                start = now().minus(1, DateTimeUnit.DAY)
            ).total_seconds / 3600).toInt().toString()
        }
    }

    Box(
        Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd
    ) {
        val ctx = rememberLifecycleOwner()
        val context = LocalContext.current
        Button(onClick = {
            ctx.lifecycle.coroutineScope.launch {
                clearToken(context)
                api.revoke()

                activity.startAuthFlow()
            }
        }) { Text("Log out") }
    }
}