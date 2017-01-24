package com.example.edwin.minesweeper

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

import java.util.ArrayList
import java.util.Random

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    enum class GameState { WON, LOST, PLAYING }
    class Game(val numRows : Int, val numColumns : Int, val numBombs : Int, var gameState : GameState)
    class CellInfo(val isBomb: Boolean, val location: Location, var isOpen : Boolean)
    class Location(val row: Int, val col: Int)

    var game = Game(numRows = 8, numColumns = 5, numBombs = 6, gameState = GameState.PLAYING)

    // UI views for each cell
    var cellViews = Array<Array<Button?>>(game.numRows) { arrayOfNulls<Button>(game.numColumns) }
    fun getCellView(row: Int, col: Int): Button {
        return cellViews[row][col]!!
    }

    // Data for each cell
    var board = Array<Array<CellInfo?>>(game.numRows) { arrayOfNulls<CellInfo>(game.numColumns) }
    fun getCellInfo(row: Int, col: Int): CellInfo { return board[row][col]!! }


    private fun getNumSurroundingBombs(loc: Location): Int {
        var count = 0
        val rowStart = loc.row - 1
        val rowEnd = loc.row + 1
        for (row in rowStart..rowEnd) {
            if (row >= 0 && row < game.numRows) {
                val colStart = loc.col - 1
                val colEnd = loc.col + 1
                for (col in colStart..colEnd) {
                    if (col >= 0 && col < game.numColumns) {
                        if (row !== loc.row || col !== loc.col) {
                            Log.d(TAG, "Test $row,$col")
                            val cellInfo = board[row][col]
                            if (cellInfo!!.isBomb) {
                                count++
                            }
                        }
                    }
                }
            }
        }
        return count
    }

    private fun checkForWin() {
        for (row in 0..game.numRows - 1) {
            for (col in 0..game.numColumns - 1) {
                if (!getCellInfo(row, col).isOpen && !getCellInfo(row, col).isBomb) {
                    return
                }
            }
        }
        game.gameState = GameState.WON
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeBoard()
        createBoardUI()
        updateBoardUI()

        findViewById(R.id.new_game).setOnClickListener {
            game.gameState = GameState.PLAYING
            initializeBoard()
            val rows = findViewById(R.id.gridRows) as LinearLayout
            rows.removeAllViews()
            createBoardUI()
            updateBoardUI()
        }
    }

    private fun initializeBoard() {
        val bombLocations = ArrayList<Int>()
        while (bombLocations.size < game.numBombs) {
            val r = Random()
            val location = r.nextInt(game.numRows * game.numColumns)
            if (!bombLocations.contains(location)) {
                bombLocations.add(location)
            }
        }

        // Create board and place bombs
        for (row in 0..game.numRows - 1) {
            for (col in 0..game.numColumns - 1) {
                var isBomb = false
                val location = row * game.numRows + col

                if (bombLocations.contains(location)) {
                    isBomb = true
                }

                val cellInfo = CellInfo(isBomb, Location(row, col), false)
                board[row][col] = cellInfo

            }
        }
    }

    fun createBoardUI() {
        val myLayout = findViewById(R.id.gridRows) as LinearLayout

        for (row in 0..game.numRows - 1) {
            val linearLayoutRow = LinearLayout(this)
            val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            linearLayoutRow.layoutParams = layoutParams

            for (col in 0..game.numColumns - 1) {
                val button = Button(this)
                button.layoutParams = LinearLayout.LayoutParams(150, 150)
                val cellInfo = board[row][col]
                button.tag = cellInfo
                button.textSize = 16f
                cellViews[row][col] = button
                linearLayoutRow.addView(button)

                button.setOnClickListener { v ->
                    handleButtonClick(v.tag as CellInfo)
                }
            }

            myLayout.addView(linearLayoutRow)
        }
    }

    private fun updateBoardUI() {
        for (row in 0..game.numRows - 1) {
            for (col in 0..game.numColumns - 1) {
                val cellInfo = board[row][col]
                var textToShow = ""
                if (cellInfo!!.isOpen || game.gameState == GameState.LOST) {
                    textToShow = if (cellInfo!!.isBomb) "X"
                    else getNumSurroundingBombs(Location(row, col)).toString()
                }
                getCellView(row, col).text = textToShow
            }
        }

        if (game.gameState == GameState.PLAYING) {
            checkForWin()
        }

        val background = findViewById(R.id.activity_main)
        if (game.gameState == GameState.WON) {
            background.setBackgroundColor(Color.GREEN)
        } else if (game.gameState == GameState.LOST) {
            background.setBackgroundColor(Color.RED)
        } else {
            background.setBackgroundColor(Color.WHITE)
        }
    }

    private fun handleButtonClick(cellInfo: CellInfo) {
        if (cellInfo.isBomb) {
            game.gameState = GameState.LOST
        }
        val location = cellInfo.location
        openClearCells(location.row, location.col)
        updateBoardUI()
    }

    private fun openClearCellsHelper(row: Int, col: Int, firstClick: Boolean) {
        if (!firstClick) {
            if (row < 0 || row >= game.numRows) {
                return
            }
            if (col < 0 || col >= game.numColumns) {
                return
            }
            try {
                if (getCellInfo(row, col).isOpen) {
                    return  // already opened this [safe algo???]
                }
            } catch (ex: Exception) {
                Log.d(TAG, "huhh???")
            }

        }
        val cellInfo = getCellInfo(row, col)
        if (getNumSurroundingBombs(Location(row, col)) == 0 || firstClick) {
            cellInfo.isOpen = !cellInfo.isBomb
            openClearCellsHelper(row - 1, col, false)
            openClearCellsHelper(row + 1, col, false)
            openClearCellsHelper(row, col - 1, false)
            openClearCellsHelper(row, col + 1, false)
        } else {
            cellInfo.isOpen = true
        }
    }

    private fun openClearCells(row: Int, col: Int) {
        openClearCellsHelper(row, col, true)
    }

}
