package karty1.cz.view

import android.os.Bundle

import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import karty1.cz.R
import karty1.cz.util.LogHelper

/**
 * Aktivita pro zobrazení návodu k aplikaci.
 */
class HelpActivity : BaseActivity() {

    companion object {
        private const val TAG = "HelpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // Nastavení toolbaru
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Skrytí titulku

        // Nastavení tlačítka pro zavření
        val fabClose = findViewById<FloatingActionButton>(R.id.fabClose)
        fabClose.setOnClickListener {
            LogHelper.d(TAG, "Kliknutí na tlačítko zavřít")
            finish()
        }
    }
}
