package dev.betterclient.hackatimewidgets.heatmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
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
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.betterclient.hackatimewidgets.api.now
import dev.betterclient.hackatimewidgets.getApi

class ActivityWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ActivityWidget()
}

class ActivityWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val missingDays = getMissingDaysCount(context)

        if (missingDays == -1 || missingDays > 7) {
            provideContent {
                Box(GlanceModifier.background(GlanceTheme.colors.background).cornerRadius(16.dp)) {
                    Text(
                        "Heatmap not ready, launch the app",
                        style = TextStyle(fontSize = 24.sp, color = GlanceTheme.colors.onBackground)
                    )
                }
            }
            return
        }

        if (missingDays > 0) {
            val api = getApi(context)
            if(api == null) return
            enqueueHeatmapJob(api.token, context)
        }

        val heatmap = loadHeatMap(context)
        if (heatmap == null) return

        provideContent {
            val size = LocalSize.current
            val density = context.resources.displayMetrics.density

            val maxRows = (size.height / 13.dp).toInt()
            val maxCols = (size.width / 13.dp).toInt()

            if (maxRows <= 0 || maxCols <= 0) return@provideContent

            val emptyEntry = HeatMapEntry(
                day = now(), 0
            )

            val totalCells = maxRows * maxCols
            val recentUsage = heatmap.usage.takeLast(totalCells)
            val missingCellsCount = totalCells - recentUsage.size
            val paddedUsage = List(missingCellsCount) { emptyEntry } + recentUsage
            val columns = paddedUsage.chunked(maxRows)

            val bmp = renderBitmap(
                width = (size.width.value * density).toInt(),
                height = (size.height.value * density).toInt(),
                columns = columns,
                gapPx = 3f * density,
                cellSizePx = 10f * density
            )

            Box(
                modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(bmp),
                    modifier = GlanceModifier.fillMaxSize(),
                    contentDescription = null
                )
            }
        }
    }

    private fun renderBitmap(
        width: Int,
        height: Int,
        columns: List<List<HeatMapEntry>>,
        cellSizePx: Float,
        gapPx: Float
    ): Bitmap {
        if (width <= 0 || height <= 0) return createBitmap(1, 1)

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        val colsCount = columns.size
        val rowsCount = columns.maxOfOrNull { it.size } ?: 0

        val gridWidth = colsCount * (cellSizePx + gapPx) - gapPx
        val gridHeight = rowsCount * (cellSizePx + gapPx) - gapPx

        val offsetX = (width - gridWidth) / 2f
        val offsetY = (height - gridHeight) / 2f

        for (x in columns.indices) {
            val column = columns[x]

            for (y in column.indices) {
                val cell = column[y]

                paint.color = cell.color.toArgb()

                val left = offsetX + x * (cellSizePx + gapPx)
                val top = offsetY + y * (cellSizePx + gapPx)

                canvas.drawRect(
                    left,
                    top,
                    left + cellSizePx,
                    top + cellSizePx,
                    paint
                )
            }
        }

        return bitmap
    }
}