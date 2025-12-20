package com.example.sorty.ui.account

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sorty.DatabaseHelper
import com.example.sorty.LoginActivity
import com.example.sorty.MainActivity
import com.example.sorty.R
import com.example.sorty.SessionManager
import com.example.sorty.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var bind: FragmentAccountBinding
    private lateinit var db: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentAccountBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val currentEmail = sessionManager.getEmail()

        if (currentEmail != null) {
            val user = db.getUserByEmail(currentEmail)
            if (user != null) {
                bind.tvIdName.text = "${user.firstName} ${user.lastName}"
                bind.tvIdCourse.text = user.course
                bind.tvIdSchool.text = user.school
                bind.tvIdEmail.text = user.email
                bind.tvIdBday.text = "Born: ${user.birthday}"

                if (!user.imageUri.isNullOrEmpty()) {
                    try {
                        bind.ivProfilePic.setImageURI(Uri.parse(user.imageUri))
                    } catch (e: Exception) {
                        bind.ivProfilePic.setImageResource(R.drawable.ic_file_empty)
                    }
                }
            } else {
                bind.tvIdName.text = "User Not Found"
            }
        }
    }

    private fun setupButtons() {
        bind.btnResetAccount.setOnClickListener { showResetConfirmationDialog() }
        bind.btnLogout.setOnClickListener { showLogoutConfirmation() }

        // Ensure these activities exist in your project


        bind.btnEditId.setOnClickListener {
            // Uncomment when EditProfileActivity is ready
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
    }

    // --- LOGOUT DIALOG ---
    private fun showLogoutConfirmation() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_confirmation, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        tvTitle.text = "Logout?"
        tvMessage.text = "Are you sure you want to log out? You will need to sign in again to access your notes."
        btnConfirm.text = "Logout"

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }
        dialog.show()
    }

    // --- RESET DATA DIALOG ---
    private fun showResetConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_confirmation, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        tvTitle.text = "Reset Account?"
        tvMessage.text = "This will permanently delete all your notes, files, and folders. This action cannot be undone."
        btnConfirm.text = "Reset"

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performDataReset()
        }
        dialog.show()
    }

    private fun performLogout() {
        sessionManager.logout()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun performDataReset() {
        val currentEmail = sessionManager.getEmail() ?: return

        Thread {
            val success = db.resetUserData(currentEmail)
            if (isAdded) {
                requireActivity().runOnUiThread {
                    if (success) {
                        sessionManager.logout()
                        Toast.makeText(requireContext(), "Your data has been reset.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "Failed to reset data.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }
}