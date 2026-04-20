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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dev.betterclient.hackatimewidgets.api.Api
import dev.betterclient.hackatimewidgets.api.clearToken
import dev.betterclient.hackatimewidgets.api.now
import dev.betterclient.hackatimewidgets.heatmap.HeatmapWorker
import kotlinx.coroutines.launch

@Composable
fun AddWidgetsUI(api: Api, startAuthFlow: () -> Unit) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        val request = OneTimeWorkRequestBuilder<HeatmapWorker>()
            .setInputData(Data.Builder().putString("token", api.token).build())
            .build()

        WorkManager.getInstance(ctx).enqueueUniqueWork(
            "heatmap_job",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    val workManager = WorkManager.getInstance(ctx)

    val workInfos by workManager
        .getWorkInfosForUniqueWorkLiveData("heatmap_job")
        .observeAsState()

    val info = workInfos?.firstOrNull()
    val progress = info?.progress?.getInt("progress", 0) ?: 360

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

        if (info?.state != WorkInfo.State.SUCCEEDED) {
            Text("The heatmap widget is not ready")
            Text("Heatmap progress: $progress/365")
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