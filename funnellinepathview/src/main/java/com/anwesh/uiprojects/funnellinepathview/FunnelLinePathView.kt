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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class FLPNode(var i : Int, val state : State = State()) {

        private var next : FLPNode? = null
        private var prev : FLPNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = FLPNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawFLPNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : FLPNode {
            var curr : FLPNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class FunnelLinePath(var i : Int) {

        private var curr : FLPNode = FLPNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : FunnelLinePathView) {

        private val animator : Animator = Animator(view)
        private val flp : FunnelLinePath = FunnelLinePath(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            flp.draw(canvas, paint)
            animator.animate {
                flp.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            flp.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity: Activity) : FunnelLinePathView {
            val view : FunnelLinePathView = FunnelLinePathView(activity)
            activity.setContentView(view)
            return view
        }
    }
}