package com.colab.myfriend

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.colab.friendlist.Friend
import com.colab.myfriend.databinding.ActivityAddFriendBinding
import com.colab.myfriend.databinding.ActivityEditFriendBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class AddFriendActivity : AppCompatActivity() {

    // Variabel untuk binding ke layout XML menggunakan DataBinding
    private lateinit var binding: ActivityAddFriendBinding

    // Variabel untuk ViewModel yang akan digunakan untuk mengelola data
    private lateinit var viewModel: FriendViewModel

    // Variabel untuk file gambar yang akan diambil dari galeri
    private lateinit var photoFile: File

    // Variabel untuk menyimpan gambar dalam bentuk string
    private var photoStr: String = ""

    // Variabel untuk menyimpan data teman yang lama, digunakan saat mengedit
    private var oldFriend: Friend? = null

    // Registrasi activity result untuk mengambil gambar dari galeri
    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // Mengambil file descriptor dari gambar yang dipilih
                val parcelFileDescriptor = contentResolver.openFileDescriptor(
                    it?.data?.data
                        ?: return@registerForActivityResult, "r"
                )
                val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)

                // Menyalin file gambar ke file lokal
                val outputStream = FileOutputStream(photoFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Menutup file descriptor
                parcelFileDescriptor?.close()

                // Mengkonversi file gambar menjadi Bitmap dan menampilkannya
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.cameraButton.setImageBitmap(takenImage)

                // Mengkonversi Bitmap menjadi string base64 dan menyimpannya
                photoStr = bitmapToString(takenImage)
            }
        }

    private var cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(takenImage)
            }
        }

    // Variabel untuk menyimpan ID teman, digunakan untuk pengeditan data
    private var idFriend: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Mengaktifkan fitur Edge to Edge untuk UI
        enableEdgeToEdge()

        // Mengatur padding sesuai dengan insets dari sistem bar (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Membuat file gambar untuk menyimpan foto yang diambil dari galeri
        photoFile = try {
            creteImageFile()
        } catch (ex: IOException) {
            // Menampilkan pesan jika terjadi kesalahan saat membuat file
            Toast.makeText(this, "Cannot create Image File", Toast.LENGTH_SHORT).show()
            return
        }

        // Mengambil ID teman dari Intent, jika tersedia (digunakan untuk pengeditan)
        idFriend = intent.getIntExtra("id", 0)

        // Inisialisasi ViewModel dengan Factory
        val viewModelFactory = FriendVMFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendViewModel::class.java]

        // Jika ID teman tidak nol, berarti ini adalah pengeditan data, bukan penambahan
        if (idFriend != 0) {
            getFriend() // Mendapatkan data teman yang akan diedit
        }

        // Menangani klik tombol simpan
        binding.saveButton.setOnClickListener {
            showSaveDialog()// Menyimpan data teman
        }

        binding.backButton.setOnClickListener {
            val destination = Intent(this, MenuHomeActivity::class.java)
            startActivity(destination)
        }

        // Menangani klik gambar untuk membuka galeri
        binding.cameraButton.setOnClickListener {
            showInsertPhotoDialog()
            Toast.makeText(this, "Pick Photo clicked", Toast.LENGTH_SHORT).show()
        }

        // Menangani klik tombol hapus

    }

    private fun showInsertPhotoDialog() {
        // Inflate the custom layout for the dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_photo, null)

        // Create AlertDialog builder
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        // Create and show the dialog
        val alertDialog = dialogBuilder.create()

        // Find the TextViews in the dialog and set click listeners
        val fromCamera = dialogView.findViewById<TextView>(R.id.from_camera)
        val pickGallery = dialogView.findViewById<TextView>(R.id.pick_gallery)

        fromCamera.setOnClickListener {
            takePhoto()
            Toast.makeText(this, "From Camera selected", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        pickGallery.setOnClickListener {
            openGallery()
            Toast.makeText(this, "Pick Gallery selected", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        // Show the dialog
        alertDialog.show()
    }


    private fun takePhoto() {
        val photoUri =
            FileProvider.getUriForFile(this, "com.colab.myfriend.fileprovider", photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }

        try {
            cameraLauncher.launch(cameraIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Cannot use Camera", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // Fungsi untuk mendapatkan data teman berdasarkan ID
    private fun getFriend() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getFriendById(idFriend).collect { friend ->
                        oldFriend = friend
                        binding.etName.setText(friend?.name)
                        binding.etSchool.setText(friend?.school)
                        binding.etBio.setText(friend?.bio)

                        // Jika teman memiliki foto, konversi dari string dan tampilkan
                        if (friend?.photo?.isNotEmpty() == true) {
                            val photo = stringToBitmap(friend.photo)
                            binding.cameraButton.setImageBitmap(photo)
                        }

                    }
                }
            }
        }
    }


    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Friend")
        builder.setMessage("Are you sure you want to save this friend's details?")
        builder.setPositiveButton("Save") { dialog, which ->
            addData()
            Toast.makeText(this, "Friend saved", Toast.LENGTH_SHORT).show()
            finish() // Menutup Activity setelah menyimpan
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Menutup dialog jika dibatalkan
        }
        builder.create().show()
    }

    // Fungsi untuk menambah atau mengedit data teman
    private fun addData() {
        val name = binding.etName.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        // Validasi jika ada form yang kosong
        if (name.isEmpty() || school.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "Please fill the blank form", Toast.LENGTH_SHORT).show()
            return
        }

        if (oldFriend == null) {
            // Jika teman baru, buat objek Friend baru dan simpan ke database
            val data = Friend(name, school, bio, photoStr)
            lifecycleScope.launch {
                viewModel.insertFriend(data)
            }
        } else {
            // Jika data tidak berubah, tampilkan pesan dan kembali
            if (name == oldFriend?.name && school == oldFriend?.school && bio == oldFriend?.bio && photoStr.isEmpty()) {
                Toast.makeText(this, "Data not change", Toast.LENGTH_SHORT).show()
                return
            }

            // Buat objek Friend dengan data yang baru atau yang sudah diperbarui
            val data: Friend
            if (photoStr.isEmpty()) {
                data = oldFriend!!.copy(
                    name = name,
                    school = school,
                    bio = bio
                ).apply {
                    id = idFriend
                }
            } else {
                data = oldFriend!!.copy(
                    name = name,
                    school = school,
                    bio = bio,
                    photo = photoStr
                ).apply {
                    id = idFriend
                }
            }

            // Simpan perubahan data ke database
            lifecycleScope.launch {
                viewModel.editFriend(data)
            }
        }

        // Kembali ke aktivitas sebelumnya setelah menyimpan
        finish()
    }

    // Fungsi untuk membuka galeri dan memilih gambar
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    // Fungsi untuk membuat file gambar di direktori eksternal
    @Throws(IOException::class)
    private fun creteImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_", ".jpg", storageDir)
    }

    // Fungsi untuk mengkonversi Bitmap menjadi string base64
    fun bitmapToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Fungsi untuk mengkonversi string base64 menjadi Bitmap
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
