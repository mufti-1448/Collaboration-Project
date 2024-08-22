package com.colab.myfriend

// FriendViewModel.kt

import androidx.lifecycle.ViewModel
import com.colab.friendlist.Friend

class FriendViewModel(private val friendDao: FriendDao) : ViewModel() {

    fun getFriend() = friendDao.getAll()

    fun getFriendById(id:Int) = friendDao.getAll()

    suspend fun insertFriend(data: Friend) {
        friendDao.insert(data)
    }
}
