package com.colab.myfriend

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Menggunakan entitas Friend dan versi 3 dari skema database
@Database(entities = [Friend::class], version = 3)
abstract class MyDatabase : RoomDatabase() {

    // Deklarasi abstract function untuk DAO (Data Access Object)
    abstract fun friendDao(): FriendDao

    companion object {
        // Variabel INSTANCE digunakan untuk menyimpan instance dari MyDatabase
        @Volatile
        private var INSTANCE: MyDatabase? = null

        // Fungsi untuk mendapatkan instance database
        fun getInstance(context: Context): MyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "my_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
