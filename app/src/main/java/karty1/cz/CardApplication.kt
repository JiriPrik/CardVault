package karty1.cz

import android.app.Application
import karty1.cz.database.CardDatabase
import karty1.cz.repository.CardRepository

/**
 * Aplikační třída pro inicializaci komponent.
 */
class CardApplication : Application() {

    // Databáze bude inicializována při prvním přístupu
    val database by lazy { CardDatabase.getDatabase(this) }
    
    // Repository bude inicializován při prvním přístupu
    val repository by lazy { CardRepository(database.cardDao()) }
}
