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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.colab.friendlist.Friend
import com.colab.myfriend.databinding.ActivityAddFriendBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class AddFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFriendBinding
    private lateinit var viewModel: FriendViewModel
    private lateinit var photoFile: File
    private var oldFriend: Friend? = null
    private var idFriend: Int = 0

    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(
                    it.data?.data ?: return@registerForActivityResult, "r"
                )
                val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val outputStream = FileOutputStream(photoFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                parcelFileDescriptor?.close()

                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(takenImage)
            }
        }

    private var cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(takenImage)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFriendBinding.inflate(layoutInflater)
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

        idFriend = intent.getIntExtra("id", 0)

        viewModel = ViewModelProvider(this, FriendVMFactory(this))[FriendViewModel::class.java]

        if (idFriend != 0) {
            getFriend()
        }

        binding.saveButton.setOnClickListener {
            showSaveDialog()
        }

        binding.backButton.setOnClickListener {
            val destination = Intent(this, MenuHomeActivity::class.java)
            startActivity(destination)
        }

        binding.cameraButton.setOnClickListener {
            showInsertPhotoDialog()
            Toast.makeText(this, "Pick Photo clicked", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "From Camera selected", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        pickGallery.setOnClickListener {
            openGallery()
            Toast.makeText(this, "Pick Gallery selected", Toast.LENGTH_SHORT).show()
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

    private fun getFriend() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getFriendById(idFriend).collect { friend ->
                    oldFriend = friend
                    binding.etName.setText(friend?.name)
                    binding.etSchool.setText(friend?.school)
                    binding.etBio.setText(friend?.bio)

                    if (friend?.photoPath?.isNotEmpty() == true) {
                        val photo = BitmapFactory.decodeFile(friend.photoPath)
                        binding.profileImage.setImageBitmap(photo)
                    }
                }
            }
        }
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Friend")
        builder.setMessage("Are you sure you want to add this friend?")
        builder.setPositiveButton("Save") { dialog, which ->
            addData()
            Toast.makeText(this, "Friend saved", Toast.LENGTH_SHORT).show()
            finish()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun addData() {
        val name = binding.etName.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        if (name.isEmpty() || school.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "Please fill the blank form", Toast.LENGTH_SHORT).show()
            return
        }

        if (oldFriend == null) {
            val data = Friend(name, school, bio, photoFile.absolutePath) // Save new friend with photoPath
            lifecycleScope.launch {
                viewModel.insertFriend(data)
            }
        } else {
            val data: Friend = oldFriend!!.copy(
                name = name,
                school = school,
                bio = bio,
                photoPath = photoFile.absolutePath
            ).apply {
                id = idFriend
            }

            lifecycleScope.launch {
                viewModel.editFriend(data)
            }
        }

        finish()
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
}
