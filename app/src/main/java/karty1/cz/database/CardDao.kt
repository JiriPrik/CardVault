package karty1.cz.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DAO pro přístup ke kartám v databázi.
 */
@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardById(id: Long): LiveData<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardByIdSync(id: Long): CardEntity?

    @Query("SELECT * FROM cards ORDER BY name ASC")
    fun getAllCards(): LiveData<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchCards(searchQuery: String): LiveData<List<CardEntity>>
}
