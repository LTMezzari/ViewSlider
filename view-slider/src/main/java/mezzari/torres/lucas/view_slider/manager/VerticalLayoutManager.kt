package mezzari.torres.lucas.view_slider.manager

import android.view.View
import mezzari.torres.lucas.view_slider.ViewSlider

/**
 * @author Lucas T. Mezzari
 * @since 23/07/2019
 **/
class VerticalLayoutManager(isReversed: Boolean = false): ViewSlider.LayoutManager(isReversed) {
    private var initialTouchY = -1F

    override fun setUpInitialPosition(view: View, width: Int, height: Int, position: Int, totalOffset: Int) {
        val nextYPosition: Float = if (isReversed) {
            -(((totalOffset - 1) - position) * height).toFloat()
        } else {
            (position * height).toFloat()
        }

        view.y = nextYPosition
    }

    override fun onStartDragging(x: Float, y: Float, width: Int, height: Int) {
        initialTouchY = y
    }

    override fun onDragging(
        currentView: View?,
        previousView: View?,
        nextView: View?,
        x: Float,
        y: Float
    ) {
        if (initialTouchY > y) {
            nextView?.run {
                currentView?.y = 0 + (y - initialTouchY)
                this.y = height + (y - initialTouchY)

                currentView?.invalidate()
                invalidate()
            }
        } else if (initialTouchY < y) {
            previousView?.run {
                currentView?.y = 0 + (y - initialTouchY)
                this.y = -height + (y - initialTouchY)

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
            initialTouchY > y && y < height / 2 -> {
                nextView?.run {
                    currentView?.animate()?.y(-height.toFloat())
                    this.animate().y(0F)
                }
                ViewSlider.Direction.NEXT
            }
            initialTouchY < y && y > height / 2 -> {
                previousView?.run {
                    currentView?.animate()?.y(height.toFloat())
                    this.animate().y(0F)
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
            this.animate().y(0F)
        }
        previousView?.run {
            this.animate().y(-height.toFloat())
        }
        nextView?.run {
            this.animate().y(height.toFloat())
        }
    }

}