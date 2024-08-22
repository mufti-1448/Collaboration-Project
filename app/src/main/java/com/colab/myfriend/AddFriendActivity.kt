package com.colab.myfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.colab.friendlist.Friend
import kotlinx.coroutines.launch

class AddFriendActivity : AppCompatActivity() {

    private lateinit var viewModel: FriendViewModel
    private var photoStr: String = ""



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
        imgProfile = findViewById(R.id.profileImage_inaddd)
        etName = findViewById(R.id.etName_inaddd)
        etSchool = findViewById(R.id.etSchool_inaddd)
        etBio = findViewById(R.id.etBio_inaddd)
        btnPickPhoto = findViewById(R.id.cameraButton_inaddd)
        btnSave = findViewById(R.id.saveButton_inaddd)

        // Tombol untuk memilih foto
        btnPickPhoto.setOnClickListener {
            // Logika untuk mengambil gambar dari kamera atau galeri
            Toast.makeText(this, "Pick Photo clicked", Toast.LENGTH_SHORT).show()
            // Implementasi pemilihan gambar dari kamera atau galeri bisa ditambahkan di sini
        }

        // Tombol untuk menyimpan data
        btnSave.setOnClickListener {
            showSaveDialog()
        }

        val id = intent.getIntExtra("id", 0)

        if(id != 0) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        viewModel.getFriendById(id).collect{ friends ->
                            Log.d("DATABASE", "Friends: $friends")

//                            MENIT 59
                        }
                    }
                }
            }
        }
    }

    private fun addData() {
        val name = etName.text.toString().trim()
        val school = etSchool.text.toString().trim()

        if (name.isEmpty() || school.isEmpty() ) {
            Toast.makeText(this, "Please fill the blank form", Toast.LENGTH_SHORT).show()
            return
        }
        val data = Friend(name, school, photoStr )
        lifecycleScope.launch {
            viewModel.insertFriend(data)

        }
        finish()
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Friend")
        builder.setMessage("Are you sure you want to save this friend's details?")
        builder.setPositiveButton("Save") { dialog, which ->
            // Logika untuk menyimpan data ke database
            // Contoh: panggil ViewModel untuk menyimpan data
            // friendViewModel.addFriend(Friend(etName.text.toString(), etSchool.text.toString(), etBio.text.toString()))
            Toast.makeText(this, "Friend saved", Toast.LENGTH_SHORT).show()
//            finish() // Menutup Activity setelah menyimpan

            // Jika ingin kembali ke MenuHomeActivity
            val intent = Intent(this, MenuHomeActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Menutup dialog jika dibatalkan
        }
        builder.create().show()
    }
    fun stringToBitmap(encodedString: String): Bitmap? {
        return try {
            val byteArray = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
}

