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

    private var friendList: List<Friend> = listOf()  // Menyimpan daftar semua teman untuk pemfilteran

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Menginisialisasi View Binding untuk layout activity_menu_home
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menginisialisasi adapter dengan listener untuk menangani klik item
        adapter = FriendAdapter(emptyList()) { friend ->
            // Membuat Intent untuk berpindah ke DetailFriendActivity dan mengirimkan data teman
            val intent = Intent(this, DetailFriendActivity::class.java).apply {
                putExtra("EXTRA_NAME", friend.name)
                putExtra("EXTRA_SCHOOL", friend.school)
                putExtra("EXTRA_BIO", friend.bio)
                putExtra("EXTRA_IMAGE_PATH", friend.photoPath)  // Mengirimkan path file gambar
                putExtra("EXTRA_ID", friend.id)
            }
            startActivity(intent)
        }

        // Menetapkan GridLayoutManager dengan 2 kolom pada RecyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        // Menetapkan adapter pada RecyclerView
        binding.recyclerView.adapter = adapter

        // Mengambil data teman dari ViewModel dan memperbarui adapter
        lifecycleScope.launch {
            viewModel.getFriend().collect { friends ->
                friendList = friends  // Menyimpan daftar lengkap teman
                adapter.updateData(friends)  // Memperbarui data pada adapter
            }
        }

        // Menambahkan TextWatcher untuk memfilter teman berdasarkan input pencarian
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFriends(s.toString())  // Memfilter daftar teman sesuai dengan input pencarian
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Menangani klik pada tombol "Add Friend" untuk berpindah ke AddFriendActivity
        binding.btnAddFriend.setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi untuk memfilter daftar teman berdasarkan query pencarian
    private fun filterFriends(query: String) {
        // Menyaring daftar teman berdasarkan nama yang cocok dengan query pencarian
        val filteredList = if (query.isEmpty()) {
            friendList
        } else {
            friendList.filter { friend ->
                friend.name.contains(query, ignoreCase = true)  // Memeriksa apakah nama teman cocok dengan query pencarian
            }
        }
        // Memperbarui data pada adapter dengan daftar yang telah difilter
        adapter.updateData(filteredList)
    }
}
