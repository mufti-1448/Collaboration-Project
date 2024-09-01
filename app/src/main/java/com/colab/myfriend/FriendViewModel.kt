package com.colab.myfriend

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.colab.friendlist.Friend
import kotlinx.coroutines.flow.Flow


class FriendViewModel(private val friendDao: FriendDao) : ViewModel() {

    fun getFriends(): Flow<List<Friend>> = friendDao.getAll()

    fun getFriend() = friendDao.getAll()

    fun getFriendById(id: Int) = friendDao.getItemById(id)

    suspend fun insertFriend(data: Friend) {
        friendDao.insert(data)
    }

    suspend fun editFriend(data: Friend) {
        friendDao.update(data)
    }

    suspend fun deleteFriend(data: Friend) {
        friendDao.delete(data)
    }
}