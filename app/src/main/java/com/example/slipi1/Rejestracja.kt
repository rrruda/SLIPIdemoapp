package com.example.slipi1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Rejestracja : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rejestracja)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val repeatPasswordEditText = findViewById<EditText>(R.id.repeatPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val backArrow = findViewById<ImageView>(R.id.backArrow)

        backArrow.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repeatPassword) {
                Toast.makeText(this, "Hasła się nie zgadzają", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase rejestracja
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            zapiszUzytkownikaDoFirestore(user)
                        }
                        Toast.makeText(this, "Rejestracja zakończona sukcesem!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, EkranGlowny::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Błąd: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun zapiszUzytkownikaDoFirestore(user: FirebaseUser) {
        val dane = hashMapOf(
            "email" to user.email,
            "uid" to user.uid,
            "metoda" to "email"
        )

        val ref = firestore.collection("Uzytkownicy").document(user.uid)

        ref.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                ref.set(dane)
            }
        }
    }
}
