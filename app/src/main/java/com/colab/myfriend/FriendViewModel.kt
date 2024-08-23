package com.colab.myfriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colab.friendlist.Friend
import kotlinx.coroutines.launch

class FriendViewModel(private val friendDao: FriendDao) : ViewModel() {

    fun getFriend() = friendDao.getAll()

    fun getFriendById(id: Int) = friendDao.getAll()

    fun insertFriend(data: Friend) {
        viewModelScope.launch {
            friendDao.insert(data)
        }
    }

    fun updateFriend(data: Friend) {
        viewModelScope.launch {
            friendDao.update(data)
        }
    }

    fun deleteFriend(data: Friend) {
        viewModelScope.launch {
            friendDao.delete(data)
        }
    }
}
