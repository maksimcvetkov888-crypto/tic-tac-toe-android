package com.tictactoe.game.viewmodel

import androidx.lifecycle.ViewModel
import com.tictactoe.game.model.CameraPreset
import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.MoveRecord
import com.tictactoe.game.model.Player
import com.tictactoe.game.model.Scores
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
            is GameIntent.UndoMove -> handleUndoMove()
            is GameIntent.SetTheme -> _uiState.update { it.copy(theme = intent.theme) }
            is GameIntent.SetCameraPreset -> _uiState.update { it.copy(cameraPreset = intent.preset) }
        }
    }

    private fun handleCellClick(index: Int) {
        val current = _uiState.value
        if (index !in 0..8 || current.board[index] != null || current.isFinished) {
            return
        }

        val updatedBoard = current.board.toMutableList().also { it[index] = current.currentPlayer }
        val updatedHistory = current.moveHistory + MoveRecord(index, current.currentPlayer)
        val winCombo = checkWin(updatedBoard, current.currentPlayer)

        if (winCombo != null) {
            val isX = current.currentPlayer == Player.X
            val updatedScores = if (isX) {
                current.scores.copy(xWins = current.scores.xWins + 1)
            } else {
                current.scores.copy(oWins = current.scores.oWins + 1)
            }
            val newStreakX = if (isX) current.winStreakX + 1 else 0
            val newStreakO = if (!isX) current.winStreakO + 1 else 0
            val streak = if (isX) newStreakX else newStreakO

            val commentary = if (streak > 1) {
                "[??????] ?????? ?????? ${current.currentPlayer.symbol}! ????? ????? ??????: $streak"
            } else {
                "[??????] ????????? ?????? ?????? ${current.currentPlayer.symbol} ? 3D ?????!"
            }

            _uiState.update {
                it.copy(
                    board = updatedBoard,
                    status = GameStatus.Won(current.currentPlayer, winCombo),
                    scores = updatedScores,
                    moveHistory = updatedHistory,
                    winStreakX = newStreakX,
                    winStreakO = newStreakO,
                    commentaryText = commentary
                )
            }
        } else if (updatedBoard.all { it != null }) {
            _uiState.update {
                it.copy(
                    board = updatedBoard,
                    status = GameStatus.Draw,
                    scores = current.scores.copy(draws = current.scores.draws + 1),
                    moveHistory = updatedHistory,
                    commentaryText = "[?????] ?????? ????? . ?????? ??????? ??????"
                )
            }
        } else {
            val commentary = generateMoveCommentary(index, current.currentPlayer, updatedBoard)
            _uiState.update {
                it.copy(
                    board = updatedBoard,
                    currentPlayer = current.currentPlayer.next(),
                    moveHistory = updatedHistory,
                    commentaryText = commentary
                )
            }
        }
    }

    private fun handleUndoMove() {
        val current = _uiState.value
        if (current.moveHistory.isEmpty() || current.isFinished) return

        val lastMove = current.moveHistory.last()
        val newHistory = current.moveHistory.dropLast(1)
        val newBoard = current.board.toMutableList().also { it[lastMove.index] = null }

        _uiState.update {
            it.copy(
                board = newBoard,
                currentPlayer = lastMove.player,
                moveHistory = newHistory,
                commentaryText = "[??????] ??? ??????? . ????? ????? ????? ${lastMove.player.symbol}"
            )
        }
    }

    private fun generateMoveCommentary(index: Int, player: Player, board: List<Player?>): String {
        for (combo in winningCombinations) {
            val count = combo.count { board[it] == player }
            val empty = combo.count { board[it] == null }
            if (count == 2 && empty == 1) {
                return "[?????] ??????? ??????! ????? ${player.symbol} ??????? ?????? ??????!"
            }
        }

        return when (index) {
            4 -> "[?????] ????? ???????? ??????? ${player.symbol} . ?????????????? ????????"
            0, 2, 6, 8 -> "[?????] ??????? ??????? . ????? ${player.symbol} ??????????? ?????"
            else -> "[???] ?????? ??? ?????? ${player.symbol} . ????????? ??? ${player.next().symbol}"
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
                status = GameStatus.InProgress,
                moveHistory = emptyList(),
                commentaryText = "[????] ????? ????? . ?????? ????? ????? ${nextStarter.symbol}"
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
                scores = Scores(),
                moveHistory = emptyList(),
                winStreakX = 0,
                winStreakO = 0,
                commentaryText = "[??????] ???? ??????? . ????? ?????? ??????"
            )
        }
    }
}
