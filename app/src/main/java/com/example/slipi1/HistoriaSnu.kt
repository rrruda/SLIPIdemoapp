package com.example.slipi1

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoriaSnu : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoriaSnuAdapter
    private lateinit var listaWpisow: MutableList<WpisSnu>
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historia_snu)
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.recyclerViewHistoria)
        recyclerView.layoutManager = LinearLayoutManager(this)
        listaWpisow = mutableListOf()
        adapter = HistoriaSnuAdapter(listaWpisow)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.backArrow).setOnClickListener { finish() }

        val userId = auth.currentUser?.uid ?: return

        firestore.collection("WpisySnu")
            .document(userId)
            .collection("Dni")
            .get()
            .addOnSuccessListener { result ->
                listaWpisow.clear()
                for (doc in result.documents) {
                    val wpis = doc.toObject(WpisSnu::class.java)
                    if (wpis != null) {
                        listaWpisow.add(wpis)
                    }
                }
                listaWpisow.sortByDescending { it.data } // sortuj malejąco po dacie
                adapter.notifyDataSetChanged()
            }

    }
}
