package com.example.sorty.ui.subjects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sorty.databinding.FragmentSubjectsBinding

class SubjectsFragment : Fragment(), AddNewSubject.AddNewSubjectListener {

    private lateinit var bind: FragmentSubjectsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSubjectsBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addSubjectBtn = bind.addSubjectBtn

        addSubjectBtn.setOnClickListener {
            showAddNewSubjectSheet()
        }
    }

    private fun showAddNewSubjectSheet() {
        val bottomSheet = AddNewSubject()
        bottomSheet.setAddNewSubjectListener(this)
        bottomSheet.show(parentFragmentManager, "AddNewSubjectBottomSheetTag")
    }

    /**
     * FIXED: Added 'colorHex' parameter to match the interface change
     */
    override fun onSubjectAdded(subjectName: String, colorHex: String) {
        // You can now use the colorHex here (e.g., to save to database)
        Toast.makeText(
            requireContext(),
            "Subject: $subjectName, Color: $colorHex",
            Toast.LENGTH_SHORT
        ).show()
    }
}