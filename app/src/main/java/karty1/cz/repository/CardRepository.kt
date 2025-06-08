package karty1.cz.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import karty1.cz.database.CardDao
import karty1.cz.database.CardEntity
import karty1.cz.model.Card
import java.util.Date

/**
 * Repository pro práci s kartami.
 * Repository for handling card data operations, acting as a mediator between ViewModel and DAO.
 */
class CardRepository(private val cardDao: CardDao) {

    // Získání všech karet
    // LiveData stream of all cards, mapped from CardEntity to Card model.
    val allCards: LiveData<List<Card>> = cardDao.getAllCards().map { entities ->
        entities.map { it.toCard() }
    }

    // Vyhledávání karet
    // Searches for cards based on a query string.
    // @param query The search query.
    // @return LiveData list of cards matching the query, mapped from entities.
    fun searchCards(query: String): LiveData<List<Card>> {
        return cardDao.searchCards(query).map { entities ->
            entities.map { it.toCard() }
        }
    }

    // Získání karty podle ID
    // Retrieves a specific card by its ID.
    // @param id The ID of the card.
    // @return LiveData containing the card, mapped from its entity. Returns null LiveData if not found.
    fun getCardById(id: Long): LiveData<Card> {
        return cardDao.getCardById(id).map { it.toCard() }
    }

    // Získání karty podle ID synchronně
    // Synchronously retrieves a card by its ID. Used for operations not suitable for LiveData.
    // @param id The ID of the card.
    // @return The Card object if found, null otherwise.
    suspend fun getCardByIdSync(id: Long): Card? {
        val entity = cardDao.getCardByIdSync(id)
        return entity?.toCard()
    }

    // Vložení nové karty
    // Inserts a new card into the database.
    // @param card The Card model object to insert.
    // @return The ID of the newly inserted card.
    suspend fun insertCard(card: Card): Long {
        return cardDao.insertCard(card.toEntity())
    }

    // Aktualizace karty
    // Updates an existing card in the database.
    // @param card The Card model object to update.
    suspend fun updateCard(card: Card) {
        cardDao.updateCard(card.toEntity())
    }

    // Smazání karty
    // Deletes a card from the database.
    // @param card The Card model object to delete.
    suspend fun deleteCard(card: Card) {
        cardDao.deleteCard(card.toEntity())
    }

    // Konverze z Entity na Model
    // Converts a CardEntity (database object) to a Card (domain model).
    private fun CardEntity.toCard(): Card {
        return Card(
            id = id,
            name = name,
            cardNumber = cardNumber,
            cardType = cardType,
            notes = notes,
            barcodeData = barcodeData,
            barcodeType = barcodeType,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            createdAt = Date(createdAt), // Convert Long timestamp to Date
            updatedAt = Date(updatedAt)  // Convert Long timestamp to Date
        )
    }

    // Konverze z Model na Entity
    // Converts a Card (domain model) to a CardEntity (database object).
    private fun Card.toEntity(): CardEntity {
        return CardEntity(
            id = id,
            name = name,
            cardNumber = cardNumber,
            cardType = cardType,
            notes = notes,
            barcodeData = barcodeData,
            barcodeType = barcodeType,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            createdAt = createdAt.time, // Convert Date to Long timestamp
            updatedAt = updatedAt.time  // Convert Date to Long timestamp
        )
    }
}
