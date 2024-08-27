package com.colab.myfriend

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FriendVMFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FriendViewModel(MyDatabase.getInstance(context).friendDao()) as T
    }
}