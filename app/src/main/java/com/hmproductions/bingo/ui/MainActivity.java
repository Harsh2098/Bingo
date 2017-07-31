package com.hmproductions.bingo.ui;

import android.graphics.Color;
import android.graphics.Typeface;
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

import rb.popview.PopField;

public class MainActivity extends AppCompatActivity implements GameGridRecyclerAdapter.GridCellClickListener {

    private static final int GRID_SIZE = 5;

    private MediaPlayer celebration;
    private RecyclerView game_recyclerView;
    private GameGridRecyclerAdapter mGridAdapter;
    private TextView B, I, N, G, O;
    private Button resetButton, undoButton;
    private View lastClickedView;
    private PopField mPopField;

    ArrayList<GridCell> mData = new ArrayList<>();
    boolean[][] cellsClicked = new boolean[GRID_SIZE][GRID_SIZE];
    int lastClickedPositionX = GRID_SIZE, lastClickedPositionY = GRID_SIZE;

    /*
     *  cellsClicked[][] keeps track of the grid cells clicked by the user
     *  mData holds the list of GridCells
     *  lastClickPosition holds the X,Y position of recently clicked grid cell
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPopField = PopField.attach2Window(this);

        // Bind all views
        BindAllViews();

        // Creates an ArrayList made up of random values
        CreateGameGridList();

        // Makes all the elements of cellsClicked[][] false
        InitialiseCellsClickedList();

        mGridAdapter = new GameGridRecyclerAdapter(this, GRID_SIZE, mData, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_SIZE);

        game_recyclerView.setLayoutManager(layoutManager);
        game_recyclerView.setAdapter(mGridAdapter);
        game_recyclerView.setHasFixedSize(true);

        celebration = MediaPlayer.create(this, R.raw.tada_celebration);

        UndoButtonClickListener();
        ResetButtonClickListener();
    }

    private void BindAllViews() {
        game_recyclerView = (RecyclerView) findViewById(R.id.game_recyclerView);
        resetButton = (Button)findViewById(R.id.resetButton);
        undoButton = (Button)findViewById(R.id.undoButton);
        B = (TextView)findViewById(R.id.B_textView);
        I = (TextView)findViewById(R.id.I_textView);
        N = (TextView)findViewById(R.id.N_textView);
        G = (TextView)findViewById(R.id.G_textView);
        O = (TextView)findViewById(R.id.O_textView);

        GradientDrawable resetGradientdrawable = (GradientDrawable)resetButton.getBackground();
        resetGradientdrawable.setColor(Color.parseColor("#CC0000"));

        GradientDrawable undoGradientdrawable = (GradientDrawable)undoButton.getBackground();
        undoGradientdrawable.setColor(Color.parseColor("#009624"));
    }

    // Creates an ArrayList of GridCell using int[][] made by CreateRandomGameArray()
    private void CreateGameGridList() {
        int[][] randomArray = CreateRandomGameArray();

        for(int i=0 ; i<GRID_SIZE ; ++i)
            for(int j=0 ; j<GRID_SIZE ; ++j)
                mData.add(new GridCell(randomArray[i][j], i, j));
    }

    // Returns an int[GRID_SIZE][GRID_SIZE] containing numbers 1 to 25 randomly placed
    private int[][] CreateRandomGameArray() {

        int[][] randomArray = new int[GRID_SIZE][GRID_SIZE];
        int randomNumber1, randomNumber2, randomNumber3, randomNumber4, temp;

        for(int i=0 ; i<GRID_SIZE ; ++i)
            for(int j=0 ; j<GRID_SIZE ; ++j)
                randomArray[i][j] = i*GRID_SIZE+j+1;

        // Swapping two random elements of array 100 times
        for(int i=0 ; i<10 ; ++i)
            for(int j=0 ; j<10 ; ++j)
            {
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

    // Makes all the elements of cellsClicked[][] false
    private void InitialiseCellsClickedList() {
        for (int i=0 ; i<GRID_SIZE ; ++i)
            for (int j=0 ; j<GRID_SIZE ; ++j)
                cellsClicked[i][j]=false;
    }

    // Returns the number of rows completed
    public int numberOfRowsCompleted() {

        int counter=0;

        // Checking for columns
        for(int i=0 ; i<GRID_SIZE ; ++i)
            if(cellsClicked[0][i] && cellsClicked[1][i] && cellsClicked[2][i] && cellsClicked[3][i] && cellsClicked[4][i])
                counter++;

        // Checking for rows
        for(int i=0 ; i<GRID_SIZE ; ++i)
            if(cellsClicked[i][0] && cellsClicked[i][1] && cellsClicked[i][2] && cellsClicked[i][3] && cellsClicked[i][4])
                counter++;

        // Checking for diagonals
        if(cellsClicked[0][0] && cellsClicked[1][1] && cellsClicked[2][2] && cellsClicked[3][3] && cellsClicked[4][4])
            counter++;
        if(cellsClicked[0][4] && cellsClicked[1][3] && cellsClicked[2][2] && cellsClicked[3][1] && cellsClicked[4][0])
            counter++;

        if(counter > GRID_SIZE) counter=GRID_SIZE;

        switch(counter)
        {
            // severe fall through
            case 5 : O.setTextColor(Color.parseColor("#FF0000"));
            case 4 : G.setTextColor(Color.parseColor("#FF0000"));
            case 3 : N.setTextColor(Color.parseColor("#FF0000"));
            case 2 : I.setTextColor(Color.parseColor("#FF0000"));
            case 1 : B.setTextColor(Color.parseColor("#FF0000"));
        }

        return counter;
    }

    // Undo button click handler
    private void UndoButtonClickListener(){

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cellsClicked[lastClickedPositionX][lastClickedPositionY] = false;

                TextView value = (TextView)lastClickedView.findViewById(R.id.value_textView);
                value.setTextColor(Color.parseColor("#000000"));
                value.setTypeface(value.getTypeface(), Typeface.NORMAL);
            }
        });
    }

    // Creates new ArrayList and swaps the data with adapter
    private void ResetButtonClickListener() {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });
    }

    @Override
    public void onGridCellClick(int position, View view) {

        /* Updating cellsClicked[][] */
        cellsClicked[mData.get(position).getPositionX()][mData.get(position).getPositionY()] = true;

        /* Settings the clicked cell's font style to RED and BOLD */
        TextView value = (TextView)view.findViewById(R.id.value_textView);
        value.setTextColor(Color.parseColor("#FF0000"));
        value.setTypeface(value.getTypeface(), Typeface.BOLD);

        /* Checking of 5 rows are completed */
        if(numberOfRowsCompleted() == 5)
        {
            Toast.makeText(MainActivity.this,"CONGRATULATIONS", Toast.LENGTH_LONG).show();
            celebration.start();
            game_recyclerView.setEnabled(false);
            mPopField.popView(undoButton);
            resetButton.setText(R.string.new_game);

            GradientDrawable resetGradientDrawable = (GradientDrawable)resetButton.getBackground();
            resetGradientDrawable.setColor(Color.parseColor("#669900"));
        }

        /* Setting these variables for Undo option */
        lastClickedPositionX = mData.get(position).getPositionX();
        lastClickedPositionY = mData.get(position).getPositionY();
        lastClickedView = view;
    }
}
