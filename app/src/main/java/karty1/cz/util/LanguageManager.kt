package karty1.cz.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Třída pro správu jazyka aplikace.
 */
class LanguageManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val LANGUAGE_KEY = "selected_language"

        // Dostupné jazyky
        const val LANGUAGE_CZECH = "cs"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_GERMAN = "de"

        // Kódy jazyků pro zobrazení
        val LANGUAGE_CODES = mapOf(
            LANGUAGE_CZECH to "CZ",
            LANGUAGE_ENGLISH to "EN",
            LANGUAGE_GERMAN to "DE"
        )
    }

    /**
     * Uloží vybraný jazyk do SharedPreferences.
     */
    fun saveLanguage(languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }

    /**
     * Načte vybraný jazyk ze SharedPreferences.
     * Pokud není jazyk uložen, vrátí výchozí jazyk zařízení nebo češtinu.
     */
    fun getLanguage(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, getDefaultLanguage()) ?: getDefaultLanguage()
    }

    /**
     * Vrátí výchozí jazyk zařízení, pokud je podporován, jinak vrátí češtinu.
     */
    private fun getDefaultLanguage(): String {
        val deviceLanguage = Locale.getDefault().language
        return when (deviceLanguage) {
            "cs", "en", "de" -> deviceLanguage
            else -> LANGUAGE_CZECH
        }
    }

    /**
     * Nastaví jazyk aplikace.
     */
    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Uložení vybraného jazyka
        saveLanguage(languageCode)
    }

    /**
     * Aktualizuje kontext s vybraným jazykem.
     * Používá se v metodě attachBaseContext.
     */
    fun updateBaseContextLocale(context: Context): Context {
        val languageCode = getLanguage()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
        } else {
            updateResourcesLocaleLegacy(context, locale)
        }
    }

    /**
     * Aktualizuje zdroje s vybraným jazykem pro Android N a novější.
     */
    private fun updateResourcesLocale(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    /**
     * Aktualizuje zdroje s vybraným jazykem pro starší verze Androidu.
     */
    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        @Suppress("DEPRECATION")
        configuration.locale = locale
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}
