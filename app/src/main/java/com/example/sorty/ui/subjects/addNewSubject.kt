package com.example.sorty.ui.subjects

import android.app.AlertDialog // 👈 Import AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable // 👈 Import ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // 👈 Import Button
import android.widget.RadioButton
import android.widget.TextView // 👈 Import TextView
import com.example.sorty.R
import com.example.sorty.databinding.AddNewSubjectBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class AddNewSubject : BottomSheetDialogFragment() {

    private lateinit var bind: AddNewSubjectBinding

    interface AddNewSubjectListener {
        fun onSubjectAdded(subjectName: String, subjectDescription: String, colorHex: String)
        fun onSubjectUpdated(id: Int, subjectName: String, subjectDescription: String, colorHex: String)
        fun onSubjectDeleted(id: Int)
    }

    private var listener: AddNewSubjectListener? = null
    private var selectedColorHex: String = "#FF8A80"
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

        // The logic for checking edit mode, setting up radio buttons, and the 'Add' button listener
        // remains exactly the same as your original code.
        // ... (All your existing onViewCreated code up to the delete button)

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

                val idToSelect = when (selectedColorHex) {
                    "#FF8A80" -> R.id.radioColor1; "#B9F6CA" -> R.id.radioColor2
                    "#82B1FF" -> R.id.radioColor3; "#FFD180" -> R.id.radioColor4
                    "#EA80FC" -> R.id.radioColor5; "#80CBC4" -> R.id.radioColor6
                    "#E6EE9C" -> R.id.radioColor7; "#CFD8DC" -> R.id.radioColor8
                    "#FFCCBC" -> R.id.radioColor9; else -> R.id.radioColor1
                }
                bind.radioGroupColors.check(idToSelect)
            }
        }
        setupRadioButton(bind.radioColor1, "#FF8A80"); setupRadioButton(bind.radioColor2, "#B9F6CA")
        setupRadioButton(bind.radioColor3, "#82B1FF"); setupRadioButton(bind.radioColor4, "#FFD180")
        setupRadioButton(bind.radioColor5, "#EA80FC"); setupRadioButton(bind.radioColor6, "#80CBC4")
        setupRadioButton(bind.radioColor7, "#E6EE9C"); setupRadioButton(bind.radioColor8, "#CFD8DC")
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
                R.id.radioColor1 -> "#FF8A80"; R.id.radioColor2 -> "#B9F6CA"
                R.id.radioColor3 -> "#82B1FF"; R.id.radioColor4 -> "#FFD180"
                R.id.radioColor5 -> "#EA80FC"; R.id.radioColor6 -> "#80CBC4"
                R.id.radioColor7 -> "#E6EE9C"; R.id.radioColor8 -> "#CFD8DC"
                R.id.radioColor9 -> "#FFCCBC"; else -> "#FF8A80"
            }
            updateFolderColor(selectedColorHex)
        }
        bind.buttonAdd.setOnClickListener {
            val subjectName = bind.inputSubjectTitle.text.toString().trim()
            val subjectDesc = bind.inputSubjectDesc.text.toString().trim()
            val finalDesc = if (subjectDesc.isEmpty()) "No description" else subjectDesc
            if (subjectName.isNotEmpty()) {
                if (isEditMode) listener?.onSubjectUpdated(editId, subjectName, finalDesc, selectedColorHex)
                else listener?.onSubjectAdded(subjectName, finalDesc, selectedColorHex)
                dismiss()
            } else {
                Snackbar.make(bind.root, "Subject name can't be empty", Snackbar.LENGTH_SHORT)
                    .setAnchorView(bind.buttonAdd).setBackgroundTint(Color.parseColor("#FF5252"))
                    .setTextColor(Color.WHITE).show()
            }
        }
        bind.buttonCancel.setOnClickListener { dismiss() }


        // ======================== THE FIX: UPDATED DELETE BUTTON LISTENER ========================
        bind.buttonDelete.setOnClickListener {
            // Instead of deleting directly, show the confirmation dialog
            showDeleteConfirmationDialog()
        }
        // =======================================================================================
    }

    // ======================== THE FIX: NEW CONFIRMATION DIALOG FUNCTION ========================
    private fun showDeleteConfirmationDialog() {
        // 1. Inflate the custom layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_confirmation, null)

        // 2. Build the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 3. Make the dialog's background transparent to show the rounded corners of the CardView
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 4. Find the views inside the custom layout
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        // 5. Modify the text to be specific to deleting a subject
        val subjectName = bind.inputSubjectTitle.text.toString()
        tvTitle.text = "Delete Subject?"
        tvMessage.text = "Are you sure you want to delete '$subjectName'? All notes and files inside will also be permanently deleted."
        btnConfirm.text = "Delete"

        // Optional: Make the confirm button red for a destructive action
        btnConfirm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF5252"))

        // 6. Set up button click listeners
        btnCancel.setOnClickListener {
            // Just close the dialog. The bottom sheet stays open.
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            // Perform the deletion and close everything.
            if (isEditMode) {
                listener?.onSubjectDeleted(editId)
            }
            dialog.dismiss()
            this@AddNewSubject.dismiss() // Dismiss the bottom sheet itself
        }

        // 7. Show the dialog
        dialog.show()
    }
    // =======================================================================================

    private fun setupRadioButton(radioButton: RadioButton, colorHex: String) {
        // This function remains unchanged
        val colorInt = Color.parseColor(colorHex)
        val borderColor = Color.parseColor("#414543")
        val strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
        val checkedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(colorInt); setStroke(strokeWidthPx, borderColor)
        }
        val uncheckedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(colorInt)
        }
        val stateList = StateListDrawable()
        stateList.addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
        stateList.addState(intArrayOf(), uncheckedDrawable)
        radioButton.buttonDrawable = null
        radioButton.background = stateList
    }
}