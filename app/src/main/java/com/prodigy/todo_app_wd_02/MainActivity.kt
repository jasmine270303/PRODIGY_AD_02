package com.prodigy.todo_app_wd_02

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.prodigy.todo_app_wd_02.adapter.NoteAdapter
import com.prodigy.todo_app_wd_02.model.Note
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var db:FirebaseFirestore
    private lateinit var notesList: ArrayList<Note>
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var recyclerview: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        db = Firebase.firestore
        val addnewET : EditText = findViewById(R.id.addnewET)
        val btnadd : Button = findViewById(R.id.btnadd)
        recyclerview = findViewById(R.id.recyclerview)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        notesList = arrayListOf()
////        noteAdapter = NoteAdapter(notesList)
//        val adapter = NoteAdapter(notesList) { fetchNotes() }
//        recyclerview.adapter = adapter

        notesList = arrayListOf()
        noteAdapter = NoteAdapter(
            notesList,
            onNoteDeleted = { fetchNotes() },
            onNoteEdited = { fetchNotes() }
        )
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = noteAdapter

        fetchNotes()

        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = noteAdapter

        btnadd.setOnClickListener {

            if(addnewET.text.isNotEmpty()){
                // Create a new user with a first and last name
                val note = hashMapOf(
                    "note" to addnewET.text.toString(),
                    "Timestamp" to com.google.firebase.Timestamp(Date()),
                    "completed" to false // default not completed
                )

                // Add a new document with a generated ID
                db.collection("notes")
                    .add(note)
                    .addOnSuccessListener { documentReference ->

                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

                        addnewET.text.clear() // Clear text field after adding

                        fetchNotes()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }
            else{
                Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun fetchNotes() {
        notesList.clear()
        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val docId = document.id
                    val noteText = document.getString("note") ?: ""
                    val completed = document.getBoolean("completed") ?: false// Safe access Add ?: "" (Elvis operator) to make sure if it's null, it becomes an empty string.

                    val note = Note(docId, noteText, completed)
                    notesList.add(note)


                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                noteAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
}