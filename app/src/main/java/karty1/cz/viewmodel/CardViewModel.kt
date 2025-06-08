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
 * ViewModel for handling card operations.
 */
class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CardRepository
    // LiveData holding the list of all cards.
    val allCards: LiveData<List<Card>>

    init {
        val cardDao = CardDatabase.getDatabase(application).cardDao()
        repository = CardRepository(cardDao)
        allCards = repository.allCards
    }

    /**
     * Vložení nové karty.
     * Inserts a new card into the database.
     * @param card The card to insert.
     */
    fun insertCard(card: Card) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertCard(card)
    }

    /**
     * Aktualizace existující karty.
     * Updates an existing card in the database.
     * @param card The card to update.
     */
    fun updateCard(card: Card) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCard(card)
    }

    /**
     * Smazání karty.
     * Deletes a card from the database.
     * @param card The card to delete.
     */
    fun deleteCard(card: Card) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCard(card)
    }

    /**
     * Smazání karty podle ID.
     * Deletes a card by its ID.
     * @param id The ID of the card to delete.
     */
    fun deleteCardById(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        val card = repository.getCardByIdSync(id) // Synchronously fetch card for deletion
        if (card != null) {
            repository.deleteCard(card)
        }
    }

    /**
     * Vyhledávání karet podle dotazu.
     * Searches for cards based on a query string.
     * @param query The search query.
     * @return LiveData list of cards matching the query.
     */
    fun searchCards(query: String): LiveData<List<Card>> {
        return repository.searchCards(query)
    }

    /**
     * Získání karty podle ID.
     * Retrieves a card by its ID.
     * @param id The ID of the card to retrieve.
     * @return LiveData containing the card.
     */
    fun getCardById(id: Long): LiveData<Card> {
        return repository.getCardById(id)
    }
}
