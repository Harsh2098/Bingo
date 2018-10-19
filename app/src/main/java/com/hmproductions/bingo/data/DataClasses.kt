package com.hmproductions.bingo.data

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.hmproductions.bingo.utils.TimeLimitUtils
import kotlinx.android.parcel.Parcelize

data class GridCell(val value: Int, var isClicked: Boolean) {
    var color: String? = null
}

@Parcelize
class Player(val name: String, val color: String, var id: Int, val isReady: Boolean) : Parcelable

@Parcelize
class LeaderboardPlayer(val name: String, val color: String, val winCount: Int) : Parcelable

data class Room(val roomId: Int, val count: Int, val maxSize: Int, val name: String, val timeLimit: TimeLimitUtils.TIME_LIMIT, val passwordExists: Boolean)

data class Message(val content: String, val author: String, val timeStamp: String, val color: String) {
    constructor() : this("", "", "", "#FFFFFF")
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}