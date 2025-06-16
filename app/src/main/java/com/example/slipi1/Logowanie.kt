package com.example.slipi1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class Logowanie : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var googleLoginButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicjalizacja widoków
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        googleLoginButton = findViewById(R.id.googleLoginButton)

        // Konfiguracja Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Logowanie e-mail/hasło
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Uzupełnij email i hasło", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    zapiszUzytkownikaDoFirestore(it.user, "email")
                    startActivity(Intent(this, EkranGlowny::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Błąd logowania: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Logowanie przez Google
        googleLoginButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Blad Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                zapiszUzytkownikaDoFirestore(it.user, "google")
                startActivity(Intent(this, EkranGlowny::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd Firebase: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun zapiszUzytkownikaDoFirestore(user: FirebaseUser?, metoda: String) {
        if (user == null) return

        val uzytkownik = hashMapOf(
            "email" to user.email,
            "uid" to user.uid,
            "metoda" to metoda
        )

        val ref = firestore.collection("Uzytkownicy").document(user.uid)

        ref.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                ref.set(uzytkownik)
            }
        }
    }
}


