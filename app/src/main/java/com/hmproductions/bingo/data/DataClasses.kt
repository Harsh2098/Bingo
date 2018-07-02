package com.hmproductions.bingo.data

import android.os.Parcelable
import com.hmproductions.bingo.utils.TimeLimitUtils
import kotlinx.android.parcel.Parcelize

data class ClickCellRequest(val roomId: Int, val playerId: Int, val cellClicked: Int)

data class GridCell(val value: Int, var isClicked: Boolean) {
    var color: String? = null
}

@Parcelize
class Player(val name: String, val color: String, var id: Int, val isReady: Boolean) : Parcelable

@Parcelize
class LeaderboardPlayer(val name: String, val color: String, val winCount: Int) : Parcelable

data class Room(val roomId: Int, val count: Int, val maxSize: Int, val name: String, val timeLimit: TimeLimitUtils.TIME_LIMIT)
