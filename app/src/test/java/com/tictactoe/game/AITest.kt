package com.tictactoe.game

import com.tictactoe.game.ai.TicTacToeAI
import com.tictactoe.game.model.AiDifficulty
import com.tictactoe.game.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AITest {

    @Test
    fun aiTakesImmediateWin() {
        // O is AI, indices 0 and 1 are O, index 2 is empty
        val board: List<Player?> = listOf(
            Player.O, Player.O, null,
            Player.X, Player.X, null,
            null, null, null
        )

        val move = TicTacToeAI.findBestMove(board, Player.O, AiDifficulty.MASTER)
        assertEquals(2, move)
    }

    @Test
    fun aiBlocksHumanImmediateWin() {
        // Human X has 0 and 1, index 2 is empty. AI O must block at 2.
        val board: List<Player?> = listOf(
            Player.X, Player.X, null,
            Player.O, null, null,
            null, null, null
        )

        val move = TicTacToeAI.findBestMove(board, Player.O, AiDifficulty.MASTER)
        assertEquals(2, move)
    }

    @Test
    fun aiTakesCenterWhenAvailable() {
        val board: List<Player?> = listOf(
            Player.X, null, null,
            null, null, null,
            null, null, null
        )

        val move = TicTacToeAI.findBestMove(board, Player.O, AiDifficulty.MASTER)
        assertEquals(4, move)
    }

    @Test
    fun masterAiNeverLosesAsSecondPlayerAgainstRandom() {
        // Simulate 100 games: Random Human (X) vs Master AI (O)
        var aiWins = 0
        var draws = 0
        var humanWins = 0

        for (seed in 1..100) {
            val random = Random(seed)
            val board = MutableList<Player?>(9) { null }
            var turn = Player.X

            while (board.any { it == null } && !checkWin(board, Player.X) && !checkWin(board, Player.O)) {
                if (turn == Player.X) {
                    val available = board.indices.filter { board[it] == null }
                    val randomMove = available.random(random)
                    board[randomMove] = Player.X
                    turn = Player.O
                } else {
                    val aiMove = TicTacToeAI.findBestMove(board, Player.O, AiDifficulty.MASTER)
                    assertTrue("AI move must be legal", aiMove in 0..8 && board[aiMove] == null)
                    board[aiMove] = Player.O
                    turn = Player.X
                }
            }

            when {
                checkWin(board, Player.O) -> aiWins++
                checkWin(board, Player.X) -> humanWins++
                else -> draws++
            }
        }

        assertEquals("Master AI must never lose against random player", 0, humanWins)
        assertTrue("Master AI must win or draw all games", (aiWins + draws) == 100)
    }

    @Test
    fun masterAiNeverLosesAsFirstPlayerAgainstRandom() {
        // Simulate 100 games: Master AI (X) vs Random Human (O)
        var aiWins = 0
        var draws = 0
        var humanWins = 0

        for (seed in 101..200) {
            val random = Random(seed)
            val board = MutableList<Player?>(9) { null }
            var turn = Player.X

            while (board.any { it == null } && !checkWin(board, Player.X) && !checkWin(board, Player.O)) {
                if (turn == Player.X) {
                    val aiMove = TicTacToeAI.findBestMove(board, Player.X, AiDifficulty.MASTER)
                    assertTrue("AI move must be legal", aiMove in 0..8 && board[aiMove] == null)
                    board[aiMove] = Player.X
                    turn = Player.O
                } else {
                    val available = board.indices.filter { board[it] == null }
                    val randomMove = available.random(random)
                    board[randomMove] = Player.O
                    turn = Player.X
                }
            }

            when {
                checkWin(board, Player.X) -> aiWins++
                checkWin(board, Player.O) -> humanWins++
                else -> draws++
            }
        }

        assertEquals("Master AI as X must never lose against random player", 0, humanWins)
        assertTrue("Master AI as X must win or draw all games", (aiWins + draws) == 100)
    }

    private fun checkWin(board: List<Player?>, player: Player): Boolean {
        val winningCombinations = listOf(
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            listOf(0, 4, 8),
            listOf(2, 4, 6)
        )
        return winningCombinations.any { combo ->
            board[combo[0]] == player && board[combo[1]] == player && board[combo[2]] == player
        }
    }
}
