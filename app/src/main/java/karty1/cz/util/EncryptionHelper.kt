package karty1.cz.util

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Pomocná třída pro šifrování a dešifrování souborů.
 */
class EncryptionHelper {

    companion object {
        private const val TAG = "EncryptionHelper"
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_ALGORITHM = "AES"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256
        private const val IV_LENGTH = 12
        private const val SALT_LENGTH = 16
        private const val GCM_TAG_LENGTH = 128
        
        /**
         * Šifruje soubor pomocí hesla.
         * @param sourceFile Zdrojový soubor k zašifrování
         * @param destFile Cílový soubor pro zašifrovaná data
         * @param password Heslo pro šifrování
         * @return true, pokud šifrování proběhlo úspěšně, jinak false
         */
        fun encryptFile(sourceFile: File, destFile: File, password: String): Boolean {
            try {
                // Generování náhodné soli a inicializačního vektoru
                val random = SecureRandom()
                val salt = ByteArray(SALT_LENGTH)
                val iv = ByteArray(IV_LENGTH)
                random.nextBytes(salt)
                random.nextBytes(iv)
                
                // Odvození klíče z hesla a soli
                val secretKey = deriveKey(password, salt)
                
                // Inicializace šifry
                val cipher = Cipher.getInstance(ALGORITHM)
                val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
                
                // Čtení zdrojového souboru
                val inputStream = FileInputStream(sourceFile)
                val outputStream = FileOutputStream(destFile)
                
                // Zápis soli a IV do výstupního souboru
                outputStream.write(salt)
                outputStream.write(iv)
                
                // Šifrování a zápis dat
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val encryptedBytes = cipher.update(buffer, 0, bytesRead)
                    if (encryptedBytes != null) {
                        outputStream.write(encryptedBytes)
                    }
                }
                
                // Dokončení šifrování
                val finalBytes = cipher.doFinal()
                if (finalBytes != null) {
                    outputStream.write(finalBytes)
                }
                
                // Uzavření streamů
                inputStream.close()
                outputStream.close()
                
                return true
            } catch (e: Exception) {
                LogHelper.e(TAG, "Chyba při šifrování souboru: ${e.message}", e)
                return false
            }
        }
        
        /**
         * Dešifruje soubor pomocí hesla.
         * @param sourceFile Zdrojový zašifrovaný soubor
         * @param destFile Cílový soubor pro dešifrovaná data
         * @param password Heslo pro dešifrování
         * @return true, pokud dešifrování proběhlo úspěšně, jinak false
         */
        fun decryptFile(sourceFile: File, destFile: File, password: String): Boolean {
            try {
                // Čtení zdrojového souboru
                val inputStream = FileInputStream(sourceFile)
                
                // Čtení soli a IV ze souboru
                val salt = ByteArray(SALT_LENGTH)
                val iv = ByteArray(IV_LENGTH)
                inputStream.read(salt)
                inputStream.read(iv)
                
                // Odvození klíče z hesla a soli
                val secretKey = deriveKey(password, salt)
                
                // Inicializace šifry
                val cipher = Cipher.getInstance(ALGORITHM)
                val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
                
                // Čtení a dešifrování dat
                val outputStream = FileOutputStream(destFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val decryptedBytes = cipher.update(buffer, 0, bytesRead)
                    if (decryptedBytes != null) {
                        outputStream.write(decryptedBytes)
                    }
                }
                
                // Dokončení dešifrování
                val finalBytes = cipher.doFinal()
                if (finalBytes != null) {
                    outputStream.write(finalBytes)
                }
                
                // Uzavření streamů
                inputStream.close()
                outputStream.close()
                
                return true
            } catch (e: Exception) {
                LogHelper.e(TAG, "Chyba při dešifrování souboru: ${e.message}", e)
                return false
            }
        }
        
        /**
         * Odvození klíče z hesla a soli.
         */
        private fun deriveKey(password: String, salt: ByteArray): SecretKey {
            val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val keyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
            val keyBytes = keyFactory.generateSecret(keySpec).encoded
            return SecretKeySpec(keyBytes, KEY_ALGORITHM)
        }
    }
}
