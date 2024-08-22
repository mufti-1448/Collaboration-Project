package com.colab.myfriend

import androidx.room.*
import com.colab.friendlist.Friend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {


    @Insert
    suspend fun insert(friend: Friend)

    @Query("SELECT * FROM friend")
    fun getAll(): Flow<List<Friend>>

    @Update
    suspend fun update(friend: Friend)

    @Delete
    suspend fun delete(friend: Friend)
}
