package com.colab.myfriend

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.colab.myfriend.databinding.ActivityMenuHomeBinding
import kotlinx.coroutines.launch

class MenuHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuHomeBinding
    private lateinit var adapter: FriendAdapter
    private val viewModel: FriendViewModel by viewModels {
        FriendVMFactory(applicationContext)
    }

    private var friendList: List<Friend> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menginisialisasi View Binding untuk layout activity_menu_home
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menginisialisasi adapter dengan listener untuk menangani klik item
        adapter = FriendAdapter(emptyList()) { friend ->
            val intent = Intent(this, DetailFriendActivity::class.java).apply {
                putExtra("EXTRA_NAME", friend.name)
                putExtra("EXTRA_SCHOOL", friend.school)
                putExtra("EXTRA_BIO", friend.bio)
                putExtra("EXTRA_IMAGE_PATH", friend.photoPath)
                putExtra("EXTRA_ID", friend.id)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        // Mengambil data teman dari ViewModel dan memperbarui adapter
        lifecycleScope.launch {
            viewModel.getFriend().collect { friends ->
                friendList = friends
                adapter.updateData(friends)
            }
        }

        // Menambahkan TextWatcher untuk memfilter teman berdasarkan input pencarian
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFriends(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnAddFriend.setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi untuk memfilter daftar teman berdasarkan query pencarian
    private fun filterFriends(query: String) {
        val filteredList = if (query.isEmpty()) {
            friendList
        } else {
            friendList.filter { friend ->
                friend.name.contains(query, ignoreCase = true)
                friend.school.contains(query, ignoreCase = true )
            }
        }
        adapter.updateData(filteredList)
    }
}
