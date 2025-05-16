package karty1.cz.util

import android.util.Log

/**
 * Pomocná třída pro logování.
 */
object LogHelper {
    private const val APP_TAG = "Karty1App"
    private const val MAX_LOG_LENGTH = 4000
    
    /**
     * Loguje informační zprávu.
     */
    fun i(tag: String, message: String) {
        log(Log.INFO, "$tag: $message")
    }
    
    /**
     * Loguje chybovou zprávu.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(Log.ERROR, "$tag: $message")
        throwable?.let {
            log(Log.ERROR, "$tag: ${it.message}")
            it.stackTrace.forEach { element ->
                log(Log.ERROR, "$tag: $element")
            }
        }
    }
    
    /**
     * Loguje varovnou zprávu.
     */
    fun w(tag: String, message: String) {
        log(Log.WARN, "$tag: $message")
    }
    
    /**
     * Loguje ladící zprávu.
     */
    fun d(tag: String, message: String) {
        log(Log.DEBUG, "$tag: $message")
    }
    
    /**
     * Loguje zprávu s danou úrovní.
     */
    private fun log(priority: Int, message: String) {
        // Rozdělení dlouhých zpráv na menší části
        if (message.length > MAX_LOG_LENGTH) {
            val chunkCount = message.length / MAX_LOG_LENGTH
            for (i in 0..chunkCount) {
                val max = (MAX_LOG_LENGTH * (i + 1)).coerceAtMost(message.length)
                Log.println(priority, APP_TAG, message.substring(i * MAX_LOG_LENGTH, max))
            }
        } else {
            Log.println(priority, APP_TAG, message)
        }
    }
}
