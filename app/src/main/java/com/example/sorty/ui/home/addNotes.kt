package com.example.sorty.ui.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sorty.databinding.ActivityAddNotesBinding // Import the generated binding class

class addNotes : AppCompatActivity() {

    // 1. Declare the binding variable
    private lateinit var bind: ActivityAddNotesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Initialize the "bind" variable
        bind = ActivityAddNotesBinding.inflate(layoutInflater)

        // 3. Set the content view to bind.root instead of R.layout...
        setContentView(bind.root)

        // 4. Use "bind.main" instead of findViewById(R.id.main)
        // (Make sure your layout XML has a view with android:id="@+id/main")
        ViewCompat.setOnApplyWindowInsetsListener(bind.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}