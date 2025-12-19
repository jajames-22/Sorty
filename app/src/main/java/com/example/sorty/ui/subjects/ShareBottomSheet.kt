package com.example.sorty.ui.subjects

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.sorty.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class ShareBottomSheet : BottomSheetDialogFragment() {

    private var listener: ShareBottomSheetListener? = null
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAdd: ImageButton
    private lateinit var chipGroup: ChipGroup
    private val addedEmails = mutableListOf<String>()

    interface ShareBottomSheetListener {
        fun onShareList(emails: List<String>)
        fun checkUserExists(email: String): Boolean // 👈 New validation method
    }

    fun setShareListener(listener: ShareBottomSheetListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_share, container, false)

        val btnClose = view.findViewById<ImageButton>(R.id.btn_close)
        btnSend = view.findViewById(R.id.btn_send_invite)
        btnAdd = view.findViewById(R.id.btn_add_email)
        etEmail = view.findViewById(R.id.et_email_input)
        chipGroup = view.findViewById(R.id.chip_group_emails)

        btnClose.setOnClickListener { dismiss() }

        // Logic for the "+" Button
        btnAdd.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (isValidEmail(email)) {
                // Check if user exists in Sorty DB via Interface
                if (listener?.checkUserExists(email) == true) {
                    if (!addedEmails.contains(email)) {
                        addChip(email)
                        etEmail.text?.clear()
                        updateSendButton()
                    } else {
                        Toast.makeText(context, "Email already added", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "User not found in Sorty", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Invalid Email Format", Toast.LENGTH_SHORT).show()
            }
        }

        // Final Send
        btnSend.setOnClickListener {
            if (addedEmails.isNotEmpty()) {
                listener?.onShareList(addedEmails)
                dismiss()
            }
        }

        return view
    }

    private fun addChip(email: String) {
        val chip = Chip(context)
        chip.text = email
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.white) // Adjust per theme
        chip.setChipStrokeColorResource(R.color.primary_green)
        chip.setChipStrokeWidth(2f)

        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
            addedEmails.remove(email)
            updateSendButton()
        }

        chipGroup.addView(chip)
        addedEmails.add(email)
    }

    private fun updateSendButton() {
        if (addedEmails.isNotEmpty()) {
            btnSend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green))
            btnSend.isEnabled = true
        } else {
            btnSend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey_text))
            btnSend.isEnabled = false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
}