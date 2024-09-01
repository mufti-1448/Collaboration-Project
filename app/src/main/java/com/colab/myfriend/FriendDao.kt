package com.colab.myfriend

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    // tambahan
    @Query("SELECT * from friend WHERE id = :id")
    fun getItemById(id: Int): Flow<Friend?>

    @Insert
    suspend fun insert(friend: Friend)

    @Query("SELECT * FROM friend")
    fun getAll(): Flow<List<Friend>>

    @Update
    suspend fun update(friend: Friend)

    @Delete
    suspend fun delete(friend: Friend)
}
