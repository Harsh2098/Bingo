package com.hmproductions.bingo.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.view.animation.GridLayoutAnimationController
import android.widget.TextView
import com.hmproductions.bingo.R
import com.hmproductions.bingo.adapter.GameGridRecyclerAdapter
import com.hmproductions.bingo.animations.StrikeAnimation
import com.hmproductions.bingo.data.GridCell
import com.hmproductions.bingo.ui.main.MainActivity
import com.hmproductions.bingo.utils.Constants
import kotlinx.android.synthetic.main.activity_tutorial.*
import java.util.*

class TutorialActivity : AppCompatActivity(), GameGridRecyclerAdapter.GridCellClickListener {

    private var gridRecyclerAdapter: GameGridRecyclerAdapter? = null
    private var gameGridCellList = ArrayList<GridCell>()

    private lateinit var popSound: MediaPlayer
    private lateinit var rowCompletedSound: MediaPlayer

    private val helperText = arrayOf("Complete any 5 lines to Win! \n\nTap to continue",
            "Lines can be any combination of rows, columns or the 2 diagonals\n\nTap to continue",
            "All players will have different grids. Line is considered complete irrespective of the colors of numbers in it.\n\nTap to continue.",
            "The game has just started and looks like one of the diagonals is almost complete. \n\nTap on 18 to complete it.",
            "Awesome! You completed 1 line.\n\nTap to finish")

    private var counter = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        createFixedGridArrayList()
        gridRecyclerAdapter = GameGridRecyclerAdapter(this, Constants.GRID_SIZE, gameGridCellList, this)

        with(tutorialRecyclerView) {
            adapter = gridRecyclerAdapter
            layoutManager = GridLayoutManager(this@TutorialActivity, Constants.GRID_SIZE)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(this@TutorialActivity, R.anim.game_grid_animation) as GridLayoutAnimationController
            setHasFixedSize(true)
        }

        skipTutorialTextView.setOnClickListener { _ ->
            AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setCancelable(false)
                    .setTitle("Tutorial")
                    .setMessage("Do you want to skip tutorial ?")
                    .setPositiveButton(R.string.yes) { _, _ -> startActivity(Intent(this@TutorialActivity, MainActivity::class.java)) }
                    .setNegativeButton(R.string.no) { dI, _ -> dI.dismiss() }
                    .show()
        }

        tutorialHelperTextSwitcher.setFactory {
            val textView = TextView(this@TutorialActivity)
            textView.textSize = 18F
            textView.setTextColor(ContextCompat.getColor(this@TutorialActivity, R.color.primaryTextColor))
            textView.gravity = Gravity.CENTER
            textView
        }

        tutorialHelperTextSwitcher.setOnClickListener {
            switchText()
        }

        switchText()
    }

    override fun onGridCellClick(value: Int) {
        if (value == 18 && counter == 3) {
            gameGridCellList[8].color = resources.getStringArray(R.array.colorsName)[2]
            gameGridCellList[8].isClicked = true

            gridRecyclerAdapter?.swapData(gameGridCellList, 8)

            popSound.start()
            rowCompletedSound.start()

            val animation = StrikeAnimation(tutorialStrikeView)
            animation.duration = 1000
            tutorialStrikeView.startAnimation(animation)

            counter = 4
            tutorialHelperTextSwitcher.setText(helperText[counter])
        }
    }

    private fun createFixedGridArrayList() {

        // Hardcoding AI color to green and player color to blue
        gameGridCellList.add(GridCell(25, false))
        gameGridCellList.add(GridCell(15, false))
        gameGridCellList.add(GridCell(2, false))
        gameGridCellList.add(GridCell(21, false))
        var gridItem = GridCell(12, true)
        gridItem.color = resources.getStringArray(R.array.colorsName)[1]
        gameGridCellList.add(gridItem)

        gameGridCellList.add(GridCell(11, false))
        gameGridCellList.add(GridCell(5, false))
        gameGridCellList.add(GridCell(19, false))
        gameGridCellList.add(GridCell(18, false))
        gameGridCellList.add(GridCell(14, false))

        gameGridCellList.add(GridCell(4, false))
        gameGridCellList.add(GridCell(23, false))
        gridItem = GridCell(9, true)
        gridItem.color = resources.getStringArray(R.array.colorsName)[2]
        gameGridCellList.add(gridItem)
        gameGridCellList.add(GridCell(22, false))
        gameGridCellList.add(GridCell(13, false))

        gameGridCellList.add(GridCell(6, false))
        gridItem = GridCell(3, true)
        gridItem.color = resources.getStringArray(R.array.colorsName)[1]
        gameGridCellList.add(gridItem)
        gameGridCellList.add(GridCell(24, false))
        gameGridCellList.add(GridCell(20, false))
        gameGridCellList.add(GridCell(8, false))

        gridItem = GridCell(1, true)
        gridItem.color = resources.getStringArray(R.array.colorsName)[2]
        gameGridCellList.add(gridItem)
        gameGridCellList.add(GridCell(7, false))
        gameGridCellList.add(GridCell(17, false))
        gameGridCellList.add(GridCell(16, false))
        gameGridCellList.add(GridCell(10, false))

    }

    private fun switchText() {

        if(counter == 4) {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            mainActivityIntent.putExtra(SplashActivity.SHOW_SNACKBAR_KEY, true)
            startActivity(mainActivityIntent)
            finish()
        }

        if (counter < helperText.size - 2) {
            tutorialHelperTextSwitcher.setText(helperText[++counter])
        }
    }

    override fun onStart() {
        super.onStart()
        popSound = MediaPlayer.create(this, R.raw.pop)
        rowCompletedSound = MediaPlayer.create(this, R.raw.swoosh)
    }

    override fun onStop() {
        super.onStop()
        popSound.release()
        rowCompletedSound.release()
    }
}
