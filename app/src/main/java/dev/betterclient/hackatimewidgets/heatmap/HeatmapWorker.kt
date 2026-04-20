package dev.betterclient.hackatimewidgets.heatmap

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.betterclient.hackatimewidgets.api.Api

class HeatmapWorker(
    val context: Context,
    val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val api = Api(params.inputData.getString("token")!!)

        return try {
            setProgress(workDataOf("progress" to 0))
            recordHeatmap(api, applicationContext) {
                setProgress(workDataOf("progress" to it))
            }
            setProgress(workDataOf("progress" to 365))

            ActivityWidget().updateAll(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
