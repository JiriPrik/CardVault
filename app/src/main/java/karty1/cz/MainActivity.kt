package karty1.cz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

    // Inicializace ViewModelu
    private val cardViewModel: CardViewModel by viewModels()
    private lateinit var cardAdapter: CardAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nastavení XML layoutu jako obsahu aktivity
        setContentView(R.layout.activity_main)

        // Inicializace PreferenceManager
        preferenceManager = PreferenceManager(this)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ADD_CARD -> {
                    // Karta byla přidána, nic dalšího není potřeba dělat,
                    // protože LiveData automaticky aktualizuje seznam
                }
                REQUEST_CODE_EDIT_CARD -> {
                    // Karta byla upravena nebo smazána, nic dalšího není potřeba dělat,
                    // protože LiveData automaticky aktualizuje seznam
                }
            }
        }
    }

    // Metoda pro přidání ukázkové karty byla odstraněna

    companion object {
        private const val REQUEST_CODE_ADD_CARD = 1
        private const val REQUEST_CODE_EDIT_CARD = 2
        private const val TAG = "MainActivity"
    }

    // Seznam všech karet
    private var allCards: List<Card> = emptyList()

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
}
