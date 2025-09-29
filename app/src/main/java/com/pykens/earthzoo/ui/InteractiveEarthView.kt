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
                point(0.0647f, 0.0195f),
                point(0.4498f, 0.0195f),
                point(0.4498f, 0.1436f),
                point(0.4196f, 0.1621f),
                point(0.4498f, 0.1504f),
                point(0.4330f, 0.1992f),
                point(0.4498f, 0.1924f),
                point(0.4498f, 0.3555f),
                point(0.4314f, 0.3701f),
                point(0.4498f, 0.3672f),
                point(0.4280f, 0.3867f),
                point(0.4498f, 0.4023f),
                point(0.4185f, 0.4043f),
                point(0.4498f, 0.4053f),
                point(0.4336f, 0.4258f),
                point(0.4492f, 0.5195f),
                point(0.0513f, 0.5137f),
                point(0.0781f, 0.4834f),
                point(0.0508f, 0.4844f),
                point(0.0765f, 0.4639f),
                point(0.0525f, 0.4434f),
                point(0.1646f, 0.4443f),
                point(0.1356f, 0.4600f),
                point(0.1512f, 0.4980f),
                point(0.1311f, 0.4639f),
                point(0.1127f, 0.4980f),
                point(0.0977f, 0.4902f),
                point(0.1194f, 0.4492f),
                point(0.1858f, 0.4453f),
                point(0.0497f, 0.4395f),
                point(0.0647f, 0.3457f),
                point(0.1350f, 0.3438f),
                point(0.0497f, 0.3438f),
                point(0.0709f, 0.2920f),
                point(0.0497f, 0.2949f),
                point(0.0497f, 0.2002f),
                point(0.0675f, 0.1953f),
                point(0.0497f, 0.1738f),
                point(0.0759f, 0.1807f),
                point(0.0497f, 0.1729f),
                point(0.0497f, 0.0908f),
                point(0.1535f, 0.0947f),
                point(0.0497f, 0.0811f),
                point(0.1629f, 0.0801f),
                point(0.0497f, 0.0791f),
                point(0.1557f, 0.0615f),
                point(0.0497f, 0.0625f),
                point(0.1350f, 0.0566f),
                point(0.0497f, 0.0557f),
                point(0.1289f, 0.0430f),
                point(0.0497f, 0.0420f)
            )
        ),
        Continent(
            R.string.continent_south_america,
            listOf(
                point(0.2355f, 0.4492f),
                point(0.4219f, 0.4492f),
                point(0.4124f, 0.4805f),
                point(0.4297f, 0.4541f),
                point(0.4297f, 0.9492f),
                point(0.2299f, 0.9492f),
                point(0.2299f, 0.6729f),
                point(0.2489f, 0.6641f),
                point(0.2299f, 0.6719f)
            )
        ),
        Continent(
            R.string.continent_europe,
            listOf(
                point(0.4498f, 0.0791f),
                point(0.6797f, 0.0791f),
                point(0.6797f, 0.3398f),
                point(0.4503f, 0.3379f),
                point(0.4498f, 0.1572f),
                point(0.4844f, 0.1465f),
                point(0.4498f, 0.1553f)
            )
        ),
        Continent(
            R.string.continent_africa,
            listOf(
                point(0.3996f, 0.1992f),
                point(0.6797f, 0.2002f),
                point(0.6797f, 0.7998f),
                point(0.3996f, 0.7959f),
                point(0.3996f, 0.5137f),
                point(0.4202f, 0.5000f),
                point(0.3996f, 0.5010f)
            )
        ),
        Continent(
            R.string.continent_asia,
            listOf(
                point(0.5798f, 0.0498f),
                point(1.0000f, 0.0498f),
                point(1.0000f, 0.2217f),
                point(0.9671f, 0.2217f),
                point(1.0000f, 0.2227f),
                point(1.0000f, 0.3477f),
                point(0.9838f, 0.3555f),
                point(1.0000f, 0.3584f),
                point(1.0000f, 0.6992f),
                point(0.5798f, 0.6992f)
            )
        ),
        Continent(
            R.string.continent_australia,
            listOf(
                point(0.7009f, 0.5498f),
                point(0.9163f, 0.5508f),
                point(0.9196f, 0.6494f),
                point(0.9040f, 0.6504f),
                point(0.9196f, 0.6621f),
                point(0.8912f, 0.6816f),
                point(0.9196f, 0.6768f),
                point(0.9180f, 0.7109f),
                point(0.8917f, 0.7070f),
                point(0.9196f, 0.7275f),
                point(0.8906f, 0.7598f),
                point(0.9196f, 0.7744f),
                point(0.9196f, 0.8496f),
                point(0.8945f, 0.8486f),
                point(0.9196f, 0.8594f),
                point(0.6998f, 0.8594f),
                point(0.7015f, 0.7969f),
                point(0.7483f, 0.8018f),
                point(0.7561f, 0.7617f),
                point(0.7494f, 0.8018f),
                point(0.8064f, 0.8027f),
                point(0.8153f, 0.7676f),
                point(0.8320f, 0.8018f),
                point(0.8069f, 0.8359f),
                point(0.7785f, 0.8359f),
                point(0.7824f, 0.8037f),
                point(0.7132f, 0.8213f),
                point(0.8092f, 0.8115f),
                point(0.7985f, 0.7676f),
                point(0.8170f, 0.7637f),
                point(0.7952f, 0.7646f),
                point(0.8103f, 0.7451f),
                point(0.7952f, 0.7197f),
                point(0.8058f, 0.7451f),
                point(0.7857f, 0.7676f),
                point(0.7567f, 0.7490f),
                point(0.7651f, 0.7188f),
                point(0.7227f, 0.7197f),
                point(0.7271f, 0.7617f),
                point(0.7081f, 0.7598f),
                point(0.7171f, 0.7207f),
                point(0.6998f, 0.7969f),
                point(0.7165f, 0.7207f),
                point(0.7065f, 0.7441f),
                point(0.7003f, 0.7041f),
                point(0.7528f, 0.7051f),
                point(0.6998f, 0.7021f),
                point(0.7026f, 0.6631f),
                point(0.7305f, 0.6582f),
                point(0.6998f, 0.6650f),
                point(0.7003f, 0.6367f),
                point(0.7757f, 0.6436f),
                point(0.6998f, 0.6357f),
                point(0.6998f, 0.5771f),
                point(0.7199f, 0.5801f)
            )
        ),
        Continent(
            R.string.continent_antarctica,
            listOf(
                point(0.0000f, 0.8193f),
                point(0.9994f, 0.8193f),
                point(1.0000f, 0.9990f),
                point(0.0000f, 1.0000f)
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
