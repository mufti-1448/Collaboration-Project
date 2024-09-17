package com.colab.myfriend


import androidx.room.Entity
import androidx.room.PrimaryKey

// Anotasi @Entity digunakan untuk menandai bahwa kelas ini adalah entitas database Room
@Entity(tableName = "friend")
data class Friend(
    var name: String,
    var school: String,
    var bio: String,
    var photoPath: String?
) {
    // Anotasi @PrimaryKey menandai bahwa properti ini adalah primary key untuk tabel
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
