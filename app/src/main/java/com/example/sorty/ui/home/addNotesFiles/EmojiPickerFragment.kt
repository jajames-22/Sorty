package com.example.sorty.ui.home.addNotesFiles

// EmojiPickerFragment.kt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sorty.databinding.FragmentEmojiPickerBinding // Kailangan mo itong gawin
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Define an interface to send the selected emoji back to addNotes
interface EmojiSelectedListener {
    fun onEmojiSelected(emoji: String)
}

class EmojiPickerFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEmojiPickerBinding? = null
    private val binding get() = _binding!!
    private lateinit var emojiAdapter: EmojiAdapter

    // Parent fragment/activity na makakatanggap ng napiling emoji
    var listener: EmojiSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmojiPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        emojiAdapter = EmojiAdapter(
            emojis = EmojiDataSource.emojis,
            onEmojiClick = { emoji ->
                listener?.onEmojiSelected(emoji) // Ipadala ang napiling emoji
                dismiss() // Isara ang picker
            }
        )

        binding.recyclerViewEmojis.apply {
            layoutManager = GridLayoutManager(context, 5) // Ipakita ang 5 emojis per row
            adapter = emojiAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}