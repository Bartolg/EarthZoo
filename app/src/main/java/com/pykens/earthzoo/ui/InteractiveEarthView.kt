package com.pykens.earthzoo.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.pykens.earthzoo.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
                point(0.0333f, 0.1000f),
                point(0.0833f, 0.1333f),
                point(0.1111f, 0.1667f),
                point(0.1389f, 0.2111f),
                point(0.1583f, 0.2500f),
                point(0.1722f, 0.2889f),
                point(0.1889f, 0.3222f),
                point(0.2111f, 0.3556f),
                point(0.2333f, 0.3778f),
                point(0.2500f, 0.4000f),
                point(0.2667f, 0.4333f),
                point(0.2722f, 0.4556f),
                point(0.2778f, 0.4444f),
                point(0.2833f, 0.3889f),
                point(0.2778f, 0.3611f),
                point(0.2833f, 0.3333f),
                point(0.2944f, 0.3000f),
                point(0.3056f, 0.2667f),
                point(0.3167f, 0.2333f),
                point(0.3278f, 0.2000f),
                point(0.3389f, 0.1667f),
                point(0.3556f, 0.1333f),
                point(0.3750f, 0.1000f),
                point(0.3889f, 0.0667f),
                point(0.3611f, 0.0444f),
                point(0.3056f, 0.0444f),
                point(0.2361f, 0.0556f),
                point(0.1667f, 0.0778f),
                point(0.1111f, 0.0889f)
            )
        ),
        Continent(
            R.string.continent_south_america,
            listOf(
                point(0.2722f, 0.4333f),
                point(0.2833f, 0.4667f),
                point(0.2889f, 0.5111f),
                point(0.2944f, 0.5444f),
                point(0.3000f, 0.5889f),
                point(0.3056f, 0.6444f),
                point(0.3111f, 0.7000f),
                point(0.3167f, 0.7556f),
                point(0.3222f, 0.8000f),
                point(0.3278f, 0.8333f),
                point(0.3333f, 0.7778f),
                point(0.3389f, 0.7111f),
                point(0.3444f, 0.6111f),
                point(0.3500f, 0.5333f),
                point(0.3556f, 0.4778f),
                point(0.3500f, 0.4444f),
                point(0.3389f, 0.4333f),
                point(0.3056f, 0.4556f)
            )
        ),
        Continent(
            R.string.continent_europe,
            listOf(
                point(0.4333f, 0.1000f),
                point(0.4500f, 0.1222f),
                point(0.4722f, 0.1667f),
                point(0.4833f, 0.1889f),
                point(0.4944f, 0.2111f),
                point(0.5000f, 0.2333f),
                point(0.4861f, 0.2556f),
                point(0.4778f, 0.2667f),
                point(0.4833f, 0.2889f),
                point(0.4944f, 0.3000f),
                point(0.5167f, 0.3000f),
                point(0.5389f, 0.2889f),
                point(0.5556f, 0.2778f),
                point(0.5778f, 0.2500f),
                point(0.5889f, 0.2222f),
                point(0.6000f, 0.2000f),
                point(0.6111f, 0.1667f),
                point(0.6222f, 0.1444f),
                point(0.5889f, 0.1111f),
                point(0.5556f, 0.1000f),
                point(0.5278f, 0.0889f),
                point(0.5000f, 0.0889f),
                point(0.4667f, 0.0889f)
            )
        ),
        Continent(
            R.string.continent_africa,
            listOf(
                point(0.4500f, 0.3000f),
                point(0.4667f, 0.3222f),
                point(0.4778f, 0.3556f),
                point(0.4833f, 0.4000f),
                point(0.4889f, 0.4444f),
                point(0.4944f, 0.4889f),
                point(0.5056f, 0.5222f),
                point(0.5167f, 0.5556f),
                point(0.5278f, 0.6000f),
                point(0.5389f, 0.6444f),
                point(0.5500f, 0.6889f),
                point(0.5611f, 0.7111f),
                point(0.5722f, 0.6778f),
                point(0.5833f, 0.6111f),
                point(0.5944f, 0.5556f),
                point(0.6056f, 0.5000f),
                point(0.6000f, 0.4444f),
                point(0.5889f, 0.4000f),
                point(0.5778f, 0.3556f),
                point(0.5667f, 0.3333f),
                point(0.5556f, 0.3111f),
                point(0.5389f, 0.3000f),
                point(0.5222f, 0.2889f),
                point(0.5056f, 0.2889f),
                point(0.4833f, 0.2889f)
            )
        ),
        Continent(
            R.string.continent_asia,
            listOf(
                point(0.5889f, 0.1111f),
                point(0.6111f, 0.1333f),
                point(0.6333f, 0.1444f),
                point(0.6500f, 0.1667f),
                point(0.6667f, 0.1778f),
                point(0.6833f, 0.1889f),
                point(0.7056f, 0.2111f),
                point(0.7278f, 0.2333f),
                point(0.7500f, 0.2556f),
                point(0.7722f, 0.2889f),
                point(0.8056f, 0.3222f),
                point(0.8278f, 0.3444f),
                point(0.8444f, 0.3556f),
                point(0.8611f, 0.3667f),
                point(0.8722f, 0.4000f),
                point(0.8778f, 0.4333f),
                point(0.8833f, 0.4667f),
                point(0.8889f, 0.4889f),
                point(0.8944f, 0.5222f),
                point(0.8889f, 0.5444f),
                point(0.8722f, 0.5667f),
                point(0.8500f, 0.5778f),
                point(0.8278f, 0.5667f),
                point(0.8056f, 0.5444f),
                point(0.7889f, 0.5222f),
                point(0.7667f, 0.5000f),
                point(0.7500f, 0.4778f),
                point(0.7333f, 0.4556f),
                point(0.7222f, 0.4333f),
                point(0.7111f, 0.4000f),
                point(0.7000f, 0.3667f),
                point(0.6889f, 0.3444f),
                point(0.6722f, 0.3222f),
                point(0.6556f, 0.3000f),
                point(0.6389f, 0.2778f),
                point(0.6278f, 0.2556f),
                point(0.6167f, 0.2222f),
                point(0.6056f, 0.1889f),
                point(0.5944f, 0.1556f)
            )
        ),
        Continent(
            R.string.continent_australia,
            listOf(
                point(0.8111f, 0.5556f),
                point(0.8222f, 0.5889f),
                point(0.8389f, 0.6222f),
                point(0.8611f, 0.6444f),
                point(0.8833f, 0.6556f),
                point(0.9056f, 0.6333f),
                point(0.9222f, 0.6000f),
                point(0.9278f, 0.5667f),
                point(0.9167f, 0.5333f),
                point(0.9000f, 0.5111f),
                point(0.8778f, 0.5000f),
                point(0.8556f, 0.4889f),
                point(0.8333f, 0.5000f),
                point(0.8167f, 0.5222f)
            )
        ),
        Continent(
            R.string.continent_antarctica,
            listOf(
                point(0.0000f, 0.8333f),
                point(0.0833f, 0.8889f),
                point(0.1667f, 0.9111f),
                point(0.2500f, 0.9333f),
                point(0.3333f, 0.9444f),
                point(0.4167f, 0.9556f),
                point(0.5000f, 0.9667f),
                point(0.5833f, 0.9556f),
                point(0.6667f, 0.9444f),
                point(0.7500f, 0.9333f),
                point(0.8333f, 0.9222f),
                point(0.9167f, 0.9000f),
                point(1.0000f, 0.8778f)
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
    private val baseMatrix = Matrix()
    private val zoomMatrix = Matrix()
    private val animatedMatrix = Matrix()
    private val continentBounds = RectF()
    private val workingPath = Path()
    private val transformedPath = Path()
    private val startMatrixValues = FloatArray(9)
    private val endMatrixValues = FloatArray(9)
    private val animatedMatrixValues = FloatArray(9)

    private var selectedContinent: Continent? = null
    private var isZoomed = false
    private var onContinentSelectedListener: ((String) -> Unit)? = null
    private var zoomAnimator: ValueAnimator? = null

    init {
        isClickable = true
        scaleType = ScaleType.MATRIX
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
                        if (isZoomed) {
                            resetZoom()
                        } else {
                            isZoomed = false
                            imageMatrix = baseMatrix
                            invalidate()
                        }
                        onContinentSelectedListener?.invoke(context.getString(newSelection.nameRes))
                    } else {
                        if (isZoomed) {
                            resetZoom()
                        } else {
                            zoomToContinent(newSelection)
                        }
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
        savedState.isZoomed = isZoomed
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            val restored = state.selectedIndex.takeIf { it in continents.indices }?.let { continents[it] }
            selectedContinent = restored
            isZoomed = state.isZoomed
            if (restored != null) {
                onContinentSelectedListener?.invoke(context.getString(restored.nameRes))
                if (state.isZoomed) {
                    post { zoomToContinent(restored) }
                } else {
                    post { resetZoom() }
                }
            } else if (state.isZoomed) {
                post { resetZoom() }
            }
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBaseMatrix()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawable = drawable ?: return
        val selected = selectedContinent ?: return

        buildPathFor(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat(), selected)
        canvas.drawPath(transformedPath, fillPaint)
        canvas.drawPath(transformedPath, outlinePaint)
    }

    private fun updateBaseMatrix() {
        zoomAnimator?.cancel()
        val drawable = drawable ?: return
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        if (contentWidth <= 0 || contentHeight <= 0) {
            return
        }

        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()
        val scale = min(contentWidth / drawableWidth, contentHeight / drawableHeight)
        val dx = paddingLeft + (contentWidth - drawableWidth * scale) / 2f
        val dy = paddingTop + (contentHeight - drawableHeight * scale) / 2f

        baseMatrix.reset()
        baseMatrix.setScale(scale, scale)
        baseMatrix.postTranslate(dx, dy)

        if (isZoomed) {
            val matrix = selectedContinent?.let { computeZoomMatrix(it) }
            if (matrix != null) {
                imageMatrix = matrix
            } else {
                isZoomed = false
                imageMatrix = baseMatrix
            }
        } else {
            imageMatrix = baseMatrix
        }
        invalidate()
    }

    private fun resetZoom() {
        zoomAnimator?.cancel()
        if (isZoomed) {
            isZoomed = false
            animateImageMatrix(Matrix(baseMatrix))
        } else {
            imageMatrix = baseMatrix
            invalidate()
        }
    }

    private fun zoomToContinent(continent: Continent): Boolean {
        val matrix = computeZoomMatrix(continent) ?: return false
        isZoomed = true
        animateImageMatrix(Matrix(matrix))
        return true
    }

    private fun animateImageMatrix(targetMatrix: Matrix) {
        zoomAnimator?.cancel()

        val startMatrix = Matrix(imageMatrix)
        startMatrix.getValues(startMatrixValues)
        targetMatrix.getValues(endMatrixValues)

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600L
            interpolator = DecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                val fraction = valueAnimator.animatedValue as Float
                for (i in startMatrixValues.indices) {
                    animatedMatrixValues[i] = startMatrixValues[i] + (endMatrixValues[i] - startMatrixValues[i]) * fraction
                }
                animatedMatrix.setValues(animatedMatrixValues)
                imageMatrix = animatedMatrix
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    zoomAnimator = null
                    imageMatrix = targetMatrix
                    invalidate()
                }

                override fun onAnimationCancel(animation: Animator) {
                    zoomAnimator = null
                }
            })
        }

        zoomAnimator = animator
        animator.start()
    }

    private fun computeZoomMatrix(continent: Continent): Matrix? {
        val drawable = drawable ?: return null
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        if (contentWidth <= 0 || contentHeight <= 0) {
            return null
        }

        continent.fillBounds(continentBounds)
        val boundsWidth = continentBounds.width()
        val boundsHeight = continentBounds.height()
        if (boundsWidth <= 0f || boundsHeight <= 0f) {
            return null
        }

        val paddingFactor = 0.1f
        val paddedLeft = (continentBounds.left - boundsWidth * paddingFactor).coerceAtLeast(0f)
        val paddedTop = (continentBounds.top - boundsHeight * paddingFactor).coerceAtLeast(0f)
        val paddedRight = (continentBounds.right + boundsWidth * paddingFactor).coerceAtMost(1f)
        val paddedBottom = (continentBounds.bottom + boundsHeight * paddingFactor).coerceAtMost(1f)

        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()
        val left = paddedLeft * drawableWidth
        val top = paddedTop * drawableHeight
        val right = paddedRight * drawableWidth
        val bottom = paddedBottom * drawableHeight

        val rectWidth = max(right - left, 1f)
        val rectHeight = max(bottom - top, 1f)

        val viewWidthF = contentWidth.toFloat()
        val viewHeightF = contentHeight.toFloat()
        val scale = min(viewWidthF / rectWidth, viewHeightF / rectHeight)

        zoomMatrix.reset()
        zoomMatrix.setScale(scale, scale)
        val dx = paddingLeft + (viewWidthF - rectWidth * scale) / 2f - left * scale
        val dy = paddingTop + (viewHeightF - rectHeight * scale) / 2f - top * scale
        zoomMatrix.postTranslate(dx, dy)

        return zoomMatrix
    }

    private fun Continent.fillBounds(outRect: RectF) {
        var minX = Float.POSITIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY
        for (point in polygon) {
            minX = min(minX, point.x)
            minY = min(minY, point.y)
            maxX = max(maxX, point.x)
            maxY = max(maxY, point.y)
        }
        outRect.set(minX, minY, maxX, maxY)
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
        var isZoomed: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            selectedIndex = parcel.readInt()
            isZoomed = parcel.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedIndex)
            out.writeInt(if (isZoomed) 1 else 0)
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
