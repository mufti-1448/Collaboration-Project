package com.colab.myfriend

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class AddFriendActivity : AppCompatActivity() {
    private lateinit var imgProfile: ImageView
    private lateinit var etName: EditText
    private lateinit var etSchool: EditText
    private lateinit var etBio: EditText
    private lateinit var btnPickPhoto: ImageButton
    private lateinit var btnSave: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_friend)
        imgProfile = findViewById(R.id.profileImage)
        etName = findViewById(R.id.etName)
        etSchool = findViewById(R.id.etSchool)
        etBio = findViewById(R.id.etBio)
        btnPickPhoto = findViewById(R.id.cameraButton) // Sesuaikan dengan ImageButton
        btnSave = findViewById(R.id.saveButton) // Sesuaikan dengan ImageButton

        // Tombol untuk memilih foto
        btnPickPhoto.setOnClickListener {
            // Logika untuk mengambil gambar dari kamera atau galeri
            Toast.makeText(this, "Pick Photo clicked", Toast.LENGTH_SHORT).show()
        }

        // Tombol untuk menyimpan data
        btnSave.setOnClickListener {
            showSaveDialog()
        }
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Friend")
        builder.setMessage("Are you sure you want to save this friend's details?")
        builder.setPositiveButton("Save") { dialog, which ->
            // Logika untuk menyimpan data ke database
            Toast.makeText(this, "Friend saved", Toast.LENGTH_SHORT).show()
            finish() // Menutup Activity setelah menyimpan
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Menutup dialog jika dibatalkan
        }
        builder.create().show()
    }
}
