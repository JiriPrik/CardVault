package karty1.cz.view

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import com.github.chrisbanes.photoview.PhotoView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import karty1.cz.AppConfig
import karty1.cz.R
import karty1.cz.model.Card
import karty1.cz.util.CameraHelper
import karty1.cz.util.LogHelper
import karty1.cz.viewmodel.CardViewModel
import karty1.cz.view.CardEditActivity
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CardDetailActivity : BaseActivity() {

    private val cardViewModel: CardViewModel by viewModels()
    private var cardId: Long = -1
    private lateinit var cameraHelper: CameraHelper

    // UI komponenty
    private lateinit var textCardName: TextView
    private lateinit var textCardType: TextView
    private lateinit var textCardNumber: TextView
    private lateinit var textNotes: TextView
    private lateinit var textCreatedAt: TextView
    private lateinit var textUpdatedAt: TextView
    private lateinit var imageBarcode: ImageView
    private lateinit var imageFront: ImageView
    private lateinit var imageBack: ImageView
    private lateinit var fabEdit: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_detail)

        // Nastavení toolbaru
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.card_detail)

        // Inicializace CameraHelper
        cameraHelper = CameraHelper(this)

        // Inicializace UI komponent
        initViews()

        // Získání ID karty z intentu
        cardId = intent.getLongExtra(EXTRA_CARD_ID, -1)
        if (cardId == -1L) {
            finish()
            return
        }

        // Načtení dat karty
        loadCardData()

        // Nastavení tlačítka pro editaci
        fabEdit.setOnClickListener {
            val intent = CardEditActivity.newIntent(this, cardId)
            startActivityForResult(intent, REQUEST_CODE_EDIT_CARD)
        }
    }

    private fun initViews() {
        textCardName = findViewById(R.id.textCardName)
        textCardType = findViewById(R.id.textCardType)
        textCardNumber = findViewById(R.id.textCardNumber)
        textNotes = findViewById(R.id.textNotes)
        textCreatedAt = findViewById(R.id.textCreatedAt)
        textUpdatedAt = findViewById(R.id.textUpdatedAt)
        imageBarcode = findViewById(R.id.imageBarcode)
        imageFront = findViewById(R.id.imageFront)
        imageBack = findViewById(R.id.imageBack)
        fabEdit = findViewById(R.id.fabEdit)
    }

    private fun loadCardData() {
        cardViewModel.getCardById(cardId).observe(this) { card ->
            if (card != null) {
                // Nastavení textu
                textCardName.text = card.name
                textCardType.text = card.cardType ?: getString(R.string.not_specified)
                textCardNumber.text = card.cardNumber ?: getString(R.string.not_specified)
                textNotes.text = card.notes ?: getString(R.string.no_notes)

                // Formátování data
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                textCreatedAt.text = dateFormat.format(card.createdAt)
                textUpdatedAt.text = dateFormat.format(card.updatedAt)

                // Zobrazení čárového kódu, pokud je k dispozici
                if (!card.barcodeData.isNullOrEmpty()) {
                    try {
                        val barcodeBitmap = generateBarcode(card.barcodeData, card.barcodeType)
                        imageBarcode.setImageBitmap(barcodeBitmap)

                        // Nastavení kliknutí pro zobrazení na celou obrazovku
                        imageBarcode.setOnClickListener {
                            showFullscreenBarcode(card.barcodeData, card.barcodeType)
                        }
                    } catch (e: Exception) {
                        imageBarcode.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.error_generating_barcode), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    imageBarcode.visibility = View.GONE
                }

                // Zobrazení obrázků, pokud jsou k dispozici
                if (card.frontImagePath != null) {
                    // Použití hesla pro dešifrování, pokud je obrázek zašifrovaný
                    val isEncrypted = card.frontImagePath.endsWith(".enc")
                    val password = if (isEncrypted) AppConfig.encryptionPassword else null

                    LogHelper.d("CardDetailActivity", "Načítání předního obrázku: ${card.frontImagePath}, zašifrovaný: $isEncrypted")

                    val bitmap = cameraHelper.loadImageFromFile(card.frontImagePath, password)
                    if (bitmap != null) {
                        imageFront.setImageBitmap(bitmap)
                        imageFront.visibility = View.VISIBLE

                        // Nastavení kliknutí pro zobrazení na celou obrazovku
                        imageFront.setOnClickListener {
                            showFullscreenImage(card.frontImagePath)
                        }
                    } else {
                        LogHelper.e("CardDetailActivity", "Nepodařilo se načíst přední obrázek: ${card.frontImagePath}")
                        imageFront.visibility = View.GONE
                    }
                } else {
                    imageFront.visibility = View.GONE
                }

                if (card.backImagePath != null) {
                    // Použití hesla pro dešifrování, pokud je obrázek zašifrovaný
                    val isEncrypted = card.backImagePath.endsWith(".enc")
                    val password = if (isEncrypted) AppConfig.encryptionPassword else null

                    LogHelper.d("CardDetailActivity", "Načítání zadního obrázku: ${card.backImagePath}, zašifrovaný: $isEncrypted")

                    val bitmap = cameraHelper.loadImageFromFile(card.backImagePath, password)
                    if (bitmap != null) {
                        imageBack.setImageBitmap(bitmap)
                        imageBack.visibility = View.VISIBLE

                        // Nastavení kliknutí pro zobrazení na celou obrazovku
                        imageBack.setOnClickListener {
                            showFullscreenImage(card.backImagePath)
                        }
                    } else {
                        LogHelper.e("CardDetailActivity", "Nepodařilo se načíst zadní obrázek: ${card.backImagePath}")
                        imageBack.visibility = View.GONE
                    }
                } else {
                    imageBack.visibility = View.GONE
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Generuje bitmap čárového kódu z dat a typu kódu.
     */
    private fun generateBarcode(data: String, type: String?): Bitmap? {
        val format = when (type?.uppercase()) {
            "QR_CODE" -> BarcodeFormat.QR_CODE
            "EAN_13" -> BarcodeFormat.EAN_13
            "EAN_8" -> BarcodeFormat.EAN_8
            "CODE_128" -> BarcodeFormat.CODE_128
            "CODE_39" -> BarcodeFormat.CODE_39
            else -> BarcodeFormat.QR_CODE // Výchozí hodnota
        }

        val multiFormatWriter = MultiFormatWriter()
        val bitMatrix = multiFormatWriter.encode(data, format, 500, 200)
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.createBitmap(bitMatrix)
    }

    /**
     * Zobrazí čárový kód na celou obrazovku v dialogu.
     */
    private fun showFullscreenBarcode(data: String, type: String?) {
        try {
            // Vytvoření většího čárového kódu pro celou obrazovku
            val format = when (type?.uppercase()) {
                "QR_CODE" -> BarcodeFormat.QR_CODE
                "EAN_13" -> BarcodeFormat.EAN_13
                "EAN_8" -> BarcodeFormat.EAN_8
                "CODE_128" -> BarcodeFormat.CODE_128
                "CODE_39" -> BarcodeFormat.CODE_39
                else -> BarcodeFormat.QR_CODE
            }

            val multiFormatWriter = MultiFormatWriter()
            val width = if (format == BarcodeFormat.QR_CODE) 800 else 1000
            val height = if (format == BarcodeFormat.QR_CODE) 800 else 400
            val bitMatrix = multiFormatWriter.encode(data, format, width, height)
            val barcodeEncoder = BarcodeEncoder()
            val barcodeBitmap = barcodeEncoder.createBitmap(bitMatrix)

            // Vytvoření a zobrazení dialogu
            val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_fullscreen_barcode)
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            val imageView = dialog.findViewById<ImageView>(R.id.imageFullscreenBarcode)
            imageView.setImageBitmap(barcodeBitmap)

            // Zavření dialogu při kliknutí kamkoliv
            dialog.findViewById<View>(android.R.id.content).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_fullscreen_barcode), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Zobrazí fotografii na celou obrazovku v dialogu.
     */
    private fun showFullscreenImage(imagePath: String?) {
        if (imagePath == null) return

        try {
            // Načtení fotografie s použitím hesla pro dešifrování, pokud je obrázek zašifrovaný
            val isEncrypted = imagePath.endsWith(".enc")
            val password = if (isEncrypted) AppConfig.encryptionPassword else null

            LogHelper.d("CardDetailActivity", "Zobrazování obrázku na celou obrazovku: $imagePath, zašifrovaný: $isEncrypted")

            val bitmap = cameraHelper.loadImageFromFile(imagePath, password)
            if (bitmap == null) {
                LogHelper.e("CardDetailActivity", "Nepodařilo se načíst obrázek pro celou obrazovku: $imagePath")
                Toast.makeText(this, getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show()
                return
            }

            // Vytvoření a zobrazení dialogu
            val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_fullscreen_image)
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            val photoView = dialog.findViewById<PhotoView>(R.id.imageFullscreen)
            photoView.setImageBitmap(bitmap)

            // Zavření dialogu při dlouhém kliknutí
            photoView.setOnLongClickListener {
                dialog.dismiss()
                true
            }

            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_fullscreen_image), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_EDIT_CARD) {
            // Karta byla upravena nebo smazána
            // Pokud byla smazána, můžeme zjistit, že již neexistuje a zavřít aktivitu
            cardViewModel.getCardById(cardId).observe(this) { card ->
                if (card == null) {
                    // Karta byla smazána, zavřeme aktivitu
                    finish()
                }
                // Jinak se data automaticky aktualizují díky LiveData
            }
        }
    }

    companion object {
        const val EXTRA_CARD_ID = "extra_card_id"
        private const val REQUEST_CODE_EDIT_CARD = 1
    }
}
