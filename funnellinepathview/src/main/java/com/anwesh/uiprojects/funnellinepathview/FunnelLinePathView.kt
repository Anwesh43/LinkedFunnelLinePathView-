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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i  * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
fun Float.to(end : Float, scale : Float) : Float = this + (end - this) * scale

fun Canvas.drawFunnelLinePath(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = scale.divideScale(0, parts)
    val sf2 : Float = scale.divideScale(1, parts)
    val sf3 : Float = scale.divideScale(2, parts)
    val sf4 : Float = scale.divideScale(3, parts)
    val x : Float = w / wFactor
    val y : Float = h / hFactor
    val xGap : Float = w / 2 - x

    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        drawLine(-w / 2, h / 2, - w / 2 + xGap * sf1, h / 2 - y * sf2, paint)
        drawLine(-w / 2 + xGap, h / 2 - y, - w/ 2 + xGap, (h / 2 - y).to(-(h / 2 - y), sf2), paint)
        drawLine(-w / 2 + xGap, (-h / 2 + y), -w / 2 + xGap + xGap * sf3, (-h / 2 + y).to(-h / 2, sf3), paint)
        val path : Path = Path()
        path.moveTo(0f, h / 2)
        path.lineTo(- w / 2, h / 2)
        path.lineTo(-w / 2 + xGap, h / 2 - y)
        path.lineTo(-w / 2 + xGap, -h / 2 + y)
        path.lineTo(-w / 2, -h / 2)
        path.lineTo(0f, -h / 2)
        path.lineTo(0f, h / 2)
        clipPath(path)
        drawRect(RectF(-w / 2, h / 2 - h * sf4, w / 2, h / 2), paint)
        restore()
    }
    restore()
}

fun Canvas.drawFLPNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawFunnelLinePath(scale, w, h, paint)
}

class FunnelLinePathView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}