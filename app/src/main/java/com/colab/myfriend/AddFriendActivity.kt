package com.colab.myfriend

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.colab.myfriend.databinding.ActivityAddFriendBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class AddFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFriendBinding // Menghubungkan view binding untuk mengakses elemen UI pada layout XML
    private lateinit var viewModel: FriendViewModel // View model yang mengelola data teman
    private lateinit var photoFile: File // File tempat penyimpanan foto
    private var oldFriend: Friend? = null // Teman lama (digunakan saat mengedit data)
    private var idFriend: Int = 0 // ID teman yang akan di-edit
    private var isImageChanged = false // Variabel untuk memeriksa apakah gambar telah diubah

    // Launcher untuk meminta izin penggunaan kamera
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePhoto() // Jika izin kamera diberikan, ambil foto
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher untuk meminta izin akses penyimpanan (galeri)
    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery() // Jika izin penyimpanan diberikan, buka galeri
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher untuk mengambil gambar dengan kamera
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val rotatedImage = rotateImageIfRequired(photoFile.absolutePath) // Rotasi gambar jika diperlukan
                binding.profileImage.setImageBitmap(rotatedImage) // Tampilkan gambar di ImageView
                isImageChanged = true // Setel gambar telah diubah
            }
        }

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(
                    it.data?.data ?: return@registerForActivityResult, "r"
                )
                val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val outputStream = FileOutputStream(photoFile)

                // Salin gambar dari galeri ke file foto yang telah dibuat
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                parcelFileDescriptor?.close()

                val rotatedImage = rotateImageIfRequired(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(rotatedImage)
                isImageChanged = true // Setel gambar telah diubah
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan ViewBinding untuk menghubungkan UI
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Konfigurasi tampilan edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Membuat file untuk menyimpan foto
        photoFile = try {
            createImageFile() // Fungsi untuk membuat file gambar
        } catch (ex: IOException) {
            Toast.makeText(this, "Cannot create Image File", Toast.LENGTH_SHORT).show()
            return
        }

        // Mengambil ID teman dari Intent
        idFriend = intent.getIntExtra("id", 0)

        // Inisialisasi ViewModel
        viewModel = ViewModelProvider(this, FriendVMFactory(this))[FriendViewModel::class.java]

        // Jika ID teman tidak nol, ambil data teman untuk di-edit
        if (idFriend != 0) {
            getFriend()
        }

        // Tombol simpan di klik
        binding.saveButton.setOnClickListener {
            showSaveDialog() // Tampilkan dialog konfirmasi simpan
        }

        // Tombol kembali di klik
        binding.backButton.setOnClickListener {
            val destination = Intent(this, MenuHomeActivity::class.java)
            startActivity(destination)
        }

        // Tombol kamera di klik
        binding.cameraButton.setOnClickListener {
            showInsertPhotoDialog() // Tampilkan dialog untuk memilih foto
        }
    }

    // Menampilkan dialog untuk memilih foto dari kamera atau galeri
    private fun showInsertPhotoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_photo, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val alertDialog = dialogBuilder.create()

        val fromCamera = dialogView.findViewById<TextView>(R.id.from_camera)
        val pickGallery = dialogView.findViewById<TextView>(R.id.pick_gallery)

        // Pilih dari kamera
        fromCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            alertDialog.dismiss()
        }

        // Pilih dari galeri
        pickGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    // Fungsi untuk mengambil foto menggunakan kamera
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

    // Fungsi untuk membuka galeri
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    // Mendapatkan data teman berdasarkan ID
    private fun getFriend() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getFriendById(idFriend).collect { friend ->
                    oldFriend = friend
                    binding.etName.setText(friend?.name)
                    binding.etSchool.setText(friend?.school)
                    binding.etBio.setText(friend?.bio)

                    // Menampilkan foto jika tersedia
                    if (friend?.photoPath?.isNotEmpty() == true) {
                        val photo = BitmapFactory.decodeFile(friend.photoPath)
                        binding.profileImage.setImageBitmap(photo)
                        isImageChanged = false // Gambar belum diubah
                    }
                }
            }
        }
    }

    // Menampilkan dialog konfirmasi simpan data
    private fun showSaveDialog() {
        if (isFormValid()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Friend")
            builder.setMessage("Are you sure you want to add this friend?")
            builder.setPositiveButton("Save") { _, _ ->
                addData() // Menyimpan data
                Toast.makeText(this, "Friend saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    // Memvalidasi form
    private fun isFormValid(): Boolean {
        val name = binding.etName.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        return when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please fill in the name", Toast.LENGTH_SHORT).show()
                false
            }
            school.isEmpty() -> {
                Toast.makeText(this, "Please fill in the school", Toast.LENGTH_SHORT).show()
                false
            }
            bio.isEmpty() -> {
                Toast.makeText(this, "Please fill in the bio", Toast.LENGTH_SHORT).show()
                false
            }
            !isImageChanged -> {
                Toast.makeText(this, "Please change the image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    // Menambah atau mengedit data teman
    private fun addData() {
        val name = binding.etName.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        // Jika data teman lama kosong, tambahkan teman baru
        if (oldFriend == null) {
            val data = Friend(name, school, bio, photoFile.absolutePath) // Save new friend with photoPath
            lifecycleScope.launch {
                viewModel.insertFriend(data)
            }
        } else {
            // Edit data teman yang sudah ada
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

    // Membuat file untuk menyimpan gambar
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_", ".jpg", storageDir)
    }

    // Fungsi untuk merotasi gambar jika diperlukan
    private fun rotateImageIfRequired(imagePath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val ei = ExifInterface(imagePath)
        val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
    }

    // Fungsi untuk merotasi bitmap gambar
    private fun rotateImage(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
