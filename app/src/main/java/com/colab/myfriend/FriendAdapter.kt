package com.colab.myfriend

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException

class FriendAdapter(
    private var friendList: List<Friend>,
    private val onItemClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendList[position]
        holder.bind(friend, onItemClick)
    }

    override fun getItemCount(): Int = friendList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFriends: List<Friend>) {
        friendList = newFriends
        notifyDataSetChanged()
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_friend_name)
        private val schoolTextView: TextView = itemView.findViewById(R.id.tv_friend_school)
        private val bioTextView: TextView = itemView.findViewById(R.id.tv_friend_bio)
        private val profileImageView: ImageView = itemView.findViewById(R.id.img_friend)

        fun bind(friend: Friend, onItemClick: (Friend) -> Unit) {
            nameTextView.text = friend.name
            schoolTextView.text = friend.school
            bioTextView.text = friend.bio

            // Set profile image from file path if available, else use a placeholder
            if (friend.photoPath?.isNotEmpty() == true) {
                val imgFile = File(friend.photoPath!!)
                if (imgFile.exists()) {
                    val rotatedBitmap = rotateImageIfRequired(imgFile)
                    profileImageView.setImageBitmap(rotatedBitmap)
                } else {
                    profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                }
            } else {
                profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
            }

            // Handle item click
            itemView.setOnClickListener {
                onItemClick(friend)
            }
        }

        private fun rotateImageIfRequired(imgFile: File): Bitmap? {
            val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            try {
                val exif = ExifInterface(imgFile.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bitmap
        }

        private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
            val matrix = Matrix().apply { postRotate(degrees) }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }
}
