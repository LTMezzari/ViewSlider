package mezzari.torres.lucas.viewslider.widget.manager

import android.view.View
import mezzari.torres.lucas.viewslider.widget.ViewSlider

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

    override fun onStartDragging(x: Float, y: Float, width: Int, height: Int): ViewSlider.Direction {
        initialTouchX = x
        return when {
            x > (width / 3 * 2) -> {
                ViewSlider.Direction.NEXT
            }
            x < width / 3 -> {
                ViewSlider.Direction.PREVIOUS
            }
            else -> {
                ViewSlider.Direction.NONE
            }
        }
    }

    override fun onDragging(
        direction: ViewSlider.Direction,
        currentView: View?,
        draggedView: View?,
        x: Float,
        y: Float
    ) {
        if (direction == ViewSlider.Direction.NEXT) {
            draggedView?.run {
                currentView?.x = 0 + (x - initialTouchX)
                this.x = width + (x - initialTouchX)
            }
        } else if (direction == ViewSlider.Direction.PREVIOUS) {
            draggedView?.run {
                currentView?.x = 0 + (x - initialTouchX)
                this.x = -width + (x - initialTouchX)
            }
        }
    }

    override fun onEndDragging(
        direction: ViewSlider.Direction,
        currentView: View?,
        draggedView: View?,
        x: Float,
        y: Float,
        width: Int,
        height: Int
    ): Boolean {
        return when {
            direction == ViewSlider.Direction.NEXT && x < (width / 3) * 2 -> {
                draggedView?.run {
                    currentView?.animate()?.x(-width.toFloat())
                    this.animate().x(0F)
                }
                true
            }
            direction == ViewSlider.Direction.PREVIOUS && x > width / 3 -> {
                draggedView?.run {
                    currentView?.animate()?.x(width.toFloat())
                    this.animate().x(0F)
                }
                true
            }
            else -> {
                false
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