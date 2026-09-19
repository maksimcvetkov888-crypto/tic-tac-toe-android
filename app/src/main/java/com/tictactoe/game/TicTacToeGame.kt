package com.tictactoe.game

class TicTacToeGame {

    enum class State {
        IN_PROGRESS,
        X_WON,
        O_WON,
        DRAW
    }

    private val board = Array<String?>(9) { null }
    var currentPlayer: String = "X"
        private set

    var state: State = State.IN_PROGRESS
        private set

    var winningLine: List<Int>? = null
        private set

    var xScore: Int = 0
        private set
    var oScore: Int = 0
        private set
    var drawScore: Int = 0
        private set

    private var startingPlayer: String = "X"

    private val winningCombinations = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    fun makeMove(index: Int): Boolean {
        if (index !in 0..8 || board[index] != null || state != State.IN_PROGRESS) {
            return false
        }

        board[index] = currentPlayer
        val winCombo = checkWin(currentPlayer)

        if (winCombo != null) {
            winningLine = winCombo
            if (currentPlayer == "X") {
                state = State.X_WON
                xScore++
            } else {
                state = State.O_WON
                oScore++
            }
        } else if (board.all { it != null }) {
            state = State.DRAW
            drawScore++
        } else {
            currentPlayer = if (currentPlayer == "X") "O" else "X"
        }

        return true
    }

    fun getSymbol(index: Int): String? {
        return if (index in 0..8) board[index] else null
    }

    private fun checkWin(player: String): List<Int>? {
        for (combo in winningCombinations) {
            if (board[combo[0]] == player && board[combo[1]] == player && board[combo[2]] == player) {
                return combo
            }
        }
        return null
    }

    fun resetBoard() {
        for (i in board.indices) {
            board[i] = null
        }
        state = State.IN_PROGRESS
        winningLine = null
        startingPlayer = if (startingPlayer == "X") "O" else "X"
        currentPlayer = startingPlayer
    }

    fun resetAll() {
        xScore = 0
        oScore = 0
        drawScore = 0
        startingPlayer = "X"
        resetBoard()
        currentPlayer = "X"
    }
}
