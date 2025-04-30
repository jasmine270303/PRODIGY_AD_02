package com.prodigy.todo_app_wd_02.adapter

import android.content.ContentValues.TAG
import android.graphics.Color.BLACK
import android.graphics.Color.GRAY
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.prodigy.todo_app_wd_02.R
import com.prodigy.todo_app_wd_02.model.Note

class NoteAdapter(
    private val notesList: List<Note>,
    private val onNoteDeleted: () -> Unit,
    private val onNoteEdited: () -> Unit //call back function
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTextView: TextView = itemView.findViewById(R.id.noteText)
        val edit : Button = itemView.findViewById(R.id.edit)
        val delete : Button = itemView.findViewById(R.id.delete)
        val checkBox :CheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList[position]
        holder.noteTextView.text = note.noteText

        holder.checkBox.setOnCheckedChangeListener(null) // Remove old listener
        holder.checkBox.isChecked = note.completed // Set checkbox state based on Firestore data

        // Apply strike-through and gray color if completed
        if (note.completed) {
            holder.noteTextView.paintFlags = STRIKE_THRU_TEXT_FLAG
            holder.noteTextView.setTextColor(GRAY)
        } else {
            holder.noteTextView.paintFlags = holder.noteTextView.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
            holder.noteTextView.setTextColor(BLACK)
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val noteRef = db.collection("notes").document(note.docId)

            noteRef.update("completed", isChecked)
                .addOnSuccessListener {
                    Log.d(TAG, "Completed status updated")
                    Toast.makeText(holder.itemView.context, "Status updated", Toast.LENGTH_SHORT).show()
                    onNoteEdited() // Refresh notes to reflect change
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating completed status", e)
                    Toast.makeText(holder.itemView.context, "Error updating status", Toast.LENGTH_SHORT).show()
                }
        }


        holder.edit.setOnClickListener {
            val context = holder.itemView.context
            val editText = android.widget.EditText(context)
            editText.setText(note.noteText)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Edit Note")
                .setView(editText)
                .setPositiveButton("OK") { dialogInterface, i ->
                    val updatedText = editText.text.toString()

                    val noteRef = db.collection("notes").document(note.docId)

                    noteRef
                        .update("note", updatedText)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")
                            Toast.makeText(context, "Note Updated", Toast.LENGTH_SHORT).show()
                            onNoteEdited()  // Refresh the notes
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                            Toast.makeText(context, "Error updating note", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }


        holder.delete.setOnClickListener {
            db.collection("notes").document(note.docId)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!")
                Toast.makeText(holder.itemView.context, "Note Deleted", Toast.LENGTH_SHORT).show() // 'this, cannot be used because it refers to this adapter itself
                onNoteDeleted() //to refresh recycler view after deleting
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e)
                Toast.makeText(holder.itemView.context, "Error deleting note",Toast.LENGTH_SHORT).show() // instead the item called and stored should be used
                }
        }
    }

    override fun getItemCount(): Int {
        return notesList.size
    }
}
