package com.colab.myfriend

// FriendVMFactory.kt
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class FriendVMFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FriendViewModel(MyDatabase.getInstance(context).friendDao()) as T
    }
}
