package mezzari.torres.lucas.view_slider.manager

import android.view.View
import mezzari.torres.lucas.view_slider.ViewSlider

/**
 * @author Lucas T. Mezzari
 * @since 23/07/2019
 **/
class HorizontalLayoutManager(isReversed: Boolean = false): ViewSlider.LayoutManager(isReversed) {
    private var initialTouchX = -1F

    override fun setUpInitialPosition(view: View, width: Int, height: Int, position: Int, totalOffset: Int) {
        val nextXPosition: Float = if (isReversed) {
            -(((totalOffset - 1) - position) * width).toFloat()
        } else {
            (position * width).toFloat()
        }

        view.x = nextXPosition
    }

    override fun onStartDragging(x: Float, y: Float, width: Int, height: Int) {
        initialTouchX = x
    }

    override fun onDragging(
        currentView: View?,
        previousView: View?,
        nextView: View?,
        x: Float,
        y: Float
    ) {
        if (initialTouchX > x) {
            nextView?.run {
                currentView?.x = 0 + (x - initialTouchX)
                this.x = width + (x - initialTouchX)

                currentView?.invalidate()
                invalidate()
            }
        } else if (initialTouchX < x) {
            previousView?.run {
                currentView?.x = 0 + (x - initialTouchX)
                this.x = -width + (x - initialTouchX)

                currentView?.invalidate()
                invalidate()
            }
        }
    }

    override fun onEndDragging(
        currentView: View?,
        previousView: View?,
        nextView: View?,
        x: Float,
        y: Float,
        width: Int,
        height: Int
    ): ViewSlider.Direction {
        return when {
            initialTouchX > x && x < (width / 3) * 2 -> {
                nextView?.run {
                    currentView?.animate()?.x(-width.toFloat())
                    this.animate().x(0F)
                }
                ViewSlider.Direction.NEXT
            }
            initialTouchX < x && x > width / 3 -> {
                previousView?.run {
                    currentView?.animate()?.x(width.toFloat())
                    this.animate().x(0F)
                }
                ViewSlider.Direction.PREVIOUS
            }
            else -> {
                ViewSlider.Direction.NONE
            }
        }
    }

    override fun resetViewsPosition(currentView: View?, previousView: View?, nextView: View?, width: Int, height: Int) {
        currentView?.run {
            this.animate().x(0F)
        }
        previousView?.run {
            this.animate().x(-width.toFloat())
        }
        nextView?.run {
            this.animate().x(width.toFloat())
        }
    }
}