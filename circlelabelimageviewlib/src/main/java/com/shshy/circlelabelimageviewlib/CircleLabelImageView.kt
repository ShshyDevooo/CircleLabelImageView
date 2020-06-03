package com.shshy.circlelabelimageviewlib

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author  ShiShY
 * @Description:
 * @data  2020/6/3 09:31
 */
class CircleLabelImageView : AppCompatImageView {
    private var textSize = 14f
    private var textColor = Color.WHITE
    private var text = ""
    private var textBGColor = Color.argb(127, 0, 0, 0)
    private var textPaddingVertical = 0f
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var circleXfermode: PorterDuffXfermode
    private var circleRadius = 0f
    private val viewRect = RectF()
    private val clipPath = Path()
    private val srcPath = Path()

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleLabelImageView)
        textSize = typedArray.getDimension(R.styleable.CircleLabelImageView_cliv_textSize, textSize)
        textColor = typedArray.getColor(R.styleable.CircleLabelImageView_cliv_textColor, textColor)
        text = typedArray.getString(R.styleable.CircleLabelImageView_cliv_text) ?: ""
        textBGColor = typedArray.getColor(R.styleable.CircleLabelImageView_cliv_textBGColor, textBGColor)
        textPaddingVertical = typedArray.getDimension(R.styleable.CircleLabelImageView_cliv_textBGPaddingVertical, textPaddingVertical)
        typedArray.recycle()

        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER

        circleXfermode = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
            PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        else
            PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    override fun onDraw(canvas: Canvas?) {
        clipPath.reset()
        clipPath.addCircle(width.toFloat() / 2, height.toFloat() / 2, circleRadius, Path.Direction.CCW)
        canvas?.saveLayer(viewRect, null, Canvas.ALL_SAVE_FLAG)
        super.onDraw(canvas)
        if (!TextUtils.isEmpty(text)) {
            textPaint.color = textBGColor
            canvas?.drawRect(getBGRect(), textPaint)
            textPaint.color = textColor
            canvas?.drawText(text, width.toFloat() / 2, getTextDrawBaseLine(), textPaint)
        }
        textPaint.xfermode = circleXfermode
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
            canvas?.drawPath(clipPath, textPaint)
        else {
            srcPath.reset()
            srcPath.addRect(viewRect, Path.Direction.CCW)
            srcPath.op(clipPath, Path.Op.DIFFERENCE)
            canvas?.drawPath(srcPath, textPaint)
        }
        textPaint.xfermode = null
        canvas?.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        circleRadius = Math.min(width, height) / 2f
        viewRect.set(width / 2f - circleRadius, height / 2f - circleRadius, width / 2f + circleRadius, height / 2f + circleRadius)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun getBGRect(): RectF {
        return RectF(0f, height - calculateBGHeight(), width.toFloat(), height.toFloat())
    }

    private fun calculateBGHeight(): Float {
        val fontMetrics = textPaint.fontMetrics
        //字体高度加上垂直方向的padding
        return (fontMetrics.bottom - fontMetrics.top) + 2 * textPaddingVertical
    }

    private fun getTextDrawBaseLine(): Float {
        val fontMetrics = textPaint.fontMetrics
        val bgRect = getBGRect()
        return (bgRect.bottom - bgRect.top) / 2 + bgRect.top + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }
}