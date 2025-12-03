package com.example.sorty.ui.home.addNotesFiles // Tiyakin na ito ang package mo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.R // Kukunin nito ang R.layout.list_item_emoji
// Import din ang Data Source kung nasa ibang package
// import com.example.sorty.data.EmojiDataSource

// ... (Rest of the EmojiAdapter class code)
class EmojiAdapter(
    private val emojis: List<String>,
    private val onEmojiClick: (String) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    inner class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiTextView: TextView = itemView.findViewById(R.id.text_emoji_icon)

        init {
            itemView.setOnClickListener {
                onEmojiClick(emojis[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_emoji, parent, false)
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.emojiTextView.text = emojis[position]
    }

    override fun getItemCount(): Int = emojis.size
}