package com.tictactoe.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tictactoe.game.ai.TicTacToeAI
import com.tictactoe.game.model.AiDifficulty
import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameMode
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.MoveRecord
import com.tictactoe.game.model.Player
import com.tictactoe.game.model.Scores
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            is GameIntent.SetGameMode -> handleSetGameMode(intent.mode)
            is GameIntent.SetAiDifficulty -> _uiState.update { it.copy(aiDifficulty = intent.difficulty) }
            is GameIntent.ToggleDevHud -> _uiState.update { it.copy(showDevHud = !it.showDevHud) }
            is GameIntent.UpdatePerformanceMetrics -> _uiState.update {
                it.copy(currentFps = intent.fps, frameTimeMs = intent.frameTime)
            }
        }
    }

    private fun handleSetGameMode(mode: GameMode) {
        val commentary = if (mode == GameMode.VS_AI) {
            "[РЕЖИМ] БИТВА С 3D ИИ • ВАШ ХОД (Х)"
        } else {
            "[РЕЖИМ] 2 ИГРОКА НА ОДНОМ УСТРОЙСТВЕ"
        }
        _uiState.update {
            it.copy(
                gameMode = mode,
                board = List(9) { null },
                currentPlayer = Player.X,
                startingPlayer = Player.X,
                status = GameStatus.InProgress,
                moveHistory = emptyList(),
                isAiThinking = false,
                commentaryText = commentary
            )
        }
    }

    private fun handleCellClick(index: Int) {
        val current = _uiState.value
        if (index !in 0..8 || current.board[index] != null || current.isFinished || current.isAiThinking) {
            return
        }

        val updatedBoard = current.board.toMutableList().also { it[index] = current.currentPlayer }
        val updatedHistory = current.moveHistory + MoveRecord(index, current.currentPlayer)
        val winCombo = checkWin(updatedBoard, current.currentPlayer)

        if (winCombo != null) {
            applyWinState(current.currentPlayer, winCombo, updatedBoard, updatedHistory)
        } else if (updatedBoard.all { it != null }) {
            applyDrawState(updatedBoard, updatedHistory)
        } else {
            val nextPlayer = current.currentPlayer.next()
            val commentary = generateMoveCommentary(index, current.currentPlayer, updatedBoard)

            _uiState.update {
                it.copy(
                    board = updatedBoard,
                    currentPlayer = nextPlayer,
                    moveHistory = updatedHistory,
                    commentaryText = commentary
                )
            }

            // Trigger AI response if playing in VS_AI mode and it's AI's turn (O)
            if (current.gameMode == GameMode.VS_AI && nextPlayer == Player.O) {
                triggerAiTurn(updatedBoard, updatedHistory)
            }
        }
    }

    private fun triggerAiTurn(currentBoard: List<Player?>, currentHistory: List<MoveRecord>) {
        _uiState.update {
            it.copy(
                isAiThinking = true,
                commentaryText = "[ИИ] ГРАНДМАСТЕР РАССЧИТЫВАЕТ ОПТИМАЛЬНЫЙ ХОД..."
            )
        }

        viewModelScope.launch {
            // Realistic deliberate thinking time (360ms)
            delay(360)

            val stateNow = _uiState.value
            if (stateNow.isFinished || stateNow.currentPlayer != Player.O) {
                _uiState.update { it.copy(isAiThinking = false) }
                return@launch
            }

            val aiMove = TicTacToeAI.findBestMove(
                board = currentBoard,
                aiPlayer = Player.O,
                difficulty = stateNow.aiDifficulty
            )

            if (aiMove != -1 && currentBoard[aiMove] == null) {
                val aiBoard = currentBoard.toMutableList().also { it[aiMove] = Player.O }
                val aiHistory = currentHistory + MoveRecord(aiMove, Player.O)
                val winCombo = checkWin(aiBoard, Player.O)

                if (winCombo != null) {
                    applyWinState(Player.O, winCombo, aiBoard, aiHistory)
                } else if (aiBoard.all { it != null }) {
                    applyDrawState(aiBoard, aiHistory)
                } else {
                    val commentary = generateMoveCommentary(aiMove, Player.O, aiBoard)
                    _uiState.update {
                        it.copy(
                            board = aiBoard,
                            currentPlayer = Player.X,
                            moveHistory = aiHistory,
                            isAiThinking = false,
                            commentaryText = commentary
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isAiThinking = false) }
            }
        }
    }

    private fun applyWinState(
        winner: Player,
        winCombo: List<Int>,
        board: List<Player?>,
        history: List<MoveRecord>
    ) {
        val current = _uiState.value
        val isX = winner == Player.X
        val updatedScores = if (isX) {
            current.scores.copy(xWins = current.scores.xWins + 1)
        } else {
            current.scores.copy(oWins = current.scores.oWins + 1)
        }
        val newStreakX = if (isX) current.winStreakX + 1 else 0
        val newStreakO = if (!isX) current.winStreakO + 1 else 0
        val streak = if (isX) newStreakX else newStreakO

        val commentary = if (current.gameMode == GameMode.VS_AI) {
            if (isX) "[ПОБЕДА] ВЫ ПОБЕДИЛИ 3D ИИ! БЛЕСТЯЩАЯ ТАКТИКА!" else "[ИИ] ПОБЕДА ГРАНДМАСТЕР ИИ! ПОПРОБУЙТЕ СНОВА"
        } else {
            if (streak > 1) {
                "[ПОБЕДА] ТРИУМФ ИГРОКА ${winner.symbol}! СЕРИЯ ПОБЕД ПОДРЯД: $streak"
            } else {
                "[ПОБЕДА] БЛЕСТЯЩАЯ ПОБЕДА ИГРОКА ${winner.symbol} В 3D МАТЧЕ!"
            }
        }

        _uiState.update {
            it.copy(
                board = board,
                status = GameStatus.Won(winner, winCombo),
                scores = updatedScores,
                moveHistory = history,
                winStreakX = newStreakX,
                winStreakO = newStreakO,
                isAiThinking = false,
                commentaryText = commentary
            )
        }
    }

    private fun applyDrawState(board: List<Player?>, history: List<MoveRecord>) {
        val current = _uiState.value
        _uiState.update {
            it.copy(
                board = board,
                status = GameStatus.Draw,
                scores = current.scores.copy(draws = current.scores.draws + 1),
                moveHistory = history,
                isAiThinking = false,
                commentaryText = "[НИЧЬЯ] БОЕВАЯ НИЧЬЯ • ПОЛНЫЙ ПАРИТЕТ СТОРОН"
            )
        }
    }

    private fun handleUndoMove() {
        val current = _uiState.value
        if (current.moveHistory.isEmpty() || current.isFinished || current.isAiThinking) return

        if (current.gameMode == GameMode.VS_AI) {
            // In VS_AI mode: rollback 2 moves (AI move and player move) to return turn to player
            val countToDrop = if (current.moveHistory.size >= 2) 2 else 1
            val newHistory = current.moveHistory.dropLast(countToDrop)
            val newBoard = MutableList<Player?>(9) { null }
            newHistory.forEach { newBoard[it.index] = it.player }

            _uiState.update {
                it.copy(
                    board = newBoard,
                    currentPlayer = Player.X,
                    moveHistory = newHistory,
                    commentaryText = "[ОТМЕНА] ХОД ОТМЕНЁН • ВАШ ХОД (Х)"
                )
            }
        } else {
            // In 2-player local mode: rollback 1 move
            val lastMove = current.moveHistory.last()
            val newHistory = current.moveHistory.dropLast(1)
            val newBoard = current.board.toMutableList().also { it[lastMove.index] = null }

            _uiState.update {
                it.copy(
                    board = newBoard,
                    currentPlayer = lastMove.player,
                    moveHistory = newHistory,
                    commentaryText = "[ОТМЕНА] ХОД ОТМЕНЁН • СНОВА ХОДИТ ИГРОК ${lastMove.player.symbol}"
                )
            }
        }
    }

    private fun generateMoveCommentary(index: Int, player: Player, board: List<Player?>): String {
        for (combo in winningCombinations) {
            val count = combo.count { board[it] == player }
            val empty = combo.count { board[it] == null }
            if (count == 2 && empty == 1) {
                return "[АТАКА] ОПАСНЫЙ МОМЕНТ! ИГРОК ${player.symbol} СОЗДАЁТ УГРОЗУ ПОБЕДЫ!"
            }
        }

        return when (index) {
            4 -> "[ЦЕНТР] ЦЕНТР ЗАХВАЧЕН ИГРОКОМ ${player.symbol} • СТРАТЕГИЧЕСКИЙ КОНТРОЛЬ"
            0, 2, 6, 8 -> "[ФЛАНГ] УГЛОВАЯ ПОЗИЦИЯ • ИГРОК ${player.symbol} ВЫСТРАИВАЕТ КЛЕЩИ"
            else -> "[ХОД] ТОЧНЫЙ ХОД ИГРОКА ${player.symbol} • СЛЕДУЮЩИЙ ХОД ${player.next().symbol}"
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
        val nextStarter = if (current.gameMode == GameMode.VS_AI) Player.X else current.startingPlayer.next()
        _uiState.update {
            it.copy(
                board = List(9) { null },
                currentPlayer = nextStarter,
                startingPlayer = nextStarter,
                status = GameStatus.InProgress,
                moveHistory = emptyList(),
                isAiThinking = false,
                commentaryText = "[МАТЧ] РАУНД НАЧАТ • ПЕРВЫМ ХОДИТ ИГРОК ${nextStarter.symbol}"
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
                isAiThinking = false,
                commentaryText = "[ТУРНИР] СЧЁТ СБРОШЕН • СТАРТ НОВОГО СЕЗОНА"
            )
        }
    }
}
