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

        // 1. Initialize Database & Session
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
                        e.printStackTrace()
                    }
                }
            } else {
                bind.tvIdName.text = "User Not Found"
            }
        } else {
            bind.tvIdName.text = "No Session Found"
        }
    }

    private fun setupButtons() {
        // Setup Password Button
        bind.btnSetupPassword.setOnClickListener {
            val intent = Intent(requireContext(), SetupPinActivity::class.java)
            startActivity(intent)
        }

        // Reset Account Button
        bind.btnResetAccount.setOnClickListener {
            showResetConfirmationDialog()
        }

        // Edit Profile Button
        bind.btnEditId.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Logout Button
        bind.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        sessionManager.logout()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showResetConfirmationDialog() {
        try {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_confirmation, null)
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                dialog.dismiss()
                // Call the safe reset function
                performDataReset()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error opening dialog: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 👇 THIS IS THE KEY FIX 👇
    private fun performDataReset() {
        // 1. Get the current user's email
        val currentEmail = sessionManager.getEmail()

        if (currentEmail.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error: No user currently logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            // 2. Pass the email to the safe delete function
            // This ensures ONLY this user's data is wiped
            val success = db.resetUserData(currentEmail)

            if (isAdded) {
                requireActivity().runOnUiThread {
                    if (success) {
                        // 3. If successful, Logout and redirect to Start
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