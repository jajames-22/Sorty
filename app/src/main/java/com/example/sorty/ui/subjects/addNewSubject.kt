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

    interface AddNewSubjectListener {
        fun onSubjectAdded(subjectName: String, colorHex: String)
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

        // 2. Setup Radio Buttons with Custom Selectors (Color + Border)
        setupRadioButton(bind.radioColor1, "#FF8A80")
        setupRadioButton(bind.radioColor2, "#B9F6CA")
        setupRadioButton(bind.radioColor3, "#82B1FF")
        setupRadioButton(bind.radioColor4, "#FFD180")
        setupRadioButton(bind.radioColor5, "#EA80FC")

        // Helper function to update folder icons
        fun updateFolderColor(hex: String) {
            val color = Color.parseColor(hex)
            // MULTIPLY keeps black outlines black, but tints white fills
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
                else -> "#FF8A80"
            }
            updateFolderColor(selectedColorHex)
        }

        bind.buttonAdd.setOnClickListener {
            val subjectName = bind.subN.text.toString().trim()
            if (subjectName.isNotEmpty()) {
                listener?.onSubjectAdded(subjectName, selectedColorHex)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Subject name can't be empty", Toast.LENGTH_SHORT).show()
            }
        }

        bind.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Creates a circular background with a Border that only appears when selected.
     */
    private fun setupRadioButton(radioButton: RadioButton, colorHex: String) {
        val colorInt = Color.parseColor(colorHex)
        val borderColor = Color.parseColor("#414543") // Dark Grey Border

        // Convert 4dp to pixels for the stroke width
        val strokeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            2f,
            resources.displayMetrics
        ).toInt()

        // 1. Create the "Selected" State (Color + Border)
        val checkedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(colorInt)
            setStroke(strokeWidthPx, borderColor) // <--- THIS ADDS THE BORDER
        }

        // 2. Create the "Normal" State (Color Only)
        val uncheckedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(colorInt)
        }

        // 3. Create a StateListDrawable to handle the switching
        val stateList = StateListDrawable()
        stateList.addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
        stateList.addState(intArrayOf(), uncheckedDrawable)

        // 4. Apply to the button
        radioButton.buttonDrawable = null // Hide the default Android radio dot
        radioButton.background = stateList // Apply our custom circle
    }
}