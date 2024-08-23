package com.colab.myfriend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.colab.friendlist.Friend
import com.colab.myfriend.databinding.ActivityEditFriendBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class EditFriendActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var etName: EditText
    private lateinit var etSchool: EditText
    private lateinit var etBio: EditText
    private lateinit var btnPickPhoto: ImageButton
    private lateinit var btnUpdate: ImageButton
    private lateinit var binding: ActivityEditFriendBinding
    private lateinit var viewModel: FriendViewModel
    private lateinit var photoFile: File
    private var photoStr: String = ""
    private lateinit var friend: Friend

    private val cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handleCameraResult()
            } else {
                Log.e("EditFriendActivity", "Failed to capture photo")
            }
        }

    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handleGalleryResult(result.data)
            } else {
                Log.e("EditFriendActivity", "Failed to select photo from gallery")
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewComponents()
        initViewModel()
        setupListeners()

        friend = intent.getParcelableExtra("friend") ?: return
        loadFriendData(friend)
    }

    private fun initViewComponents() {
        imgProfile = binding.profileImageInedit
        etName = binding.etNameInedit
        etSchool = binding.etSchoolInedit
        etBio = binding.etBioInedit
        btnPickPhoto = binding.cameraButtonInedit
        btnUpdate = binding.saveButtonInedit

        photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Cannot create image file", Toast.LENGTH_SHORT).show()
            Log.e("EditFriendActivity", "Error occurred while creating the file", ex)
            finish()
            return
        }
    }

    private fun initViewModel() {
        val viewModelFactory = FriendVMFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[FriendViewModel::class.java]
    }

    private fun setupListeners() {
        btnPickPhoto.setOnClickListener { openGallery() }
        btnUpdate.setOnClickListener { showUpdateDialog() }
    }

    private fun loadFriendData(friend: Friend) {
        etName.setText(friend.name)
        etSchool.setText(friend.school)
        etBio.setText(friend.bio)

        friend.photo?.let {
            val photoBitmap = stringToBitmap(it)
            photoBitmap?.let { imgProfile.setImageBitmap(it) }
        }
    }

    private fun handleCameraResult() {
        val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
        imgProfile.setImageBitmap(takenImage)
        photoStr = bitmapToString(takenImage)
        Log.d("EditFriendActivity", "Photo captured and saved at: ${photoFile.absolutePath}")
    }

    private fun handleGalleryResult(data: Intent?) {
        val uri = data?.data ?: return
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r") ?: return
        val fileDescriptor = parcelFileDescriptor.fileDescriptor

        val inputStream = FileInputStream(fileDescriptor)
        val outputStream = FileOutputStream(photoFile)
        inputStream.use { it.copyTo(outputStream) }

        val selectedImage = BitmapFactory.decodeFile(photoFile.absolutePath)
        imgProfile.setImageBitmap(selectedImage)
        photoStr = bitmapToString(selectedImage)
        Log.d("EditFriendActivity", "Photo selected from gallery")

        parcelFileDescriptor.close()
    }

    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Update Friend")
            .setMessage("Are you sure you want to update this friend's details?")
            .setPositiveButton("Update") { dialog, _ ->
                val updatedFriend = friend.copy(
                    name = etName.text.toString().trim(),
                    school = etSchool.text.toString().trim(),
                    bio = etBio.text.toString().trim(),
                    photo = photoStr.ifEmpty { friend.photo }
                )

                viewModel.updateFriend(updatedFriend)
                Toast.makeText(this, "Friend details updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        builder.show()
    }


    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_", ".jpg", storageDir).apply {
            Log.d("EditFriendActivity", "Image file created: $absolutePath")
        }
    }

    private fun bitmapToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun stringToBitmap(encodedString: String): Bitmap? {
        return try {
            val byteArray = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: IllegalArgumentException) {
            Log.e("EditFriendActivity", "Error converting string to bitmap", e)
            null
        }
    }
}
