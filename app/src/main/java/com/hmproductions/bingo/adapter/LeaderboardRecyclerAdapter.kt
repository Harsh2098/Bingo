package com.hmproductions.bingo.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.hmproductions.bingo.R
import com.hmproductions.bingo.data.LeaderboardPlayer
import kotlinx.android.synthetic.main.leaderboard_list_item.view.*

import java.util.ArrayList
import java.util.Arrays

class LeaderboardRecyclerAdapter(private val context: Context, private val leaderBoardArrayList: ArrayList<LeaderboardPlayer>) : RecyclerView.Adapter<LeaderboardRecyclerAdapter.LeaderBoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LeaderBoardViewHolder(LayoutInflater.from(context).inflate(R.layout.leaderboard_list_item, parent, false))

    override fun onBindViewHolder(holder: LeaderBoardViewHolder, position: Int) {

        val currentPlayer = leaderBoardArrayList[position]
        val colorPosition = Arrays.asList(*context.resources.getStringArray(R.array.colorsName)).indexOf(currentPlayer.color)

        with(holder.itemView) {
            playerNameTextView.text = currentPlayer.name
            winCountTextView.text = "${currentPlayer.winCount}"
            val backgroundDrawable = colorView.background as GradientDrawable

            backgroundDrawable.setStroke(2, Color.parseColor(context.resources.getStringArray(R.array.colorsRim)[colorPosition]))
            backgroundDrawable.setColor(Color.parseColor(context.resources.getStringArray(R.array.colorsHex)[colorPosition]))
        }
    }

    override fun getItemCount() = if (leaderBoardArrayList.size == 0) 0 else leaderBoardArrayList.size

    class LeaderBoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}