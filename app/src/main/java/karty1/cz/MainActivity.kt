package karty1.cz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import karty1.cz.util.BackupHelper
import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.content.pm.PackageManager
import karty1.cz.model.Card
import karty1.cz.util.LogHelper
import karty1.cz.util.PreferenceManager
import karty1.cz.view.BaseActivity
import karty1.cz.view.CardAdapter
import karty1.cz.view.CardDetailActivity
import karty1.cz.view.CardEditActivity
import karty1.cz.viewmodel.CardViewModel
import java.util.Date

class MainActivity : BaseActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_ADD_CARD = 1
        private const val REQUEST_CODE_VIEW_CARD = 2
        private const val REQUEST_CODE_SELECT_BACKUP = 3
        private const val REQUEST_STORAGE_PERMISSION = 100
    }

    // Inicializace ViewModelu
    private val cardViewModel: CardViewModel by viewModels()
    private lateinit var cardAdapter: CardAdapter
    // preferenceManager je již inicializován v BaseActivity

    // Seznam všech karet pro filtrování
    private var allCards: List<Card> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nastavení XML layoutu jako obsahu aktivity
        setContentView(R.layout.activity_main)

        // Získání hesla pro šifrování z intentu
        val encryptionPassword = intent.getStringExtra("encryption_password") ?: ""
        if (encryptionPassword.isNotEmpty()) {
            // Nastavení hesla pro šifrování
            AppConfig.encryptionPassword = encryptionPassword

            // Nastavení, zda mají být fotografie šifrovány
            AppConfig.encryptImages = preferenceManager.getEncryptImages()

            LogHelper.i(TAG, "Inicializováno šifrování fotografií: ${AppConfig.encryptImages}")
        } else {
            LogHelper.w(TAG, "Heslo pro šifrování není k dispozici")
        }

        // Nastavení toolbaru
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inicializace RecyclerView a adaptéru
        setupRecyclerView()

        // Nastavení FAB pro přidání nové karty
        findViewById<FloatingActionButton>(R.id.fabAddCard).setOnClickListener {
            // Otevření aktivity pro přidání nové karty
            val intent = CardEditActivity.newIntent(this)
            startActivityForResult(intent, REQUEST_CODE_ADD_CARD)
        }

        // Pozorování změn v seznamu karet
        cardViewModel.allCards.observe(this) { cards ->
            // Uložení všech karet pro pozdější filtrování
            allCards = cards
            cardAdapter.submitList(cards)
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerCards)
        val fabAddCard = findViewById<FloatingActionButton>(R.id.fabAddCard)

        cardAdapter = CardAdapter { card ->
            // Otevření detailu karty při kliknutí
            openCardDetail(card.id)
        }
        recyclerView.adapter = cardAdapter

        // Použití GridLayoutManager pro zobrazení karet ve dvou sloupcích
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager

        // Nastavení posluchače pro umístění FAB pod poslední kartu
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Pokud je seznam prázdný, umístíme FAB na výchozí pozici
                if (cardAdapter.itemCount == 0) {
                    fabAddCard.translationY = 0f
                    return
                }

                // Získání pozice poslední viditelné položky
                val lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition()

                // Pokud je poslední položka viditelná, umístíme FAB pod ni
                if (lastVisibleItemPosition == cardAdapter.itemCount - 1) {
                    val lastView = gridLayoutManager.findViewByPosition(lastVisibleItemPosition)
                    if (lastView != null) {
                        // Umístění FAB pod poslední kartu
                        val translationY = lastView.bottom.toFloat() - fabAddCard.height - 16f
                        fabAddCard.translationY = translationY
                    }
                }
            }
        })
    }

    private fun openCardDetail(cardId: Long) {
        val intent = Intent(this, CardDetailActivity::class.java).apply {
            putExtra(CardDetailActivity.EXTRA_CARD_ID, cardId)
        }
        startActivity(intent)
    }

    // Metoda pro přidání ukázkové karty byla odstraněna

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Nahraje menu z XML souboru
        menuInflater.inflate(R.menu.menu_main, menu)

        // Získání položky vyhledávání
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        // Nastavení nápovědy
        searchView.queryHint = getString(R.string.search_cards)

        // Nastavení posluchače pro vyhledávání
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Není potřeba reagovat na potvrzení vyhledávání
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtrování karet podle zadaného textu
                filterCards(newText)
                return true
            }
        })

        // Nastavení posluchače pro zavření vyhledávání
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Obnovení původního seznamu karet
                cardAdapter.submitList(allCards)
                return true
            }
        })

        return true
    }

    /**
     * Filtruje karty podle zadaného textu.
     */
    private fun filterCards(query: String?) {
        try {
            LogHelper.d(TAG, "Filtrování karet podle: $query")

            if (query.isNullOrBlank()) {
                // Pokud je dotaz prázdný, zobrazíme všechny karty
                cardAdapter.submitList(allCards)
                return
            }

            // Filtrování karet podle názvu
            val filteredList = allCards.filter { card ->
                card.name.contains(query, ignoreCase = true)
            }

            LogHelper.d(TAG, "Nalezeno ${filteredList.size} karet")
            cardAdapter.submitList(filteredList)
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při filtrování karet: ${e.message}", e)
            Toast.makeText(this, "${getString(R.string.error_searching)}: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                showThemeSelectionDialog()
                true
            }
            R.id.action_backup -> {
                backupData()
                true
            }
            R.id.action_restore -> {
                restoreData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Zobrazí dialog pro výběr motivu aplikace.
     */
    private fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )

        val currentThemeMode = preferenceManager.getThemeMode()
        val currentThemeIndex = when (currentThemeMode) {
            PreferenceManager.THEME_MODE_LIGHT -> 0
            PreferenceManager.THEME_MODE_DARK -> 1
            else -> 2 // THEME_MODE_SYSTEM
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.theme))
            .setSingleChoiceItems(themes, currentThemeIndex) { dialog, which ->
                val selectedThemeMode = when (which) {
                    0 -> PreferenceManager.THEME_MODE_LIGHT
                    1 -> PreferenceManager.THEME_MODE_DARK
                    else -> PreferenceManager.THEME_MODE_SYSTEM
                }

                // Uložení vybraného motivu
                preferenceManager.setThemeMode(selectedThemeMode)

                // Aplikace vybraného motivu
                AppCompatDelegate.setDefaultNightMode(selectedThemeMode)

                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Kontroluje, zda má aplikace oprávnění pro přístup k úložišti.
     */
    private fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Vyžádá oprávnění pro přístup k úložišti.
     */
    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    /**
     * Zálohuje data aplikace.
     */
    private fun backupData() {
        // Kontrola oprávnění pro přístup k úložišti
        if (!hasStoragePermission()) {
            requestStoragePermission()
            return
        }

        // Vytvoření zálohy
        val backupHelper = BackupHelper()
        val backupFile = backupHelper.createBackup(this, preferenceManager.getEncryptionPassword())

        if (backupFile != null) {
            Toast.makeText(this, getString(R.string.backup_created, backupFile.absolutePath), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, getString(R.string.backup_failed), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Obnoví data aplikace ze zálohy.
     */
    private fun restoreData() {
        // Kontrola oprávnění pro přístup k úložišti
        if (!hasStoragePermission()) {
            requestStoragePermission()
            return
        }

        // Výběr souboru zálohy
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream"))
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_BACKUP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SELECT_BACKUP -> {
                    data?.data?.let { uri ->
                        // Kopírování vybraného souboru do dočasného souboru
                        val inputStream = contentResolver.openInputStream(uri)
                        val tempFile = File(cacheDir, "backup.enc")
                        val outputStream = FileOutputStream(tempFile)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()

                        // Obnovení zálohy
                        val backupHelper = BackupHelper()
                        val success = backupHelper.restoreBackup(this, tempFile, preferenceManager.getEncryptionPassword())

                        if (success) {
                            Toast.makeText(this, getString(R.string.restore_successful), Toast.LENGTH_SHORT).show()
                            // Restart aplikace pro načtení obnovených dat
                            val intent = Intent(this, SplashActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, getString(R.string.restore_failed), Toast.LENGTH_SHORT).show()
                        }

                        // Smazání dočasného souboru
                        tempFile.delete()
                    }
                }
                REQUEST_CODE_ADD_CARD -> {
                    // Karta byla přidána, nic nemusíme dělat, LiveData se aktualizuje automaticky
                }
                REQUEST_CODE_VIEW_CARD -> {
                    // Karta byla upravena nebo smazána, nic nemusíme dělat, LiveData se aktualizuje automaticky
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Oprávnění bylo uděleno, můžeme pokračovat
                if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE ||
                    permissions[0] == Manifest.permission.READ_MEDIA_IMAGES) {
                    // Zkusíme znovu zálohovat nebo obnovit data
                }
            } else {
                // Oprávnění bylo zamítnuto
                Toast.makeText(this, "Pro zálohování a obnovení dat je potřeba přístup k úložišti", Toast.LENGTH_LONG).show()
            }
        }
    }
}
