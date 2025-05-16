package karty1.cz.util

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Pomocná třída pro snímání čárových kódů.
 */
class BarcodeScannerHelper(private val activity: Activity) {

    /**
     * Spustí skener čárových kódů.
     * @param promptMessage Zpráva, která se zobrazí uživateli při skenování.
     * @param scanFormats Seznam formátů čárových kódů, které se mají skenovat.
     */
    fun startBarcodeScanner(
        promptMessage: String = "Naskenujte čárový kód",
        scanFormats: List<BarcodeFormat> = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39
        )
    ) {
        try {
            // Vytvoření integratoru pro skenování
            val integrator = IntentIntegrator(activity)

            // Nastavení parametrů skenování
            integrator.setPrompt(promptMessage)
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(false)
            integrator.setCameraId(0) // 0 = zadní kamera

            // Nastavení formátů čárových kódů
            val scanOptionsBuilder = ScanOptions()
            scanOptionsBuilder.setPrompt(promptMessage)
            scanOptionsBuilder.setBeepEnabled(true)
            scanOptionsBuilder.setOrientationLocked(false)

            // Převod BarcodeFormat na String pro IntentIntegrator
            val formatStrings = scanFormats.map { it.toString() }.toTypedArray()
            scanOptionsBuilder.setDesiredBarcodeFormats(*formatStrings)

            // Spuštění skenování
            integrator.initiateScan()
        } catch (e: Exception) {
            Toast.makeText(activity, "Chyba při spouštění skeneru: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Zpracuje výsledek skenování čárového kódu.
     * @param requestCode Kód požadavku.
     * @param resultCode Kód výsledku.
     * @param data Data z intentu.
     * @return Dvojice (obsah čárového kódu, formát čárového kódu) nebo null, pokud skenování selhalo.
     */
    fun processScanResult(requestCode: Int, resultCode: Int, data: Intent?): Pair<String, String>? {
        // Zpracování výsledku skenování
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null && result.contents != null) {
            // Úspěšné skenování
            val contents = result.contents
            var formatName = result.formatName ?: "UNKNOWN"

            // Lepší detekce formátu čárového kódu
            formatName = when {
                // Detekce EAN-13
                contents.length == 13 && contents.all { it.isDigit() } -> "EAN_13"

                // Detekce EAN-8
                contents.length == 8 && contents.all { it.isDigit() } -> "EAN_8"

                // Detekce CODE-39
                contents.matches(Regex("^[0-9A-Z\\s\\-\\.\\$/+%]*$")) -> "CODE_39"

                // Detekce CODE-128
                contents.any { it.code < 32 || it.code > 126 } -> "CODE_128"

                // Detekce QR kódu (pokud formát je již QR_CODE nebo obsahuje URL)
                formatName == "QR_CODE" || contents.startsWith("http") -> "QR_CODE"

                // Výchozí hodnota
                else -> formatName
            }

            // Normalizace názvu formátu
            formatName = normalizeFormatName(formatName)

            return Pair(contents, formatName)
        }

        // Skenování bylo zrušeno nebo selhalo
        return null
    }

    /**
     * Normalizuje název formátu čárového kódu.
     */
    private fun normalizeFormatName(formatName: String): String {
        return when (formatName.uppercase().replace("-", "_").replace(" ", "_")) {
            "QRCODE" -> "QR_CODE"
            "EAN13" -> "EAN_13"
            "EAN8" -> "EAN_8"
            "CODE128" -> "CODE_128"
            "CODE39" -> "CODE_39"
            else -> {
                // Pokus o nalezení nejbližšího formátu
                val formats = listOf("QR_CODE", "EAN_13", "EAN_8", "CODE_128", "CODE_39")
                val bestMatch = formats.minByOrNull { levenshteinDistance(it, formatName.uppercase()) }
                bestMatch ?: "QR_CODE"
            }
        }
    }

    /**
     * Výpočet Levenshteinovy vzdálenosti mezi dvěma řetězci.
     * Používá se pro nalezení nejbližšího formátu.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) {
            dp[i][0] = i
        }

        for (j in 0..n) {
            dp[0][j] = j
        }

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    minOf(dp[i - 1][j - 1], dp[i - 1][j], dp[i][j - 1]) + 1
                }
            }
        }

        return dp[m][n]
    }

    /**
     * Převede formát čárového kódu z řetězce na BarcodeFormat.
     */
    fun getBarcodeFormatFromString(formatString: String): BarcodeFormat {
        return when (formatString.uppercase()) {
            "QR_CODE" -> BarcodeFormat.QR_CODE
            "EAN_13" -> BarcodeFormat.EAN_13
            "EAN_8" -> BarcodeFormat.EAN_8
            "CODE_128" -> BarcodeFormat.CODE_128
            "CODE_39" -> BarcodeFormat.CODE_39
            else -> BarcodeFormat.QR_CODE // Výchozí hodnota
        }
    }

    /**
     * Převede BarcodeFormat na řetězec.
     */
    fun getBarcodeFormatString(format: BarcodeFormat): String {
        return when (format) {
            BarcodeFormat.QR_CODE -> "QR_CODE"
            BarcodeFormat.EAN_13 -> "EAN_13"
            BarcodeFormat.EAN_8 -> "EAN_8"
            BarcodeFormat.CODE_128 -> "CODE_128"
            BarcodeFormat.CODE_39 -> "CODE_39"
            else -> "QR_CODE" // Výchozí hodnota
        }
    }
}
