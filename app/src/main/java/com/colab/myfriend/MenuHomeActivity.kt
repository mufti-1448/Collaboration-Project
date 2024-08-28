package com.colab.myfriend

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.colab.friendlist.Friend
import com.colab.myfriend.databinding.ActivityMenuHomeBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MenuHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuHomeBinding
    private lateinit var adapter: FriendAdapter

    // Initialize the ViewModel using the 'by viewModels' delegate
    private val viewModel: FriendViewModel by viewModels {
        FriendVMFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the adapter with an empty list
        adapter = FriendAdapter(emptyList()) { friend ->
            // Handle item click, e.g., start a new activity
            val intent = Intent(this, DetailFriendActivity::class.java).apply {
                putExtra("EXTRA_NAME", friend.name)
                putExtra("EXTRA_SCHOOL", friend.school)
            }
            startActivity(intent)
        }

        // Set up the RecyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        // Collect the Flow from the ViewModel
        lifecycleScope.launch {
            viewModel.getFriend().collect { friends ->
                // Update adapter's data whenever the list changes
                adapter.updateData(friends)
            }
        }

        // Handle add friend button click
        binding.btnAddFriend.setOnClickListener {
            val intent = Intent(this, AddFriendActivity::class.java)
            startActivity(intent)
        }
    }
}
