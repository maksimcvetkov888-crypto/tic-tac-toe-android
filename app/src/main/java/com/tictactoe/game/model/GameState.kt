package com.tictactoe.game.model

import androidx.compose.ui.graphics.Color

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

data class MoveRecord(
    val index: Int,
    val player: Player
)

enum class BoardTheme(
    val displayName: String,
    val slabColor: Color,
    val slabSideColor: Color,
    val cellColor: Color,
    val xColor: Color,
    val oColor: Color,
    val victoryColor: Color,
    val gridLineColor: Color
) {
    OBSIDIAN(
        displayName = "Obsidian Neo",
        slabColor = Color(0xFF141417),
        slabSideColor = Color(0xFF1C1C21),
        cellColor = Color(0xFF18181D),
        xColor = Color(0xFF38BDF8), // Electric Azure
        oColor = Color(0xFFF97316), // Vivid Flame
        victoryColor = Color(0xFF10B981), // Emerald
        gridLineColor = Color(0x2238BDF8)
    ),
    CYBER(
        displayName = "Cyber Matrix",
        slabColor = Color(0xFF061A14),
        slabSideColor = Color(0xFF0C2B22),
        cellColor = Color(0xFF08221B),
        xColor = Color(0xFFA3E635), // Acid Lime
        oColor = Color(0xFFF43F5E), // Hot Rose / Magenta
        victoryColor = Color(0xFF2DD4BF), // Teal
        gridLineColor = Color(0x22A3E635)
    ),
    SOLAR(
        displayName = "Solar Gold",
        slabColor = Color(0xFF1E1C18),
        slabSideColor = Color(0xFF2D2922),
        cellColor = Color(0xFF24211B),
        xColor = Color(0xFFFACC15), // Polished 24K Gold
        oColor = Color(0xFF60A5FA), // Deep Sapphire
        victoryColor = Color(0xFFF59E0B), // Amber
        gridLineColor = Color(0x22FACC15)
    )
}

enum class CameraPreset(
    val label: String,
    val rotX: Float,
    val rotY: Float
) {
    ORBIT("3D Orbit", 24f, -16f),
    ISOMETRIC("?????????", 35f, -35f),
    TOP_DOWN("???????", 0f, 0f)
}

data class GameUiState(
    val board: List<Player?> = List(9) { null },
    val currentPlayer: Player = Player.X,
    val startingPlayer: Player = Player.X,
    val status: GameStatus = GameStatus.InProgress,
    val scores: Scores = Scores(),
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val theme: BoardTheme = BoardTheme.OBSIDIAN,
    val cameraPreset: CameraPreset = CameraPreset.ORBIT,
    val moveHistory: List<MoveRecord> = emptyList(),
    val winStreakX: Int = 0,
    val winStreakO: Int = 0,
    val commentaryText: String = "????? ?????????? ? 3D ??????????? ??????"
) {
    val isFinished: Boolean
        get() = status !is GameStatus.InProgress

    val canUndo: Boolean
        get() = moveHistory.isNotEmpty() && !isFinished
}

sealed interface GameIntent {
    data class CellClick(val index: Int) : GameIntent
    data object NewGame : GameIntent
    data object ResetScores : GameIntent
    data object ToggleSound : GameIntent
    data object ToggleHaptics : GameIntent
    data object UndoMove : GameIntent
    data class SetTheme(val theme: BoardTheme) : GameIntent
    data class SetCameraPreset(val preset: CameraPreset) : GameIntent
}
