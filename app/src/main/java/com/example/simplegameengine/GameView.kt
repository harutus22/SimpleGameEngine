package com.example.simplegameengine

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView


class GameView constructor(context: Context) : SurfaceView(context), Runnable {

    private var gameThread: Thread? = null
    private var ourHolder: SurfaceHolder = holder
    @Volatile
    private var playing = false

    private lateinit var canvas: Canvas
    private var paint: Paint = Paint()

    private var fps = 0L
    private var timeThisFrame = 0L

    private var bitmapBob: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bob)

    private var isMoving = false
    private val walkSpeedPerSecond = 250f
    private var bobXPosition = 10f

    private var frameWidth = 200
    private var frameHeight = 100

    private var frameCount = 5
    private var currentFrame = 0
    private var lastFrameChangeTime = 0L
    private var frameLengthInMilliSeconds = 100
    private val frameToDraw = Rect(0, 0, frameWidth, frameHeight)
    private var whereToDraw = RectF(bobXPosition, 0f, bobXPosition + frameWidth, frameHeight.toFloat())

    init {
        bitmapBob = Bitmap.createScaledBitmap(bitmapBob, frameWidth * frameCount, frameHeight, false)
    }

    override fun run() {
        while (playing) {
            val startFrameTime = System.currentTimeMillis()

            update()

            draw()

            timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame > 0) {
                fps = 1000 / timeThisFrame
            }
        }
    }

    private fun update() {
        if (isMoving) {
            bobXPosition += (walkSpeedPerSecond / fps)
        }
    }

    private fun draw() {
        if (ourHolder.surface.isValid) {
            canvas = ourHolder.lockCanvas()

            canvas.drawColor(Color.argb(255, 26, 128, 182))
            paint.color = Color.argb(255, 249, 129, 0)

            paint.textSize = 45f

            canvas.drawText("FPS:$fps", 20f, 40f, paint)

            whereToDraw.set(bobXPosition, 10f, bobXPosition.toInt() + frameWidth.toFloat(), frameHeight.toFloat())
            getCurrentFrame()
            canvas.drawBitmap(bitmapBob, frameToDraw, whereToDraw, paint)

            ourHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun getCurrentFrame(){
        val time = System.currentTimeMillis()
        if (isMoving) {
            if (time > lastFrameChangeTime + frameLengthInMilliSeconds) {
                lastFrameChangeTime = time
                currentFrame++
                if (currentFrame >= frameCount) {
                    currentFrame = 0
                }
            }
        }
        frameToDraw.left = currentFrame * frameWidth
        frameToDraw.right = frameToDraw.left + frameWidth
    }

    fun pause() {
        playing = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            Log.e("ERROR", "Joining thread")
        }
    }

    fun resume() {
        playing = true
        gameThread = Thread(this)
        gameThread!!.start()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN || MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
            isMoving = true
        } else if (event?.action == MotionEvent.ACTION_UP || MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
            isMoving = false
        }
        return true
    }
}