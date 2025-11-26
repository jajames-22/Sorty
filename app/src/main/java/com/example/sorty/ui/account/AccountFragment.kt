package com.example.sorty.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sorty.databinding.FragmentAccountBinding // Changed this to FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var bind: FragmentAccountBinding // Changed to non-nullable lateinit var and renamed

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        bind = FragmentAccountBinding.inflate(inflater, container, false) // Changed this to FragmentAccountBinding
        // You can now access your views directly using bind.
        // For example: bind.yourTextView.text = "Hello Account"

        return bind.root
    }


}
