package com.blisek.kolkokrzyzyk

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {
    private val ColumnCount = 4
    private val RowsCount = 4
    private val GridElementsTotal = ColumnCount * RowsCount

    private val board: Array<Turn>
    private val boardButtons: Array<Button?>
    private var currentTurn: Turn
    private var pcTurn: Turn
    private var gameInProgress: Boolean = true
    private var randomMoveGenerator: Random
    private lateinit var textView: TextView
    private lateinit var gridLayout: GridLayout

    init {
        board = Array(GridElementsTotal, { Turn.UNKNOWN })
        boardButtons = Array(GridElementsTotal, { null })
        currentTurn = Turn.CROSS
        randomMoveGenerator = Random()
        pcTurn = Turn.CIRCLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView) as TextView
        updateStatus()

        gridLayout = findViewById(R.id.gridLayout) as GridLayout
        buildGridLayout(gridLayout);
    }

    private fun buildGridLayout(gridLayout: GridLayout) {
        gridLayout.columnCount = ColumnCount
        gridLayout.rowCount = RowsCount

        for(i in 1..GridElementsTotal) {
            val btn = Button(this)
            btn.text = " "
            btn.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            btn.tag = i-1
            btn.setOnClickListener { onBoardButtonClick(it) }

            val row = (i-1) / ColumnCount
            val column = (i-1) % ColumnCount
            val gridLayoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(row), GridLayout.spec(column))
            gridLayout.addView(btn, gridLayoutParams)
            boardButtons[i-1] = btn
        }

        gridLayout.postInvalidate()
    }

    private fun onBoardButtonClick(btn: View) {
        if(gameInProgress) {
            val viewBtn = btn as? Button
            if (viewBtn != null) {
                nextMove(viewBtn)
            }

        }
    }

    private fun nextMove(viewBtn: Button) {
        val tag = viewBtn.tag as Int
        if(board[tag] != Turn.UNKNOWN)
            return

        board[tag] = currentTurn


        viewBtn.text = currentTurn.sign
        if(checkWinConditions())
            return
        currentTurn = currentTurn.flip()

        if(currentTurn == pcTurn)
            nextPCMove()
        else {
            updateStatus()
        }
    }

    private fun checkWinConditions() : Boolean {
        if (checkHitsInRows()) {
            endGame()
            return true
        }

        if (checkHitsInColumns()) {
            endGame()
            return true
        }

        if (checkHitsInFirstDiagonal()) {
            endGame()
            return true
        }


        if(checkHitsInSecondDiagonal()) {
            endGame()
            return true
        }

        // wszystkie pola zajęte
        if(board.none { it == Turn.UNKNOWN }) {
            endGameDraw()
            return true
        }

        return false
    }

    private fun checkHitsInColumns(): Boolean {
        for (i in 0..ColumnCount - 1) {
            val player = board[i]
            if (player == Turn.UNKNOWN)
                continue

            for (column in 0..ColumnCount - 1) {
                var hitsCount = 1
                for (row in 1..RowsCount - 1) {
                    if (player == board[ColumnCount * row + column]) {
                        ++hitsCount
                    } else {
                        break
                    }
                }

                if (hitsCount == RowsCount) {
                    return true
                }
            }

        }
        return false
    }

    private fun checkHitsInRows(): Boolean {
        for (i in board.indices step ColumnCount) {
            val player = board[i]
            if (player == Turn.UNKNOWN)
                continue

            var samePlayerHits = 1
            for (cellShift in 1..ColumnCount - 1) {
                if (player == board[i + cellShift]) {
                    ++samePlayerHits
                } else {
                    break
                }
            }

            if (samePlayerHits == ColumnCount) {
                return true
            }
        }
        return false
    }

    private fun checkHitsInFirstDiagonal(): Boolean {
        val smallerDimension = Math.min(ColumnCount, RowsCount)
        val player = board[0]
        if(smallerDimension == 0 || player == Turn.UNKNOWN)
            return false
        var hitsCount = 1
        for (shift in 1..smallerDimension-1) {
            // sprawdzanie dla przecięć
            val index = shift * smallerDimension + shift
            if(player == board[index]) {
                ++hitsCount
            } else {
                break
            }
        }

        return hitsCount == smallerDimension
    }

    private fun checkHitsInSecondDiagonal(): Boolean {
        val smallerDimension = Math.min(ColumnCount, RowsCount)
        if(smallerDimension < 1)
            return false

        val player = board[ColumnCount-1]
        if(player == Turn.UNKNOWN)
            return false

        var hitsCount = 1;
        for(shift in 1..smallerDimension-1) {
            val index = shift * smallerDimension + ColumnCount - shift - 1
            if(player == board[index])
                ++hitsCount
            else
                break
        }

        return hitsCount == smallerDimension
    }

    // TODO: zrobić coś z tym
    private fun nextPCMove() {
        if(!gameInProgress) return

        var index = randomMoveGenerator.nextInt(board.size)
        if(board[index] == Turn.UNKNOWN) {
            board[index] = pcTurn
            boardButtons[index]?.text = pcTurn.sign
        }
        else {
            index = board.indexOfFirst { it == Turn.UNKNOWN }
            board[index] = pcTurn
            boardButtons[index]?.text = pcTurn.sign
        }

        currentTurn = currentTurn.flip()
        updateStatus()
    }

    private fun endGame() {
        textView?.text = String.format("Koniec gry. Wygrał: %s.", currentTurn.verbal)
        gameInProgress = false
    }

    private fun endGameDraw() {
        textView?.text = "Koniec gry. Nikt nie wygrał."
        gameInProgress = false
    }

    private fun updateStatus() {
        textView?.text = String.format("Kolej na: %s.", currentTurn.verbal)
    }

    private enum class Turn(val sign: String, val verbal: String) {
        CROSS("X", "krzyżyk"), CIRCLE("O", "kółko"), UNKNOWN(" ", "UNKNOWN");

        fun flip() : Turn {
            if(this == CROSS)
                return CIRCLE
            else
                return CROSS
        }
    }
}
