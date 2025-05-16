package karty1.cz

/**
 * Globální konfigurace aplikace.
 */
object AppConfig {
    /**
     * Heslo pro šifrování fotografií.
     * Toto je zjednodušené řešení - v reálné aplikaci by bylo lepší použít KeyStore.
     */
    var encryptionPassword: String = ""
    
    /**
     * Zda mají být fotografie šifrovány.
     */
    var encryptImages: Boolean = true
}
