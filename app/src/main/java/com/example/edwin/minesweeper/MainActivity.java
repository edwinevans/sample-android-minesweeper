package com.example.edwin.minesweeper;

import android.graphics.Color;
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

    public enum GameState {
        WON, LOST, PLAYING
    }
    private GameState gameState = GameState.PLAYING;

    // UI views for each cell
    Button[][] cellViews = new Button[NUM_ROWS][NUM_COLUMNS];
    Button getCellView(int row, int col) {
        return cellViews[row][col];
    }

    // Data for each cell
    CellInfo[][] board = new CellInfo[NUM_ROWS][NUM_COLUMNS];
    CellInfo getCellInfo(int row, int col) {
        return board[row][col];
    }

    static class CellInfo {
        private boolean isOpen;
        private boolean isBomb;
        private Location location;
        private int numSurrounding;


        CellInfo(boolean isBomb, Location location) {
            isOpen = false;
            this.isBomb = isBomb;
            this.location = location;
        }

        boolean isBomb() {
            return isBomb;
        }

        boolean isOpen() {
            return isOpen;
        }

        int getNumSurrounding() {
            return numSurrounding;
        }

        void setNumSurrounding(int pNumSourrounding) {
            numSurrounding = pNumSourrounding;
        }

        void setIsOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }

        @Override
        public String toString() {
            return "CellInfo{" +
                    ", isBomb=" + isBomb +
                    ", isOpen=" + isOpen +
                    ", numSurrounding=" + numSurrounding +
                    '}';
        }

        Location getLocation() {
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

        int getRow() {
            return row;
        }

        int getCol() {
            return col;
        }

        static int getAsNumber(int row, int col) {
            return row * NUM_COLUMNS + col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Location location = (Location) o;

            if (row != location.row) {
                return false;
            }
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
                            CellInfo cellInfo = board[row][col];
                            if (cellInfo.isBomb()) {
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
                if (!getCellInfo(row,col).isOpen() && !getCellInfo(row, col).isBomb()) {
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
        List<Integer> bombLocations = new ArrayList<>();
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

                CellInfo cellInfo = new CellInfo(isBomb, new Location(row, col));
                board[row][col] = cellInfo;

            }
        }

        // Calculate numbers for surrounding cells
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                int numSurrounding = getNumSurroundingBombs(new Location(row, col));
                CellInfo cellInfo = board[row][col];
                cellInfo.setNumSurrounding(numSurrounding);
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
                CellInfo cellInfo = board[row][col];
                button.setTag(cellInfo);
                button.setTextSize(16);
                cellViews[row][col] = button;
                linearLayoutRow.addView(button);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CellInfo cellInfo = (CellInfo)v.getTag();
                        handleButtonClick(cellInfo);
                    }
                });
            }

            myLayout.addView(linearLayoutRow);
        }
    }

    private void updateBoardUI() {
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                CellInfo cellInfo = board[row][col];
                String textToShow = "";
                if (cellInfo.isOpen() || gameState == GameState.LOST) {
                    textToShow = cellInfo.isBomb() ? "X" : String.valueOf(cellInfo.getNumSurrounding());
                }
                getCellView(row, col).setText(textToShow);
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

    private void handleButtonClick(CellInfo cellInfo) {
        if (cellInfo.isBomb()) {
            gameState = GameState.LOST;
        }
        Location location = cellInfo.getLocation();
        openClearCells(location.getRow(), location.getCol());
        updateBoardUI();
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
                if (getCellInfo(row, col).isOpen()) {
                    return; // already opened this [safe algo???]
                }
            }
            catch (Exception ex) {
                Log.d(TAG, "huhh???");
            }
        }
        CellInfo cellInfo = getCellInfo(row, col);
        if (cellInfo.getNumSurrounding() == 0 || firstClick) {
            cellInfo.setIsOpen(!cellInfo.isBomb());
            openClearCellsHelper(row - 1, col, false);
            openClearCellsHelper(row + 1, col, false);
            openClearCellsHelper(row, col - 1, false);
            openClearCellsHelper(row, col + 1, false);
        }
        else {
            cellInfo.setIsOpen(true);
        }
    }

    private void openClearCells(int row, int col) {
        openClearCellsHelper(row, col, true);
    }
}
