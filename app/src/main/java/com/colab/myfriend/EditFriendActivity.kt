package com.colab.myfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class EditFriendActivity : AppCompatActivity() {
    private lateinit var imgProfile: ImageView
    private lateinit var etName: EditText
    private lateinit var etSchool: EditText
    private lateinit var etBio: EditText
    private lateinit var btnPickPhoto: ImageButton
    private lateinit var btnUpdate: ImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_friend)

        imgProfile = findViewById(R.id.profileImage_inedit)
        etName = findViewById(R.id.etName_inedit)
        etSchool = findViewById(R.id.etSchool_inedit)
        etBio = findViewById(R.id.etBio_inedit)
        btnPickPhoto = findViewById(R.id.cameraButton_inedit)
        btnUpdate = findViewById(R.id.saveButton_inedit)

        // Load data dari database atau Intent (contoh data sementara)
        etName.setText("Hikari Pangestika")
        etSchool.setText("SMK Negeri 9 Semarang")
        etBio.setText("Gadis koleris yang suka berimajinasi, terangi harimu dengan senyuman karamelnya.")

        // Tombol untuk memilih foto
        btnPickPhoto.setOnClickListener {
            // Logika untuk mengambil gambar dari kamera atau galeri
            Toast.makeText(this, "Pick Photo clicked", Toast.LENGTH_SHORT).show()
            // Implementasi pemilihan gambar dari kamera atau galeri bisa ditambahkan di sini
        }

        // Tombol untuk mengupdate data
        btnUpdate.setOnClickListener {
            showUpdateDialog()
        }
    }

    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Friend")
        builder.setMessage("Are you sure you want to update this friend's details?")
        builder.setPositiveButton("Update") { dialog, which ->
            // Logika untuk mengupdate data ke database
            // Contoh: panggil ViewModel untuk mengupdate data
            // friendViewModel.updateFriend(Friend(etName.text.toString(), etSchool.text.toString(), etBio.text.toString()))
            Toast.makeText(this, "Friend details updated", Toast.LENGTH_SHORT).show()
            finish() // Menutup Activity setelah mengupdate

            // Jika ingin kembali ke MenuHomeActivity
             val intent = Intent(this, MenuHomeActivity::class.java)
             startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Menutup dialog jika dibatalkan
        }
        builder.create().show()
    }
}
