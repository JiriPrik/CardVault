package karty1.cz.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Třída pro práci s hesly - hashování a ověřování.
 */
object PasswordUtils {

    private const val SALT_LENGTH = 16
    private const val HASH_ALGORITHM = "SHA-256"

    /**
     * Vygeneruje náhodnou sůl.
     */
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    /**
     * Vytvoří hash hesla s náhodnou solí.
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = hashWithSalt(password, salt)
        
        // Uložení soli a hashe ve formátu "salt:hash"
        val saltBase64 = Base64.getEncoder().encodeToString(salt)
        val hashBase64 = Base64.getEncoder().encodeToString(hash)
        
        return "$saltBase64:$hashBase64"
    }

    /**
     * Ověří, zda zadané heslo odpovídá uloženému hashi.
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        try {
            // Rozdělení uloženého hashe na sůl a hash
            val parts = storedHash.split(":")
            if (parts.size != 2) {
                return false
            }
            
            val salt = Base64.getDecoder().decode(parts[0])
            val expectedHash = Base64.getDecoder().decode(parts[1])
            
            // Vytvoření hashe ze zadaného hesla a uložené soli
            val actualHash = hashWithSalt(password, salt)
            
            // Porovnání hashů
            return MessageDigest.isEqual(expectedHash, actualHash)
        } catch (e: Exception) {
            LogHelper.e("PasswordUtils", "Chyba při ověřování hesla: ${e.message}", e)
            return false
        }
    }

    /**
     * Vytvoří hash hesla s danou solí.
     */
    private fun hashWithSalt(password: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance(HASH_ALGORITHM)
        md.update(salt)
        return md.digest(password.toByteArray())
    }
}
