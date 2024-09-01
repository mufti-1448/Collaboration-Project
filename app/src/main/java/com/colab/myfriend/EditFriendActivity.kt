package com.colab.myfriend

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.colab.friendlist.Friend
import com.colab.myfriend.databinding.ActivityEditFriendBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class EditFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditFriendBinding
    private lateinit var viewModel: FriendViewModel
    private lateinit var photoFile: File
    private var oldFriend: Friend? = null
    private var idFriend: Int = 0
    private var currentPhotoPath: String? = null // Menyimpan jalur foto saat ini

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r") ?: return@registerForActivityResult
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val outputStream = FileOutputStream(photoFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                parcelFileDescriptor.close()

                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(takenImage)
                currentPhotoPath = photoFile.absolutePath // Perbarui jalur foto dengan yang baru
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(takenImage)
                currentPhotoPath = photoFile.absolutePath // Perbarui jalur foto dengan yang baru
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Cannot create Image File", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data dari Intent
        idFriend = intent.getIntExtra("EXTRA_ID", 0)
        val name = intent.getStringExtra("EXTRA_NAME")
        val school = intent.getStringExtra("EXTRA_SCHOOL")
        val bio = intent.getStringExtra("EXTRA_BIO")
        currentPhotoPath = intent.getStringExtra("EXTRA_PHOTO_PATH") // Menyimpan jalur foto saat ini

        // Tampilkan data yang diterima
        binding.etName.setText(name)
        binding.etSchool.setText(school)
        binding.etBio.setText(bio)

        // Tampilkan gambar jika ada
        currentPhotoPath?.let {
            val photoFile = File(it)
            if (photoFile.exists()) {
                val photo = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(photo)
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
            }
        }

        val viewModelFactory = FriendVMFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendViewModel::class.java]

        if (idFriend != 0) {
            getFriendData()
        }

        binding.saveButton.setOnClickListener {
            showSaveDialog()
        }

        binding.backButton.setOnClickListener {
            navigateToDetailFriend()
        }

        binding.cameraButton.setOnClickListener {
            showInsertPhotoDialog()
        }
    }

    private fun showInsertPhotoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_photo, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val alertDialog = dialogBuilder.create()

        val fromCamera = dialogView.findViewById<TextView>(R.id.from_camera)
        val pickGallery = dialogView.findViewById<TextView>(R.id.pick_gallery)

        fromCamera.setOnClickListener {
            takePhoto()
            alertDialog.dismiss()
        }

        pickGallery.setOnClickListener {
            openGallery()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun takePhoto() {
        val photoUri = FileProvider.getUriForFile(this, "com.colab.myfriend.fileprovider", photoFile)
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

    private fun getFriendData() {
        lifecycleScope.launch {
            viewModel.getFriendById(idFriend).collect { friend ->
                oldFriend = friend
                binding.etName.setText(friend?.name)
                binding.etSchool.setText(friend?.school)
                binding.etBio.setText(friend?.bio)

                friend?.photoPath?.let { path ->
                    val photoFile = File(path)
                    if (photoFile.exists()) {
                        val photo = BitmapFactory.decodeFile(photoFile.absolutePath)
                        binding.profileImage.setImageBitmap(photo)
                        currentPhotoPath = path // Tetapkan jalur foto yang diambil dari data teman
                    } else {
                        Toast.makeText(this@EditFriendActivity, "Image file not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showSaveDialog() {
        AlertDialog.Builder(this)
            .setTitle("Edit Friend")
            .setMessage("Are you sure you want to save this friend's details?")
            .setPositiveButton("Save") { _, _ ->
                saveFriendData()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun saveFriendData() {
        val name = binding.etName.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        if (name.isEmpty() || school.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "Please fill the blank form", Toast.LENGTH_SHORT).show()
            return
        }

        // Gunakan jalur foto yang ada jika foto baru tidak dipilih
        val photoPathToSave = currentPhotoPath ?: photoFile.absolutePath

        val friendData = if (oldFriend == null) {
            Friend(name, school, bio, photoPathToSave)
        } else {
            oldFriend!!.copy(
                name = name,
                school = school,
                bio = bio,
                photoPath = photoPathToSave
            ).apply {
                id = idFriend
            }
        }

        lifecycleScope.launch {
            if (oldFriend == null) {
                viewModel.insertFriend(friendData)
            } else {
                viewModel.editFriend(friendData)
            }
            navigateToDetailFriend()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_", ".jpg", storageDir)
    }

    private fun navigateToDetailFriend() {
        val destination = Intent(this, DetailFriendActivity::class.java).apply {
            putExtra("EXTRA_ID", idFriend)
        }
        startActivity(destination)
        finish()
    }
}
