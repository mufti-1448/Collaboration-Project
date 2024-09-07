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

// Adapter untuk RecyclerView yang menampilkan daftar teman
class FriendAdapter(
    private var friendList: List<Friend>, // Daftar teman yang ditampilkan di RecyclerView
    private val onItemClick: (Friend) -> Unit // Callback untuk menangani klik item
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    // Membuat ViewHolder untuk item yang ditampilkan
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_friend, parent, false)
        return FriendViewHolder(view)
    }

    // Mengikat data teman ke ViewHolder pada posisi tertentu
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendList[position]
        holder.bind(friend, onItemClick)
    }

    // Mengembalikan jumlah item dalam daftar teman
    override fun getItemCount(): Int = friendList.size

    // Memperbarui data teman dalam adapter dan memberi tahu RecyclerView untuk melakukan refresh
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFriends: List<Friend>) {
        friendList = newFriends
        notifyDataSetChanged()
    }

    // ViewHolder untuk item teman dalam RecyclerView
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_friend_name) // TextView untuk nama teman
        private val schoolTextView: TextView = itemView.findViewById(R.id.tv_friend_school) // TextView untuk nama sekolah teman
        private val bioTextView: TextView = itemView.findViewById(R.id.tv_friend_bio) // TextView untuk biografi teman
        private val profileImageView: ImageView = itemView.findViewById(R.id.img_friend) // ImageView untuk foto profil teman

        // Mengikat data teman ke ViewHolder dan menangani klik item
        fun bind(friend: Friend, onItemClick: (Friend) -> Unit) {
            nameTextView.text = friend.name // Menetapkan nama teman
            schoolTextView.text = friend.school // Menetapkan nama sekolah teman
            bioTextView.text = friend.bio // Menetapkan biografi teman

            // Menetapkan gambar profil dari path file jika tersedia, jika tidak, gunakan placeholder
            if (friend.photoPath?.isNotEmpty() == true) {
                val imgFile = File(friend.photoPath!!)
                if (imgFile.exists()) {
                    val rotatedBitmap = rotateImageIfRequired(imgFile) // Memutar gambar jika diperlukan
                    profileImageView.setImageBitmap(rotatedBitmap) // Menetapkan gambar ke ImageView
                } else {
                    profileImageView.setImageResource(R.drawable.ic_profile_placeholder) // Menetapkan placeholder jika file tidak ada
                }
            } else {
                profileImageView.setImageResource(R.drawable.ic_profile_placeholder) // Menetapkan placeholder jika tidak ada path foto
            }

            // Menangani klik item dengan memanggil callback
            itemView.setOnClickListener {
                onItemClick(friend)
            }
        }

        // Memutar gambar jika orientasi EXIF memerlukannya
        private fun rotateImageIfRequired(imgFile: File): Bitmap? {
            val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath) // Mengambil bitmap dari file
            try {
                val exif = ExifInterface(imgFile.absolutePath) // Mengambil metadata EXIF dari file gambar
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) // Mendapatkan orientasi gambar

                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f) // Memutar gambar 90 derajat
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f) // Memutar gambar 180 derajat
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f) // Memutar gambar 270 derajat
                    else -> bitmap // Tidak perlu memutar gambar
                }
            } catch (e: IOException) {
                e.printStackTrace() // Menangani exception jika terjadi kesalahan saat membaca EXIF
            }
            return bitmap // Mengembalikan bitmap yang tidak diputar jika terjadi kesalahan
        }

        // Memutar bitmap sesuai dengan sudut yang diberikan
        private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
            val matrix = Matrix().apply { postRotate(degrees) } // Membuat matrix untuk rotasi
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) // Menghasilkan bitmap yang diputar
        }
    }
}
