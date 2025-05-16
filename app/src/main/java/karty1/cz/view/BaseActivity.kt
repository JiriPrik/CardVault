package karty1.cz.view

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import karty1.cz.util.LanguageManager

/**
 * Základní aktivita, kterou dědí všechny ostatní aktivity.
 * Zajišťuje konzistentní nastavení jazyka v celé aplikaci.
 */
open class BaseActivity : AppCompatActivity() {

    private lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        languageManager = LanguageManager(newBase)
        val context = languageManager.updateBaseContextLocale(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageManager = LanguageManager(this)
    }
}
