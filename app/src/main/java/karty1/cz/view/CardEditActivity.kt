package karty1.cz.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import karty1.cz.R
import karty1.cz.AppConfig
import karty1.cz.model.Card
import karty1.cz.util.BarcodeScannerHelper
import karty1.cz.util.CameraHelper
import karty1.cz.util.LogHelper
import karty1.cz.viewmodel.CardViewModel
import java.io.File
import java.io.IOException
import java.util.Date

class CardEditActivity : BaseActivity() {

    private val cardViewModel: CardViewModel by viewModels()
    private var cardId: Long = -1
    private var isEditMode = false

    // Proměnné pro práci s fotoaparátem
    private lateinit var cameraHelper: CameraHelper
    private var currentPhotoPath: String? = null
    private var frontImagePath: String? = null
    private var backImagePath: String? = null
    private var isFrontCapture = true // True pro přední stranu, False pro zadní stranu
    private var currentCropOutputPath: String? = null

    // Proměnná pro práci se skenerem čárových kódů
    private lateinit var barcodeScannerHelper: BarcodeScannerHelper

    // UI komponenty
    private lateinit var editCardName: TextInputEditText
    private lateinit var editCardType: AutoCompleteTextView
    private lateinit var editCardNumber: TextInputEditText
    private lateinit var editBarcodeData: TextInputEditText
    private lateinit var editBarcodeType: AutoCompleteTextView
    private lateinit var editNotes: TextInputEditText
    private lateinit var imageBarcode: ImageView
    private lateinit var imageFront: ImageView
    private lateinit var imageBack: ImageView
    private lateinit var btnCaptureFront: Button
    private lateinit var btnCaptureBack: Button
    private lateinit var btnCropFront: Button
    private lateinit var btnCropBack: Button
    // Odstraněno duplicitní tlačítko pro skenování čárového kódu
    private lateinit var btnChangeBarcodeType: Button
    private lateinit var btnCopyBarcodeToCardNumber: Button
    private lateinit var fabSave: FloatingActionButton
    private lateinit var barcodeDataLayout: TextInputLayout

    // Typy čárových kódů
    private val barcodeTypes = listOf(
        "QR_CODE", "EAN_13", "EAN_8", "CODE_128", "CODE_39"
    )

