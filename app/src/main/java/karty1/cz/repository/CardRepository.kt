package karty1.cz.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import karty1.cz.database.CardDao
import karty1.cz.database.CardEntity
import karty1.cz.model.Card
import java.util.Date

/**
 * Repository pro práci s kartami.
 */
class CardRepository(private val cardDao: CardDao) {

    // Získání všech karet
    val allCards: LiveData<List<Card>> = cardDao.getAllCards().map { entities ->
        entities.map { it.toCard() }
    }

    // Vyhledávání karet
    fun searchCards(query: String): LiveData<List<Card>> {
        return cardDao.searchCards(query).map { entities ->
            entities.map { it.toCard() }
        }
    }

    // Získání karty podle ID
    fun getCardById(id: Long): LiveData<Card> {
        return cardDao.getCardById(id).map { it.toCard() }
    }

    // Získání karty podle ID synchronně
    suspend fun getCardByIdSync(id: Long): Card? {
        val entity = cardDao.getCardByIdSync(id)
        return entity?.toCard()
    }

    // Vložení nové karty
    suspend fun insertCard(card: Card): Long {
        return cardDao.insertCard(card.toEntity())
    }

    // Aktualizace karty
    suspend fun updateCard(card: Card) {
        cardDao.updateCard(card.toEntity())
    }

    // Smazání karty
    suspend fun deleteCard(card: Card) {
        cardDao.deleteCard(card.toEntity())
    }

    // Konverze z Entity na Model
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
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }

    // Konverze z Model na Entity
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
            createdAt = createdAt.time,
            updatedAt = updatedAt.time
        )
    }
}
