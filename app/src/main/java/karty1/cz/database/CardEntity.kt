package karty1.cz.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entita reprezentující kartu v Room databázi.
 */
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cardNumber: String?,
    val cardType: String?,
    val notes: String?,
    val barcodeData: String?,
    val barcodeType: String?,
    val frontImagePath: String?,
    val backImagePath: String?,
    val createdAt: Long,
    val updatedAt: Long
)
