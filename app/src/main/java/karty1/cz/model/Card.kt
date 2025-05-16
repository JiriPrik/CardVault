package karty1.cz.model

import java.util.Date

/**
 * Model třídy reprezentující kartu v aplikaci.
 */
data class Card(
    val id: Long = 0,
    val name: String,
    val cardNumber: String? = null,
    val cardType: String? = null,
    val notes: String? = null,
    val barcodeData: String? = null,
    val barcodeType: String? = null,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
