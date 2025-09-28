package com.pykens.earthzoo.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.pykens.earthzoo.R
import kotlin.math.abs

private data class Continent(
    @StringRes val nameRes: Int,
    val polygon: List<PointF>
)

class InteractiveEarthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val continents = listOf(
        Continent(
            R.string.continent_north_america,
            listOf(
                point(0.05f, 0.22f),
                point(0.09f, 0.16f),
                point(0.16f, 0.12f),
                point(0.24f, 0.09f),
                point(0.31f, 0.16f),
                point(0.30f, 0.25f),
                point(0.24f, 0.30f),
                point(0.20f, 0.35f),
                point(0.15f, 0.45f),
                point(0.08f, 0.43f)
            )
        ),
        Continent(
            R.string.continent_south_america,
            listOf(
                point(0.26f, 0.48f),
                point(0.30f, 0.52f),
                point(0.34f, 0.62f),
                point(0.35f, 0.72f),
                point(0.32f, 0.82f),
                point(0.28f, 0.90f),
                point(0.24f, 0.78f),
                point(0.22f, 0.62f)
            )
        ),
        Continent(
            R.string.continent_europe,
            listOf(
                point(0.48f, 0.18f),
                point(0.52f, 0.14f),
                point(0.60f, 0.12f),
                point(0.66f, 0.17f),
                point(0.64f, 0.24f),
                point(0.56f, 0.26f),
                point(0.50f, 0.24f)
            )
        ),
        Continent(
            R.string.continent_africa,
            listOf(
                point(0.48f, 0.30f),
                point(0.54f, 0.30f),
                point(0.60f, 0.36f),
                point(0.64f, 0.48f),
                point(0.60f, 0.64f),
                point(0.54f, 0.72f),
                point(0.48f, 0.70f),
                point(0.44f, 0.54f)
            )
        ),
        Continent(
            R.string.continent_asia,
            listOf(
                point(0.66f, 0.13f),
                point(0.74f, 0.10f),
                point(0.84f, 0.12f),
                point(0.92f, 0.20f),
                point(0.94f, 0.32f),
                point(0.90f, 0.40f),
                point(0.82f, 0.42f),
                point(0.74f, 0.34f),
                point(0.70f, 0.24f)
            )
        ),
        Continent(
            R.string.continent_australia,
            listOf(
                point(0.76f, 0.66f),
                point(0.82f, 0.64f),
                point(0.88f, 0.68f),
                point(0.90f, 0.76f),
                point(0.84f, 0.82f),
                point(0.78f, 0.80f),
                point(0.74f, 0.74f)
            )
        ),
        Continent(
            R.string.continent_antarctica,
            listOf(
                point(0.16f, 0.90f),
                point(0.32f, 0.92f),
                point(0.50f, 0.94f),
                point(0.68f, 0.94f),
                point(0.84f, 0.90f),
                point(0.68f, 0.98f),
                point(0.32f, 0.98f)
            )
        )
    )

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.continentHighlightFill)
    }

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 2f
        color = ContextCompat.getColor(context, R.color.continentHighlightOutline)
    }

    private val inverseMatrix = Matrix()
    private val workingPath = Path()
    private val transformedPath = Path()

    private var selectedContinent: Continent? = null
    private var onContinentSelectedListener: ((String) -> Unit)? = null

    init {
        isClickable = true
    }

    fun setOnContinentSelectedListener(listener: ((String) -> Unit)?) {
        onContinentSelectedListener = listener
        selectedContinent?.let { listener?.invoke(context.getString(it.nameRes)) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val drawable = drawable ?: return super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (!imageMatrix.invert(inverseMatrix)) {
                    return super.onTouchEvent(event)
                }
                val touch = floatArrayOf(event.x, event.y)
                inverseMatrix.mapPoints(touch)
                val x = touch[0]
                val y = touch[1]
                if (x < 0f || y < 0f || x > drawable.intrinsicWidth || y > drawable.intrinsicHeight) {
                    return super.onTouchEvent(event)
                }
                val normalizedX = x / drawable.intrinsicWidth
                val normalizedY = y / drawable.intrinsicHeight
                val newSelection = continents.firstOrNull { it.contains(normalizedX, normalizedY) }
                if (newSelection != null) {
                    if (selectedContinent != newSelection) {
                        selectedContinent = newSelection
                        onContinentSelectedListener?.invoke(context.getString(newSelection.nameRes))
                        invalidate()
                    }
                    performClick()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.selectedIndex = continents.indexOf(selectedContinent)
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            val restored = state.selectedIndex.takeIf { it in continents.indices }?.let { continents[it] }
            selectedContinent = restored
            if (restored != null) {
                onContinentSelectedListener?.invoke(context.getString(restored.nameRes))
            }
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawable = drawable ?: return
        val selected = selectedContinent ?: return

        buildPathFor(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat(), selected)
        canvas.drawPath(transformedPath, fillPaint)
        canvas.drawPath(transformedPath, outlinePaint)
    }

    private fun buildPathFor(drawableWidth: Float, drawableHeight: Float, continent: Continent) {
        workingPath.reset()
        continent.polygon.forEachIndexed { index, point ->
            val x = point.x * drawableWidth
            val y = point.y * drawableHeight
            if (index == 0) {
                workingPath.moveTo(x, y)
            } else {
                workingPath.lineTo(x, y)
            }
        }
        workingPath.close()
        transformedPath.set(workingPath)
        transformedPath.transform(imageMatrix)
    }

    private fun Continent.contains(normalizedX: Float, normalizedY: Float): Boolean {
        var result = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val pi = polygon[i]
            val pj = polygon[j]
            val intersects = (pi.y > normalizedY) != (pj.y > normalizedY)
            if (intersects) {
                val denominator = pj.y - pi.y
                if (abs(denominator) > 0.00001f) {
                    val ratio = (normalizedY - pi.y) / denominator
                    val xIntersect = pi.x + ratio * (pj.x - pi.x)
                    if (normalizedX < xIntersect) {
                        result = !result
                    }
                }
            }
            j = i
        }
        return result
    }

    companion object {
        private fun point(x: Float, y: Float) = PointF(x, y)
    }

    private class SavedState : View.BaseSavedState {
        var selectedIndex: Int = -1

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            selectedIndex = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedIndex)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}
