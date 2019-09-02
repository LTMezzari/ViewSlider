package mezzari.torres.lucas.view_slider

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * @author Lucas T. Mezzari
 * @since 22/07/2019
 **/
class ViewSlider: FrameLayout {

    private var hasLayoutBeingMeasured: Boolean = false

    private val inflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }

    var adapter: Adapter? = null
        set(value) {
            field = value
            field?.run {
                onItemsChanged = {
                    initializeView()
                }
                setupViews()
            }
        }

    var layoutManager: LayoutManager? = null
        set(value) {
            field = value
            field?.run {
                onStateChanged = {
                    setupViews()
                }
                setupViews()
            }
        }

    var offsetViews: Int = MINIMUM_OFFSET
        set(value) {
            field = value
            setupViews()
        }

    private val totalOffset: Int get() {
        return offsetViews * 2 + 1
    }

    private lateinit var sliderViews: Array<View>
    private val previousView: View? get() {
        return if (
            currentView == null
            || currentViewIndex <= 0
            || (hasBlankOffset && currentItemIndex <= 0)
        ) {
            null
        } else {
            sliderViews[currentViewIndex-1]
        }
    }
    private val nextView: View? get() {
        return if (
            currentView == null
            || currentViewIndex < 0
            || currentViewIndex == totalOffset-1
            || (hasBlankOffset && currentItemIndex >= adapter!!.itemCount-1)) {
            null
        } else {
            sliderViews[currentViewIndex+1]
        }
    }
    private var currentView: View? = null
        set(value) {
            field = value
            field?.run {
                adapter?.onCurrentViewChanged?.invoke(this, currentItemIndex)
            }
        }
    private var hasBlankOffset = false

    private var currentItemIndex: Int = -1
    private val currentViewIndex: Int get() {
        return sliderViews.indexOfFirst { it == currentView }
    }

    private var isDragging = false
    private var intendedDirection = Direction.NONE

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        loadAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        loadAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        loadAttributes(context, attrs)
    }

    private fun loadAttributes(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return
        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.ViewSlider, 0, 0)
            ?: return

        try {
            var offset = typedArray.getInt(R.styleable.ViewSlider_offset,
                MINIMUM_OFFSET
            )
            if (offset < MINIMUM_OFFSET) {
                offset = MINIMUM_OFFSET
            } else if (offset > MAXIMUM_OFFSET) {
                offset = MAXIMUM_OFFSET
            }

            this.offsetViews = offset
        } finally {
            typedArray.recycle()
        }
    }

    private fun setupViews() {
        if (!hasLayoutBeingMeasured || isInEditMode || adapter == null || layoutManager == null) return
        removeAllViews()
        sliderViews = Array(totalOffset) {
            val viewType = adapter!!.getItemViewType(it)
            val view = adapter!!.onCreateView(inflater, this, viewType)
            addView(view, 0)
            return@Array view.apply {
                layoutManager!!
                    .setUpInitialPosition(this, this@ViewSlider.width, this@ViewSlider.height, it, totalOffset)
                post {
                    updateViewLayout(this, layoutParams)
                }
            }
        }
        initializeView()
    }

    private fun initializeView() {
        if (!hasLayoutBeingMeasured || layoutManager == null || adapter == null || adapter!!.itemCount <= 0) return
        if (layoutManager!!.isReversed) {
            val lastViewIndex = this.totalOffset - 1
            val lastItemIndex = adapter!!.itemCount - 1
            for (i in lastViewIndex downTo 0) {
                val currentDifference = lastViewIndex - i
                val currentPosition = lastItemIndex - currentDifference
                if (currentPosition < 0) {
                    hasBlankOffset = true
                    break
                }
                adapter!!.onBindView(sliderViews[i], currentPosition)
            }
            currentItemIndex = lastItemIndex
            currentView = sliderViews[lastViewIndex]
        } else {
            for (i in sliderViews.indices) {
                if (i >= adapter!!.itemCount) {
                    hasBlankOffset = true
                    break
                }
                adapter!!.onBindView(sliderViews[i], i)
            }
            currentItemIndex = 0
            currentView = sliderViews[0]
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        hasLayoutBeingMeasured = true
        if (!::sliderViews.isInitialized)
            setupViews()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                true
            }

            MotionEvent.ACTION_MOVE -> {
                isDragging
            }

            MotionEvent.ACTION_UP -> {
                isDragging
            }

            else -> {
                false
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!isDragging) {
                    isDragging = true
                    onStartDragging(event.x, event.y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    onDragging(event.x, event.y)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    isDragging = false
                    onStopDragging(event.x, event.y)
                }
            }

            else -> {
                return false
            }
        }
        return true
    }

    private fun onStartDragging(x: Float, y: Float) {
        layoutManager!!.onStartDragging(x, y, width, height)
    }

    private fun onDragging(x: Float, y: Float) {
        layoutManager?.onDragging(currentView, previousView, nextView, x, y)
    }

    private fun onStopDragging(x: Float, y: Float) {
        intendedDirection = layoutManager?.onEndDragging(currentView, previousView, nextView, x, y, width, height)
            ?: Direction.NONE

        when {
            intendedDirection == Direction.NEXT -> {
                goToNextView()
            }

            intendedDirection == Direction.PREVIOUS && x > width / 3 -> {
                goToPreviousView()
            }

            else -> {
                layoutManager?.resetViewsPosition(currentView, previousView, nextView, width, height)
            }
        }
        intendedDirection = Direction.NONE
    }

    private fun goToNextView() {
        val middleIndex = offsetViews
        val lastItemIndex = adapter!!.itemCount - 1
        if (currentItemIndex in 0 until lastItemIndex) {
            if (currentItemIndex + offsetViews < lastItemIndex && currentViewIndex == middleIndex) {
                val firstView = sliderViews[0]
                for (i in 0 until sliderViews.size) {
                    if (i == sliderViews.size - 1) {
                        sliderViews[i] = firstView
                        adapter!!.onBindView(sliderViews[i], currentItemIndex + (offsetViews + 1))
                    } else {
                        sliderViews[i] = sliderViews[i + 1]
                    }
                }
                currentItemIndex++
                currentView = sliderViews[middleIndex]
            } else {
                currentItemIndex++
                currentView = sliderViews[currentViewIndex + 1]
            }
        } else if (currentItemIndex == lastItemIndex) {
            adapter!!.onEndReached?.invoke()
        }
    }

    private fun goToPreviousView() {
        val middleIndex = offsetViews
        if (currentItemIndex in 1 until adapter!!.itemCount) {
            if (currentItemIndex - offsetViews > 0 && currentViewIndex == middleIndex) {
                val lastView = sliderViews[totalOffset - 1]
                for (i in sliderViews.size - 1 downTo 0) {
                    if (i == 0) {
                        sliderViews[i] = lastView
                        adapter!!.onBindView(sliderViews[i], currentItemIndex - (offsetViews + 1))
                    } else {
                        sliderViews[i] = sliderViews[i - 1]
                    }
                }
                currentItemIndex--
                currentView = sliderViews[middleIndex]
            } else {
                currentItemIndex--
                currentView = sliderViews[currentViewIndex - 1]
            }
        } else if (currentItemIndex == 0) {
            adapter!!.onStartReached?.invoke()
        }
    }

    abstract class Adapter {
        abstract fun onCreateView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View
        abstract fun onBindView(view: View, position: Int)
        open fun getItemViewType(position: Int): Int {
            return 0
        }
        abstract val itemCount: Int

        var onStartReached: (() -> Unit)? = null
        var onEndReached: (() -> Unit)? = null
        var onCurrentViewChanged: ((View, Int) -> Unit)? = null
        internal var onItemsChanged: (() -> Unit)? = null

        fun notifyDataSetChanged() {
            onItemsChanged?.invoke()
        }
    }

    abstract class LayoutManager (
        isReversed: Boolean = false
    ) {
        var isReversed: Boolean = false
            set(value) {
                field = value
                notifyStateChanged()
            }
        internal var onStateChanged: (() -> Unit)? = null

        init {
            this.isReversed = isReversed
        }

        abstract fun setUpInitialPosition(view: View, width: Int, height: Int, position: Int, totalOffset: Int)
        abstract fun onStartDragging(x: Float, y: Float, width: Int, height: Int)
        abstract fun onDragging(currentView: View?, previousView: View?, nextView: View?, x: Float, y: Float)
        abstract fun onEndDragging(currentView: View?, previousView: View?, nextView: View?, x: Float, y: Float, width: Int, height: Int)
                : Direction
        abstract fun resetViewsPosition(currentView: View?, previousView: View?, nextView: View?, width: Int, height: Int)

        fun notifyStateChanged() {
            onStateChanged?.invoke()
        }
    }

    enum class Direction {
        NONE,
        NEXT,
        PREVIOUS
    }

    companion object {
        private const val MINIMUM_OFFSET = 3
        private const val MAXIMUM_OFFSET = 10
    }
}