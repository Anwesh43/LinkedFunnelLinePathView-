package com.anwesh.uiprojects.funnellinepathview

/**
 * Created by anweshmishra on 03/10/20.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Path

val colors : Array<Int> = arrayOf(
        "#3F51B5",
        "#F44336",
        "#4CAF50",
        "#FFC107",
        "#009688"
).map({Color.parseColor(it)}).toTypedArray()
val parts : Int = 4
val scGap : Float = 0.02f
val strokeFactor : Float = 90f
val hFactor : Float = 3f
val wFactor : Float = 5.4f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
