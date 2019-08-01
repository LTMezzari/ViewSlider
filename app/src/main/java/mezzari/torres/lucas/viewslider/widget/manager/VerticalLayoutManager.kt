package mezzari.torres.lucas.viewslider.widget.manager

import android.view.View
import mezzari.torres.lucas.viewslider.widget.ViewSlider

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

    override fun onStartDragging(x: Float, y: Float, width: Int, height: Int): ViewSlider.Direction {
        initialTouchY = y
        return when {
            y > height / 2 -> {
                ViewSlider.Direction.NEXT
            }
            y < height / 2 -> {
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
                currentView?.y = 0 + (y - initialTouchY)
                this.y = height + (y - initialTouchY)
            }
        } else if (direction == ViewSlider.Direction.PREVIOUS) {
            draggedView?.run {
                currentView?.y = 0 + (y - initialTouchY)
                this.y = -height + (y - initialTouchY)
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
            direction == ViewSlider.Direction.NEXT && y < height / 2 -> {
                draggedView?.run {
                    currentView?.animate()?.y(-height.toFloat())
                    this.animate().y(0F)
                }
                true
            }
            direction == ViewSlider.Direction.PREVIOUS && y > height / 2 -> {
                draggedView?.run {
                    currentView?.animate()?.y(height.toFloat())
                    this.animate().y(0F)
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