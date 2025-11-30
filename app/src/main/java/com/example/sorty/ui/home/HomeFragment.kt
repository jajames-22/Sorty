package com.example.sorty.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sorty.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var bind: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHomeBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Set the click listener on the button
        bind.addNotesBtn.setOnClickListener {
            val bottomSheet = addNotes()
            // Double check to ensure user cannot click outside
            bottomSheet.isCancelable = false
            bottomSheet.show(parentFragmentManager, "AddNotesSheet")
        }

    }
}