package karty1.cz.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.util.concurrent.Executors
import karty1.cz.util.EncryptionHelper

/**
 * Pomocná třída pro načítání a cachování obrázků.
 */
object ImageLoader {
    private const val TAG = "ImageLoader"

    // Velikost cache pro obrázky (4MB)
    private const val CACHE_SIZE = 4 * 1024 * 1024

    // Cache pro obrázky
    private val memoryCache = object : LruCache<String, Bitmap>(CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // Velikost bitmapy v KB
            return bitmap.byteCount / 1024
        }
    }

    // Executor pro načítání obrázků na pozadí
    private val executor = Executors.newFixedThreadPool(3)

    /**
     * Načte obrázek z cesty k souboru.
     * Pokud je obrázek v cache, vrátí ho z cache.
     * Jinak ho načte z disku a uloží do cache.
     * Pokud je obrázek zašifrovaný, použije heslo k dešifrování.
     */
    fun loadImage(imagePath: String?, password: String? = null): Bitmap? {
        if (imagePath == null) return null

        // Vytvoření klíče pro cache (kombinace cesty a hesla)
        val cacheKey = if (password != null) "$imagePath:$password" else imagePath

        // Kontrola, zda je obrázek v cache
        val cachedBitmap = getBitmapFromCache(cacheKey)
        if (cachedBitmap != null) {
            return cachedBitmap
        }

        try {
            val imageFile = File(imagePath)
            if (!imageFile.exists()) return null

            // Kontrola, zda je obrázek zašifrovaný
            val isEncrypted = imagePath.endsWith(".enc")

            if (isEncrypted && password != null) {
                // Dešifrování obrázku
                val tempFile = File.createTempFile("dec_", ".jpg", File(System.getProperty("java.io.tmpdir")))
                val success = EncryptionHelper.decryptFile(imageFile, tempFile, password)

                if (success) {
                    // Načtení dešifrovaného obrázku
                    val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)

                    // Smazání dočasného souboru
                    tempFile.delete()

                    if (bitmap != null) {
                        // Uložení obrázku do cache
                        addBitmapToCache(cacheKey, bitmap)
                    }

                    return bitmap
                } else {
                    LogHelper.e(TAG, "Nepodařilo se dešifrovat obrázek: $imagePath")
                    return null
                }
            } else {
                // Načtení nezašifrovaného obrázku z disku
                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap != null) {
                    // Uložení obrázku do cache
                    addBitmapToCache(cacheKey, bitmap)
                }
                return bitmap
            }
        } catch (e: Exception) {
            LogHelper.e(TAG, "Chyba při načítání obrázku: ${e.message}", e)
            return null
        }
    }

    /**
     * Načte obrázek asynchronně a zavolá callback po dokončení.
     * Pokud je obrázek zašifrovaný, použije heslo k dešifrování.
     */
    fun loadImageAsync(imagePath: String?, password: String? = null, callback: (Bitmap?) -> Unit) {
        if (imagePath == null) {
            callback(null)
            return
        }

        // Vytvoření klíče pro cache (kombinace cesty a hesla)
        val cacheKey = if (password != null) "$imagePath:$password" else imagePath

        // Kontrola, zda je obrázek v cache
        val cachedBitmap = getBitmapFromCache(cacheKey)
        if (cachedBitmap != null) {
            callback(cachedBitmap)
            return
        }

        // Načtení obrázku na pozadí
        executor.execute {
            val bitmap = loadImage(imagePath, password)
            callback(bitmap)
        }
    }

    /**
     * Získá bitmapu z cache.
     */
    private fun getBitmapFromCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }

    /**
     * Přidá bitmapu do cache.
     */
    private fun addBitmapToCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    /**
     * Vymaže cache.
     */
    fun clearCache() {
        memoryCache.evictAll()
    }
}
