package karty1.cz.view

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import karty1.cz.util.LanguageManager
import karty1.cz.util.PreferenceManager

/**
 * Základní aktivita, kterou dědí všechny ostatní aktivity.
 * Zajišťuje konzistentní nastavení jazyka v celé aplikaci.
 */
open class BaseActivity : AppCompatActivity() {

    private lateinit var languageManager: LanguageManager
    protected lateinit var preferenceManager: PreferenceManager

    override fun attachBaseContext(newBase: Context) {
        languageManager = LanguageManager(newBase)
        val context = languageManager.updateBaseContextLocale(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializace PreferenceManager a aplikace motivu
        preferenceManager = PreferenceManager(this)
        preferenceManager.applyThemeMode()

        super.onCreate(savedInstanceState)
        languageManager = LanguageManager(this)
    }
}
