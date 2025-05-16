package karty1.cz.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pomocná třída pro práci s fotoaparátem a ukládání fotografií.
 */
class CameraHelper(private val context: Context) {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_PERMISSION_CAMERA = 100
        const val REQUEST_PERMISSION_STORAGE = 101

        private const val TAG = "CameraHelper"
    }

    /**
     * Kontroluje, zda má aplikace oprávnění pro přístup k fotoaparátu.
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Kontroluje, zda má aplikace oprávnění pro přístup k úložišti.
     */
    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Vyžádá oprávnění pro přístup k fotoaparátu.
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_PERMISSION_CAMERA
        )
    }

    /**
     * Vyžádá oprávnění pro přístup k úložišti.
     */
    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_PERMISSION_STORAGE
            )
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSION_STORAGE
            )
        }
    }

    /**
     * Vytvoří dočasný soubor pro uložení fotografie.
     */
    @Throws(IOException::class)
    fun createImageFile(): File {
        // Vytvoření unikátního názvu souboru
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )
    }

    /**
     * Spustí fotoaparát pro pořízení fotografie.
     */
    fun takePicture(activity: Activity, photoFile: File): Uri? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Kontrola, zda je dostupný fotoaparát
        if (takePictureIntent.resolveActivity(activity.packageManager) == null) {
            Log.e(TAG, "No camera app available")
            return null
        }

        // Vytvoření URI pro soubor
        val photoURI = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            photoFile
        )

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

        return photoURI
    }

    /**
     * Načte fotografii ze souboru a vrátí ji jako Bitmap.
     * Pokud je soubor zašifrovaný, použije heslo k dešifrování.
     */
    fun loadImageFromFile(filePath: String?, password: String? = null): Bitmap? {
        if (filePath == null) return null

        return try {
            val file = File(filePath)
            if (!file.exists()) return null

            // Kontrola, zda je soubor zašifrovaný
            if (filePath.endsWith(".enc") && password != null) {
                // Dešifrování souboru
                val tempFile = File.createTempFile("dec_", ".jpg", context.cacheDir)
                val success = EncryptionHelper.decryptFile(file, tempFile, password)

                if (success) {
                    // Načtení dešifrovaného souboru
                    val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                    // Smazání dočasného souboru
                    tempFile.delete()
                    bitmap
                } else {
                    LogHelper.e(TAG, "Nepodařilo se dešifrovat soubor: $filePath")
                    null
                }
            } else {
                // Běžné načtení nezašifrovaného souboru
                BitmapFactory.decodeFile(filePath)
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při načítání obrázku: ${e.message}", e)
            null
        }
    }

    /**
     * Uloží Bitmap do souboru.
     * Pokud je zadáno heslo, soubor bude zašifrován.
     */
    fun saveBitmapToFile(bitmap: Bitmap, filePath: String, password: String? = null): Boolean {
        return try {
            // Nejprve uložíme bitmapu do dočasného souboru
            val tempFile = File.createTempFile("temp_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            // Pokud je zadáno heslo, zašifrujeme soubor
            if (password != null) {
                val encryptedFilePath = if (!filePath.endsWith(".enc")) "$filePath.enc" else filePath
                val encryptedFile = File(encryptedFilePath)
                val success = EncryptionHelper.encryptFile(tempFile, encryptedFile, password)

                // Smazání dočasného souboru
                tempFile.delete()

                success
            } else {
                // Pokud není zadáno heslo, jen přesuneme dočasný soubor na cílovou cestu
                val destFile = File(filePath)
                tempFile.copyTo(destFile, overwrite = true)
                tempFile.delete()
                true
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při ukládání bitmapy: ${e.message}", e)
            false
        }
    }

    /**
     * Vytvoří adresář pro ukládání fotografií, pokud neexistuje.
     */
    fun createImageDirectory(): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) {
            storageDir.mkdirs()
        }
        return storageDir
    }

    /**
     * Zašifruje existující soubor pomocí hesla.
     * @param filePath Cesta k souboru, který má být zašifrován
     * @param password Heslo pro šifrování
     * @return Cesta k zašifrovanému souboru nebo null, pokud šifrování selhalo
     */
    fun encryptExistingFile(filePath: String, password: String): String? {
        try {
            val sourceFile = File(filePath)
            if (!sourceFile.exists()) {
                LogHelper.e(TAG, "Zdrojový soubor neexistuje: $filePath")
                return null
            }

            // Vytvoření cílového souboru pro zašifrovaná data
            val encryptedFilePath = "$filePath.enc"
            val encryptedFile = File(encryptedFilePath)

            // Zašifrování souboru
            val success = EncryptionHelper.encryptFile(sourceFile, encryptedFile, password)

            if (success) {
                // Smazání původního souboru
                sourceFile.delete()
                return encryptedFilePath
            } else {
                // Pokud šifrování selhalo, smazání cílového souboru
                encryptedFile.delete()
                return null
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při šifrování souboru: ${e.message}", e)
            return null
        }
    }

    /**
     * Dešifruje existující soubor pomocí hesla.
     * @param filePath Cesta k zašifrovanému souboru
     * @param password Heslo pro dešifrování
     * @return Cesta k dešifrovanému souboru nebo null, pokud dešifrování selhalo
     */
    fun decryptExistingFile(filePath: String, password: String): String? {
        try {
            if (!filePath.endsWith(".enc")) {
                LogHelper.e(TAG, "Soubor není zašifrovaný: $filePath")
                return filePath
            }

            val sourceFile = File(filePath)
            if (!sourceFile.exists()) {
                LogHelper.e(TAG, "Zdrojový soubor neexistuje: $filePath")
                return null
            }

            // Vytvoření cílového souboru pro dešifrovaná data
            val decryptedFilePath = filePath.substring(0, filePath.length - 4) // Odstranění přípony .enc
            val decryptedFile = File(decryptedFilePath)

            // Dešifrování souboru
            val success = EncryptionHelper.decryptFile(sourceFile, decryptedFile, password)

            if (success) {
                return decryptedFilePath
            } else {
                // Pokud dešifrování selhalo, smazání cílového souboru
                decryptedFile.delete()
                return null
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při dešifrování souboru: ${e.message}", e)
            return null
        }
    }
}
