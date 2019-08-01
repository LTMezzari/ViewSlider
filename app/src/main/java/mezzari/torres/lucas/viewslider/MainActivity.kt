package mezzari.torres.lucas.viewslider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.page_slider.view.*
import mezzari.torres.lucas.viewslider.widget.manager.VerticalLayoutManager
import mezzari.torres.lucas.viewslider.widget.ViewSlider
import mezzari.torres.lucas.viewslider.widget.manager.HorizontalLayoutManager

class MainActivity : AppCompatActivity() {

    private val adapter: ViewSlider.Adapter by lazy {
        object: ViewSlider.Adapter() {
            override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
                return inflater.inflate(R.layout.page_slider, parent, false)
            }

            override fun onBindView(view: View, position: Int) {
                view.tvPage.text = "${position + 1}"
            }

            override val itemCount: Int = TOTAL_ITEMS
        }.apply {
            onStartReached = {
                Toast.makeText(this@MainActivity, "Start Reached", Toast.LENGTH_LONG).show()
            }

            onCurrentViewChanged = { _, position ->
                title = "${position + 1} - $TOTAL_ITEMS"
            }

            onEndReached = {
                Toast.makeText(this@MainActivity, "End Reached", Toast.LENGTH_LONG).show()
            }
        }
    }

    private var layoutManager: ViewSlider.LayoutManager =
        HorizontalLayoutManager()
    private val isHorizontal: Boolean get() {
        return layoutManager is HorizontalLayoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewSlider?.apply {
            adapter = this@MainActivity.adapter
            layoutManager = this@MainActivity.layoutManager
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_revert) {
            layoutManager.isReversed = !layoutManager.isReversed
        } else if (item.itemId == R.id.menu_rotate) {
            layoutManager = if (isHorizontal) {
                VerticalLayoutManager()
            } else {
                HorizontalLayoutManager()
            }
            viewSlider.layoutManager = layoutManager
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val TOTAL_ITEMS = 180
    }
}
