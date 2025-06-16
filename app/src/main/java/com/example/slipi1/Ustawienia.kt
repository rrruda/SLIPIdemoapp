package com.example.slipi1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Ustawienia : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val scheduleButton = findViewById<Button>(R.id.scheduleButton)
        val notificationsButton = findViewById<Button>(R.id.notificationsButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)

        backArrow.setOnClickListener {
            finish()
        }

        scheduleButton.setOnClickListener {
            startActivity(Intent(this, Harmonogram::class.java))
        }

        notificationsButton.setOnClickListener {
            val intent = Intent(this, Powiadomienia::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, EkranPoczatkowy::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        deleteAccountButton.setOnClickListener {
            pokazDialogUsuwania()
        }
    }

    private fun pokazDialogUsuwania() {
        AlertDialog.Builder(this)
            .setTitle("Usuwanie konta")
            .setMessage("Czy na pewno chcesz usunąć swoje konto? Tej operacji nie można cofnąć.")
            .setPositiveButton("Usuń") { _, _ -> usunKonto() }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun usunKonto() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (user != null && userId != null) {
            val ref = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("Harmonogram").child(userId)
            ref.removeValue()

            user.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Konto usunięte", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EkranPoczatkowy::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Błąd: ${it.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Brak zalogowanego użytkownika", Toast.LENGTH_SHORT).show()
        }
    }
}
