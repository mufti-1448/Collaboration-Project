package com.colab.friendlist

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend")
data class Friend(
    var name: String,
    var school: String,
    var bio: String,
    var photo: String = ""
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
        id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(school)
        parcel.writeString(bio)
        parcel.writeString(photo)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Friend> {
        override fun createFromParcel(parcel: Parcel): Friend {
            return Friend(parcel)
        }

        override fun newArray(size: Int): Array<Friend?> {
            return arrayOfNulls(size)
        }
    }
}
