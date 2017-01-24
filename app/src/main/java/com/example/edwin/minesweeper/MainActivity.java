package com.example.edwin.minesweeper;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    static final int NUM_ROWS = 8; // 10;
    static final int NUM_COLUMNS = 5; //10;
    static final int NUM_BOMBS = 3;
    Cell[][] board = new Cell[NUM_ROWS][NUM_COLUMNS];

    public enum GameState {
        WON, LOST, PLAYING
    }
    private GameState gameState = GameState.PLAYING;

    static class Cell {
        boolean isOpen;
        boolean isBomb;
        Location location;
        Button view;
        int numSurrounding;

        Cell(boolean isBomb, Location location) {
            isOpen = false;
            this.isBomb = isBomb;
            this.location = location;
        }

        @Override
        public String toString() {
            return "Cell{" +
                    "location=" + location +
                    ", isBomb=" + isBomb +
                    ", isOpen=" + isOpen +
                    ", numSurrounding=" + numSurrounding +
                    '}';
        }

        public boolean isBomb() {
            return isBomb;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public int getNumSurrounding() {
            return numSurrounding;
        }

        public void setNumSurrounding(int pNumSourrounding) {
            numSurrounding = pNumSourrounding;
        }

        public void setIsOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }

        public Button getView() {
            return view;
        }

        public void setView(Button view) {
            this.view = view;
        }

        public Location getLocation() {
            return location;
        }

    }

    static class Location {
        private int row;
        private int col;

        Location(int pRow, int pCol) {
            row = pRow;
            col = pCol;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        static public int getAsNumber(int row, int col) {
            return row * NUM_COLUMNS + col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Location location = (Location) o;

            if (row != location.row) return false;
            return col == location.col;

        }

        @Override
        public int hashCode() {
            int result = row;
            result = 31 * result + col;
            return result;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "row=" + row +
                    ", col=" + col +
                    '}';
        }
    }

    private int getNumSurroundingBombs(Location loc) {
        Log.d(TAG, "getNumSurroundingBombs " + loc.toString());
        int count = 0;
        int rowStart = loc.getRow() - 1;
        int rowEnd = loc.getRow() + 1;
        for (Integer row = rowStart; row <= rowEnd; row++) {
            if (row >= 0 && row < NUM_ROWS) {
                int colStart = loc.getCol() - 1;
                int colEnd = loc.getCol() + 1;
                for (Integer col = colStart; col <= colEnd; col++) {
                    if (col >= 0 && col < NUM_COLUMNS) {
                        if (row != loc.getRow() || col != loc.getCol()) {
                            Log.d(TAG, "Test " + row + "," + col);
                            Cell cell = board[row][col];
                            if (cell.isBomb()) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "-----");
        return count;
    }

    private void checkForWin() {
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                if (!getCell(row,col).isOpen() && !getCell(row, col).isBomb()) {
                    return;
                }
            }
        }
        gameState = GameState.WON;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeBoard();
        createBoardUI();
        updateBoardUI();

        findViewById(R.id.new_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameState = GameState.PLAYING;
                initializeBoard();
                LinearLayout rows = (LinearLayout)findViewById(R.id.gridRows);
                rows.removeAllViews();
                createBoardUI();
                updateBoardUI();
            }
        });
    }

    private void initializeBoard() {
        List<Integer> bombLocations = new ArrayList<Integer>();
        while (bombLocations.size() < NUM_BOMBS) {
            Random r = new Random();
            Integer location = r.nextInt(NUM_ROWS * NUM_COLUMNS);
            if (!bombLocations.contains(location)) {
                bombLocations.add(location);
            }
        }

        // Create board and place bombs
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                boolean isBomb = false;
                int location = Location.getAsNumber(row, col);
                if (bombLocations.contains(location)) {
                    isBomb = true;
                }

                Cell cell = new Cell(isBomb, new Location(row, col));
                board[row][col] = cell;

            }
        }

        //dumpBoard();

        // Calculate numbers for surrounding cells
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                int numSurrounding = getNumSurroundingBombs(new Location(row, col));
                Cell cell = board[row][col];
                cell.setNumSurrounding(numSurrounding);
            }
        }

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                int count = getNumSurroundingBombs(new Location(row, col));
            }
        }
    }

    void createBoardUI() {
        LinearLayout myLayout = (LinearLayout) findViewById(R.id.gridRows);

        for (int row = 0; row < NUM_ROWS; row++) {
            LinearLayout linearLayoutRow = new LinearLayout(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayoutRow.setLayoutParams(layoutParams);

            for (int col = 0; col < NUM_COLUMNS; col++) {
                Button button = new Button(this);
                button.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
                Cell cell = board[row][col];
                button.setTag(cell);
                button.setTextSize(16);
                cell.setView(button);
                linearLayoutRow.addView(button);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cell cell = (Cell)v.getTag();
                        handleButtonClick(cell);
                    }
                });
            }

            myLayout.addView(linearLayoutRow);
        }
    }

    private void updateBoardUI() {
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                Cell cell = board[row][col];
                String textToShow = "";
                if (cell.isOpen() || gameState == GameState.LOST) {
                    textToShow = cell.isBomb() ? "X" : String.valueOf(cell.getNumSurrounding());
                }
                cell.getView().setText(textToShow);
            }
        }

        if (gameState == GameState.PLAYING) {
            checkForWin();
        }

        View background = findViewById(R.id.activity_main);
        if (gameState == GameState.WON) {
            background.setBackgroundColor(Color.GREEN);
        }
        else if (gameState == GameState.LOST) {
            background.setBackgroundColor(Color.RED);
        }
        else {
            background.setBackgroundColor(Color.WHITE);
        }
    }

    private void handleButtonClick(Cell cell) {
        if (cell.isBomb()) {
            gameState = GameState.LOST;
        }
        openClearCells(cell.getLocation().getRow(), cell.getLocation().getCol());
        updateBoardUI();
    }

    Cell getCell(int row, int col) {
        return board[row][col];
    }

    private void openClearCellsHelper(int row, int col, boolean firstClick) {
        if (!firstClick) {
            if (row < 0 || row >= NUM_ROWS) {
                return;
            }
            if (col < 0 || col >= NUM_COLUMNS) {
                return;
            }
            try {
                if (getCell(row, col).isOpen()) {
                    return; // already opened this [safe algo???]
                }
            }
            catch (Exception ex) {
                Log.d(TAG, "huhh???");
            }
        }
        Cell cell = getCell(row, col);
        if (cell.getNumSurrounding() == 0 || firstClick) {
            cell.setIsOpen(!cell.isBomb());
            openClearCellsHelper(row - 1, col, false);
            openClearCellsHelper(row + 1, col, false);
            openClearCellsHelper(row, col - 1, false);
            openClearCellsHelper(row, col + 1, false);
        }
        else {
            cell.setIsOpen(true);
        }
    }

    private void openClearCells(int row, int col) {
        openClearCellsHelper(row, col, true);
    }
}
