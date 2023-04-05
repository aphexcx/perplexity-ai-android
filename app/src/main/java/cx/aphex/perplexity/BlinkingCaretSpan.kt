import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

class BlinkingCaretSpan(
    color: Int,
//    private val containingView: View
) : ReplacementSpan() {

    private var currentColor = color
    private val rect = RectF()

    var color: Int
        get() = currentColor
        set(value) {
            currentColor = value
//            containingView.invalidate()
        }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint.color = currentColor
        val width = paint.measureText(text, start, end)
        rect.set(x, top.toFloat(), x + width, bottom.toFloat())
        canvas.drawRect(rect, paint)
    }
}
