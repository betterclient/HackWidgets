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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.betterclient.hackatimewidgets.api.Api
import dev.betterclient.hackatimewidgets.api.clearToken
import dev.betterclient.hackatimewidgets.api.now
import kotlinx.coroutines.launch

@Composable
fun AddWidgetsUI(api: Api, startAuthFlow: () -> Unit) {
    Column(
        Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Add a widget to start!")

        var codeTime by remember { mutableIntStateOf(0) }
        Text("You did $codeTime hours of coding today")

        LaunchedEffect(Unit) {
            codeTime = api.getCodeTime(
                start = now()
            ).total_seconds / 3600
        }
    }

    Box(
        Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        Button(onClick = {
            coroutineScope.launch {
                clearToken(context)
                api.revoke()

                startAuthFlow()
            }
        }) { Text("Log out") }
    }
}