package com.colab.myfriend

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Menandakan bahwa kelas ini adalah database Room
// Menggunakan entitas Friend dan versi 3 dari skema database
@Database(entities = [Friend::class], version = 3)
abstract class MyDatabase : RoomDatabase() {

    // Deklarasi abstract function untuk DAO (Data Access Object)
    // yang memungkinkan akses ke database
    abstract fun friendDao(): FriendDao

    companion object {
        // Variabel INSTANCE digunakan untuk menyimpan instance dari MyDatabase
        // @Volatile memastikan perubahan pada INSTANCE terlihat di semua thread
        @Volatile
        private var INSTANCE: MyDatabase? = null

        // Fungsi untuk mendapatkan instance database
        // Jika INSTANCE belum diinisialisasi, buat instance baru secara thread-safe
        fun getInstance(context: Context): MyDatabase {
            // Jika INSTANCE belum ada, buat instance baru dengan synchronized block
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    // Menggunakan application context untuk menghindari memory leaks
                    context.applicationContext,
                    // Menggunakan MyDatabase class
                    MyDatabase::class.java,
                    // Menyebut nama file database sebagai "my_database"
                    "my_database"
                )
                    // Mengizinkan destructive migration ketika terjadi perubahan skema database
                    .fallbackToDestructiveMigration()
                    // Membangun instance database
                    .build()
                // Menyimpan instance yang baru dibuat ke variabel INSTANCE
                INSTANCE = instance
                // Mengembalikan instance database
                instance
            }
        }
    }
}
