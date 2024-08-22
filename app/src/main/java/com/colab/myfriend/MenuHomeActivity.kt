package com.colab.myfriend

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.colab.friendlist.Friend
import com.colab.friendlist.FriendAdapter
import com.colab.myfriend.databinding.ActivityMenuHomeBinding
import kotlinx.coroutines.launch

class MenuHomeActivity : AppCompatActivity() {

    private var friendList: ArrayList<Friend> = ArrayList()
    private lateinit var binding: ActivityMenuHomeBinding
    private lateinit var Adapter: FriendAdapter
    private lateinit var viewModel: FriendViewModel
    private lateinit var adapter: AdapterRVFriend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root) // Mengatur tampilan menggunakan ViewBinding

        // Inisialisasi RecyclerView dengan GridLayoutManager
        Adapter = FriendAdapter(getFriendsList())
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)  // Mengatur grid dengan 2 kolom
        binding.recyclerView.adapter = adapter

        // Klik listener untuk tombol tambah teman
        binding.btnAddFriend.setOnClickListener {
            // Implementasi fungsionalitas untuk menambah teman baru
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getFriend().collect{ friends ->
                        Log.d("DATABASE", "Friends: $friends")
                        friendList.clear()
                        friendList.addAll(friends)
                        adapter.setData(friendList)
                    }
                }
            }
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
