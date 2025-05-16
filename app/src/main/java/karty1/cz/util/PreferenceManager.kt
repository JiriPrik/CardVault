package karty1.cz.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Třída pro správu preferencí aplikace.
 */
class PreferenceManager(context: Context) {

    companion object {
        private const val PREF_NAME = "karty1_preferences"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_ENCRYPT_IMAGES = "encrypt_images"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Uloží hash hesla do preferencí.
     */
    fun savePasswordHash(passwordHash: String) {
        sharedPreferences.edit().putString(KEY_PASSWORD_HASH, passwordHash).apply()
    }

    /**
     * Získá hash hesla z preferencí.
     */
    fun getPasswordHash(): String {
        return sharedPreferences.getString(KEY_PASSWORD_HASH, "") ?: ""
    }

    /**
     * Vymaže hash hesla z preferencí.
     */
    fun clearPasswordHash() {
        sharedPreferences.edit().remove(KEY_PASSWORD_HASH).apply()
    }

    /**
     * Nastaví, zda mají být fotografie šifrovány.
     */
    fun setEncryptImages(encrypt: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ENCRYPT_IMAGES, encrypt).apply()
    }

    /**
     * Zjistí, zda mají být fotografie šifrovány.
     */
    fun getEncryptImages(): Boolean {
        return sharedPreferences.getBoolean(KEY_ENCRYPT_IMAGES, true) // Výchozí hodnota je true pro vyšší bezpečnost
    }

    /**
     * Uloží heslo pro šifrování fotografií.
     * Používáme stejné heslo jako pro přihlášení, ale ukládáme ho v nešifrované podobě pro použití při šifrování.
     */
    fun getEncryptionPassword(): String {
        // Získání hesla z hash hodnoty - používáme původní heslo pro šifrování
        // Toto je zjednodušené řešení - v reálné aplikaci by bylo lepší použít KeyStore
        return "defaultPassword" // Toto by mělo být nahrazeno skutečným heslem uživatele
    }
}
