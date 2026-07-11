package cvc.dashingdog.wheelie

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.min

class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var slots: List<TaskSlot> = emptyList()

    // Rotation of the whole wheel, in degrees. Phase 3 (spin animation) will
    // update this value; for now it just controls the wheel's static angle.
    var rotationDegrees: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val wedgeColors = listOf(
        "#F94144", "#F3722C", "#F8961E", "#F9C74F",
        "#90BE6D", "#43AA8B", "#4D908E", "#577590",
        "#277DA1", "#9B5DE5"
    ).map { Color.parseColor(it) }

    private val wedgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 0f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private val emptyStatePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = spToPx(16f)
    }

    private val wheelBounds = RectF()

    fun setSlots(newSlots: List<TaskSlot>) {
        slots = newSlots
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val padding = 16f
        wheelBounds.set(padding, padding, size - padding, size - padding)

        if (slots.isEmpty()) {
            drawEmptyState(canvas)
            return
        }

        drawWheel(canvas)
        drawPointer(canvas)
    }

    private fun drawEmptyState(canvas: Canvas) {
        val text = "No tasks yet — tap + to add one"
        val availableWidth = (width * 0.8f).toInt().coerceAtLeast(1)

        val staticLayout = android.text.StaticLayout.Builder
            .obtain(text, 0, text.length, emptyStatePaint, availableWidth)
            .setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
            .build()

        canvas.save()
        val dx = width / 2f
        val dy = (height - staticLayout.height) / 2f
        canvas.translate(dx - availableWidth / 2f, dy)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawWheel(canvas: Canvas) {
        val sweepPerSlot = 360f / slots.size
        val textSize = calculateTextSize(slots.size)
        textPaint.textSize = textSize

        val cx = wheelBounds.centerX()
        val cy = wheelBounds.centerY()
        val radius = wheelBounds.width() / 2f

        canvas.save()
        canvas.rotate(rotationDegrees, cx, cy)

        slots.forEachIndexed { index, slot ->
            val startAngle = index * sweepPerSlot
            wedgePaint.color = wedgeColors[index % wedgeColors.size]

            canvas.drawArc(wheelBounds, startAngle, sweepPerSlot, true, wedgePaint)
            canvas.drawArc(wheelBounds, startAngle, sweepPerSlot, true, dividerPaint)

            drawSlotText(canvas, slot.text, cx, cy, radius, startAngle, sweepPerSlot)
        }

        canvas.restore()
    }

    private fun drawSlotText(
        canvas: Canvas,
        text: String,
        cx: Float,
        cy: Float,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float
    ) {
        val midAngle = startAngle + sweepAngle / 2f
        val textRadius = radius * 0.65f
        val maxTextWidth = radius - textRadius - 12f // small end-margin

        canvas.save()
        canvas.rotate(midAngle, cx, cy)
        canvas.translate(cx + textRadius, cy)
        canvas.rotate(180f) // orient text pointing inward to center
        canvas.drawText(truncate(text, maxTextWidth), 0f, 0f, textPaint)
        canvas.restore()
    }

    private fun drawPointer(canvas: Canvas) {
        val cx = wheelBounds.centerX()
        val wheelDiameter = wheelBounds.width()
        val triangleWidth = wheelDiameter * 0.06f
        val triangleHeight = wheelDiameter * 0.07f
        val overlapIntoWheel = wheelDiameter * 0.015f
        val topY = wheelBounds.top + overlapIntoWheel // slightly above the wheel's edge

        pointerPath.reset()
        pointerPath.moveTo(cx - triangleWidth / 2f, topY - triangleHeight)
        pointerPath.lineTo(cx + triangleWidth / 2f, topY - triangleHeight)
        pointerPath.lineTo(cx, topY)
        pointerPath.close()

        canvas.drawPath(pointerPath, pointerPaint)
    }

    private fun truncate(text: String, maxWidthPx: Float): String {
        if (textPaint.measureText(text) <= maxWidthPx) return text

        var end = text.length
        while (end > 0) {
            val candidate = text.substring(0, end) + "…"
            if (textPaint.measureText(candidate) <= maxWidthPx) {
                return candidate
            }
            end--
        }
        return "…"
    }

      private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    private fun calculateTextSize(slotCount: Int): Float {
        // Shrinks as slots increase. Tune these numbers once you see it on
        // a real device — this is a reasonable starting point.
        val sizeSp = when {
            slotCount <= 6 -> 16f
            slotCount <= 10 -> 13f
            slotCount <= 16 -> 10f
            else -> 8f
        }
        return spToPx(sizeSp)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Force a square view, sized to whichever dimension is smaller
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4B0082") // matches your Spin button purple — adjust if it's a different exact hex
        style = Paint.Style.FILL
    }

    private val pointerPath = android.graphics.Path()
}