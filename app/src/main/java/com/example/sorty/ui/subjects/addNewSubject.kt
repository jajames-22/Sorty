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
import com.example.sorty.R
import com.example.sorty.databinding.AddNewSubjectBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar // 👈 Import Snackbar

class AddNewSubject : BottomSheetDialogFragment() {

    private lateinit var bind: AddNewSubjectBinding

    interface AddNewSubjectListener {
        fun onSubjectAdded(subjectName: String, subjectDescription: String, colorHex: String)
        fun onSubjectUpdated(id: Int, subjectName: String, subjectDescription: String, colorHex: String)
        fun onSubjectDeleted(id: Int)
    }

    private var listener: AddNewSubjectListener? = null
    private var selectedColorHex: String = "#FF8A80"

    // Variables for Edit Mode
    private var isEditMode = false
    private var editId: Int = -1

    companion object {
        fun newInstance(id: Int, name: String, desc: String, color: String): AddNewSubject {
            val fragment = AddNewSubject()
            val args = Bundle()
            args.putInt("ARG_ID", id)
            args.putString("ARG_NAME", name)
            args.putString("ARG_DESC", desc)
            args.putString("ARG_COLOR", color)
            fragment.arguments = args
            return fragment
        }
    }

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

        // Check if in Edit Mode
        arguments?.let {
            editId = it.getInt("ARG_ID", -1)
            if (editId != -1) {
                isEditMode = true
                val name = it.getString("ARG_NAME", "")
                val desc = it.getString("ARG_DESC", "")
                selectedColorHex = it.getString("ARG_COLOR", "#FF8A80")

                bind.title.text = "Edit Subject"
                bind.inputSubjectTitle.setText(name)
                bind.inputSubjectDesc.setText(desc)
                bind.buttonAdd.text = "Save"
                bind.buttonDelete.visibility = View.VISIBLE

                // Highlight selected color
                val idToSelect = when (selectedColorHex) {
                    "#FF8A80" -> R.id.radioColor1
                    "#B9F6CA" -> R.id.radioColor2
                    "#82B1FF" -> R.id.radioColor3
                    "#FFD180" -> R.id.radioColor4
                    "#EA80FC" -> R.id.radioColor5
                    "#80CBC4" -> R.id.radioColor6
                    "#E6EE9C" -> R.id.radioColor7
                    "#CFD8DC" -> R.id.radioColor8
                    "#FFCCBC" -> R.id.radioColor9
                    else -> R.id.radioColor1
                }
                bind.radioGroupColors.check(idToSelect)
            }
        }

        setupRadioButton(bind.radioColor1, "#FF8A80")
        setupRadioButton(bind.radioColor2, "#B9F6CA")
        setupRadioButton(bind.radioColor3, "#82B1FF")
        setupRadioButton(bind.radioColor4, "#FFD180")
        setupRadioButton(bind.radioColor5, "#EA80FC")
        setupRadioButton(bind.radioColor6, "#80CBC4")
        setupRadioButton(bind.radioColor7, "#E6EE9C")
        setupRadioButton(bind.radioColor8, "#CFD8DC")
        setupRadioButton(bind.radioColor9, "#FFCCBC")

        fun updateFolderColor(hex: String) {
            try {
                val color = Color.parseColor(hex)
                bind.folderIcon1.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                bind.folderIcon2.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            } catch (e: Exception) { }
        }

        updateFolderColor(selectedColorHex)

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
            val subjectDesc = bind.inputSubjectDesc.text.toString().trim()
            val finalDesc = if (subjectDesc.isEmpty()) "No description" else subjectDesc

            if (subjectName.isNotEmpty()) {
                if (isEditMode) {
                    listener?.onSubjectUpdated(editId, subjectName, finalDesc, selectedColorHex)
                } else {
                    listener?.onSubjectAdded(subjectName, finalDesc, selectedColorHex)
                }
                dismiss()
            } else {
                // 👇 REPLACED TOAST WITH SNACKBAR 👇
                Snackbar.make(bind.root, "Subject name can't be empty", Snackbar.LENGTH_SHORT)
                    .setAnchorView(bind.buttonAdd) // Floats above the button
                    .setBackgroundTint(Color.parseColor("#FF5252")) // Red error color
                    .setTextColor(Color.WHITE)
                    .show()
            }
        }

        bind.buttonDelete.setOnClickListener {
            if (isEditMode) {
                listener?.onSubjectDeleted(editId)
                dismiss()
            }
        }

        bind.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun setupRadioButton(radioButton: RadioButton, colorHex: String) {
        val colorInt = Color.parseColor(colorHex)
        val borderColor = Color.parseColor("#414543")
        val strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()

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