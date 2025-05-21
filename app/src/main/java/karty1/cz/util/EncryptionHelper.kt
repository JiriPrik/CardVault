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
                LogHelper.d(TAG, "Začínám šifrovat soubor: ${sourceFile.absolutePath}")
                LogHelper.d(TAG, "Velikost zdrojového souboru: ${sourceFile.length()} bytů")
                LogHelper.d(TAG, "Cílový soubor: ${destFile.absolutePath}")
                LogHelper.d(TAG, "Použité heslo pro šifrování: $password")

                // Generování náhodné soli a inicializačního vektoru
                val random = SecureRandom()
                val salt = ByteArray(SALT_LENGTH)
                val iv = ByteArray(IV_LENGTH)
                random.nextBytes(salt)
                random.nextBytes(iv)

                LogHelper.d(TAG, "Vygenerována sůl a IV")
                LogHelper.d(TAG, "Sůl (Base64): ${android.util.Base64.encodeToString(salt, android.util.Base64.DEFAULT)}")
                LogHelper.d(TAG, "IV (Base64): ${android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT)}")

                // Odvození klíče z hesla a soli
                val secretKey = deriveKey(password, salt)
                LogHelper.d(TAG, "Klíč úspěšně odvozen z hesla a soli")

                // Inicializace šifry
                val cipher = Cipher.getInstance(ALGORITHM)
                val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
                LogHelper.d(TAG, "Šifra inicializována v režimu šifrování")

                // Čtení zdrojového souboru
                val inputStream = FileInputStream(sourceFile)
                val outputStream = FileOutputStream(destFile)

                // Zápis soli a IV do výstupního souboru
                outputStream.write(salt)
                outputStream.write(iv)
                LogHelper.d(TAG, "Sůl a IV zapsány do výstupního souboru")

                // Šifrování a zápis dat
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0
                var totalBytesWritten = SALT_LENGTH + IV_LENGTH // Počítáme s již zapsanými byty (sůl + IV)

                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        totalBytesRead += bytesRead
                        LogHelper.d(TAG, "Přečteno ${bytesRead} bytů, celkem: $totalBytesRead")

                        val encryptedBytes = cipher.update(buffer, 0, bytesRead)
                        if (encryptedBytes != null) {
                            outputStream.write(encryptedBytes)
                            totalBytesWritten += encryptedBytes.size
                            LogHelper.d(TAG, "Zapsáno ${encryptedBytes.size} zašifrovaných bytů, celkem: $totalBytesWritten")
                        }
                    }

                    // Dokončení šifrování
                    val finalBytes = cipher.doFinal()
                    if (finalBytes != null) {
                        outputStream.write(finalBytes)
                        totalBytesWritten += finalBytes.size
                        LogHelper.d(TAG, "Zapsáno ${finalBytes.size} finálních bytů, celkem: $totalBytesWritten")
                    }

                    LogHelper.d(TAG, "Šifrování dokončeno, celkem přečteno: $totalBytesRead bytů, zapsáno: $totalBytesWritten bytů")
                } catch (e: Exception) {
                    LogHelper.e(TAG, "Chyba při šifrování dat: ${e.message}", e)
                    throw e
                } finally {
                    // Uzavření streamů
                    try {
                        inputStream.close()
                        outputStream.close()
                        LogHelper.d(TAG, "Streamy uzavřeny")
                    } catch (e: Exception) {
                        LogHelper.e(TAG, "Chyba při uzavírání streamů: ${e.message}", e)
                    }
                }

                LogHelper.d(TAG, "Šifrování úspěšně dokončeno, velikost výstupního souboru: ${destFile.length()} bytů")
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
                LogHelper.d(TAG, "Začínám dešifrovat soubor: ${sourceFile.absolutePath}")
                LogHelper.d(TAG, "Velikost zdrojového souboru: ${sourceFile.length()} bytů")
                LogHelper.d(TAG, "Cílový soubor: ${destFile.absolutePath}")
                LogHelper.d(TAG, "Použité heslo pro dešifrování: $password")

                // Čtení zdrojového souboru
                val inputStream = FileInputStream(sourceFile)

                // Čtení soli a IV ze souboru
                val salt = ByteArray(SALT_LENGTH)
                val iv = ByteArray(IV_LENGTH)
                val saltBytesRead = inputStream.read(salt)
                val ivBytesRead = inputStream.read(iv)

                LogHelper.d(TAG, "Přečteno bytů soli: $saltBytesRead, IV: $ivBytesRead")
                LogHelper.d(TAG, "Sůl (Base64): ${android.util.Base64.encodeToString(salt, android.util.Base64.DEFAULT)}")
                LogHelper.d(TAG, "IV (Base64): ${android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT)}")

                // Odvození klíče z hesla a soli
                val secretKey = deriveKey(password, salt)
                LogHelper.d(TAG, "Klíč úspěšně odvozen z hesla a soli")

                // Inicializace šifry
                val cipher = Cipher.getInstance(ALGORITHM)
                val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
                LogHelper.d(TAG, "Šifra inicializována v režimu dešifrování")

                // Čtení a dešifrování dat
                val outputStream = FileOutputStream(destFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0
                var totalBytesWritten = 0

                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        totalBytesRead += bytesRead
                        LogHelper.d(TAG, "Přečteno ${bytesRead} bytů, celkem: $totalBytesRead")

                        val decryptedBytes = cipher.update(buffer, 0, bytesRead)
                        if (decryptedBytes != null) {
                            outputStream.write(decryptedBytes)
                            totalBytesWritten += decryptedBytes.size
                            LogHelper.d(TAG, "Zapsáno ${decryptedBytes.size} dešifrovaných bytů, celkem: $totalBytesWritten")
                        }
                    }

                    // Dokončení dešifrování
                    val finalBytes = cipher.doFinal()
                    if (finalBytes != null) {
                        outputStream.write(finalBytes)
                        totalBytesWritten += finalBytes.size
                        LogHelper.d(TAG, "Zapsáno ${finalBytes.size} finálních bytů, celkem: $totalBytesWritten")
                    }

                    LogHelper.d(TAG, "Dešifrování dokončeno, celkem přečteno: $totalBytesRead bytů, zapsáno: $totalBytesWritten bytů")
                } catch (e: Exception) {
                    LogHelper.e(TAG, "Chyba při dešifrování dat: ${e.message}", e)
                    throw e
                } finally {
                    // Uzavření streamů
                    try {
                        inputStream.close()
                        outputStream.close()
                        LogHelper.d(TAG, "Streamy uzavřeny")
                    } catch (e: Exception) {
                        LogHelper.e(TAG, "Chyba při uzavírání streamů: ${e.message}", e)
                    }
                }

                LogHelper.d(TAG, "Dešifrování úspěšně dokončeno, velikost výstupního souboru: ${destFile.length()} bytů")
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
