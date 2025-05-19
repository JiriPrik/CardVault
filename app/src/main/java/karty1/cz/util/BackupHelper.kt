package karty1.cz.util

import android.content.Context
import android.os.Environment
import karty1.cz.database.CardDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Pomocná třída pro zálohování a obnovení dat aplikace.
 */
class BackupHelper {

    companion object {
        private const val TAG = "BackupHelper"
    }

    /**
     * Vytvoří zálohu databáze a fotografií.
     * @param context Kontext aplikace
     * @param password Heslo pro šifrování zálohy
     * @return Soubor se zálohou nebo null, pokud se záloha nepodařila
     */
    fun createBackup(context: Context, password: String): File? {
        try {
            LogHelper.i(TAG, "Vytváření zálohy...")
            
            // Vytvoření adresáře pro zálohy v Download složce
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupDir = File(downloadDir, "KartyBackup")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Vytvoření dočasného adresáře pro zálohu
            val tempDir = File(context.cacheDir, "backup_temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()

            // Kopírování databáze
            val databaseFile = context.getDatabasePath("card_database")
            val databaseBackup = File(tempDir, "card_database")
            if (databaseFile.exists()) {
                LogHelper.d(TAG, "Kopírování databáze: ${databaseFile.absolutePath}")
                databaseFile.copyTo(databaseBackup, overwrite = true)
            } else {
                LogHelper.w(TAG, "Databázový soubor neexistuje: ${databaseFile.absolutePath}")
            }

            // Kopírování fotografií
            val photosDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photosBackup = File(tempDir, "photos")
            photosBackup.mkdirs()
            
            photosDir?.listFiles()?.forEach { file ->
                LogHelper.d(TAG, "Kopírování fotografie: ${file.name}")
                file.copyTo(File(photosBackup, file.name), overwrite = true)
            }

            // Vytvoření ZIP archivu
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "karty_backup_$timestamp.zip")
            
            LogHelper.d(TAG, "Vytváření ZIP archivu: ${backupFile.absolutePath}")
            val zipOutputStream = ZipOutputStream(FileOutputStream(backupFile))
            tempDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryPath = file.relativeTo(tempDir).path
                    val entry = ZipEntry(entryPath)
                    zipOutputStream.putNextEntry(entry)
                    file.inputStream().copyTo(zipOutputStream)
                    zipOutputStream.closeEntry()
                }
            }
            zipOutputStream.close()

            // Šifrování zálohy
            val encryptedBackupFile = File(backupDir, "karty_backup_$timestamp.enc")
            LogHelper.d(TAG, "Šifrování zálohy: ${encryptedBackupFile.absolutePath}")
            val success = EncryptionHelper.encryptFile(backupFile, encryptedBackupFile, password)
            
            // Smazání nešifrované zálohy a dočasných souborů
            backupFile.delete()
            tempDir.deleteRecursively()
            
            return if (success) {
                LogHelper.i(TAG, "Záloha úspěšně vytvořena: ${encryptedBackupFile.absolutePath}")
                encryptedBackupFile
            } else {
                LogHelper.e(TAG, "Nepodařilo se zašifrovat zálohu")
                null
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při vytváření zálohy: ${e.message}", e)
            return null
        }
    }

    /**
     * Obnoví data ze zálohy.
     * @param context Kontext aplikace
     * @param backupFile Soubor se zálohou
     * @param password Heslo pro dešifrování zálohy
     * @return true, pokud se obnovení podařilo, jinak false
     */
    fun restoreBackup(context: Context, backupFile: File, password: String): Boolean {
        try {
            LogHelper.i(TAG, "Obnovování zálohy: ${backupFile.absolutePath}")
            
            // Vytvoření dočasného adresáře pro dešifrovanou zálohu
            val tempDir = File(context.cacheDir, "restore_temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()
            
            // Dešifrování zálohy
            val decryptedBackupFile = File(tempDir, "backup.zip")
            LogHelper.d(TAG, "Dešifrování zálohy...")
            val success = EncryptionHelper.decryptFile(backupFile, decryptedBackupFile, password)
            if (!success) {
                LogHelper.e(TAG, "Nepodařilo se dešifrovat zálohu")
                return false
            }
            
            // Rozbalení ZIP archivu
            val extractDir = File(tempDir, "extracted")
            extractDir.mkdirs()
            
            LogHelper.d(TAG, "Rozbalování ZIP archivu...")
            val zipInputStream = ZipInputStream(FileInputStream(decryptedBackupFile))
            var entry: ZipEntry? = zipInputStream.nextEntry
            while (entry != null) {
                val entryFile = File(extractDir, entry.name)
                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    val outputStream = FileOutputStream(entryFile)
                    zipInputStream.copyTo(outputStream)
                    outputStream.close()
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            zipInputStream.close()
            
            // Obnovení databáze
            val databaseFile = context.getDatabasePath("card_database")
            val databaseBackup = File(extractDir, "card_database")
            if (databaseBackup.exists()) {
                LogHelper.d(TAG, "Obnovování databáze...")
                
                // Zavření databáze před obnovením
                val database = CardDatabase.getDatabase(context)
                database.close()
                
                // Vytvoření adresáře pro databázi, pokud neexistuje
                databaseFile.parentFile?.mkdirs()
                
                // Kopírování databáze
                databaseBackup.copyTo(databaseFile, overwrite = true)
            } else {
                LogHelper.w(TAG, "Databázový soubor v záloze neexistuje")
            }
            
            // Obnovení fotografií
            val photosDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photosBackup = File(extractDir, "photos")
            if (photosBackup.exists() && photosBackup.isDirectory) {
                LogHelper.d(TAG, "Obnovování fotografií...")
                
                // Vytvoření adresáře pro fotografie, pokud neexistuje
                photosDir?.mkdirs()
                
                // Smazání stávajících fotografií
                photosDir?.listFiles()?.forEach { it.delete() }
                
                // Kopírování zálohovaných fotografií
                photosBackup.listFiles()?.forEach { file ->
                    LogHelper.d(TAG, "Obnovování fotografie: ${file.name}")
                    file.copyTo(File(photosDir, file.name), overwrite = true)
                }
            } else {
                LogHelper.w(TAG, "Adresář s fotografiemi v záloze neexistuje")
            }
            
            // Smazání dočasných souborů
            tempDir.deleteRecursively()
            
            LogHelper.i(TAG, "Záloha úspěšně obnovena")
            return true
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při obnovení zálohy: ${e.message}", e)
            return false
        }
    }
}
