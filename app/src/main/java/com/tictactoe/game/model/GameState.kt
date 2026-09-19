package com.tictactoe.game.model

enum class Player(val symbol: String) {
    X("X"),
    O("O");

    fun next(): Player = if (this == X) O else X
}

sealed interface GameStatus {
    data object InProgress : GameStatus
    data class Won(val winner: Player, val line: List<Int>) : GameStatus
    data object Draw : GameStatus
}

data class Scores(
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0
)

data class GameUiState(
    val board: List<Player?> = List(9) { null },
    val currentPlayer: Player = Player.X,
    val startingPlayer: Player = Player.X,
    val status: GameStatus = GameStatus.InProgress,
    val scores: Scores = Scores(),
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true
) {
    val isFinished: Boolean
        get() = status !is GameStatus.InProgress
}

sealed interface GameIntent {
    data class CellClick(val index: Int) : GameIntent
    data object NewGame : GameIntent
    data object ResetScores : GameIntent
    data object ToggleSound : GameIntent
    data object ToggleHaptics : GameIntent
}
