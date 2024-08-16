package com.colab.myfriend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.colab.friendlist.Friend
import com.colab.friendlist.FriendAdapter
import com.colab.myfriend.databinding.ActivityMenuHomeBinding

class MenuHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuHomeBinding
    private lateinit var adapter: FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root) // Mengatur tampilan menggunakan ViewBinding

        // Inisialisasi RecyclerView dengan GridLayoutManager
        adapter = FriendAdapter(getFriendsList())
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)  // Mengatur grid dengan 2 kolom
        binding.recyclerView.adapter = adapter

        // Klik listener untuk tombol tambah teman
        binding.btnAddFriend.setOnClickListener {
            // Implementasi fungsionalitas untuk menambah teman baru
        }
    }

    private fun getFriendsList(): List<Friend> {
        return listOf(
            Friend("Name1", "School1"),
            Friend("Name2", "School2"),
            Friend("Name3", "School3"),
            Friend("Name4", "School4")
        )
    }
}
