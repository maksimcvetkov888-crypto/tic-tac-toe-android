package com.tictactoe.game.viewmodel

import androidx.lifecycle.ViewModel
import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

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

    fun processIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.CellClick -> handleCellClick(intent.index)
            is GameIntent.NewGame -> handleNewGame()
            is GameIntent.ResetScores -> handleResetScores()
            is GameIntent.ToggleSound -> _uiState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
            is GameIntent.ToggleHaptics -> _uiState.update { it.copy(isHapticsEnabled = !it.isHapticsEnabled) }
        }
    }

    private fun handleCellClick(index: Int) {
        val current = _uiState.value
        if (index !in 0..8 || current.board[index] != null || current.isFinished) {
            return
        }

        val updatedBoard = current.board.toMutableList().also { it[index] = current.currentPlayer }
        val winCombo = checkWin(updatedBoard, current.currentPlayer)

        if (winCombo != null) {
            val updatedScores = if (current.currentPlayer == Player.X) {
                current.scores.copy(xWins = current.scores.xWins + 1)
            } else {
                current.scores.copy(oWins = current.scores.oWins + 1)
            }
            _uiState.update {
                it.copy(
                    board = updatedBoard,
                    status = GameStatus.Won(current.currentPlayer, winCombo),
                    scores = updatedScores
                )
            }
        } else if (updatedBoard.all { it != null }) {
            _uiState.update {
                it.copy(
                    board = updatedBoard,
                    status = GameStatus.Draw,
                    scores = current.scores.copy(draws = current.scores.draws + 1)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    board = updatedBoard,
                    currentPlayer = current.currentPlayer.next()
                )
            }
        }
    }

    private fun checkWin(board: List<Player?>, player: Player): List<Int>? {
        for (combo in winningCombinations) {
            if (board[combo[0]] == player && board[combo[1]] == player && board[combo[2]] == player) {
                return combo
            }
        }
        return null
    }

    private fun handleNewGame() {
        val current = _uiState.value
        val nextStarter = current.startingPlayer.next()
        _uiState.update {
            it.copy(
                board = List(9) { null },
                currentPlayer = nextStarter,
                startingPlayer = nextStarter,
                status = GameStatus.InProgress
            )
        }
    }

    private fun handleResetScores() {
        val nextStarter = Player.X
        _uiState.update {
            it.copy(
                board = List(9) { null },
                currentPlayer = nextStarter,
                startingPlayer = nextStarter,
                status = GameStatus.InProgress,
                scores = com.tictactoe.game.model.Scores()
            )
        }
    }
}
