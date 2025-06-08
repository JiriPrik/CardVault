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
import androidx.activity.result.ActivityResultLauncher
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

import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.content.pm.PackageManager
import android.text.InputType
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
        // REQUEST_CODE_ADD_CARD byl odstraněn v předchozím kroku
        private const val REQUEST_CODE_VIEW_CARD = 2 // Používá se v onActivityResult

        // REQUEST_STORAGE_PERMISSION bude odstraněn, řešeno přes ActivityResultLauncher

        // Klíč pro získání hesla pro šifrování z Intentu
        private const val ENCRYPTION_PASSWORD_KEY = "encryption_password"
    }

    // Inicializace ViewModelu
    private val cardViewModel: CardViewModel by viewModels()
    private lateinit var cardAdapter: CardAdapter
    private lateinit var editCardLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    // preferenceManager je již inicializován v BaseActivity

    // Seznam všech karet pro filtrování
    // List of all cards for filtering purposes
    private var allCards: List<Card> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nastavení XML layoutu jako obsahu aktivity
        // Set the XML layout as the activity's content
        setContentView(R.layout.activity_main)

        // Initialize the ActivityResultLauncher for editing/adding cards.
        // This launcher handles the result from CardEditActivity.
        editCardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Karta byla přidána, LiveData se aktualizuje automaticky.
                // Card was added/edited, LiveData will update automatically.
                LogHelper.i(TAG, "Nová karta byla úspěšně přidána/editována přes ActivityResultLauncher.")
            }
        }

        // Initialize the ActivityResultLauncher for requesting permissions.
        // This launcher handles the results of permission requests.
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Zkontrolujeme, zda bylo uděleno alespoň jedno z požadovaných oprávnění.
            // Check if at least one of the requested permissions was granted.
            // For Android Tiramisu+, it's READ_MEDIA_IMAGES; for older versions, READ_EXTERNAL_STORAGE.
            val granted = permissions.entries.any { (permission, isGranted) ->
                isGranted && (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (granted) {
                LogHelper.i(TAG, "Storage permission granted via ActivityResultLauncher.")
                // Zde by mohla být logika pro pokračování operace, která vyžadovala oprávnění.
                // Logic to continue the operation that required permission could be placed here.
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                LogHelper.w(TAG, "Storage permission denied via ActivityResultLauncher.")
            }
        }

        // Získání hesla pro šifrování z intentu
        // Retrieve the encryption password from the intent.
        val encryptionPassword = intent.getStringExtra(ENCRYPTION_PASSWORD_KEY) ?: ""
        if (encryptionPassword.isNotEmpty()) {
            // Nastavení hesla pro šifrování
            // Set the encryption password in AppConfig.
            AppConfig.encryptionPassword = encryptionPassword

            // Nastavení, zda mají být fotografie šifrovány
            // Set whether images should be encrypted based on preferences.
            AppConfig.encryptImages = preferenceManager.getEncryptImages()

            LogHelper.i(TAG, "Inicializováno šifrování fotografií: ${AppConfig.encryptImages}")
        } else {
            LogHelper.w(TAG, "Heslo pro šifrování není k dispozici")
        }

        // Nastavení toolbaru
        // Setup the toolbar.
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inicializace RecyclerView a adaptéru
        // Initialize RecyclerView and its adapter.
        setupRecyclerView()

        // Nastavení FAB pro přidání nové karty
        // Setup the FloatingActionButton for adding new cards.
        findViewById<FloatingActionButton>(R.id.fabAddCard).setOnClickListener {
            // Otevření aktivity pro přidání nové karty
            // Open the activity to add a new card.
            val intent = CardEditActivity.newIntent(this)
            editCardLauncher.launch(intent)
        }

        // Pozorování změn v seznamu karet
        // Observe changes in the list of cards from the ViewModel.
        cardViewModel.allCards.observe(this) { cards ->
            // Uložení všech karet pro pozdější filtrování
            // Store all cards for later filtering.
            allCards = cards
            cardAdapter.submitList(cards)
        }
    }

    // Sets up the RecyclerView, its adapter, layout manager, and scroll listener for FAB positioning.
    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerCards)
        val fabAddCard = findViewById<FloatingActionButton>(R.id.fabAddCard)

        cardAdapter = CardAdapter { card ->
            // Otevření detailu karty při kliknutí
            // Open card detail on click.
            openCardDetail(card.id)
        }
        recyclerView.adapter = cardAdapter

        // Použití GridLayoutManager pro zobrazení karet ve dvou sloupcích
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager

        // Nastavení posluchače pro dynamické umístění FAB.
        // Cílem je, aby FAB "plaval" nad obsahem a posunul se nahoru,
        // když se uživatel doscrolluje na konec seznamu, aby nepřekrýval poslední položku.
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Pokud je seznam prázdný, resetujeme pozici FAB na výchozí (obvykle definovanou v XML).
                if (cardAdapter.itemCount == 0) {
                    fabAddCard.translationY = 0f
                    return
                }

                // Získáme pozici poslední viditelné položky v GridLayoutManager.
                val lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition()

                // Zkontrolujeme, zda je poslední položka v adapteru aktuálně viditelná.
                // Check if the last item in the adapter is currently visible.
                // Toto je klíčová podmínka pro aktivaci posunu FAB.
                // This is the key condition for activating the FAB's translation.
                if (lastVisibleItemPosition == cardAdapter.itemCount - 1) {
                    // Získáme view poslední viditelné položky. Může být null, pokud se RecyclerView mezitím změnil.
                    // Get the view of the last visible item. Can be null if RecyclerView changes in the meantime.
                    gridLayoutManager.findViewByPosition(lastVisibleItemPosition)?.let { lastView ->
                        // Výpočet nové pozice Y pro FAB.
                        // Calculation of the new Y position for the FAB.
                        // lastView.bottom: Spodní hrana poslední viditelné položky. Bottom edge of the last visible item.
                        // fabAddCard.height: Výška FABu. Odečítáme ji, protože translationY ovlivňuje horní hranu FABu. Height of the FAB. Subtracted because translationY affects the FAB's top edge.
                        // Chceme, aby spodní hrana FABu byla 16f (pixelů/dp) nad spodní hranou lastView. We want the FAB's bottom edge to be 16f (pixels/dp) above the lastView's bottom edge.
                        // 16f: Pevně daný okraj (margin) mezi spodní hranou FAB a spodní hranou poslední položky. Fixed margin between FAB's bottom and last item's bottom.
                        // Ideálně by tato hodnota měla být definována jako dimen resource pro konzistenci a flexibilitu. Ideally, this value should be a dimen resource.
                        val translationY = lastView.bottom.toFloat() - fabAddCard.height - 16f
                        fabAddCard.translationY = translationY
                    }
                }
                // Pokud poslední položka není viditelná (např. uživatel scrolluje nahoru od konce),
                // If the last item is not visible (e.g., user scrolls up from the end),
                // FAB si zachová svou aktuální translationY, což může být buď 0f (pokud je nahoře)
                // nebo předchozí vypočítaná hodnota, pokud byl konec seznamu opuštěn.
                // Pro explicitní reset pozice FABu, když poslední položka přestane být viditelná,
                // by sem byla přidána větev 'else { fabAddCard.translationY = 0f }'.
                // Současná logika ho nechává "viset" na poslední pozici, dokud není seznam prázdný nebo není dosaženo konce znovu.
            }
        })
    }

    // Opens the CardDetailActivity for the given card ID.
    private fun openCardDetail(cardId: Long) {
        val intent = Intent(this, CardDetailActivity::class.java).apply {
            putExtra(CardDetailActivity.EXTRA_CARD_ID, cardId)
        }
        startActivity(intent)
    }

    // Metoda pro přidání ukázkové karty byla odstraněna
    // Method for adding a sample card was removed.

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Nahraje menu z XML souboru
        // Inflate the menu from the XML file.
        menuInflater.inflate(R.menu.menu_main, menu)

        // Získání položky vyhledávání
        // Get the search menu item.
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        // Nastavení nápovědy
        // Set the query hint for the search view.
        searchView.queryHint = getString(R.string.search_cards)

        // Nastavení posluchače pro vyhledávání
        // Set the listener for search query changes.
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Není potřeba reagovat na potvrzení vyhledávání
                // No action needed on search query submission.
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtrování karet podle zadaného textu
                // Filter cards based on the entered text.
                filterCards(newText)
                return true
            }
        })

        // Nastavení posluchače pro zavření vyhledávání
        // Set listener for search view close action.
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true // Return true to expand the view.
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Obnovení původního seznamu karet
                // Restore the original list of cards when search is closed.
                cardAdapter.submitList(allCards)
                return true
            }
        })

        return true
    }

    /**
     * Filtruje karty podle zadaného textu.
     * Filters cards based on the provided query text.
     */
    private fun filterCards(query: String?) {
        try {
            LogHelper.d(TAG, "Filtrování karet podle: $query") // Logging search query

            if (query.isNullOrBlank()) {
                // Pokud je dotaz prázdný, zobrazíme všechny karty
                // If the query is null or blank, display all cards.
                cardAdapter.submitList(allCards)
                return
            }

            // Filtrování karet podle názvu
            // Filter cards by name, case-insensitive.
            val filteredList = allCards.filter { card ->
                card.name.contains(query, ignoreCase = true)
            }

            LogHelper.d(TAG, "Nalezeno ${filteredList.size} karet") // Log number of cards found
            cardAdapter.submitList(filteredList)
        } catch (e: Exception) {
            // Log error during filtering and show a toast.
            LogHelper.e(TAG, "Chyba při filtrování karet (${e::class.java.simpleName}): ${e.message}", e)
            Toast.makeText(this, "${getString(R.string.error_searching)}: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks.
        return when (item.itemId) {
            R.id.action_theme -> {
                // Show theme selection dialog when theme action is clicked.
                showThemeSelectionDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Zobrazí dialog pro výběr motivu aplikace.
     * Displays a dialog for selecting the application theme.
     */
    private fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light), // Light theme
            getString(R.string.theme_dark),  // Dark theme
            getString(R.string.theme_system) // System default theme
        )

        // Determine the currently selected theme index.
        val currentThemeMode = preferenceManager.getThemeMode()
        val currentThemeIndex = when (currentThemeMode) {
            PreferenceManager.THEME_MODE_LIGHT -> 0
            PreferenceManager.THEME_MODE_DARK -> 1
            else -> 2 // THEME_MODE_SYSTEM or default
        }

        // Build and show the theme selection dialog.
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.theme))
            .setSingleChoiceItems(themes, currentThemeIndex) { dialog, which ->
                val selectedThemeMode = when (which) {
                    0 -> PreferenceManager.THEME_MODE_LIGHT
                    1 -> PreferenceManager.THEME_MODE_DARK
                    else -> PreferenceManager.THEME_MODE_SYSTEM
                }

                // Uložení vybraného motivu
                // Save the selected theme mode.
                preferenceManager.setThemeMode(selectedThemeMode)

                // Aplikace vybraného motivu
                // Apply the selected theme.
                AppCompatDelegate.setDefaultNightMode(selectedThemeMode)

                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss() // Dismiss dialog on cancel.
            }
            .show()
    }

    /**
     * Kontroluje, zda má aplikace oprávnění pro přístup k úložišti.
     * Checks if the application has storage permissions.
     * For Android 13 (Tiramisu) and above, checks for READ_MEDIA_IMAGES.
     * For older versions, checks for READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE.
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
     * Requests storage permissions using the ActivityResultLauncher.
     * The specific permissions requested depend on the Android version.
     */
    private fun requestStoragePermission() {
        val permissionsToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        requestPermissionLauncher.launch(permissionsToRequest)
    }


    // Handles results from activities started for result (e.g., CardDetailActivity).
    // This is part of the older activity result mechanism.
    // REQUEST_CODE_ADD_CARD is handled by editCardLauncher.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // Případ REQUEST_CODE_ADD_CARD byl odstraněn, protože je nyní řešen pomocí editCardLauncher
                // Case for REQUEST_CODE_ADD_CARD was removed as it's now handled by editCardLauncher.
                REQUEST_CODE_VIEW_CARD -> {
                    // Karta byla upravena nebo smazána, nic nemusíme dělat, LiveData se aktualizuje automaticky
                    // Card was edited or deleted, no specific action needed here as LiveData updates automatically.
                }
            }
        }
    }

    // Metoda onRequestPermissionsResult je nyní nahrazena logikou v requestPermissionLauncher.
    // The onRequestPermissionsResult method is now replaced by logic in requestPermissionLauncher.
    // Pokud by tato metoda zpracovávala i jiné kódy žádostí o oprávnění,
    // If this method also processed other permission request codes,
    // museli bychom zachovat ty části. V tomto případě zpracovávala pouze REQUEST_STORAGE_PERMISSION.
    // we would have to preserve those parts. In this case, it only handled REQUEST_STORAGE_PERMISSION.

}
