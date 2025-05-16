package karty1.cz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import karty1.cz.database.CardDatabase
import karty1.cz.model.Card
import karty1.cz.repository.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel pro práci s kartami.
 */
class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CardRepository
    val allCards: LiveData<List<Card>>

    init {
        val cardDao = CardDatabase.getDatabase(application).cardDao()
        repository = CardRepository(cardDao)
        allCards = repository.allCards
    }

    /**
     * Vložení nové karty.
     */
    fun insertCard(card: Card) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertCard(card)
    }

    /**
     * Aktualizace existující karty.
     */
    fun updateCard(card: Card) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCard(card)
    }

    /**
     * Smazání karty.
     */
    fun deleteCard(card: Card) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCard(card)
    }

    /**
     * Smazání karty podle ID.
     */
    fun deleteCardById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        val card = repository.getCardByIdSync(id)
        if (card != null) {
            repository.deleteCard(card)
        }
    }

    /**
     * Vyhledávání karet podle dotazu.
     */
    fun searchCards(query: String): LiveData<List<Card>> {
        return repository.searchCards(query)
    }

    /**
     * Získání karty podle ID.
     */
    fun getCardById(id: Long): LiveData<Card> {
        return repository.getCardById(id)
    }
}
