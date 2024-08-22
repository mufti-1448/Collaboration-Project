package com.colab.myfriend

import com.colab.friendlist.Friend
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RvFriendAdapter(
    private val context: Context,
    private val onItemClick: (position: Int, data: Friend) -> Unit
) : RecyclerView.Adapter<RvFriendAdapter.Companion.FriendViewHolder>() {

    private var listItem = emptyList<Friend>()

    companion object {
        class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvSchool: TextView = view.findViewById(R.id.tv_school)
            val tvPhoto: ImageView = view.findViewById(R.id.img_friend)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        return FriendViewHolder(
            LayoutInflater.from(context).inflate(R.layout.activity_item_friend, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = listItem[position]

        holder.tvName.text = currentItem.name
        holder.tvSchool.text = currentItem.school
        val photoBtm = AddFriendActivity().stringToBitmap(currentItem.photo )
        photoBtm?.let {
            holder.tvPhoto.setImageBitmap(it)
        }

        holder.itemView.setOnClickListener { onItemClick(position, currentItem) }
    }

    fun setData(list: List<Friend>) {
        this.listItem = list
        notifyDataSetChanged()
    }
}
