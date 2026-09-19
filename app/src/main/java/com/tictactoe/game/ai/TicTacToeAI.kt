package com.tictactoe.game.ai

import com.tictactoe.game.model.AiDifficulty
import com.tictactoe.game.model.Player
import kotlin.random.Random

object TicTacToeAI {

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

    fun findBestMove(
        board: List<Player?>,
        aiPlayer: Player,
        difficulty: AiDifficulty
    ): Int {
        val availableMoves = board.indices.filter { board[it] == null }
        if (availableMoves.isEmpty()) return -1

        return when (difficulty) {
            AiDifficulty.EASY -> chooseEasyMove(board, aiPlayer, availableMoves)
            AiDifficulty.MEDIUM -> chooseMediumMove(board, aiPlayer, availableMoves)
            AiDifficulty.MASTER -> chooseMasterMove(board, aiPlayer, availableMoves)
        }
    }

    private fun chooseEasyMove(
        board: List<Player?>,
        aiPlayer: Player,
        availableMoves: List<Int>
    ): Int {
        // 70% random move, 30% instant win if available
        val winMove = findImmediateWinningMove(board, aiPlayer, availableMoves)
        return if (winMove != null && Random.nextFloat() < 0.35f) {
            winMove
        } else {
            availableMoves.random()
        }
    }

    private fun chooseMediumMove(
        board: List<Player?>,
        aiPlayer: Player,
        availableMoves: List<Int>
    ): Int {
        val human = aiPlayer.next()

        // 1. Take immediate win
        val winMove = findImmediateWinningMove(board, aiPlayer, availableMoves)
        if (winMove != null) return winMove

        // 2. Block human immediate win
        val blockMove = findImmediateWinningMove(board, human, availableMoves)
        if (blockMove != null) return blockMove

        // 3. Take center if available
        if (board[4] == null) return 4

        // 4. Take random available corner or edge
        val corners = listOf(0, 2, 6, 8).filter { it in availableMoves }
        if (corners.isNotEmpty() && Random.nextBoolean()) {
            return corners.random()
        }

        return availableMoves.random()
    }

    private fun chooseMasterMove(
        board: List<Player?>,
        aiPlayer: Player,
        availableMoves: List<Int>
    ): Int {
        // Fast opening path: if entire board is empty, claiming center is optimal
        if (availableMoves.size == 9) return 4

        var bestScore = Int.MIN_VALUE
        var bestMove = availableMoves.first()

        val mutableBoard = board.toMutableList()

        // Move ordering: evaluate center, then corners, then edges for optimal alpha-beta cutoffs
        val prioritizedMoves = availableMoves.sortedByDescending { move ->
            when (move) {
                4 -> 3 // Center
                0, 2, 6, 8 -> 2 // Corners
                else -> 1 // Edges
            }
        }

        for (move in prioritizedMoves) {
            mutableBoard[move] = aiPlayer
            val score = minimax(
                board = mutableBoard,
                depth = 0,
                isMaximizing = false,
                aiPlayer = aiPlayer,
                humanPlayer = aiPlayer.next(),
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE
            )
            mutableBoard[move] = null

            if (score > bestScore) {
                bestScore = score
                bestMove = move
                // Maximum possible score is 10 (immediate win)
                if (bestScore == 10) break
            }
        }

        return bestMove
    }

    /**
     * Minimax algorithm with Alpha-Beta Pruning.
     */
    private fun minimax(
        board: MutableList<Player?>,
        depth: Int,
        isMaximizing: Boolean,
        aiPlayer: Player,
        humanPlayer: Player,
        alpha: Int,
        beta: Int
    ): Int {
        var currentAlpha = alpha
        var currentBeta = beta

        if (checkWinForPlayer(board, aiPlayer)) {
            return 10 - depth
        }
        if (checkWinForPlayer(board, humanPlayer)) {
            return depth - 10
        }
        val emptySlots = board.indices.filter { board[it] == null }
        if (emptySlots.isEmpty()) {
            return 0 // Draw
        }

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in emptySlots) {
                board[move] = aiPlayer
                val evaluation = minimax(board, depth + 1, false, aiPlayer, humanPlayer, currentAlpha, currentBeta)
                board[move] = null
                maxEval = maxOf(maxEval, evaluation)
                currentAlpha = maxOf(currentAlpha, evaluation)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in emptySlots) {
                board[move] = humanPlayer
                val evaluation = minimax(board, depth + 1, true, aiPlayer, humanPlayer, currentAlpha, currentBeta)
                board[move] = null
                minEval = minOf(minEval, evaluation)
                currentBeta = minOf(currentBeta, evaluation)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }

    private fun findImmediateWinningMove(
        board: List<Player?>,
        player: Player,
        availableMoves: List<Int>
    ): Int? {
        val testBoard = board.toMutableList()
        for (move in availableMoves) {
            testBoard[move] = player
            if (checkWinForPlayer(testBoard, player)) {
                return move
            }
            testBoard[move] = null
        }
        return null
    }

    fun checkWinForPlayer(board: List<Player?>, player: Player): Boolean {
        for (combo in winningCombinations) {
            if (board[combo[0]] == player && board[combo[1]] == player && board[combo[2]] == player) {
                return true
            }
        }
        return false
    }
}
