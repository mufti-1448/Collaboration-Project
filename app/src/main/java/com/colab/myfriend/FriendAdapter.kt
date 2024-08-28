    package com.colab.myfriend

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.colab.friendlist.Friend

    class FriendAdapter(
        private var friends: List<Friend>, // Ubah ke var agar bisa diubah saat update
        private val onItemClick: (Friend) -> Unit
    ) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_friend, parent, false)
            return FriendViewHolder(view)
        }

        override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
            val friend = friends[position]
            holder.bind(friend, onItemClick)
        }

        override fun getItemCount(): Int = friends.size

        // Fungsi untuk memperbarui data dalam adapter
        fun updateData(newFriends: List<Friend>) {
            friends = newFriends
            notifyDataSetChanged() // Pemberitahuan bahwa data telah berubah
        }

        class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tv_friend_name)
            private val tvSchool: TextView = itemView.findViewById(R.id.tv_friend_school)

            fun bind(friend: Friend, onItemClick: (Friend) -> Unit) {
                tvName.text = friend.name
                tvSchool.text = friend.school
                // Mengatur listener klik pada item
                itemView.setOnClickListener { onItemClick(friend) }
            }
        }
    }
