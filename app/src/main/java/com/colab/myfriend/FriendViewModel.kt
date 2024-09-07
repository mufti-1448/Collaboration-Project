package com.colab.myfriend

import androidx.lifecycle.ViewModel

// ViewModel untuk mengelola data teman dengan menggunakan FriendDao
class FriendViewModel(private val friendDao: FriendDao) : ViewModel() {

    // Mengambil semua data teman dari database melalui FriendDao
    fun getFriend() = friendDao.getAll()

    // Mengambil data teman berdasarkan ID dari database melalui FriendDao
    fun getFriendById(id: Int) = friendDao.getItemById(id)

    // Menyisipkan data teman baru ke dalam database
    // Fungsi ini bersifat suspend dan harus dipanggil dari dalam coroutine
    suspend fun insertFriend(data: Friend) {
        friendDao.insert(data)
    }

    // Memperbarui data teman yang ada dalam database
    // Fungsi ini bersifat suspend dan harus dipanggil dari dalam coroutine
    suspend fun editFriend(data: Friend) {
        friendDao.update(data)
    }

    // Menghapus data teman dari database
    // Fungsi ini bersifat suspend dan harus dipanggil dari dalam coroutine
    suspend fun deleteFriend(data: Friend) {
        friendDao.delete(data)
    }
}
