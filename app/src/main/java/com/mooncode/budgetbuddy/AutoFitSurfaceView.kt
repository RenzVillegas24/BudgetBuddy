package com.mooncode.budgetbuddy

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView


// https://stackoverflow.com/questions/31410118/camera2-with-a-surfaceview
/* *
 * A [SurfaceView] that self-adjusts to a square aspect ratio.
* */
class AutoFitSurfaceView: SurfaceView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

     //Adjusts the aspect ratio of this view.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec) // Force a 1:1 aspect ratio

    }

}
