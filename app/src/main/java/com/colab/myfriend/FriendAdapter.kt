package com.colab.friendlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colab.myfriend.R

// Adapter untuk menghubungkan data ke RecyclerView
class FriendAdapter(private val friends: List<Friend>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    // Membuat ViewHolder baru setiap kali RecyclerView membutuhkan item baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_friend, parent, false) // Menginflate layout item_friend
        return FriendViewHolder(view)
    }

    // Menghubungkan data teman ke ViewHolder yang ditampilkan di layar
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.friendName.text = friend.name // Mengatur teks nama teman
        holder.friendSchool.text = friend.school // Mengatur teks sekolah teman
    }

    // Mengembalikan jumlah total teman yang ada di daftar
    override fun getItemCount() = friends.size

    // ViewHolder yang mewakili setiap item teman di RecyclerView
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendName: TextView = itemView.findViewById(R.id.tv_friend_name) // Inisialisasi TextView untuk nama teman
        val friendSchool: TextView = itemView.findViewById(R.id.tv_friend_school) // Inisialisasi TextView untuk sekolah teman
    }
}
