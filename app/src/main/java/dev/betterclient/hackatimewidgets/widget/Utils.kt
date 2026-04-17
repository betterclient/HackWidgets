package dev.betterclient.hackatimewidgets.widget

import android.graphics.Paint
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalContext

@Composable
fun measureText(text: String, fontSize: TextUnit): Dp {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val paint = Paint()

    paint.textSize = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        fontSize.value,
        displayMetrics
    )

    return (paint.measureText(text) / displayMetrics.density).dp
}


@Composable
fun calculateFittingFontSize(
    text: String,
    maxWidth: Dp,
    maxFontSizeSp: Float = 32f,
    minFontSizeSp: Float = 1f
): TextUnit {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics

    val maxWidthPx = maxWidth.value * displayMetrics.density

    val paint = Paint()

    var low = minFontSizeSp
    var high = maxFontSizeSp

    while (high - low > 0.1f) {
        val mid = low + (high - low) / 2f

        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            mid,
            displayMetrics
        )

        val textWidthPx = paint.measureText(text)

        if (textWidthPx <= maxWidthPx) {
            low = mid
        } else {
            high = mid
        }
    }

    return low.sp
}