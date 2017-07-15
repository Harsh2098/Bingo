package com.hmproductions.bingo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 5;

    private MediaPlayer celebration;
    private GridView game_gridView;
    private GameGridAdapter mGridAdapter;
    private TextView B, I, N, G, O;
    private Button resetButton, undoButton;
    private View lastClickedView;

    ArrayList<GridCell> mData = new ArrayList<>();
    boolean[][] cellsClicked = new boolean[5][5];
    int lastClickedPositionX = GRID_SIZE, lastClickedPositionY = GRID_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind all views
        BindAllViews();

        // Creates an array list made up of random values
        CreateGameGridList();
        InitialiseCellsClickedList();


        mGridAdapter = new GameGridAdapter(this, R.layout.grid_item, mData);
        game_gridView.setAdapter(mGridAdapter);
        celebration = MediaPlayer.create(this, R.raw.tada_celebration);

        GameGridClickListener();
        UndoButtonClickListener();
        ResetButtonClickListener();
    }

    private void BindAllViews() {
        game_gridView = (GridView)findViewById(R.id.game_gridView);
        resetButton = (Button)findViewById(R.id.resetButton);
        undoButton = (Button)findViewById(R.id.undoButton);
        B = (TextView)findViewById(R.id.B_textView);
        I = (TextView)findViewById(R.id.I_textView);
        N = (TextView)findViewById(R.id.N_textView);
        G = (TextView)findViewById(R.id.G_textView);
        O = (TextView)findViewById(R.id.O_textView);
    }

    private void GameGridClickListener() {

        game_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                cellsClicked[mData.get(position).getPositionX()][mData.get(position).getPositionY()] = true;

                TextView value = (TextView)view.findViewById(R.id.value_textView);
                value.setTextColor(Color.parseColor("#FF0000"));
                value.setTypeface(value.getTypeface(), Typeface.BOLD);

                if(numberOfRowsCompleted() == 5)
                {
                    Toast.makeText(MainActivity.this,"CONGRATULATIONS", Toast.LENGTH_LONG).show();
                    celebration.start();
                    game_gridView.setEnabled(false);
                }

                // Setting these variables for Undo option
                lastClickedPositionX = mData.get(position).getPositionX();
                lastClickedPositionY = mData.get(position).getPositionY();
                lastClickedView = view;
            }
        });
    }

    private void CreateGameGridList() {
        int[][] randomArray = CreateRandomGameArray();

        for(int i=0 ; i<GRID_SIZE ; ++i)
            for(int j=0 ; j<GRID_SIZE ; ++j)
                mData.add(new GridCell(randomArray[i][j], i, j));
    }

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

    private void InitialiseCellsClickedList() {
        for (int i=0 ; i<GRID_SIZE ; ++i)
            for (int j=0 ; j<GRID_SIZE ; ++j)
                cellsClicked[i][j]=false;
    }

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

    private void ResetButtonClickListener() {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });
    }
}
