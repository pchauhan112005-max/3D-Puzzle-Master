package com.example.puzzlemaster

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.os.Handler
import android.os.Looper
import kotlin.math.min
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(23, 20, 58)
        window.navigationBarColor = Color.rgb(23, 20, 58)
        setContentView(PuzzleView())
    }
}

class PuzzleView : View {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tiles = IntArray(16) { if (it == 15) 0 else it + 1 }
    private var moves = 0
    private var seconds = 0
    private var started = false
    private var won = false
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            if (started && !won) { seconds++; invalidate(); handler.postDelayed(this, 1000) }
        }
    }
    private var boardLeft = 0f
    private var boardTop = 0f
    private var cell = 0f
    private var gap = 9f
    private var buttonTop = 0f
    private var buttonSize = 0f

    constructor() : super(null) { shuffle() }

    private fun shuffle() {
        // Start from solved state and make legal moves, so the puzzle is always solvable.
        for (i in 0 until 350) {
            val blank = tiles.indexOf(0)
            val options = mutableListOf<Int>()
            if (blank / 4 > 0) options.add(blank - 4)
            if (blank / 4 < 3) options.add(blank + 4)
            if (blank % 4 > 0) options.add(blank - 1)
            if (blank % 4 < 3) options.add(blank + 1)
            val pick = options[Random.nextInt(options.size)]
            tiles[blank] = tiles[pick]
            tiles[pick] = 0
        }
        moves = 0; seconds = 0; started = false; won = false
        handler.removeCallbacks(tick)
        invalidate()
    }

    private fun moveBy(delta: Int) {
        if (won) return
        val b = tiles.indexOf(0)
        val target = b + delta
        if (target !in 0..15) return
        if (delta == -1 && b % 4 == 0) return
        if (delta == 1 && b % 4 == 3) return
        if (delta != -1 && delta != 1 && delta != -4 && delta != 4) return
        tiles[b] = tiles[target]; tiles[target] = 0
        moves++
        if (!started) { started = true; handler.postDelayed(tick, 1000) }
        won = (0 until 15).all { tiles[it] == it + 1 }
        if (won) handler.removeCallbacks(tick)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        val bg = LinearGradient(0f, 0f, w, h, intArrayOf(Color.rgb(25,18,65), Color.rgb(14,45,87), Color.rgb(65,22,91)), null, Shader.TileMode.CLAMP)
        paint.shader = bg; canvas.drawRect(0f, 0f, w, h, paint); paint.shader = null

        paint.color = Color.WHITE; paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        paint.textSize = w * .085f
        canvas.drawText("3D PUZZLE MASTER", w/2, h*.105f, paint)
        paint.textSize = w*.038f; paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        paint.color = Color.rgb(196, 220, 255)
        canvas.drawText("Arrange the numbers from 1 to 15", w/2, h*.145f, paint)

        val panelW = w*.88f
        val panelX = (w-panelW)/2
        val panelY = h*.19f
        drawRound(canvas, panelX, panelY, panelX+panelW, panelY+h*.095f, 24f, Color.argb(65,255,255,255), Color.argb(40,255,255,255))
        paint.color = Color.WHITE; paint.textSize = w*.045f; paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        canvas.drawText("MOVES   $moves                 TIME   ${seconds/60}:${(seconds%60).toString().padStart(2,'0')}", w/2, panelY+h*.059f, paint)

        val boardSize = min(w*.88f, h*.49f)
        cell = boardSize/4f
        gap = boardSize*.018f
        boardLeft = (w-boardSize)/2
        boardTop = h*.33f
        drawRound(canvas, boardLeft-10, boardTop-10, boardLeft+boardSize+10, boardTop+boardSize+10, 30f, Color.argb(90,0,0,0), Color.argb(70,255,255,255))
        for (i in 0..15) {
            val row=i/4; val col=i%4
            val x=boardLeft+col*cell+gap/2
            val y=boardTop+row*cell+gap/2
            val v=tiles[i]
            if (v==0) {
                drawRound(canvas,x,y,x+cell-gap,y+cell-gap,20f,Color.argb(55,0,0,0),Color.argb(35,255,255,255))
            } else {
                val colors = intArrayOf(Color.rgb(255,91,125),Color.rgb(255,165,66),Color.rgb(255,215,74),Color.rgb(75,213,160),Color.rgb(58,185,255),Color.rgb(125,112,255),Color.rgb(213,99,255))
                val c=colors[(v-1)%colors.size]
                drawTile(canvas,x,y,x+cell-gap,y+cell-gap,c)
                paint.color=Color.WHITE; paint.textAlign=Paint.Align.CENTER; paint.typeface=Typeface.create("sans-serif-black",Typeface.BOLD); paint.textSize=cell*.34f
                canvas.drawText(v.toString(),x+(cell-gap)/2,y+(cell-gap)*.62f,paint)
            }
        }

        buttonTop = boardTop+boardSize+h*.045f
        buttonSize = w*.145f
        val cx=w/2
        drawControl(canvas,cx-buttonSize/2,buttonTop, "↑")
        drawControl(canvas,cx-buttonSize*1.65f,buttonTop+buttonSize+10, "←")
        drawControl(canvas,cx-buttonSize/2,buttonTop+buttonSize+10, "↓")
        drawControl(canvas,cx+buttonSize*.65f,buttonTop+buttonSize+10, "→")

        val restartY=buttonTop+buttonSize*2+35
        val rw=w*.48f
        drawRound(canvas,(w-rw)/2,restartY,(w+rw)/2,restartY+h*.065f,22f,Color.rgb(255,91,145),Color.rgb(255,185,205))
        paint.color=Color.WHITE; paint.textSize=w*.043f; paint.typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        canvas.drawText("↻  NEW GAME",w/2,restartY+h*.044f,paint)
        if (won) {
            paint.color=Color.rgb(255,238,120); paint.textSize=w*.065f
            canvas.drawText("🎉 YOU WON! 🎉",w/2,h*.91f,paint)
        } else {
            paint.color=Color.argb(210,255,255,255); paint.textSize=w*.032f; paint.typeface=Typeface.create("sans-serif",Typeface.NORMAL)
            canvas.drawText("Tap the arrow buttons to move tiles",w/2,h*.965f,paint)
        }
    }

    private fun drawTile(c: Canvas,x:Float,y:Float,r:Float,b:Float,color:Int) {
        val shadow=Paint(Paint.ANTI_ALIAS_FLAG); shadow.color=Color.argb(100,0,0,0)
        c.drawRoundRect(x+3,y+7,r+3,b+7,20f,20f,shadow)
        val shader=LinearGradient(x,y,r,b, lighten(color), color, Shader.TileMode.CLAMP)
        paint.shader=shader; c.drawRoundRect(x,y,r,b,20f,20f,paint); paint.shader=null
        paint.style=Paint.Style.STROKE; paint.strokeWidth=2f; paint.color=Color.argb(100,255,255,255)
        c.drawRoundRect(x+2,y+2,r-2,b-2,18f,18f,paint); paint.style=Paint.Style.FILL
    }

    private fun drawControl(c:Canvas,x:Float,y:Float,label:String) {
        drawRound(c,x,y,x+buttonSize,y+buttonSize,20f,Color.rgb(57,174,255),Color.rgb(180,239,255))
        paint.color=Color.WHITE; paint.textAlign=Paint.Align.CENTER; paint.typeface=Typeface.create("sans-serif-black",Typeface.BOLD); paint.textSize=buttonSize*.62f
        c.drawText(label,x+buttonSize/2,y+buttonSize*.72f,paint)
    }

    private fun drawRound(c:Canvas,l:Float,t:Float,r:Float,b:Float,rad:Float,color:Int,stroke:Int) {
        paint.color=Color.argb(70,0,0,0); c.drawRoundRect(l+2,t+5,r+2,b+5,rad,rad,paint)
        paint.color=color; c.drawRoundRect(l,t,r,b,rad,rad,paint)
        paint.style=Paint.Style.STROKE; paint.strokeWidth=2f; paint.color=stroke
        c.drawRoundRect(l+1,t+1,r-1,b-1,rad,rad,paint); paint.style=Paint.Style.FILL
    }
    private fun lighten(c:Int):Int {
        fun up(v:Int)= (v+65).coerceAtMost(255)
        return Color.rgb(up(Color.red(c)),up(Color.green(c)),up(Color.blue(c)))
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x=e.x; val y=e.y; val cx=width/2f
        if (y >= buttonTop && y < buttonTop+buttonSize) {
            moveBy(-4); return true
        }
        if (y >= buttonTop+buttonSize && y < buttonTop+buttonSize*2+12) {
            when {
                x < cx-buttonSize*.55f -> moveBy(-1)
                x > cx+buttonSize*.55f -> moveBy(1)
                else -> moveBy(4)
            }
            return true
        }
        val restartY=buttonTop+buttonSize*2+35
        if (y in restartY..(restartY+height*.08f)) { shuffle(); return true }
        // Also allow tapping a tile adjacent to the empty space.
        if (x in boardLeft..(boardLeft+cell*4) && y in boardTop..(boardTop+cell*4)) {
            val col=((x-boardLeft)/cell).toInt().coerceIn(0,3)
            val row=((y-boardTop)/cell).toInt().coerceIn(0,3)
            val idx=row*4+col; val blank=tiles.indexOf(0)
            if ((idx/4==blank/4 && kotlin.math.abs(idx%4-blank%4)==1) || (idx%4==blank%4 && kotlin.math.abs(idx/4-blank/4)==1)) {
                val temp=tiles[blank]; tiles[blank]=tiles[idx]; tiles[idx]=temp
                moves++; if(!started){started=true;handler.postDelayed(tick,1000)}
                won=(0 until 15).all{tiles[it]==it+1}; invalidate()
            }
            return true
        }
        return true
    }
}
