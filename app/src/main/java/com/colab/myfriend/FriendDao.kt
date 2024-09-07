package com.colab.myfriend

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Interface Data Access Object (DAO) untuk entitas Friend
@Dao
interface FriendDao {

    // Mengambil item teman berdasarkan ID. Mengembalikan Flow<Friend?> untuk observasi data.
    @Query("SELECT * from friend WHERE id = :id")
    fun getItemById(id: Int): Flow<Friend?>

    // Menyisipkan entitas teman ke dalam database. Fungsi ini bersifat suspend untuk digunakan dalam coroutine.
    @Insert
    suspend fun insert(friend: Friend)

    // Mengambil semua entitas teman dari database. Mengembalikan Flow<List<Friend>> untuk observasi data.
    @Query("SELECT * FROM friend")
    fun getAll(): Flow<List<Friend>>

    // Memperbarui entitas teman yang ada dalam database. Fungsi ini bersifat suspend untuk digunakan dalam coroutine.
    @Update
    suspend fun update(friend: Friend)

    // Menghapus entitas teman dari database. Fungsi ini bersifat suspend untuk digunakan dalam coroutine.
    @Delete
    suspend fun delete(friend: Friend)
}
