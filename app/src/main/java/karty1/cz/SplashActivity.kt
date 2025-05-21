package karty1.cz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.textfield.TextInputEditText
import karty1.cz.util.LanguageManager
import karty1.cz.util.LogHelper
import karty1.cz.util.PasswordUtils
import karty1.cz.util.PreferenceManager

/**
 * Úvodní obrazovka aplikace s informacemi o tvůrci a ověřením hesla.
 */
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

    private lateinit var editPassword: TextInputEditText
    private lateinit var buttonContinue: Button
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var logoContainer: FrameLayout
    private lateinit var imageLogo: ImageView
    private lateinit var cardsContainer: LinearLayout
    private lateinit var card1: View
    private lateinit var card2: View
    private lateinit var card3: View
    private lateinit var card4: View
    private lateinit var buttonHelp: Button
    private lateinit var buttonCzech: Button
    private lateinit var buttonEnglish: Button
    private lateinit var buttonGerman: Button
    private lateinit var buttonDonate: Button
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializace PreferenceManager a aplikace motivu
        preferenceManager = PreferenceManager(this)
        preferenceManager.applyThemeMode()

        super.onCreate(savedInstanceState)

        // Inicializace správce jazyka a nastavení jazyka
        languageManager = LanguageManager(this)
        languageManager.setLocale(languageManager.getLanguage())

        setContentView(R.layout.activity_splash)

        // Inicializace komponent
        editPassword = findViewById(R.id.editPassword)
        buttonContinue = findViewById(R.id.buttonContinue)
        logoContainer = findViewById(R.id.logoContainer)
        imageLogo = findViewById(R.id.imageLogo)
        cardsContainer = findViewById(R.id.cardsContainer)
        card1 = findViewById(R.id.card1)
        card2 = findViewById(R.id.card2)
        card3 = findViewById(R.id.card3)
        card4 = findViewById(R.id.card4)
        buttonHelp = findViewById(R.id.buttonHelp)
        buttonCzech = findViewById(R.id.buttonCzech)
        buttonEnglish = findViewById(R.id.buttonEnglish)
        buttonGerman = findViewById(R.id.buttonGerman)
        buttonDonate = findViewById(R.id.buttonDonate)

        // Zvýraznění aktuálně vybraného jazyka
        highlightSelectedLanguage()

        // Nastavení posluchače pro tlačítko pokračovat
        buttonContinue.setOnClickListener {
            validatePassword()
        }

        // Nastavení posluchače pro tlačítko návodu
        buttonHelp.setOnClickListener {
            LogHelper.d(TAG, "Kliknutí na tlačítko návodu")
            showHelp()
        }

        // Nastavení posluchačů pro tlačítka jazyků
        buttonCzech.setOnClickListener {
            changeLanguage(LanguageManager.LANGUAGE_CZECH)
        }

        buttonEnglish.setOnClickListener {
            changeLanguage(LanguageManager.LANGUAGE_ENGLISH)
        }

        buttonGerman.setOnClickListener {
            changeLanguage(LanguageManager.LANGUAGE_GERMAN)
        }

        // Nastavení posluchače pro tlačítko příspěvku
        buttonDonate.setOnClickListener {
            LogHelper.d(TAG, "Kliknutí na tlačítko příspěvku")
            showDonateDialog()
        }

        // Nastavení verze aplikace
        val textVersion = findViewById<TextView>(R.id.textVersion)
        // Použijeme pevně nastavenou verzi, protože BuildConfig není dostupný
        textVersion.text = "v1.5"

        // Spuštění animací
        startAnimations()
    }

    /**
     * Spustí animace pro komponenty na úvodní obrazovce.
     */
    private fun startAnimations() {
        try {
            // Animace pro logo
            val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
            logoContainer.startAnimation(pulseAnimation)

            // Animace pro karty
            val cardAnimation = AnimationUtils.loadAnimation(this, R.anim.card_animation)

            // Nastavení zpoždění pro jednotlivé karty
            card1.startAnimation(cardAnimation)

            cardAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    // Po dokončení animace první karty spustíme animaci druhé karty
                    val cardAnimation2 = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.card_animation)
                    card2.startAnimation(cardAnimation2)

                    cardAnimation2.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            // Po dokončení animace druhé karty spustíme animaci třetí karty
                            val cardAnimation3 = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.card_animation)
                            card3.startAnimation(cardAnimation3)

                            cardAnimation3.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {}

                                override fun onAnimationEnd(animation: Animation?) {
                                    // Po dokončení animace třetí karty spustíme animaci čtvrté karty
                                    val cardAnimation4 = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.card_animation)
                                    card4.startAnimation(cardAnimation4)
                                }

                                override fun onAnimationRepeat(animation: Animation?) {}
                            })
                        }

                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při spuštění animací: ${e.message}", e)
        }
    }

    /**
     * Ověří zadané heslo a případně spustí hlavní aktivitu.
     */
    private fun validatePassword() {
        val password = editPassword.text.toString()

        if (password.isEmpty()) {
            Toast.makeText(this, "Zadejte heslo", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Kontrola, zda již existuje uložený hash hesla
            val savedPasswordHash = preferenceManager.getPasswordHash()

            if (savedPasswordHash.isEmpty()) {
                // První spuštění - uložení hashe hesla
                LogHelper.i(TAG, "První spuštění - ukládání hesla")
                val passwordHash = PasswordUtils.hashPassword(password)
                preferenceManager.savePasswordHash(passwordHash)

                // Nastavení šifrování fotografií
                preferenceManager.setEncryptImages(true)

                // Uložení hesla pro šifrování
                saveEncryptionPassword(password)

                startMainActivity(password)
            } else {
                // Ověření hesla
                LogHelper.i(TAG, "Ověřování hesla")
                if (PasswordUtils.verifyPassword(password, savedPasswordHash)) {
                    LogHelper.i(TAG, "Heslo ověřeno")

                    // Uložení hesla pro šifrování
                    saveEncryptionPassword(password)

                    startMainActivity(password)
                } else {
                    LogHelper.w(TAG, "Nesprávné heslo")
                    Toast.makeText(this, "Nesprávné heslo", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při ověřování hesla: ${e.message}", e)
            Toast.makeText(this, "Chyba při ověřování hesla: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Uloží heslo pro šifrování fotografií.
     * V reálné aplikaci by bylo lepší použít Android KeyStore pro bezpečné ukládání klíčů.
     */
    private fun saveEncryptionPassword(password: String) {
        // Toto je zjednodušené řešení - v reálné aplikaci by bylo lepší použít KeyStore
        // Pro účely demonstrace používáme globální proměnnou
        AppConfig.encryptionPassword = password
    }

    /**
     * Spustí hlavní aktivitu a ukončí úvodní aktivitu.
     */
    private fun startMainActivity(password: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("encryption_password", password)
        startActivity(intent)
        finish()
    }

    /**
     * Zobrazí návod k aplikaci.
     */
    private fun showHelp() {
        try {
            val intent = Intent(this, Class.forName("karty1.cz.view.HelpActivity"))
            startActivity(intent)
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při zobrazování návodu: ${e.message}", e)
            Toast.makeText(this, "Nepodařilo se zobrazit návod", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Změní jazyk aplikace.
     * @param languageCode Kód jazyka (cs, en, de).
     */
    private fun changeLanguage(languageCode: String) {
        LogHelper.d(TAG, "Změna jazyka na: $languageCode")

        // Nastavení nového jazyka
        languageManager.setLocale(languageCode)

        // Restart aktivity pro aplikaci změn
        val intent = intent
        finish()
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Zvýrazní aktuálně vybraný jazyk.
     */
    private fun highlightSelectedLanguage() {
        // Získání aktuálního jazyka
        val currentLanguage = languageManager.getLanguage()

        // Resetování všech tlačítek
        buttonCzech.alpha = 0.5f
        buttonEnglish.alpha = 0.5f
        buttonGerman.alpha = 0.5f

        // Zvýraznění vybraného jazyka
        when (currentLanguage) {
            LanguageManager.LANGUAGE_CZECH -> buttonCzech.alpha = 1.0f
            LanguageManager.LANGUAGE_ENGLISH -> buttonEnglish.alpha = 1.0f
            LanguageManager.LANGUAGE_GERMAN -> buttonGerman.alpha = 1.0f
        }
    }

    /**
     * Zobrazí dialog s informacemi o příspěvku.
     */
    private fun showDonateDialog() {
        try {
            // Vytvoření dialogu
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.donate_title))
            builder.setMessage(getString(R.string.donate_message))
            builder.setPositiveButton(getString(R.string.donate_close)) { dialog, _ ->
                dialog.dismiss()
            }

            // Zobrazení dialogu
            val dialog = builder.create()
            dialog.show()

            LogHelper.d(TAG, "Zobrazen dialog s informacemi o příspěvku")
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při zobrazování dialogu s informacemi o příspěvku: ${e.message}", e)
            Toast.makeText(this, "Nepodařilo se zobrazit informace o příspěvku", Toast.LENGTH_SHORT).show()
        }
    }
}
