package com.tictactoe.game.model

import androidx.compose.runtime.Immutable
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

enum class GameMode(val label: String) {
    PVP_LOCAL("2 ИГРОКА"),
    VS_AI("ПРОТИВ ИИ")
}

enum class AiDifficulty(val label: String) {
    EASY("НОВИЧОК"),
    MEDIUM("ТАКТИК"),
    MASTER("ГРАНДМАСТЕР")
}

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
        xColor = Color(0xFF38BDF8),
        oColor = Color(0xFFF97316),
        victoryColor = Color(0xFF10B981),
        gridLineColor = Color(0x2238BDF8)
    ),
    CYBER(
        displayName = "Cyber Matrix",
        slabColor = Color(0xFF061A14),
        slabSideColor = Color(0xFF0C2B22),
        cellColor = Color(0xFF08221B),
        xColor = Color(0xFFA3E635),
        oColor = Color(0xFFF43F5E),
        victoryColor = Color(0xFF2DD4BF),
        gridLineColor = Color(0x22A3E635)
    ),
    SOLAR(
        displayName = "Solar Gold",
        slabColor = Color(0xFF1E1C18),
        slabSideColor = Color(0xFF2D2922),
        cellColor = Color(0xFF24211B),
        xColor = Color(0xFFFACC15),
        oColor = Color(0xFF60A5FA),
        victoryColor = Color(0xFFF59E0B),
        gridLineColor = Color(0x22FACC15)
    )
}

enum class CameraPreset(
    val label: String,
    val rotX: Float,
    val rotY: Float
) {
    ORBIT("3D ORBIT", 24f, -16f),
    ISOMETRIC("ИЗОМЕТРИЯ", 35f, -35f),
    TOP_DOWN("СВЕРХУ", 0f, 0f)
}

@Immutable
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
    val gameMode: GameMode = GameMode.PVP_LOCAL,
    val aiDifficulty: AiDifficulty = AiDifficulty.MASTER,
    val isAiThinking: Boolean = false,
    val showDevHud: Boolean = false,
    val currentFps: Int = 60,
    val frameTimeMs: Float = 3.8f,
    val activePolygons: Int = 196,
    val moveHistory: List<MoveRecord> = emptyList(),
    val winStreakX: Int = 0,
    val winStreakO: Int = 0,
    val commentaryText: String = "[ТУРНИР] ДОБРО ПОЖАЛОВАТЬ В 3D ГРАНДМАСТЕР МАТЧ"
) {
    val isFinished: Boolean
        get() = status !is GameStatus.InProgress

    val canUndo: Boolean
        get() = moveHistory.isNotEmpty() && !isFinished && !isAiThinking
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
    data class SetGameMode(val mode: GameMode) : GameIntent
    data class SetAiDifficulty(val difficulty: AiDifficulty) : GameIntent
    data object ToggleDevHud : GameIntent
    data class UpdatePerformanceMetrics(val fps: Int, val frameTime: Float) : GameIntent
}
