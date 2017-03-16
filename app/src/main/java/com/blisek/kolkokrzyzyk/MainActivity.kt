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
    private val ColumnCount = 3
    private val RowsCount = 3
    private val GridElementsTotal = ColumnCount * RowsCount

    private val board: Array<Turn>
    private var currentTurn: Turn;
    private var gameInProgress: Boolean = true
    private var randomMoveGenerator: Random
    private lateinit var textView: TextView
    private lateinit var gridLayout: GridLayout

    init {
        board = Array(GridElementsTotal, { Turn.UNKNOWN })
        currentTurn = Turn.CROSS
        randomMoveGenerator = Random()
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
            btn.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            btn.tag = i-1
            btn.setOnClickListener { onBoardButtonClick(it) }

            val row = (i-1) / ColumnCount
            val column = (i-1) % ColumnCount
            val gridLayoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(row), GridLayout.spec(column))
            gridLayout.addView(btn, gridLayoutParams)
        }
    }

    private fun onBoardButtonClick(btn: View) {
        if(gameInProgress) {
            val viewBtn = btn as? Button
            if (viewBtn != null) {
                viewBtn.text = currentTurn.toString()
                board[viewBtn.tag as Int] = currentTurn
            }

            if(!checkWinConditions()) {
                currentTurn = currentTurn.flip()
                updateStatus()
            }
        }
    }

    private fun checkWinConditions() : Boolean {
        for (i in 0..GridElementsTotal-1 step 3) {
            if(board[i] != Turn.UNKNOWN && board[i] == board[i+1] && board[i] == board[i+2]) {
                endGame()
                return true
            }
        }

        for (i in 0..ColumnCount-1) {
            if(board[i] != Turn.UNKNOWN && board[i] == board[i+ColumnCount] &&
                    board[i] == board[i + ColumnCount*2]) {
                endGame()
                return true
            }
        }

        if(board[0] != Turn.UNKNOWN && board[0] == board[4] && board[4] == board[8]) {
            endGame()
            return true
        }

        if(board[2] != Turn.UNKNOWN && board[2] == board[4] && board[4] == board[6]) {
            endGame()
            return true
        }

        return false
    }

    // TODO: zrobić coś z tym
    private fun nextPCMove(btn: Button) {
        if(!gameInProgress) return

        var index = randomMoveGenerator.nextInt(board.size)
        if(board[index] == Turn.UNKNOWN) {
            board[index] = currentTurn
        }
    }

    private fun endGame() {
        textView?.text = String.format("Koniec gry. Wygrał: %s.", currentTurn.verbal)
        gameInProgress = false
    }

    private fun updateStatus() {
        textView?.text = String.format("Kolej na: %s.", currentTurn.verbal)
    }

    private enum class Turn(private val text: String, val verbal: String) {
        CROSS("X", "krzyżyk"), CIRCLE("O", "kółko"), UNKNOWN(" ", "UNKNOWN");

        fun flip() : Turn {
            if(this == CROSS)
                return CIRCLE
            else
                return CROSS
        }

        override fun toString(): String {
            return text
        }
    }
}
