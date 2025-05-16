package karty1.cz.view

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karty1.cz.AppConfig
import karty1.cz.R
import karty1.cz.model.Card
import karty1.cz.util.ImageLoader
import karty1.cz.util.LogHelper

/**
 * Adaptér pro zobrazení seznamu karet v RecyclerView.
 */
class CardAdapter(private val onItemClick: (Card) -> Unit) :
    ListAdapter<Card, CardAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textCardName: TextView = itemView.findViewById(R.id.textCardName)
        private val textCardType: TextView = itemView.findViewById(R.id.textCardType)
        private val textCardNumber: TextView = itemView.findViewById(R.id.textCardNumber)
        private val cardContentLayout: View = itemView.findViewById(R.id.cardContentLayout)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(card: Card) {
            textCardName.text = card.name
            textCardType.text = card.cardType ?: itemView.context.getString(R.string.card_type_not_specified)
            textCardNumber.text = card.cardNumber ?: itemView.context.getString(R.string.card_number_not_specified)

            // Načtení a zobrazení náhledu fotografie přední strany karty jako pozadí
            loadCardImage(card.frontImagePath, cardContentLayout)
        }

        /**
         * Načte a nastaví obrázek karty jako pozadí layoutu.
         * Pokud je obrázek zašifrovaný, použije heslo k dešifrování.
         */
        private fun loadCardImage(imagePath: String?, view: View) {
            try {
                // Kontrola, zda je cesta k obrázku platná
                if (imagePath == null) return

                // Kontrola, zda je obrázek zašifrovaný
                val isEncrypted = imagePath.endsWith(".enc")

                if (isEncrypted && AppConfig.encryptionPassword.isEmpty()) {
                    LogHelper.w("CardAdapter", "${view.context.getString(R.string.error_loading_encrypted_image)}: ${view.context.getString(R.string.password_not_available)}")
                    return
                }

                // Asynchronní načtení obrázku pomocí ImageLoader
                val password = if (isEncrypted) AppConfig.encryptionPassword else null
                ImageLoader.loadImageAsync(imagePath, password) { originalBitmap ->
                    if (originalBitmap != null) {
                        // Získání rozměrů view pro ořezání
                        val viewWidth = view.width
                        val viewHeight = view.height

                        // Pokud je view již inicializováno a má rozměry
                        if (viewWidth > 0 && viewHeight > 0) {
                            // Vytvoření oříznutého a změněného obrázku
                            val scaledBitmap = createScaledBitmap(originalBitmap, viewWidth, viewHeight)

                            // Vytvoření průhledného drawable z bitmapy
                            val drawable = BitmapDrawable(view.resources, scaledBitmap).apply {
                                alpha = 38 // 15% z 255
                            }

                            // Nastavení pozadí layoutu
                            view.post {
                                view.background = drawable
                            }
                        } else {
                            // Pokud view ještě nemá rozměry, počkáme na layout
                            view.post {
                                // Zkusíme znovu po dokončení layoutu
                                loadCardImage(imagePath, view)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                LogHelper.e("CardAdapter", "Chyba při načítání obrázku: ${e.message}", e)
            }
        }

        /**
         * Vytvoří změněnou a oříznutou bitmapu, která se vejde do daných rozměrů
         * a zachová poměr stran karty (typicky kreditní karty mají poměr stran 1.586:1).
         */
        private fun createScaledBitmap(original: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
            // Typický poměr stran kreditní karty (85.6mm × 54mm)
            val cardAspectRatio = 1.586f

            // Výpočet ideální výšky na základě šířky a poměru stran karty
            val idealHeight = (targetWidth / cardAspectRatio).toInt()

            // Omezení výšky na maximální výšku view
            val finalHeight = minOf(idealHeight, targetHeight)

            // Výpočet šířky na základě výšky a poměru stran karty
            val finalWidth = (finalHeight * cardAspectRatio).toInt()

            // Změna velikosti bitmapy
            return Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true)
        }
    }

    /**
     * DiffUtil pro efektivní aktualizaci seznamu.
     */
    class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }
}
