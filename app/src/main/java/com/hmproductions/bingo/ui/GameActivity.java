package com.hmproductions.bingo.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.data.GridCell;
import com.hmproductions.bingo.utils.GameGridRecyclerAdapter;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameActivity extends AppCompatActivity implements GameGridRecyclerAdapter.GridCellClickListener {

    private static final int GRID_SIZE = 5;

    @BindView(R.id.game_recyclerView)
    RecyclerView game_recyclerView;

    @BindView(R.id.B_textView)
    TextView B;

    @BindView(R.id.I_textView)
    TextView I;

    @BindView(R.id.N_textView)
    TextView N;

    @BindView(R.id.G_textView)
    TextView G;

    @BindView(R.id.O_textView)
    TextView O;

    private Button resetButton;

    private MediaPlayer celebration;
    private GameGridRecyclerAdapter mGridAdapter;

    ArrayList<GridCell> gameGridCellList = new ArrayList<>();
    private boolean isGameStarted = false;

    /*
     *  cellsClicked[][] keeps track of the grid cells clicked by the user
     *  mData holds the list of GridCells
     *  lastClickPosition holds the X,Y position of recently clicked grid cell
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);

        BindButtonViews();

        // Creates an ArrayList made up of random values
        CreateGameGridList();

        mGridAdapter = new GameGridRecyclerAdapter(this, GRID_SIZE, gameGridCellList, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_SIZE);

        game_recyclerView.setLayoutManager(layoutManager);
        game_recyclerView.setAdapter(mGridAdapter);
        game_recyclerView.setHasFixedSize(true);

        celebration = MediaPlayer.create(this, R.raw.tada_celebration);
    }

    private void BindButtonViews() {

        resetButton = findViewById(R.id.resetButton);

        GradientDrawable resetGradientdrawable = (GradientDrawable) resetButton.getBackground();
        resetGradientdrawable.setColor(Color.parseColor("#CC0000"));
    }

    // Returns an int[GRID_SIZE][GRID_SIZE] containing numbers 1 to 25 randomly placed
    private int[][] CreateRandomGameArray() {

        int[][] randomArray = new int[GRID_SIZE][GRID_SIZE];
        int randomNumber1, randomNumber2, randomNumber3, randomNumber4, temp;

        for (int i = 0; i < GRID_SIZE; ++i)
            for (int j = 0; j < GRID_SIZE; ++j)
                randomArray[i][j] = i * GRID_SIZE + j + 1;

        // Swapping two random elements of array 100 times
        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 10; ++j) {
                // Generating 4 random numbers to swap any 2 elements in randomArray
                Random r1 = new Random();
                Random r2 = new Random();
                Random r3 = new Random();
                Random r4 = new Random();
                randomNumber1 = (r1.nextInt(GRID_SIZE));
                randomNumber2 = (r2.nextInt(GRID_SIZE));
                randomNumber3 = (r3.nextInt(GRID_SIZE));
                randomNumber4 = (r4.nextInt(GRID_SIZE));

                temp = randomArray[randomNumber1][randomNumber2];
                randomArray[randomNumber1][randomNumber2] = randomArray[randomNumber3][randomNumber4];
                randomArray[randomNumber3][randomNumber4] = temp;
            }

        return randomArray;
    }

    // Creates an ArrayList of GridCell using int[][] made by CreateRandomGameArray()
    private void CreateGameGridList() {
        int[][] randomArray = CreateRandomGameArray();

        for (int i = 0; i < GRID_SIZE; ++i) {
            for (int j = 0; j < GRID_SIZE; ++j) {
                gameGridCellList.add(new GridCell(randomArray[i][j], i, j, false));
            }
        }
    }

    // Returns the number of lines completed
    public int numberOfLinesCompleted() {

        int counter = 0;

        // Checking for columns
        for (int i = 0; i < GRID_SIZE; ++i)
            if (
                    gameGridCellList.get(i).getIsClicked() &&
                            gameGridCellList.get(GRID_SIZE + i).getIsClicked() &&
                            gameGridCellList.get(2 * GRID_SIZE + i).getIsClicked() &&
                            gameGridCellList.get(3 * GRID_SIZE + i).getIsClicked() &&
                            gameGridCellList.get(4 * GRID_SIZE + i).getIsClicked()) {
                counter++;
            }

        // Checking for rows
        for (int i = 0; i < GRID_SIZE; ++i)
            if (
                    gameGridCellList.get(i * GRID_SIZE).getIsClicked() &&
                            gameGridCellList.get(i * GRID_SIZE + 1).getIsClicked() &&
                            gameGridCellList.get(i * GRID_SIZE + 2).getIsClicked() &&
                            gameGridCellList.get(i * GRID_SIZE + 3).getIsClicked() &&
                            gameGridCellList.get(i * GRID_SIZE + 4).getIsClicked()) {
                counter++;
            }

        // Checking for diagonals
        if (gameGridCellList.get(0).getIsClicked() &&
                gameGridCellList.get(GRID_SIZE + 1).getIsClicked() &&
                gameGridCellList.get(2 * GRID_SIZE + 2).getIsClicked() &&
                gameGridCellList.get(3 * GRID_SIZE + 3).getIsClicked() &&
                gameGridCellList.get(4 * GRID_SIZE + 4).getIsClicked()) {
            counter++;

        }

        if (gameGridCellList.get(4).getIsClicked() &&
                gameGridCellList.get(GRID_SIZE + 3).getIsClicked() &&
                gameGridCellList.get(2 * GRID_SIZE + 2).getIsClicked() &&
                gameGridCellList.get(3 * GRID_SIZE + 1).getIsClicked() &&
                gameGridCellList.get(4 * GRID_SIZE).getIsClicked()) {
            counter++;
        }

        if (counter > GRID_SIZE) counter = GRID_SIZE;

        switch (counter) {
            // severe fall through
            case 5:
                O.setTextColor(Color.parseColor("#FF0000"));
            case 4:
                G.setTextColor(Color.parseColor("#FF0000"));
            case 3:
                N.setTextColor(Color.parseColor("#FF0000"));
            case 2:
                I.setTextColor(Color.parseColor("#FF0000"));
            case 1:
                B.setTextColor(Color.parseColor("#FF0000"));
        }

        return counter;
    }

    // Creates new ArrayList and swaps the data with adapter
    @OnClick(R.id.resetButton)
    void onResetButtonClick() {

        if (!isGameStarted) {
            Toast.makeText(this, "Please start the game", Toast.LENGTH_SHORT).show();
        } else {
            recreate();
        }
    }

    @Override
    public void onGridCellClick(int position, View view) {

        /* Updating cellsClicked[][] */
        isGameStarted = true;

        gameGridCellList.get(
                gameGridCellList.get(position).getPositionX() * GRID_SIZE +
                gameGridCellList.get(position).getPositionY()).setIsClicked(true);

        mGridAdapter.swapData(gameGridCellList);

        /* Checking of 5 rows are completed */
        if (numberOfLinesCompleted() == 5) {
            Toast.makeText(GameActivity.this, "CONGRATULATIONS", Toast.LENGTH_LONG).show();
            celebration.start();
            game_recyclerView.setEnabled(false);
            resetButton.setText(R.string.new_game);

            GradientDrawable resetGradientDrawable = (GradientDrawable) resetButton.getBackground();
            resetGradientDrawable.setColor(Color.parseColor("#669900"));
        }
    }

}