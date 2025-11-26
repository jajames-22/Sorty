package com.example.sorty.ui.subjects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// 1. Change the import to match the fragment's layout file name
import com.example.sorty.databinding.FragmentSubjectsBinding

class SubjectsFragment : Fragment() {

    // 2. Change the type of the binding variable
    private lateinit var bind: FragmentSubjectsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 3. Inflate using the correct binding class
        bind = FragmentSubjectsBinding.inflate(inflater, container, false)

        // You can now access your views directly using bind.
        // For example: bind.yourTextView.text = "My Subjects"
        return bind.root
    }
}
