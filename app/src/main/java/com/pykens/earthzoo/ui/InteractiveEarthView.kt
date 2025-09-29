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
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.pykens.earthzoo.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private data class Continent(
    @StringRes val nameRes: Int,
    val polygon: List<PointF>,
    @DrawableRes val baseLayerRes: Int? = null
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
                point(0.2786f, 0.0889f),
                point(0.2448f, 0.1475f),
                point(0.2070f, 0.1377f),
                point(0.2734f, 0.1748f),
                point(0.2956f, 0.2080f),
                point(0.2878f, 0.2393f),
                point(0.2760f, 0.2529f),
                point(0.2500f, 0.2334f),
                point(0.2591f, 0.2158f),
                point(0.2422f, 0.1865f),
                point(0.2435f, 0.2393f),
                point(0.2240f, 0.2334f),
                point(0.2070f, 0.2666f),
                point(0.2448f, 0.3154f),
                point(0.2435f, 0.2471f),
                point(0.2604f, 0.2412f),
                point(0.2904f, 0.2666f),
                point(0.3190f, 0.3447f),
                point(0.2917f, 0.3311f),
                point(0.2982f, 0.3564f),
                point(0.2591f, 0.3877f),
                point(0.2409f, 0.4580f),
                point(0.2005f, 0.4443f),
                point(0.1940f, 0.4775f),
                point(0.2214f, 0.4756f),
                point(0.2344f, 0.5342f),
                point(0.2865f, 0.5264f),
                point(0.3555f, 0.5986f)
            )
        ),
        Continent(
            R.string.continent_south_america,
            listOf(
                point(0.2194f, 0.5215f),
                point(0.2467f, 0.5508f),
                point(0.2337f, 0.6055f),
                point(0.2650f, 0.6680f),
                point(0.2493f, 0.8398f),
                point(0.2715f, 0.8809f),
                point(0.2806f, 0.8711f),
                point(0.2702f, 0.8555f),
                point(0.2806f, 0.7852f),
                point(0.3483f, 0.6836f),
                point(0.3639f, 0.6055f),
                point(0.2650f, 0.5195f)
            )
        ),
        Continent(
            R.string.continent_europe,
            listOf(
                point(0.4300f, 0.2400f),
                point(0.4450f, 0.2000f),
                point(0.4650f, 0.1700f),
                point(0.4900f, 0.1500f),
                point(0.5200f, 0.1450f),
                point(0.5500f, 0.1550f),
                point(0.5750f, 0.1750f),
                point(0.5950f, 0.2050f),
                point(0.6050f, 0.2350f),
                point(0.5980f, 0.2600f),
                point(0.5800f, 0.2800f),
                point(0.5550f, 0.2950f),
                point(0.5250f, 0.3050f),
                point(0.4950f, 0.3100f),
                point(0.4650f, 0.3050f),
                point(0.4450f, 0.2900f),
                point(0.4300f, 0.2700f)
            )
        ),
        Continent(
            R.string.continent_africa,
            listOf(
                point(0.4844f, 0.2202f),
                point(0.4632f, 0.2510f),
                point(0.4814f, 0.2725f),
                point(0.4720f, 0.3032f),
                point(0.4570f, 0.3218f),
                point(0.4401f, 0.2710f),
                point(0.4235f, 0.3047f),
                point(0.4521f, 0.3262f),
                point(0.4378f, 0.3359f),
                point(0.4473f, 0.3599f),
                point(0.4255f, 0.3652f),
                point(0.4346f, 0.4072f),
                point(0.4001f, 0.5117f),
                point(0.4271f, 0.5610f),
                point(0.4730f, 0.5625f),
                point(0.4971f, 0.7500f),
                point(0.5260f, 0.7437f),
                point(0.5589f, 0.6592f),
                point(0.5563f, 0.6055f),
                point(0.5895f, 0.5312f),
                point(0.5667f, 0.5225f),
                point(0.6038f, 0.4961f),
                point(0.6012f, 0.4551f),
                point(0.6400f, 0.4839f)
            ),
            R.drawable.africa
        ),
        Continent(
            R.string.continent_asia,
            listOf(
                point(0.6100f, 0.2000f),
                point(0.6300f, 0.1700f),
                point(0.6600f, 0.1300f),
                point(0.7000f, 0.0900f),
                point(0.7500f, 0.1100f),
                point(0.8000f, 0.1300f),
                point(0.8600f, 0.1500f),
                point(0.9100f, 0.1900f),
                point(0.9500f, 0.2400f),
                point(0.9550f, 0.2800f),
                point(0.9400f, 0.3200f),
                point(0.9200f, 0.3500f),
                point(0.9000f, 0.3800f),
                point(0.8800f, 0.4100f),
                point(0.8600f, 0.4500f),
                point(0.8500f, 0.4900f),
                point(0.8300f, 0.5200f),
                point(0.8100f, 0.5400f),
                point(0.7900f, 0.5700f),
                point(0.7700f, 0.6000f),
                point(0.7400f, 0.6100f),
                point(0.7100f, 0.5900f),
                point(0.6900f, 0.5600f),
                point(0.6700f, 0.5200f),
                point(0.6550f, 0.4800f),
                point(0.6450f, 0.4400f),
                point(0.6400f, 0.4000f),
                point(0.6300f, 0.3400f),
                point(0.6200f, 0.2800f)
            )
        ),
        Continent(
            R.string.continent_australia,
            listOf(
                point(0.8001f, 0.6328f),
                point(0.7520f, 0.6855f),
                point(0.7546f, 0.7480f),
                point(0.8027f, 0.7383f),
                point(0.8418f, 0.7773f),
                point(0.8639f, 0.7090f),
                point(0.8340f, 0.6328f),
                point(0.8262f, 0.6602f)
            )
        ),
        Continent(
            R.string.continent_antarctica,
            listOf(
                point(0.2507f, 0.8496f),
                point(0.2682f, 0.8799f),
                point(0.2806f, 0.8711f),
                point(0.2715f, 0.8496f)
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

    private var defaultDrawableState: Drawable.ConstantState? = null
    @DrawableRes
    private val fallbackDefaultDrawableResId: Int = R.drawable.earth
    @DrawableRes
    private var currentOverrideDrawableRes: Int? = null

    init {
        isClickable = true
        scaleType = ScaleType.MATRIX
        defaultDrawableState = drawable?.constantState
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
                        updateBaseLayerForSelection(newSelection)
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
            updateBaseLayerForSelection(restored)
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

    private fun updateBaseLayerForSelection(continent: Continent?) {
        val overrideResId = continent?.baseLayerRes
        if (overrideResId != null) {
            if (currentOverrideDrawableRes != overrideResId) {
                currentOverrideDrawableRes = overrideResId
                setImageResource(overrideResId)
                updateBaseMatrix()
            }
        } else if (currentOverrideDrawableRes != null) {
            currentOverrideDrawableRes = null
            val restored = defaultDrawableState?.newDrawable()?.mutate()
            if (restored != null) {
                setImageDrawable(restored)
            } else {
                setImageResource(fallbackDefaultDrawableResId)
            }
            updateBaseMatrix()
        }
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
