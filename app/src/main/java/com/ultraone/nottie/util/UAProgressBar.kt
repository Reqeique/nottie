package com.ultraone.nottie.util

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSeekBar

class UAProgressBar : AppCompatSeekBar {
    private var mProgressItemsList: MutableList<ProgressItem>? = null

    constructor(context: Context?) : super(context!!) {
        mProgressItemsList = ArrayList<ProgressItem>()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
    }

    fun initData(progressItemsList: MutableList<ProgressItem>?) {
        mProgressItemsList = progressItemsList?.sortedBy {
            it.color
        }?.toMutableList()
    }

    @Synchronized
    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        if (mProgressItemsList!!.size > 0) {
            val progressBarWidth = width
            val progressBarHeight = height
            val thumboffset = thumbOffset
            var lastProgressX = 0
            var progressItemWidth: Int
            var progressItemRight: Int
            val progressPaint = Paint()

            for (i in mProgressItemsList!!.indices) {

                val progressItem: ProgressItem = mProgressItemsList!![i]

                progressPaint.color = progressItem.color
                progressItemWidth = ((progressItem.progressItemPercentage
                        * progressBarWidth )/ 100).toInt()
                progressItemRight = lastProgressX + progressItemWidth

                // for last item give right to progress item to the width
                if (i == mProgressItemsList!!.size - 1
                    && progressItemRight != progressBarWidth
                ) {
                    progressItemRight = progressBarWidth
                }

                val progressRect = RectF()
               // progressRect

                progressRect[lastProgressX.toFloat() , (thumboffset / 2).toFloat(), progressItemRight.toFloat()] =
                    (progressBarHeight - thumboffset / 2).toFloat()
                canvas.drawRoundRect(progressRect, 10f,10f , progressPaint)
                lastProgressX = progressItemRight
            }

//            canvas.drawRoundRect(
//                canvas.height.toFloat(), canvas.width.toFloat(), canvas.height.toFloat(),
//                canvas.width.toFloat(),
//                10F,10f, progressPaint)

            super.onDraw(canvas)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun dispatchDraw(canvas: Canvas?) {
        val path = Path()
        path.addRoundRect(height.toFloat(), width.toFloat(), height.toFloat(),
            width.toFloat(),
            10F,10f, Path.Direction.CW)
        canvas!!.clipPath(path)

       // canvas?.clipOutPath(path)
        super.dispatchDraw(canvas)
    }
}

data class ProgressItem (
    var color: Int ,
    var progressItemPercentage : Float
)