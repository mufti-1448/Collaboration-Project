package com.colab.friendlist

// Kelas data untuk merepresentasikan informasi teman
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend")
data class Friend(
    var name: String,
    var school: String,
    var bio: String,
    var photo: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
