package com.nickmitrokhin.dialer.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.nickmitrokhin.dialer.R
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

class CircularIndicator(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var _max: Int = 100
    private var _min: Int = 0
    private var _value: Int = 50
    private var _isSetting: Boolean = false
    private var _lineColor: Int = Color.BLUE
    private var _backLineColor: Int = Color.GRAY
    private var _lineWidth: Int = 10
    private var _textColor: Int = Color.BLACK
    private var _textSize: Int = 100
    private lateinit var viewInfo: CircularIndicatorViewInfo
    private lateinit var painter: CircularIndicatorPainter

    init {
        loadAttributes(context, attrs)
        init()
    }

    private fun loadAttributes(context: Context, attrs: AttributeSet) {
        val ta: TypedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularIndicator,
            0, 0
        )
        try {
            _max = ta.getInteger(R.styleable.CircularIndicator_max, _max)
            _min = ta.getInteger(R.styleable.CircularIndicator_min, _min)
            _value = ta.getInteger(R.styleable.CircularIndicator_value, _value)
            _lineColor = ta.getColor(R.styleable.CircularIndicator_lineColor, _lineColor)
            _backLineColor =
                ta.getColor(R.styleable.CircularIndicator_backLineColor, _backLineColor)
            _lineWidth = ta.getInteger(R.styleable.CircularIndicator_lineWidth, _lineWidth)
            _textColor = ta.getColor(R.styleable.CircularIndicator_textColor, _textColor)
            _textSize = ta.getInteger(R.styleable.CircularIndicator_textSize, _textSize)
        } finally {
            ta.recycle()
        }
        validateOptions()
    }

    private fun init() {
        viewInfo = CircularIndicatorViewInfo(this)
        painter = CircularIndicatorPainter(this)
    }

    private fun validateOptions() {
        val temp = _max
        _max = _min.coerceAtLeast(_max)
        _min = _min.coerceAtMost(temp)

        _value = if(_value > _max) _max else _value.coerceAtLeast(_min)
    }

    var lineColor: Int
        get() {
            return _lineColor
        }
        set(color) {
            if(color != _lineColor) {
                _lineColor = color
                redraw()
            }
        }

    var backLineColor: Int
        get() {
            return _backLineColor
        }
        set(color) {
            if(color != _backLineColor) {
                _backLineColor = color
                redraw()
            }
        }

    var lineWidth: Int
        get() {
            return _lineWidth
        }
        set(width) {
            if(width != _lineWidth) {
                _lineWidth = width
                redraw()
            }
        }

    var textColor: Int
        get() {
            return _textColor
        }
        set(color) {
            if(color != _textColor) {
                _textColor = color
                redraw()
            }
        }

    var textSize: Int
        get() {
            return _textSize
        }
        set(size) {
            if(size != _textSize) {
                _textSize = size
                redraw()
            }
        }

    var max: Int
        get() {
            return _max
        }
        set(maxVal) {
            if(_max != maxVal) {
                _max = maxVal
                redraw()
            }
        }

    var min: Int
        get() {
            return _min
        }
        set(minVal) {
            if(_min != minVal) {
                _min = minVal
                redraw()
            }
        }

    fun beginUpdate() {
        _isSetting = true
    }

    fun endUpdate() {
        if(_isSetting) {
            _isSetting = false
            redraw()
        }
    }

    var value: Int
        get() {
            return _value
        }
        set(newValue) {
            if(_value != newValue) {
                _value = newValue
                redraw()
            }
        }

    private fun redraw() {
        if(!_isSetting) {
            validateOptions()
            invalidate()
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewInfo.calculate()
        setMeasuredDimension(viewInfo.width, viewInfo.height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        painter.drawLine(canvas, viewInfo)
        painter.drawLabel(canvas, viewInfo)
    }
}


private class CircularIndicatorViewInfo(private val indicator: CircularIndicator) {
    private val maxAngle = 360
    private var _bounds: Rect = Rect()
    private var _labelBounds: Rect = Rect()
    private var _currentAngle: Int = 0
    private var _width: Int = 0
    private var _height: Int = 0

    val currentAngle: Int
        get() {
            return _currentAngle
        }

    val bounds: Rect
        get() {
            return Rect(_bounds)
        }

    val labelBounds: Rect
        get() {
            return Rect(_labelBounds)
        }

    val width: Int
        get() {
            return _width
        }

    val height: Int
        get() {
            return _height
        }

    private fun calculateIndicatorInfo() {
        val widthWithoutPadding =
            indicator.measuredWidth - indicator.paddingLeft - indicator.paddingRight
        val heightWithoutPadding =
            indicator.measuredHeight - indicator.paddingTop - indicator.paddingBottom
        val workingSize = widthWithoutPadding.coerceAtMost(heightWithoutPadding)
        val lineWidthOffset = floor(indicator.lineWidth / 2.toDouble()).toInt() + 1
        _bounds.set(
            indicator.paddingLeft + lineWidthOffset,
            indicator.paddingTop + lineWidthOffset,
            workingSize - lineWidthOffset,
            workingSize - lineWidthOffset
        )
        _width = workingSize + indicator.paddingLeft + indicator.paddingRight
        _height = workingSize + indicator.paddingTop + indicator.paddingBottom
        val proportion: Double =
            (indicator.value - indicator.min).toDouble() / (indicator.max - indicator.min).toDouble()
        _currentAngle = ceil(proportion * maxAngle).toInt()
    }

    private fun calculateLabelInfo() {
        val centerY = ceil(_bounds.exactCenterY()).toInt()
        val centerX = ceil(_bounds.exactCenterX()).toInt()
        val radius = ceil((_bounds.width() - indicator.lineWidth) / 2.toDouble()).toInt()
        val rectHalfSize = floor(sqrt(radius.toDouble().pow(2) * 2) / 2).toInt()
        _labelBounds.set(
            centerX - rectHalfSize,
            centerY - rectHalfSize,
            centerX + rectHalfSize,
            centerY + rectHalfSize
        )
    }

    fun calculate() {
        calculateIndicatorInfo()
        calculateLabelInfo()
    }
}


private class CircularIndicatorPainter(private val indicator: CircularIndicator) {
    private val startAngle = 270
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val backLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawLine(canvas: Canvas, viewInfo: CircularIndicatorViewInfo) {
        linePaint.color = indicator.lineColor
        linePaint.strokeWidth = indicator.lineWidth.toFloat()
        backLinePaint.strokeWidth = linePaint.strokeWidth
        backLinePaint.color = indicator.backLineColor
        val drawBounds = RectF(viewInfo.bounds)
        canvas.drawArc(
            drawBounds,
            viewInfo.currentAngle.toFloat(),
            360.toFloat(),
            false,
            backLinePaint
        )
        canvas.drawArc(
            drawBounds,
            startAngle.toFloat(),
            viewInfo.currentAngle.toFloat(),
            false,
            linePaint
        )
    }

    fun drawLabel(canvas: Canvas, viewInfo: CircularIndicatorViewInfo) {
        labelPaint.color = indicator.textColor
        labelPaint.textSize = indicator.textSize.toFloat()
        val labelBounds = viewInfo.labelBounds
        val text = indicator.value.toString()
        val charCount = labelPaint.breakText(text, true, labelBounds.width().toFloat(), null)
        val startPos = (text.length - charCount) / 2
        val textBounds = Rect()
        labelPaint.getTextBounds(text, startPos, charCount, textBounds)
        canvas.drawText(
            text, startPos, startPos + charCount,
            floor(labelBounds.exactCenterX() - textBounds.width() / 2.toDouble()).toFloat(),
            floor(labelBounds.exactCenterY() + (textBounds.height() - textBounds.height() * 0.3) / 2.toDouble()).toFloat(),
            labelPaint
        )

    }
}