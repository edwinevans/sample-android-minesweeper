package com.example.edwin.minesweeper

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import java.util.*

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    var game = Game(numRows = 8, numColumns = 5, numBombs = 6, gameState = GameState.PLAYING)

    enum class GameState { WON, LOST, PLAYING }
    class Game(val numRows : Int, val numColumns : Int, val numBombs : Int, var gameState : GameState)
    class CellInfo(val isBomb: Boolean, val location: Location, var isOpen : Boolean)
    class Location(val row: Int, val col: Int)

    // UI views for each cell
    var cellViews = Array(game.numRows) { arrayOfNulls<Button>(game.numColumns) }
    fun getCellView(row: Int, col: Int): Button { return cellViews[row][col]!! }

    // Data for each cell
    var board = Array(game.numRows) { arrayOfNulls<CellInfo>(game.numColumns) }
    fun getCellInfo(row: Int, col: Int): CellInfo { return board[row][col]!! }

    private fun getNumSurroundingBombs(loc: Location): Int {
        var count = 0
        for (row in loc.row - 1..loc.row + 1) {
            if (row >= 0 && row < game.numRows) {
                for (col in loc.col - 1..loc.col + 1) {
                    if (col >= 0 && col < game.numColumns) {
                        if (row !== loc.row || col !== loc.col) {
                            if (getCellInfo(row,col).isBomb) {
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
        createGame()

        findViewById(R.id.new_game).setOnClickListener {
            // Clear everything and recreate
            val rows = findViewById(R.id.gridRows) as LinearLayout
            rows.removeAllViews()
            createGame()
        }
    }

    private fun createGame() {
        game.gameState = GameState.PLAYING
        initializeBoard()
        createBoardUI()
        updateBoardUI()
    }

    private fun initializeBoard() {
        // Get random locations of bombs
        var locations = mutableListOf(0..game.numRows * game.numColumns).flatten()
        Collections.shuffle(locations)
        val bombLocations = locations.subList(fromIndex = 0, toIndex = game.numBombs)

        // Create board and place bombs
        for (row in 0..game.numRows - 1) {
            for (col in 0..game.numColumns - 1) {
                val location = row * game.numColumns + col
                val isBomb = bombLocations.contains(location)
                val cellInfo = CellInfo(
                        isBomb = isBomb, location = Location(row, col), isOpen = false)
                board[row][col] = cellInfo
            }
        }
    }

    private fun createBoardUI() {
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
                button.setOnClickListener { handleButtonClick(it.tag as CellInfo) }
            }

            myLayout.addView(linearLayoutRow)
        }
    }

    private fun updateBoardUI() {
        for (row in 0..game.numRows - 1) {
            for (col in 0..game.numColumns - 1) {
                val cellView = getCellView(row, col)
                cellView.text = getTextToShow(Location(row, col))
                if (getCellInfo(row, col).isOpen) {
                    cellView.setBackgroundColor(Color.GREEN)
                }
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

    private fun getTextToShow(loc : Location): String {
        val cellInfo = getCellInfo(loc.row, loc.col)
        val reveal = cellInfo.isOpen || game.gameState != GameState.PLAYING
        if (reveal) {
            if (cellInfo.isBomb) {
                return "X"
            }
            else {
                val numSurrounding = getNumSurroundingBombs(loc)
                when (numSurrounding) {
                    0 -> return ""
                    else -> return numSurrounding.toString()
                }

            }
        }
        else {
            return "?"
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
            if (getCellInfo(row, col).isOpen) {
                return  // don't recurse forever
            }
        }


        val cellInfo = getCellInfo(row, col)
        if (!cellInfo.isBomb) {
            cellInfo.isOpen = true
            if (getNumSurroundingBombs(Location(row, col)) == 0 || firstClick) {
                cellInfo.isOpen = !cellInfo.isBomb
                openClearCellsHelper(row - 1, col, false)
                openClearCellsHelper(row + 1, col, false)
                openClearCellsHelper(row, col - 1, false)
                openClearCellsHelper(row, col + 1, false)
            }
        }
    }

    private fun openClearCells(row: Int, col: Int) {
        openClearCellsHelper(row, col, true)
    }
}