    // Předdefinované typy karet
    private val cardTypes by lazy {
        listOf(
            getString(R.string.loyalty_card),
            getString(R.string.discount_card),
            getString(R.string.membership_card),
            getString(R.string.payment_card),
            getString(R.string.other_card)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogHelper.i(TAG, "onCreate: Inicializace aktivity pro editaci karty")

        try {
            setContentView(R.layout.activity_card_edit)

            // Nastavení toolbaru
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // Inicializace CameraHelper
            cameraHelper = CameraHelper(this)
            LogHelper.d(TAG, "onCreate: CameraHelper inicializován")

            // Vytvoření adresáře pro ukládání fotografií
            cameraHelper.createImageDirectory()

            // Inicializace BarcodeScannerHelper
            barcodeScannerHelper = BarcodeScannerHelper(this)
            LogHelper.d(TAG, "onCreate: BarcodeScannerHelper inicializován")
        } catch (e: Exception) {
            LogHelper.e(TAG, "onCreate: Chyba při inicializaci aktivity", e)
            Toast.makeText(this, "Chyba při inicializaci aplikace: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inicializace UI komponent
        initViews()

        // Nastavení adaptéru pro dropdown menu typů čárových kódů
        val barcodeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barcodeTypes)
        editBarcodeType.setAdapter(barcodeAdapter)

        // Nastavení adaptéru pro dropdown menu typů karet
        val cardTypeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cardTypes)
        editCardType.setAdapter(cardTypeAdapter)

        // Získání ID karty z intentu (pokud existuje)
        cardId = intent.getLongExtra(EXTRA_CARD_ID, -1)
        isEditMode = cardId != -1L

        // Nastavení titulku podle režimu
        supportActionBar?.title = if (isEditMode) getString(R.string.edit_card) else getString(R.string.add_new_card)

        // Načtení dat karty v režimu editace
        if (isEditMode) {
            loadCardData()
        } else {
            // Výchozí hodnota pro typ čárového kódu v režimu přidávání
            editBarcodeType.setText(barcodeTypes[0], false)
        }

        // Nastavení posluchačů událostí
        setupListeners()
    }

    private fun initViews() {
        try {
            LogHelper.d(TAG, "initViews: Inicializace UI komponent")
            editCardName = findViewById(R.id.editCardName)
            editCardType = findViewById(R.id.editCardType)
            editCardNumber = findViewById(R.id.editCardNumber)
            editBarcodeData = findViewById(R.id.editBarcodeData)
            editBarcodeType = findViewById(R.id.editBarcodeType)
            editNotes = findViewById(R.id.editNotes)
            imageBarcode = findViewById(R.id.imageBarcode)
            imageFront = findViewById(R.id.imageFront)
            imageBack = findViewById(R.id.imageBack)
            btnCaptureFront = findViewById(R.id.btnCaptureFront)
            btnCaptureBack = findViewById(R.id.btnCaptureBack)
            btnCropFront = findViewById(R.id.btnCropFront)
            btnCropBack = findViewById(R.id.btnCropBack)
            // Odstraněno duplicitní tlačítko pro skenování čárového kódu
            btnChangeBarcodeType = findViewById(R.id.btnChangeBarcodeType)
            btnCopyBarcodeToCardNumber = findViewById(R.id.btnCopyBarcodeToCardNumber)
            fabSave = findViewById(R.id.fabSave)
            barcodeDataLayout = findViewById(R.id.barcodeDataLayout)
            LogHelper.d(TAG, "initViews: UI komponenty úspěšně inicializovány")
        } catch (e: Exception) {
            LogHelper.e(TAG, "initViews: Chyba při inicializaci UI komponent", e)
            Toast.makeText(this, "Chyba při inicializaci UI: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupListeners() {
        // Posluchač pro změnu dat čárového kódu
        editBarcodeData.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateBarcodePreview()
            }
        })

        // Posluchač pro změnu typu čárového kódu
        editBarcodeType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateBarcodePreview()
            }
        })

        // Odstraněno duplicitní tlačítko pro skenování čárového kódu

        // Ikona skenování v poli pro data čárového kódu
        barcodeDataLayout.setEndIconOnClickListener {
            LogHelper.d(TAG, "Kliknutí na ikonu skenování v poli pro data čárového kódu")
            // Spuštění skeneru čárových kódů
            barcodeScannerHelper.startBarcodeScanner("Naskenujte čárový kód karty")
        }

        // Tlačítko pro změnu typu čárového kódu
        btnChangeBarcodeType.setOnClickListener {
            LogHelper.d(TAG, "Kliknutí na tlačítko pro změnu typu čárového kódu")
            // Získání aktuálních dat čárového kódu
            val barcodeData = editBarcodeData.text.toString()
            val currentType = editBarcodeType.text.toString()

            // Zobrazení dialogu pro výběr typu čárového kódu
            showBarcodeTypeSelectionDialog(currentType, barcodeData)
        }

        // Tlačítka pro pořízení fotografií
        btnCaptureFront.setOnClickListener {
            // Nastavení příznaků pro přední stranu
            isFrontCapture = true
            dispatchTakePictureIntent()
        }

        btnCaptureBack.setOnClickListener {
            // Nastavení příznaků pro zadní stranu
            isFrontCapture = false
            dispatchTakePictureIntent()
        }

        // Tlačítka pro ořez fotografií
        btnCropFront.setOnClickListener {
            if (frontImagePath == null) {
                Toast.makeText(this, R.string.no_image_to_crop, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Nastavení příznaků pro přední stranu
            isFrontCapture = true
            cropCardImage(frontImagePath)
        }

        btnCropBack.setOnClickListener {
            if (backImagePath == null) {
                Toast.makeText(this, R.string.no_image_to_crop, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Nastavení příznaků pro zadní stranu
            isFrontCapture = false
            cropCardImage(backImagePath)
        }

        // Tlačítko pro kopírování čárového kódu do čísla karty
        btnCopyBarcodeToCardNumber.setOnClickListener {
            LogHelper.d(TAG, "Kliknutí na tlačítko pro kopírování čárového kódu do čísla karty")

            // Získání dat čárového kódu
            val barcodeData = editBarcodeData.text.toString()

            if (barcodeData.isNotEmpty()) {
                // Kopírování dat čárového kódu do pole čísla karty
                editCardNumber.setText(barcodeData)
                Toast.makeText(this, R.string.barcode_copied, Toast.LENGTH_SHORT).show()
                LogHelper.d(TAG, "Čárový kód byl zkopírován do čísla karty: $barcodeData")
            } else {
                Toast.makeText(this, R.string.no_barcode_to_copy, Toast.LENGTH_SHORT).show()
                LogHelper.d(TAG, "Žádný čárový kód k zkopírování")
            }
        }

        // Tlačítko pro uložení karty
        fabSave.setOnClickListener {
            saveCard()
        }
    }

    private fun loadCardData() {
        cardViewModel.getCardById(cardId).observe(this) { card ->
            if (card != null) {
                // Nastavení hodnot do formuláře
                editCardName.setText(card.name)
                editCardType.setText(card.cardType ?: "")
                editCardNumber.setText(card.cardNumber ?: "")
                editBarcodeData.setText(card.barcodeData ?: "")
                editBarcodeType.setText(card.barcodeType ?: barcodeTypes[0], false)
                editNotes.setText(card.notes ?: "")

                // Aktualizace náhledu čárového kódu
                updateBarcodePreview()

                // Načtení obrázků
                frontImagePath = card.frontImagePath
                backImagePath = card.backImagePath

                // Zobrazení obrázků, pokud existují
                if (frontImagePath != null) {
                    val bitmap = cameraHelper.loadImageFromFile(frontImagePath)
                    if (bitmap != null) {
                        imageFront.setImageBitmap(bitmap)
                    }
                }

                if (backImagePath != null) {
                    val bitmap = cameraHelper.loadImageFromFile(backImagePath)
                    if (bitmap != null) {
                        imageBack.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun updateBarcodePreview() {
        val barcodeData = editBarcodeData.text.toString()
        val barcodeType = editBarcodeType.text.toString()

        if (barcodeData.isNotEmpty() && barcodeType.isNotEmpty()) {
            try {
                val barcodeBitmap = generateBarcode(barcodeData, barcodeType)
                imageBarcode.setImageBitmap(barcodeBitmap)
                imageBarcode.visibility = View.VISIBLE
            } catch (e: Exception) {
                imageBarcode.visibility = View.GONE
                Toast.makeText(this, "Nelze vygenerovat čárový kód", Toast.LENGTH_SHORT).show()
            }
        } else {
            imageBarcode.visibility = View.GONE
        }
    }

    /**
     * Zobrazí dialog pro výběr typu čárového kódu.
     * @param detectedFormat Detekovaný formát čárového kódu.
     * @param barcodeData Data čárového kódu.
     */
    private fun showBarcodeTypeSelectionDialog(detectedFormat: String, barcodeData: String) {
        try {
            // Seznam dostupných typů čárových kódů
            val barcodeTypes = arrayOf("QR_CODE", "EAN_13", "EAN_8", "CODE_128", "CODE_39")

            // Nalezení indexu detekovaného formátu v seznamu
            val defaultSelection = barcodeTypes.indexOf(detectedFormat).takeIf { it >= 0 } ?: 0

            // Vytvoření dialogu
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.select_barcode_type))
            builder.setSingleChoiceItems(barcodeTypes, defaultSelection) { dialog, which ->
                // Nastavení vybraného typu čárového kódu
                val selectedType = barcodeTypes[which]
                editBarcodeType.setText(selectedType, false)

                // Aktualizace náhledu čárového kódu
                updateBarcodePreview()

                // Zavření dialogu
                dialog.dismiss()

                LogHelper.d(TAG, "showBarcodeTypeSelectionDialog: Vybrán typ čárového kódu: $selectedType")
            }

            // Přidání tlačítka pro potvrzení detekovaného typu
            builder.setPositiveButton(getString(R.string.use_detected_type) + " ($detectedFormat)") { dialog, _ ->
                // Nastavení detekovaného typu čárového kódu
                editBarcodeType.setText(detectedFormat, false)

                // Aktualizace náhledu čárového kódu
                updateBarcodePreview()

                dialog.dismiss()

                LogHelper.d(TAG, "showBarcodeTypeSelectionDialog: Použit detekovaný typ čárového kódu: $detectedFormat")
            }

            // Přidání tlačítka pro zrušení
            builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }

            // Zobrazení dialogu
            val dialog = builder.create()
            dialog.show()

            LogHelper.d(TAG, "showBarcodeTypeSelectionDialog: Zobrazen dialog pro výběr typu čárového kódu")
        } catch (e: Exception) {
            LogHelper.e(TAG, "showBarcodeTypeSelectionDialog: Chyba při zobrazování dialogu", e)

            // V případě chyby použijeme detekovaný formát
            editBarcodeType.setText(detectedFormat, false)
            updateBarcodePreview()
        }
    }

    /**
     * Spustí fotoaparát pro pořízení fotografie.
     */
    private fun dispatchTakePictureIntent() {
        // Kontrola oprávnění pro přístup k fotoaparátu
        if (!cameraHelper.hasCameraPermission()) {
            cameraHelper.requestCameraPermission(this)
            return
        }

        // Kontrola oprávnění pro přístup k úložišti
        if (!cameraHelper.hasStoragePermission()) {
            cameraHelper.requestStoragePermission(this)
            return
        }

        try {
            // Vytvoření dočasného souboru pro uložení fotografie
            val photoFile = cameraHelper.createImageFile()
            currentPhotoPath = photoFile.absolutePath

            // Spuštění fotoaparátu
            cameraHelper.takePicture(this, photoFile)
        } catch (ex: IOException) {
            // Chyba při vytváření souboru
            Toast.makeText(this, "Chyba při vytváření souboru pro fotografii", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCard() {
        LogHelper.i(TAG, "saveCard: Ukládání karty")
        try {
            val name = editCardName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Zadejte název karty", Toast.LENGTH_SHORT).show()
                LogHelper.w(TAG, "saveCard: Prázdný název karty")
                return
            }

            val cardType = editCardType.text.toString().trim().takeIf { it.isNotEmpty() }
            val cardNumber = editCardNumber.text.toString().trim().takeIf { it.isNotEmpty() }
            val barcodeData = editBarcodeData.text.toString().trim().takeIf { it.isNotEmpty() }
            val barcodeType = editBarcodeType.text.toString().trim().takeIf { it.isNotEmpty() }
            val notes = editNotes.text.toString().trim().takeIf { it.isNotEmpty() }

            LogHelper.d(TAG, "saveCard: Data karty - název: $name, typ: $cardType, číslo: $cardNumber")
            LogHelper.d(TAG, "saveCard: Data čárového kódu - data: $barcodeData, typ: $barcodeType")
            LogHelper.d(TAG, "saveCard: Cesty k obrázkům - přední: $frontImagePath, zadní: $backImagePath")

            val currentTime = Date()
            val card = Card(
                id = if (isEditMode) cardId else 0,
                name = name,
                cardType = cardType,
                cardNumber = cardNumber,
                barcodeData = barcodeData,
                barcodeType = barcodeType,
                notes = notes,
                frontImagePath = frontImagePath,
                backImagePath = backImagePath,
                createdAt = if (isEditMode) cardViewModel.getCardById(cardId).value?.createdAt ?: currentTime else currentTime,
                updatedAt = currentTime
            )

            if (isEditMode) {
                LogHelper.i(TAG, "saveCard: Aktualizace existující karty s ID: $cardId")
                cardViewModel.updateCard(card)
                Toast.makeText(this, getString(R.string.card_updated), Toast.LENGTH_SHORT).show()
            } else {
                LogHelper.i(TAG, "saveCard: Přidání nové karty")
                cardViewModel.insertCard(card)
                Toast.makeText(this, getString(R.string.card_added), Toast.LENGTH_SHORT).show()
            }

            // Nastavení výsledku a ukončení aktivity
            setResult(Activity.RESULT_OK)
            finish()
        } catch (e: Exception) {
            LogHelper.e(TAG, "saveCard: Chyba při ukládání karty", e)
            Toast.makeText(this, "${getString(R.string.error_saving_card)}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteCard() {
        if (!isEditMode) return

        LogHelper.i(TAG, "deleteCard: Mazání karty s ID: $cardId")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                try {
                    // Získání karty pro získání cest k souborům
                    LogHelper.d(TAG, "deleteCard: Získávání karty pro smazání")
                    cardViewModel.getCardById(cardId).observe(this) { card ->
                        if (card != null) {
                            LogHelper.d(TAG, "deleteCard: Karta nalezena, mazání souborů a záznamu")
                            // Odstranění souborů s fotografiemi, pokud existují
                            if (card.frontImagePath != null) {
                                val frontFile = File(card.frontImagePath)
                                if (frontFile.exists()) {
                                    val deleted = frontFile.delete()
                                    LogHelper.d(TAG, "deleteCard: Mazání předního obrázku: $deleted")
                                }
                            }

                            if (card.backImagePath != null) {
                                val backFile = File(card.backImagePath)
                                if (backFile.exists()) {
                                    val deleted = backFile.delete()
                                    LogHelper.d(TAG, "deleteCard: Mazání zadního obrázku: $deleted")
                                }
                            }

                            // Odstranění karty z databáze pomocí ID
                            LogHelper.d(TAG, "deleteCard: Mazání záznamu z databáze")
                            cardViewModel.deleteCardById(cardId)

                            LogHelper.i(TAG, "deleteCard: Karta úspěšně smazána")
                            Toast.makeText(this, getString(R.string.card_deleted), Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            LogHelper.w(TAG, "deleteCard: Karta s ID $cardId nebyla nalezena")
                            Toast.makeText(this, getString(R.string.card_not_found), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    LogHelper.e(TAG, "deleteCard: Chyba při mazání karty", e)
                    Toast.makeText(this, "${getString(R.string.error_deleting_card)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                LogHelper.d(TAG, "deleteCard: Mazání zrušeno uživatelem")
            }
            .show()
    }

    /**
     * Generuje bitmap čárového kódu z dat a typu kódu.
     */
    private fun generateBarcode(data: String, type: String): Bitmap? {
        val format = when (type.uppercase()) {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Zobrazení menu pro smazání pouze v režimu editace
        if (isEditMode) {
            menuInflater.inflate(R.menu.menu_card_edit, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_delete -> {
                deleteCard()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        LogHelper.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        try {
            if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                // Fotografie byla pořízena úspěšně
                LogHelper.i(TAG, "onActivityResult: Fotografie pořízena, cesta: $currentPhotoPath")
                if (currentPhotoPath != null) {
                    // Načtení fotografie a zobrazení náhledu
                    val bitmap = cameraHelper.loadImageFromFile(currentPhotoPath)
                    if (bitmap != null) {
                        // Pokud je zapnuto šifrování, zašifrujeme fotografii
                        val photoPath = currentPhotoPath // Uložení do lokální proměnné pro bezpečné použití
                        if (photoPath != null && AppConfig.encryptImages && AppConfig.encryptionPassword.isNotEmpty()) {
                            LogHelper.d(TAG, "onActivityResult: Šifrování fotografie")

                            // Zašifrování fotografie
                            val encryptedPath = cameraHelper.encryptExistingFile(photoPath, AppConfig.encryptionPassword)

                            if (encryptedPath != null) {
                                // Aktualizace cesty k fotografii
                                currentPhotoPath = encryptedPath
                                LogHelper.d(TAG, "onActivityResult: Fotografie úspěšně zašifrována: $encryptedPath")
                            } else {
                                LogHelper.e(TAG, "onActivityResult: Nepodařilo se zašifrovat fotografii")
                            }
                        }

                        if (isFrontCapture) {
                            // Uložení cesty k přední straně karty
                            frontImagePath = currentPhotoPath
                            imageFront.setImageBitmap(bitmap)
                            LogHelper.d(TAG, "onActivityResult: Nastaven přední obrázek karty")
                        } else {
                            // Uložení cesty k zadní straně karty
                            backImagePath = currentPhotoPath
                            imageBack.setImageBitmap(bitmap)
                            LogHelper.d(TAG, "onActivityResult: Nastaven zadní obrázek karty")
                        }
                    } else {
                        LogHelper.w(TAG, "onActivityResult: Nelze načíst fotografii z cesty: $currentPhotoPath")
                    }
                } else {
                    LogHelper.w(TAG, "onActivityResult: Cesta k fotografii je null")
                }
            } else if (requestCode == CameraHelper.REQUEST_IMAGE_CROP && resultCode == Activity.RESULT_OK) {
                // Obrázek byl ořezán úspěšně
                LogHelper.i(TAG, "onActivityResult: Obrázek ořezán, cesta: $currentCropOutputPath")

                if (currentCropOutputPath != null) {
                    // Kontrola, zda soubor existuje
                    val outputFile = File(currentCropOutputPath!!)
                    if (!outputFile.exists()) {
                        LogHelper.e(TAG, "onActivityResult: Výstupní soubor neexistuje: $currentCropOutputPath")
                        Toast.makeText(this, R.string.error_cropping_image, Toast.LENGTH_SHORT).show()
                        return
                    }

                    LogHelper.d(TAG, "onActivityResult: Výstupní soubor existuje, velikost: ${outputFile.length()} bytů")

                    // Načtení ořezaného obrázku a zobrazení náhledu
                    val bitmap = cameraHelper.loadImageFromFile(currentCropOutputPath)
                    if (bitmap != null) {
                        LogHelper.d(TAG, "onActivityResult: Bitmap úspěšně načten, rozměry: ${bitmap.width}x${bitmap.height}")

                        // Pokud je zapnuto šifrování, zašifrujeme fotografii
                        val photoPath = currentCropOutputPath // Uložení do lokální proměnné pro bezpečné použití
                        if (photoPath != null && AppConfig.encryptImages && AppConfig.encryptionPassword.isNotEmpty()) {
                            LogHelper.d(TAG, "onActivityResult: Šifrování ořezané fotografie")

                            // Zašifrování fotografie
                            val encryptedPath = cameraHelper.encryptExistingFile(photoPath, AppConfig.encryptionPassword)

                            if (encryptedPath != null) {
                                // Aktualizace cesty k fotografii
                                currentCropOutputPath = encryptedPath
                                LogHelper.d(TAG, "onActivityResult: Ořezaná fotografie úspěšně zašifrována: $encryptedPath")
                            } else {
                                LogHelper.e(TAG, "onActivityResult: Nepodařilo se zašifrovat ořezanou fotografii")
                            }
                        }

                        if (isFrontCapture) {
                            // Uložení cesty k přední straně karty
                            frontImagePath = currentCropOutputPath
                            imageFront.setImageBitmap(bitmap)
                            LogHelper.d(TAG, "onActivityResult: Nastaven ořezaný přední obrázek karty: $frontImagePath")
                        } else {
                            // Uložení cesty k zadní straně karty
                            backImagePath = currentCropOutputPath
                            imageBack.setImageBitmap(bitmap)
                            LogHelper.d(TAG, "onActivityResult: Nastaven ořezaný zadní obrázek karty: $backImagePath")
                        }

                        Toast.makeText(this, R.string.image_cropped, Toast.LENGTH_SHORT).show()
                    } else {
                        LogHelper.w(TAG, "onActivityResult: Nelze načíst ořezanou fotografii z cesty: $currentCropOutputPath")
                        Toast.makeText(this, R.string.error_cropping_image, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    LogHelper.w(TAG, "onActivityResult: Cesta k ořezané fotografii je null")
                    Toast.makeText(this, R.string.error_cropping_image, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Zpracování výsledku skenování čárového kódu
                val scanResult = barcodeScannerHelper.processScanResult(requestCode, resultCode, data)
                if (scanResult != null) {
                    // Získání dat a formátu čárového kódu
                    val (barcodeData, barcodeFormat) = scanResult
                    LogHelper.i(TAG, "onActivityResult: Čárový kód naskenován - data: $barcodeData, formát: $barcodeFormat")

                    // Nastavení dat čárového kódu do formuláře
                    editBarcodeData.setText(barcodeData)

                    // Zobrazení dialogu pro výběr typu čárového kódu
                    showBarcodeTypeSelectionDialog(barcodeFormat, barcodeData)

                    Toast.makeText(this, "Čárový kód úspěšně naskenován", Toast.LENGTH_SHORT).show()
                } else if (resultCode != Activity.RESULT_CANCELED) {
                    LogHelper.w(TAG, "onActivityResult: Skenování čárového kódu selhalo nebo bylo zrušeno")
                }
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "onActivityResult: Chyba při zpracování výsledku aktivity", e)
            Toast.makeText(this, "Chyba při zpracování výsledku: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CameraHelper.REQUEST_PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Oprávnění pro fotoaparát bylo uděleno, zkontrolujeme oprávnění pro úložiště
                    if (!cameraHelper.hasStoragePermission()) {
                        cameraHelper.requestStoragePermission(this)
                    } else {
                        dispatchTakePictureIntent()
                    }
                } else {
                    Toast.makeText(this, "Pro pořízení fotografie je potřeba povolit přístup k fotoaparátu", Toast.LENGTH_SHORT).show()
                }
            }
            CameraHelper.REQUEST_PERMISSION_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Oprávnění pro úložiště bylo uděleno, můžeme spustit fotoaparát
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this, "Pro uložení fotografie je potřeba povolit přístup k úložišti", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Spustí aktivitu pro ořezání obrázku karty.
     */
    private fun cropCardImage(imagePath: String?) {
        if (imagePath == null) {
            Toast.makeText(this, R.string.no_image_to_crop, Toast.LENGTH_SHORT).show()
            LogHelper.e(TAG, "cropCardImage: Cesta k obrázku je null")
            return
        }

        LogHelper.d(TAG, "cropCardImage: Začínám ořezávat obrázek: $imagePath")

        // Kontrola, zda soubor existuje
        val file = File(imagePath)
        if (!file.exists()) {
            Toast.makeText(this, R.string.no_image_to_crop, Toast.LENGTH_SHORT).show()
            LogHelper.e(TAG, "cropCardImage: Soubor neexistuje: $imagePath")
            return
        }

        // Kontrola, zda je obrázek zašifrovaný
        val isEncrypted = imagePath.endsWith(".enc")
        val password = if (isEncrypted) AppConfig.encryptionPassword else null

        LogHelper.d(TAG, "cropCardImage: Obrázek je zašifrovaný: $isEncrypted, heslo je k dispozici: ${password != null}")

        // Spuštění aktivity pro ořezání obrázku
        currentCropOutputPath = cameraHelper.cropImage(this, imagePath, password)

        if (currentCropOutputPath == null) {
            Toast.makeText(this, R.string.error_cropping_image, Toast.LENGTH_SHORT).show()
            LogHelper.e(TAG, "cropCardImage: Nepodařilo se připravit ořezávání obrázku")
        } else {
            LogHelper.d(TAG, "cropCardImage: Ořezávání připraveno, výstupní cesta: $currentCropOutputPath")
        }
    }

    companion object {
        const val EXTRA_CARD_ID = "extra_card_id"
        private const val TAG = "CardEditActivity"

        /**
         * Vytvoří intent pro spuštění aktivity v režimu přidávání nové karty.
         */
        fun newIntent(activity: Activity): Intent {
            return Intent(activity, CardEditActivity::class.java)
        }

        /**
         * Vytvoří intent pro spuštění aktivity v režimu editace existující karty.
         */
        fun newIntent(activity: Activity, cardId: Long): Intent {
            return Intent(activity, CardEditActivity::class.java).apply {
                putExtra(EXTRA_CARD_ID, cardId)
            }
        }
    }
}
