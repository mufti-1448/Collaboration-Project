package com.colab.myfriend

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.colab.friendlist.Friend
import com.colab.myfriend.databinding.ActivityMenuHomeBinding

class MenuHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuHomeBinding
    private lateinit var adapter: FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi RecyclerView dengan GridLayoutManager
        adapter = FriendAdapter(getFriendsList()) { friend ->
            // Kirim data ke DetailFriendActivity
            val intent = Intent(this, DetailFriendActivity::class.java).apply {
                putExtra("EXTRA_NAME", friend.name)
                putExtra("EXTRA_SCHOOL", friend.school)
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        // Klik listener untuk tombol tambah teman
        binding.btnAddFriend.setOnClickListener {
            // Implementasi fungsionalitas untuk menambah teman baru
        }
    }

    private fun getFriendsList(): List<Friend> {
        return listOf(
            Friend("Name1", "School1", "Bio1"),
            Friend("Name2", "School2", "Bio2"),
            Friend("Name3", "School3", "Bio3"),
            Friend("Name4", "School4", "Bio4")
        )
    }
}
