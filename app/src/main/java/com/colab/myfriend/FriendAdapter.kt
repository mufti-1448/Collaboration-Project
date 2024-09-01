package com.colab.myfriend

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colab.friendlist.Friend
import java.io.File

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
                val imgFile = File(friend.photoPath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    profileImageView.setImageBitmap(bitmap)
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
    }
}
