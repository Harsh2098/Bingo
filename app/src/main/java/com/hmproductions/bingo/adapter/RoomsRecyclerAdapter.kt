package com.hmproductions.bingo.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.hmproductions.bingo.R
import com.hmproductions.bingo.data.Room
import com.hmproductions.bingo.utils.Miscellaneous.getTimeLimitString
import com.hmproductions.bingo.utils.TimeLimitUtils
import kotlinx.android.synthetic.main.room_list_item.view.*
import java.util.ArrayList
import kotlin.Comparator

class RoomsRecyclerAdapter(private val context: Context, private var roomArrayList: ArrayList<Room>, private val listener: OnRoomItemClickListener) : RecyclerView.Adapter<RoomsRecyclerAdapter.RoomViewHolder>() {

    companion object {
        private const val HEADER_TYPE = -435
        private const val NORMAL_TYPE = -107
    }

    interface OnRoomItemClickListener {
        fun onRoomClick(view: View, position: Int)
    }

    init {
        createHeadersAndRefactorRooms()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder = RoomViewHolder(LayoutInflater.from(context).inflate(
            if (viewType == NORMAL_TYPE) R.layout.room_list_item else R.layout.room_heading_item, parent, false), listener)

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) = holder.bindRoom(roomArrayList[position])

    override fun getItemCount() = roomArrayList.size

    override fun getItemViewType(position: Int) = if (roomArrayList[position].roomId == HEADER_TYPE) HEADER_TYPE else NORMAL_TYPE

    private fun createHeadersAndRefactorRooms() {
        if (roomArrayList.size > 0) {
            roomArrayList.sortWith(Comparator { (_, countA, maxSizeA), (_, countB, maxSizeB) -> Integer.compare(maxSizeB - countB, maxSizeA - countA) })

            if (roomArrayList[0].maxSize != roomArrayList[0].count)
                roomArrayList.add(0, Room(HEADER_TYPE, 0, 0, "Available Rooms", TimeLimitUtils.TIME_LIMIT.MINUTE_1))

            var roomsFullPosition = -1
            for (room in roomArrayList)
                if (room.maxSize == room.count && room.roomId != HEADER_TYPE) {
                    roomsFullPosition = roomArrayList.indexOf(room)
                    break
                }

            if (roomsFullPosition != -1) {
                roomArrayList.add(roomsFullPosition, Room(HEADER_TYPE, 0, 0, "Full Rooms - In game", TimeLimitUtils.TIME_LIMIT.MINUTE_1))
            }
        }
    }

    class RoomViewHolder(itemView: View, private val listener: OnRoomItemClickListener) : RecyclerView.ViewHolder(itemView) {

        fun bindRoom(room: Room?) {
            itemView.roomNameTextView.text = room?.name

            if (itemViewType == NORMAL_TYPE) {
                with(itemView) {
                    countTextView.text = "${room?.count}"
                    maxCountTextView.text = "${room?.maxSize}"
                    timeLimitTextView.text = getTimeLimitString(room?.timeLimit)
                    roomIconTextView.text = room?.name?.get(0).toString().toUpperCase()

                    val displayMetrics = DisplayMetrics()
                    (this.context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

                    if(displayMetrics.widthPixels < 500) {
                        val params = roomIconTextView.layoutParams as RelativeLayout.LayoutParams
                        params.height = context.resources.getDimensionPixelSize(R.dimen.small_room_icon)
                        params.width = context.resources.getDimensionPixelSize(R.dimen.small_room_icon)
                        roomIconTextView.layoutParams = params

                        roomNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                        timeLimitTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                        roomIconTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                    }

                    roomIconTextView.setBackgroundResource(R.drawable.cell_circle_foreground)
                    (roomIconTextView.background as GradientDrawable).setColor(Color.parseColor("#EEEEEE"))
                    setOnClickListener { listener.onRoomClick(itemView, adapterPosition) }
                }
            }
        }
    }
}