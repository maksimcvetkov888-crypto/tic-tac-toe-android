package com.tictactoe.game

import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.Player
import com.tictactoe.game.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameLogicTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel()
    }

    @Test
    fun testInitialGameState() {
        val state = viewModel.uiState.value
        assertEquals(Player.X, state.currentPlayer)
        assertEquals(GameStatus.InProgress, state.status)
        assertTrue(state.board.all { it == null })
        assertEquals(0, state.scores.xWins)
        assertEquals(0, state.scores.oWins)
        assertEquals(0, state.scores.draws)
    }

    @Test
    fun testTurnAlternation() {
        viewModel.processIntent(GameIntent.CellClick(0))
        assertEquals(Player.X, viewModel.uiState.value.board[0])
        assertEquals(Player.O, viewModel.uiState.value.currentPlayer)

        viewModel.processIntent(GameIntent.CellClick(4))
        assertEquals(Player.O, viewModel.uiState.value.board[4])
        assertEquals(Player.X, viewModel.uiState.value.currentPlayer)
    }

    @Test
    fun testWinConditionHorizontal() {
        // X: 0, O: 3, X: 1, O: 4, X: 2 -> X wins top row [0, 1, 2]
        viewModel.processIntent(GameIntent.CellClick(0)) // X
        viewModel.processIntent(GameIntent.CellClick(3)) // O
        viewModel.processIntent(GameIntent.CellClick(1)) // X
        viewModel.processIntent(GameIntent.CellClick(4)) // O
        viewModel.processIntent(GameIntent.CellClick(2)) // X

        val state = viewModel.uiState.value
        assertTrue(state.status is GameStatus.Won)
        val won = state.status as GameStatus.Won
        assertEquals(Player.X, won.winner)
        assertEquals(listOf(0, 1, 2), won.line)
        assertEquals(1, state.scores.xWins)
        assertEquals(1, state.winStreakX)
        assertEquals(0, state.winStreakO)
    }

    @Test
    fun testWinConditionDiagonal() {
        // X: 0, O: 1, X: 4, O: 2, X: 8 -> X wins main diagonal [0, 4, 8]
        viewModel.processIntent(GameIntent.CellClick(0))
        viewModel.processIntent(GameIntent.CellClick(1))
        viewModel.processIntent(GameIntent.CellClick(4))
        viewModel.processIntent(GameIntent.CellClick(2))
        viewModel.processIntent(GameIntent.CellClick(8))

        val state = viewModel.uiState.value
        assertTrue(state.status is GameStatus.Won)
        val won = state.status as GameStatus.Won
        assertEquals(Player.X, won.winner)
        assertEquals(listOf(0, 4, 8), won.line)
    }

    @Test
    fun testUndoMove() {
        viewModel.processIntent(GameIntent.CellClick(4)) // X at center
        assertEquals(Player.X, viewModel.uiState.value.board[4])
        assertEquals(Player.O, viewModel.uiState.value.currentPlayer)
        assertTrue(viewModel.uiState.value.canUndo)

        viewModel.processIntent(GameIntent.UndoMove)
        assertNull(viewModel.uiState.value.board[4])
        assertEquals(Player.X, viewModel.uiState.value.currentPlayer)
        assertFalse(viewModel.uiState.value.canUndo)
    }

    @Test
    fun testDrawCondition() {
        // Board layout for draw:
        // X O X
        // X X O
        // O X O
        val moves = listOf(0, 1, 2, 5, 3, 6, 4, 8, 7)
        // 0:X, 1:O, 2:X, 5:O, 3:X, 6:O, 4:X, 8:O, 7:X
        moves.forEach { viewModel.processIntent(GameIntent.CellClick(it)) }

        val state = viewModel.uiState.value
        assertEquals(GameStatus.Draw, state.status)
        assertEquals(1, state.scores.draws)
    }
}
