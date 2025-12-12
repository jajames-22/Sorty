package com.example.sorty.ui.account

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sorty.DatabaseHelper
import com.example.sorty.CreateAccount
import com.example.sorty.databinding.FragmentAccountBinding
import android.content.Intent
import com.example.sorty.ui.account.SetupPinActivity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView
import com.example.sorty.R

class AccountFragment : Fragment() {

    private lateinit var bind: FragmentAccountBinding
    private lateinit var db: DatabaseHelper
    private lateinit var sessionManager: com.example.sorty.SessionManager // Add this

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

        // 1. Initialize Database
        db = DatabaseHelper(requireContext())
        sessionManager = com.example.sorty.SessionManager(requireContext())
        setupButtons()
    }

    private fun loadUserData() {
        // Fetch the user from the database
        val user = db.getUser()

        if (user != null) {
            // --- POPULATE ID CARD ---
            bind.tvIdName.text = "${user.firstName} ${user.lastName}"
            bind.tvIdCourse.text = user.course
            bind.tvIdSchool.text = user.school
            bind.tvIdEmail.text = user.email
            bind.tvIdBday.text = "Born: ${user.birthday}"

            // Handle Profile Picture
            if (!user.imageUri.isNullOrEmpty()) {
                try {
                    bind.ivProfilePic.setImageURI(Uri.parse(user.imageUri))
                } catch (e: Exception) {
                    // If image fails to load, keep the placeholder
                    e.printStackTrace()
                }
            }
        } else {
            // --- HANDLE EMPTY STATE ---
            bind.tvIdName.text = "No Profile Set"
            bind.tvIdCourse.text = "----"
            bind.tvIdSchool.text = "----"
        }
    }

    // ADD THIS FUNCTION
    override fun onResume() {
        super.onResume()
        // This runs every time the screen becomes visible again
        loadUserData()
    }

    private fun setupButtons() {
        // Setup Password Button
        bind.btnSetupPassword.setOnClickListener {
            val intent = Intent(requireContext(), com.example.sorty.ui.account.SetupPinActivity::class.java)
            startActivity(intent)
        }

        // Reset Account Button
        bind.btnResetAccount.setOnClickListener {
            showResetConfirmationDialog() // <--- Check this line
        }



        // EDIT ICON NAVIGATION
        bind.btnEditId.setOnClickListener {
            // 1. Create the Intent
            // Make sure to import android.content.Intent
            val intent = android.content.Intent(requireContext(), EditProfileActivity::class.java)

            // 2. Start the Activity
            startActivity(intent)
        }
    }

    private fun showResetConfirmationDialog() {
        // DEBUG: Uncomment this if you suspect the button isn't clicking
        // Toast.makeText(requireContext(), "Opening Dialog...", Toast.LENGTH_SHORT).show()

        try {
            // 1. Inflate the View
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_confirmation, null)

            // 2. Build the Dialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)

            // 3. Create and Show
            val dialog = builder.create()

            // Important: This makes the corners rounded by removing the default square white box
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

            dialog.show() // <--- MAKE SURE THIS IS CALLED

            // 4. Initialize Buttons INSIDE the dialog view
            val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btn_cancel)
            val btnConfirm = dialogView.findViewById<android.widget.Button>(R.id.btn_confirm)

            // 5. Button Logic
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                performDataReset()
                dialog.dismiss()
            }

        } catch (e: Exception) {
            // This will tell you if the XML file is missing or has an error
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error opening dialog: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun performDataReset() {
        // 1. Run in Background Thread
        Thread {
            // Delete DB Data
            val success = db.resetAccountData()

            if (isAdded) {
                requireActivity().runOnUiThread {
                    if (success) {
                        // 2. Clear Session (Shared Preferences)
                        sessionManager.logout()

                        Toast.makeText(requireContext(), "Account reset successfully.", Toast.LENGTH_SHORT).show()

                        // 3. NAVIGATE TO MAIN ACTIVITY (The Change)
                        // Make sure to import com.example.sorty.MainActivity at the top
                        val intent = Intent(requireContext(), com.example.sorty.MainActivity::class.java)

                        // Clear the back stack (History) so the user cannot go back
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)

                        // Optional: Kill process to ensure a completely fresh start
                        // android.os.Process.killProcess(android.os.Process.myPid())
                    } else {
                        Toast.makeText(requireContext(), "Failed to reset data.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }
}