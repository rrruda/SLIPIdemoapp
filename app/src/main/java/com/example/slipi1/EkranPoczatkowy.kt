package com.example.slipi1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class EkranPoczatkowy : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // Jeśli użytkownik już zalogowany, przejdź od razu do Dashboard
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, EkranGlowny::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, Logowanie::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, Rejestracja::class.java))
        }
    }
}
