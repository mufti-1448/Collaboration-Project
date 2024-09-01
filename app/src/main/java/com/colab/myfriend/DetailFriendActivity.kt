package com.colab.myfriend

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.colab.myfriend.databinding.ActivityDetailFriendBinding
import kotlinx.coroutines.launch
import java.io.File
import androidx.exifinterface.media.ExifInterface

class DetailFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFriendBinding
    private lateinit var viewModel: FriendViewModel
    private var friendId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel using FriendVMFactory
        viewModel = ViewModelProvider(this, FriendVMFactory(applicationContext))[FriendViewModel::class.java]

        // Retrieve Friend ID from the Intent
        friendId = intent.getIntExtra("EXTRA_ID", 0)

        if (friendId != 0) {
            loadFriendData(friendId)
        } else {
            Log.e("DetailFriendActivity", "Invalid Friend ID received.")
            finish()  // Close activity if ID is invalid
        }

        binding.editButton.setOnClickListener {
            val destination = Intent(this, EditFriendActivity::class.java).apply {
                putExtra("EXTRA_ID", friendId)
            }
            startActivity(destination)
        }

        binding.backButton.setOnClickListener {
            val destination = Intent(this, MenuHomeActivity::class.java)
            startActivity(destination)
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        setDrawable()
    }

    private fun loadFriendData(id: Int) {
        lifecycleScope.launch {
            viewModel.getFriendById(id).collect { friend ->
                if (friend != null) {
                    binding.tvName.text = friend.name
                    binding.tvSchool.text = friend.school
                    binding.tvBio.text = friend.bio

                    if (!friend.photoPath.isNullOrEmpty()) {
                        val imgFile = File(friend.photoPath!!)
                        if (imgFile.exists()) {
                            val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            val orientedBitmap = getOrientedBitmap(imgFile.absolutePath, bitmap)
                            binding.profileImage.setImageBitmap(orientedBitmap)
                        } else {
                            binding.profileImage.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } else {
                        binding.profileImage.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {
                    Log.e("DetailFriendActivity", "No friend found with ID: $id")
                }
            }
        }
    }

    private fun getOrientedBitmap(filePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun setDrawable() {
        val personDrawable = ContextCompat.getDrawable(this, R.drawable.ic_person)
        val schoolDrawable = ContextCompat.getDrawable(this, R.drawable.ic_school)
        val infoDrawable = ContextCompat.getDrawable(this, R.drawable.ic_info)

        val scaledSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._23sdp)
        personDrawable?.setBounds(0, 0, scaledSize, scaledSize)
        schoolDrawable?.setBounds(0, 0, scaledSize, scaledSize)
        infoDrawable?.setBounds(0, 0, scaledSize, scaledSize)

        binding.tvName.setCompoundDrawablesRelative(personDrawable, null, null, null)
        binding.tvSchool.setCompoundDrawablesRelative(schoolDrawable, null, null, null)
        binding.tvBio.setCompoundDrawablesRelative(infoDrawable, null, null, null)
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove Friend")
        builder.setMessage("Are you sure you want to remove this friend?")
        builder.setPositiveButton("Remove") { _, _ ->
            deleteFriend()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteFriend() {
        if (friendId != 0) {
            lifecycleScope.launch {
                viewModel.getFriendById(friendId).collect { friend ->
                    friend?.let {
                        viewModel.deleteFriend(it)
                        // Navigate back to the home screen
                        val destination = Intent(this@DetailFriendActivity, MenuHomeActivity::class.java)
                        startActivity(destination)
                        finish()
                    } ?: run {
                        Log.e("DeleteFriend", "No friend found with ID: $friendId")
                    }
                }
            }
        } else {
            Log.e("DeleteFriend", "Invalid ID: ${this.friendId}")
        }
    }
}
