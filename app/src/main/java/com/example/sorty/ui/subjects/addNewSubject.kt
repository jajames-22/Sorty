package com.example.sorty.ui.subjects

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.example.sorty.R
import com.example.sorty.databinding.AddNewSubjectBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddNewSubject : BottomSheetDialogFragment() {

    private lateinit var bind: AddNewSubjectBinding

    // 👇 1. UPDATE INTERFACE: Add 'subjectDescription' parameter
    interface AddNewSubjectListener {
        fun onSubjectAdded(subjectName: String, subjectDescription: String, colorHex: String)
    }

    private var listener: AddNewSubjectListener? = null

    // Default Color (Red)
    private var selectedColorHex: String = "#FF8A80"

    fun setAddNewSubjectListener(listener: AddNewSubjectListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = AddNewSubjectBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Folder ImageViews
        val folderIcon1 = bind.folderIcon1
        val folderIcon2 = bind.folderIcon2

        // 2. Setup Radio Buttons
        setupRadioButton(bind.radioColor1, "#FF8A80") // Red
        setupRadioButton(bind.radioColor2, "#B9F6CA") // Green
        setupRadioButton(bind.radioColor3, "#82B1FF") // Blue
        setupRadioButton(bind.radioColor4, "#FFD180") // Orange
        setupRadioButton(bind.radioColor5, "#EA80FC") // Purple
        setupRadioButton(bind.radioColor6, "#80CBC4") // Teal
        setupRadioButton(bind.radioColor7, "#E6EE9C") // Lime
        setupRadioButton(bind.radioColor8, "#CFD8DC") // Blue Grey
        setupRadioButton(bind.radioColor9, "#FFCCBC")

        fun updateFolderColor(hex: String) {
            val color = Color.parseColor(hex)
            folderIcon1.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            folderIcon2.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }

        // Initialize with default color
        updateFolderColor(selectedColorHex)

        // 3. Listen for Selection Changes
        bind.radioGroupColors.setOnCheckedChangeListener { _, checkedId ->
            selectedColorHex = when (checkedId) {
                R.id.radioColor1 -> "#FF8A80"
                R.id.radioColor2 -> "#B9F6CA"
                R.id.radioColor3 -> "#82B1FF"
                R.id.radioColor4 -> "#FFD180"
                R.id.radioColor5 -> "#EA80FC"
                R.id.radioColor6 -> "#80CBC4"
                R.id.radioColor7 -> "#E6EE9C"
                R.id.radioColor8 -> "#CFD8DC"
                R.id.radioColor9 -> "#FFCCBC"
                else -> "#FF8A80"
            }
            updateFolderColor(selectedColorHex)
        }

        bind.buttonAdd.setOnClickListener {
            val subjectName = bind.inputSubjectTitle.text.toString().trim()

            // 👇 2. GET DESCRIPTION INPUT
            val subjectDesc = bind.inputSubjectDesc.text.toString().trim()

            // Optional: Set default text if empty
            val finalDesc = if (subjectDesc.isEmpty()) "No description" else subjectDesc

            if (subjectName.isNotEmpty()) {
                // 👇 3. PASS DESCRIPTION TO LISTENER
                listener?.onSubjectAdded(subjectName, finalDesc, selectedColorHex)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Subject name can't be empty", Toast.LENGTH_SHORT).show()
            }
        }

        bind.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRadioButton(radioButton: RadioButton, colorHex: String) {
        val colorInt = Color.parseColor(colorHex)
        val borderColor = Color.parseColor("#414543")

        val strokeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        ).toInt()

        val checkedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(colorInt)
            setStroke(strokeWidthPx, borderColor)
        }

        val uncheckedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(colorInt)
        }

        val stateList = StateListDrawable()
        stateList.addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
        stateList.addState(intArrayOf(), uncheckedDrawable)

        radioButton.buttonDrawable = null
        radioButton.background = stateList
    }
}