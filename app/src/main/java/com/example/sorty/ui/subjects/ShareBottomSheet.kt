package com.example.sorty.ui.subjects

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.R
import com.example.sorty.data.models.SharedUser
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText

class ShareBottomSheet : BottomSheetDialogFragment() {

    private var listener: ShareBottomSheetListener? = null
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAdd: ImageButton
    private lateinit var chipGroup: ChipGroup
    private lateinit var rvSharedUsers: RecyclerView
    private val addedEmails = mutableListOf<String>()

    interface ShareBottomSheetListener {
        fun onShareList(emails: List<String>)
        fun checkUserExists(email: String): Boolean
        fun getSharedUsers(): List<SharedUser>
        fun onRemoveAccess(email: String)
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
        rvSharedUsers = view.findViewById(R.id.rv_shared_users)

        btnClose.setOnClickListener { dismiss() }

        btnAdd.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (isValidEmail(email)) {
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

        btnSend.setOnClickListener {
            if (addedEmails.isNotEmpty()) {
                listener?.onShareList(addedEmails)
                val message = if (addedEmails.size == 1) {
                    "Folder shared with ${addedEmails[0]}"
                } else {
                    "Folder shared with ${addedEmails.size} users"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        setupSharedUsersList()
        return view
    }

    private fun setupSharedUsersList() {
        val users = listener?.getSharedUsers() ?: emptyList()
        rvSharedUsers.layoutManager = LinearLayoutManager(context)
        rvSharedUsers.adapter = SharedUserAdapter(users) { email ->
            showRemoveUserConfirmation(email)
        }
    }

    private fun showRemoveUserConfirmation(email: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_confirmation, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        tvTitle.text = "Remove Access?"
        tvMessage.text = "Are you sure you want to remove access for $email? They will no longer see this shared folder."
        btnConfirm.text = "Remove"

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            listener?.onRemoveAccess(email)
            dialog.dismiss()
            setupSharedUsersList() // Refresh UI
            Toast.makeText(context, "Access revoked", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    // --- Internal Adapter for Shared Users ---
    private inner class SharedUserAdapter(
        private val users: List<SharedUser>,
        private val onRemove: (String) -> Unit
    ) : RecyclerView.Adapter<SharedUserAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val ivPic: ShapeableImageView = v.findViewById(R.id.iv_user_pic)
            val tvName: TextView = v.findViewById(R.id.tv_user_name)
            val tvEmail: TextView = v.findViewById(R.id.tv_user_email)
            val btnRemove: ImageButton = v.findViewById(R.id.btn_remove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_shared_user, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            holder.tvName.text = "${user.firstName} ${user.lastName}"
            holder.tvEmail.text = user.email

            if (!user.imageUri.isNullOrEmpty()) {
                try {
                    holder.ivPic.setImageURI(Uri.parse(user.imageUri))
                } catch (e: Exception) {
                    holder.ivPic.setImageResource(R.drawable.ic_file_empty)
                }
            } else {
                holder.ivPic.setImageResource(R.drawable.ic_file_empty)
            }

            holder.btnRemove.setOnClickListener { onRemove(user.email) }
        }

        override fun getItemCount() = users.size
    }

    private fun addChip(email: String) {
        val chip = Chip(context)
        chip.text = email
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.white)
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
        val color = if (addedEmails.isNotEmpty()) R.color.primary_green else R.color.grey_text
        btnSend.setColorFilter(ContextCompat.getColor(requireContext(), color))
        btnSend.isEnabled = addedEmails.isNotEmpty()
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
}