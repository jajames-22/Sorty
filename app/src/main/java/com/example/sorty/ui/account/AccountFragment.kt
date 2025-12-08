package com.example.sorty.ui.account

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sorty.DatabaseHelper
import com.example.sorty.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var bind: FragmentAccountBinding
    private lateinit var db: DatabaseHelper

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
            Toast.makeText(requireContext(), "Setup Password Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Password Setup Screen
        }

        // Reset Account Button
        bind.btnResetAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Reset Account Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Show confirmation dialog to clear data
        }

        // Delete Account Button
        bind.btnDeleteAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Delete Account Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Show confirmation dialog to delete user
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
}