package com.tapbi.spark.yokey.ui.custom.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.tapbi.spark.yokey.R

class CustomText : androidx.appcompat.widget.AppCompatTextView {
    private var colors1 = Color.BLUE
    private var colors2 = Color.RED
    private var colors3 = Color.YELLOW
    private var colors4 = Color.YELLOW
    private var colors5 = Color.YELLOW
    private var isShowOffer: Boolean? = null
    private var radiusBorder = 10
    private var radiusCircle = 10
    private var sizeBorders = 2
    private var strokeCircle = 2
    var nameText = ""
    private var nameTxtTop = ""
    private var marginStartTxt = 10
    private var marginEndTxtTop = 10
    private var sizeTxt = 10
    private var sizeTxtTop = 10
    private var colorTxt = Color.BLUE
    private var colorTxtTop = Color.BLUE
    private var paint: Paint? = null
    private var linearGradientRoundRect: LinearGradient? = null
    private var lineTxt = 1
    private var linearGradientRoundCircle: LinearGradient? = null
    private var colors: IntArray? = null
    private var colorsRectFill: IntArray? = null
    private var checkAction: Boolean = false
    private val rectTextCenter = Rect()
    private var typeFont: String? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        getAllAttr(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
        getAllAttr(attrs)

    }

    @SuppressLint("Recycle")
    private fun getAllAttr(attrs: AttributeSet?) {
        // val type = context.obtainStyledAttributes(attrs, R.styleable.CustomText)
        context.withStyledAttributes(attrs, R.styleable.CustomText) {
            colors1 = getColor(R.styleable.CustomText_color1, Color.parseColor("#4355FF"))
            colors2 = getColor(R.styleable.CustomText_color2, Color.parseColor("#933DFE"))
            colors3 = getColor(R.styleable.CustomText_color3, Color.parseColor("#FF35FD"))
            colors4 = getColor(R.styleable.CustomText_color4, Color.parseColor("#FF8E61"))
            colors5 = getColor(R.styleable.CustomText_color5, Color.parseColor("#FFE600"))
            radiusBorder = getDimensionPixelOffset(R.styleable.CustomText_radiusBorder, 20)
            radiusCircle = getDimensionPixelOffset(R.styleable.CustomText_radiusCircle, 40)
            strokeCircle = getDimensionPixelOffset(R.styleable.CustomText_strokeCircle, 10)
            sizeBorders = getDimensionPixelOffset(R.styleable.CustomText_borderSize, 20)
            nameText = getString(R.styleable.CustomText_nameText) ?: "hello word"
            nameTxtTop = getString(R.styleable.CustomText_nameTextTop) ?: "hello word"
            sizeTxt = getDimensionPixelOffset(R.styleable.CustomText_sizeTexts, 100)
            sizeTxtTop = getDimensionPixelOffset(R.styleable.CustomText_sizeTextsTop, 70)
            colorTxt = getColor(R.styleable.CustomText_colorTexts, Color.RED)
            colorTxtTop = getColor(R.styleable.CustomText_colorTextsTop, Color.RED)
            lineTxt = getDimensionPixelOffset(R.styleable.CustomText_lineTxt, 2)
            marginStartTxt = getDimensionPixelOffset(R.styleable.CustomText_marginStartTxt, 0)
            marginEndTxtTop = getDimensionPixelOffset(R.styleable.CustomText_marginEndTxtTop, -300)
            typeFont = getString(R.styleable.CustomText_typeFont) ?: "poppins_regular.ttf"
            isShowOffer = getBoolean(R.styleable.CustomText_icShowBestOffer, false)
        }
        colors = intArrayOf(colors1, colors2, colors3, colors4, colors5)
        colorsRectFill = intArrayOf(colors2, colors3, colors4, Color.parseColor("#FFBA30"))
    }

    private fun drawRoundRect(canvas: Canvas) {
        paint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG

            /**
             * Draw rect with stroke
             */
            style = Paint.Style.STROKE
            strokeWidth = sizeBorders.toFloat()
            if (checkAction) {
                shader = linearGradientRoundRect
            } else {
                shader = null
                color = Color.parseColor("#999999")
            }
            canvas.drawRoundRect(
                sizeBorders.toFloat(),
                height.toFloat() / 4 + sizeBorders,
                width.toFloat() - sizeBorders,
                (height - sizeBorders).toFloat(),
                radiusBorder.toFloat(),
                radiusBorder.toFloat(),
                this
            )

            /**
             * Draw text center in rect stroke
             */
            shader = null
            style = Paint.Style.FILL

            val typefaceTextMoney = Typeface.createFromAsset(context.assets, "fonts/poppins_regular.ttf")
            typeface = typefaceTextMoney
            textSize = sizeTxt.toFloat()

            // Prepare to ellipsize text
            val textPaint = TextPaint()
            textPaint.typeface = typefaceTextMoney
            textPaint.textSize = sizeTxt.toFloat()

            // Set text color
            color = if (checkAction) {
                Color.parseColor("#000000")
            } else {
                Color.parseColor("#999999")
            }
            textPaint.color = color
            val paddingText = 16f
            // Tính max width cho text (chừa khoảng cho circle và margins)
            val maxWidth = width.toFloat() - (width.toFloat() / 9 + radiusCircle * 2 + marginStartTxt + sizeBorders * 2 + paddingText)

            // Ellipsize nếu text quá dài
            val ellipsizedText = TextUtils.ellipsize(
                nameText,
                textPaint,
                maxWidth,
                TextUtils.TruncateAt.END
            ).toString()

            // Get bounds cho text đã ellipsized
            textPaint.getTextBounds(ellipsizedText, 0, ellipsizedText.length, rectTextCenter)

            // Xác định vị trí vẽ text
            val x = width.toFloat() / 8 + radiusCircle * 2 + marginStartTxt + paddingText
            val y = height.toFloat() / 2 + rectTextCenter.height() / 2 + height / 10

            // Draw text
            canvas.drawText(ellipsizedText, x, y, this)

            /**
             * Add stroke circle
             */
            style = Paint.Style.STROKE
            strokeWidth = strokeCircle.toFloat()
            if (checkAction) {
                shader = linearGradientRoundCircle
            } else {
                color = Color.parseColor("#999999")
                shader = null
            }
            val pathCircleStroke = Path().apply {
                addCircle(
                    width.toFloat() / 9,
                    height.toFloat() / 2 + strokeCircle + height / 10,
                    radiusCircle.toFloat(),
                    Path.Direction.CCW
                )
            }
            canvas.drawPath(pathCircleStroke, this)

            /**
             * Add circle fill
             */
            style = Paint.Style.FILL
            shader = null
            color = Color.parseColor("#FF3CE0")
            val pathCircleFill = Path().apply {
                addCircle(
                    width.toFloat() / 9,
                    height.toFloat() / 2 + strokeCircle + height / 10,
                    radiusCircle.toFloat() - strokeCircle * 2,
                    Path.Direction.CCW
                )
            }
            if (checkAction) {
                canvas.drawPath(pathCircleFill, this)
            }
        }
    }


    fun isCheckActionUser(checkAction: Boolean) {
        this.checkAction = checkAction
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        drawRoundRect(canvas)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        linearGradientRoundRect = colors?.let {
            LinearGradient(
                0f, 0f,
                width.toFloat(), height.toFloat(), it, null, Shader.TileMode.CLAMP
            )
        }
        linearGradientRoundCircle = colors?.let {
            LinearGradient(
                width.toFloat() / 8 - radiusCircle, 0f,
                width.toFloat() / 8 + radiusCircle,
                0f, it, null, Shader.TileMode.CLAMP
            )
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }


}