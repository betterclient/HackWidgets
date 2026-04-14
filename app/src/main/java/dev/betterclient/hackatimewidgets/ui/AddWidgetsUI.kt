package dev.betterclient.hackatimewidgets.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.lifecycle.coroutineScope
import dev.betterclient.hackatimewidgets.Api
import dev.betterclient.hackatimewidgets.clearToken
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Composable
fun AddWidgetsUI(api: Api) {
    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text("Add a widget to start!")
    }

    Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd
    ) {
        val ctx = rememberLifecycleOwner()
        val context = LocalContext.current
        Button(onClick = {
            ctx.lifecycle.coroutineScope.launch {
                clearToken(context)
                api.revoke()
                exitProcess(0)
            }

        }) { Text("Log out") }
    }
}