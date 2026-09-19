package com.tictactoe.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tictactoe.game.audio.SoundEffects
import com.tictactoe.game.model.AiDifficulty
import com.tictactoe.game.model.BoardTheme
import com.tictactoe.game.model.CameraPreset
import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameMode
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.Player
import com.tictactoe.game.ui.components.AiVectorIcon
import com.tictactoe.game.ui.components.BorderBeamContainer
import com.tictactoe.game.ui.components.DevHudVectorIcon
import com.tictactoe.game.ui.components.FlameStreakIcon
import com.tictactoe.game.ui.components.HapticsVectorIcon
import com.tictactoe.game.ui.components.MagicMarqueeBar
import com.tictactoe.game.ui.components.ShimmerButton
import com.tictactoe.game.ui.components.SoundVectorIcon
import com.tictactoe.game.ui.components.ThemeVectorIcon
import com.tictactoe.game.ui.components.UndoVectorIcon
import com.tictactoe.game.ui.components3d.Interactive3DBoard
import com.tictactoe.game.ui.theme.BackgroundObsidian
import com.tictactoe.game.ui.theme.BorderSubtle
import com.tictactoe.game.ui.theme.SurfaceCard
import com.tictactoe.game.ui.theme.TextMuted
import com.tictactoe.game.ui.theme.TextPrimary
import com.tictactoe.game.ui.theme.TextSecondary
import com.tictactoe.game.viewmodel.GameViewModel

@Composable
fun TicTacToeScreen(
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val soundEffects = remember { SoundEffects(context) }

    LaunchedEffect(state.status) {
        when (state.status) {
            is GameStatus.Won -> {
                if (state.isSoundEnabled) soundEffects.playWin()
                if (state.isHapticsEnabled) soundEffects.vibrateWin()
            }
            is GameStatus.Draw -> {
                if (state.isSoundEnabled) soundEffects.playDraw()
                if (state.isHapticsEnabled) soundEffects.vibrate(80)
            }
            GameStatus.InProgress -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundObsidian)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Editorial Header with Vector Icons & Dev HUD toggle
            EditorialHeader(
                isSoundEnabled = state.isSoundEnabled,
                isHapticsEnabled = state.isHapticsEnabled,
                showDevHud = state.showDevHud,
                currentTheme = state.theme,
                onToggleSound = {
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.ToggleSound)
                },
                onToggleHaptics = {
                    soundEffects.vibrate(30)
                    viewModel.processIntent(GameIntent.ToggleHaptics)
                },
                onToggleDevHud = {
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.ToggleDevHud)
                },
                onSelectTheme = { theme ->
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.SetTheme(theme))
                }
            )

            // 2. Dev HUD Telemetry Bar (Conditional)
            if (state.showDevHud) {
                DevHudTelemetryBar(state = state)
            }

            // 3. Game Mode & AI Difficulty Switcher
            GameModeSelectorBar(
                currentMode = state.gameMode,
                currentDifficulty = state.aiDifficulty,
                theme = state.theme,
                onSelectMode = { mode ->
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.SetGameMode(mode))
                },
                onSelectDifficulty = { diff ->
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.SetAiDifficulty(diff))
                }
            )

            // 4. Magic UI Marquee Live Commentary Ticker
            MagicMarqueeBar(
                text = state.commentaryText,
                accentColor = state.theme.xColor,
                modifier = Modifier.padding(vertical = 1.dp)
            )

            // 5. Bento Scoreboard with Vector Flame Streak Counters
            BentoScoreboard(state = state)

            // 6. Active Turn Card with Magic UI Border Beam
            ActiveTurnCard(state = state)

            // 7. Camera Presets & Undo Button Row
            ControlsRow(
                currentPreset = state.cameraPreset,
                canUndo = state.canUndo,
                theme = state.theme,
                onSelectPreset = { preset ->
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.SetCameraPreset(preset))
                },
                onUndo = {
                    soundEffects.playClick()
                    soundEffects.vibrate(30)
                    viewModel.processIntent(GameIntent.UndoMove)
                }
            )

            // 8. Real 3D Responsive Kinetic Board
            Interactive3DBoard(
                state = state,
                onCellClick = { index ->
                    if (state.board[index] == null && !state.isFinished && !state.isAiThinking) {
                        if (state.isSoundEnabled) soundEffects.playMove(state.currentPlayer == Player.X)
                        if (state.isHapticsEnabled) soundEffects.vibrate(35)
                        viewModel.processIntent(GameIntent.CellClick(index))
                    }
                },
                onImpact = {
                    if (state.isSoundEnabled) soundEffects.playImpact()
                    if (state.isHapticsEnabled) soundEffects.vibrateImpact()
                }
            )

            // 9. Tactical Control Hint
            Text(
                text = "ВРАЩАЙТЕ 3D ПОЛЕ • ДВОЙНОЕ НАЖАТИЕ: СБРОС КАМЕРЫ",
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )

            // 10. Bottom Action Bar with Magic UI Shimmer Button
            BottomActionBar(
                onNewGame = {
                    if (state.isSoundEnabled) soundEffects.playClick()
                    if (state.isHapticsEnabled) soundEffects.vibrate(40)
                    viewModel.processIntent(GameIntent.NewGame)
                },
                onResetScore = {
                    if (state.isSoundEnabled) soundEffects.playClick()
                    if (state.isHapticsEnabled) soundEffects.vibrate(60)
                    viewModel.processIntent(GameIntent.ResetScores)
                }
            )
        }

        // Particle Celebration Confetti
        if (state.status is GameStatus.Won) {
            ConfettiEffect()
        }
    }
}

@Composable
private fun EditorialHeader(
    isSoundEnabled: Boolean,
    isHapticsEnabled: Boolean,
    showDevHud: Boolean,
    currentTheme: BoardTheme,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleDevHud: () -> Unit,
    onSelectTheme: (BoardTheme) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "GRANDMASTER 3D",
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = TextMuted
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Крестики-Нолики",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Selector Vector Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable {
                        val nextTheme = when (currentTheme) {
                            BoardTheme.OBSIDIAN -> BoardTheme.CYBER
                            BoardTheme.CYBER -> BoardTheme.SOLAR
                            BoardTheme.SOLAR -> BoardTheme.OBSIDIAN
                        }
                        onSelectTheme(nextTheme)
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThemeVectorIcon(theme = currentTheme, size = 14.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when (currentTheme) {
                            BoardTheme.OBSIDIAN -> "NEO"
                            BoardTheme.CYBER -> "CYBER"
                            BoardTheme.SOLAR -> "SOLAR"
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Dev HUD Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (showDevHud) SurfaceCard.copy(alpha = 0.95f) else SurfaceCard)
                    .border(1.dp, if (showDevHud) Color(0xFF38BDF8) else BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggleDevHud)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                DevHudVectorIcon(
                    isActive = showDevHud,
                    color = if (showDevHud) Color(0xFF38BDF8) else TextMuted,
                    size = 15.dp
                )
            }

            // Sound Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggleSound)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                SoundVectorIcon(isEnabled = isSoundEnabled, color = if (isSoundEnabled) TextPrimary else TextMuted, size = 15.dp)
            }

            // Haptics Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggleHaptics)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                HapticsVectorIcon(isEnabled = isHapticsEnabled, color = if (isHapticsEnabled) TextPrimary else TextMuted, size = 15.dp)
            }
        }
    }
}

@Composable
private fun DevHudTelemetryBar(state: GameUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xE60A0A0F))
            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "FPS: ${state.currentFps}",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981)
        )
        Text(
            text = "FRAME: ${state.frameTimeMs}ms",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF38BDF8)
        )
        Text(
            text = "POLYS: ${state.activePolygons}",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFFACC15)
        )
        Text(
            text = "GC: 0 B/f",
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFA78BFA)
        )
    }
}

@Composable
private fun GameModeSelectorBar(
    currentMode: GameMode,
    currentDifficulty: AiDifficulty,
    theme: BoardTheme,
    onSelectMode: (GameMode) -> Unit,
    onSelectDifficulty: (AiDifficulty) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Switch Segment Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GameMode.values().forEach { mode ->
                val isSelected = mode == currentMode
                val bg = if (isSelected) theme.xColor.copy(alpha = 0.2f) else Color.Transparent
                val border = if (isSelected) theme.xColor.copy(alpha = 0.6f) else Color.Transparent
                val textColor = if (isSelected) TextPrimary else TextMuted

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(8.dp))
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mode == GameMode.VS_AI) {
                            AiVectorIcon(color = if (isSelected) theme.xColor else TextMuted, size = 12.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = mode.label,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Difficulty Selector Badges (Visible when VS_AI)
        AnimatedVisibility(
            visible = currentMode == GameMode.VS_AI,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AiDifficulty.values().forEach { diff ->
                    val isSelected = diff == currentDifficulty
                    val bg = if (isSelected) SurfaceCard.copy(alpha = 0.95f) else Color.Transparent
                    val border = if (isSelected) theme.oColor.copy(alpha = 0.6f) else BorderSubtle.copy(alpha = 0.4f)
                    val textColor = if (isSelected) theme.oColor else TextMuted

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(6.dp))
                            .clickable { onSelectDifficulty(diff) }
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diff.label,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlsRow(
    currentPreset: CameraPreset,
    canUndo: Boolean,
    theme: BoardTheme,
    onSelectPreset: (CameraPreset) -> Unit,
    onUndo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera Preset Badges
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            CameraPreset.values().forEach { preset ->
                val isSelected = preset == currentPreset
                val bg = if (isSelected) SurfaceCard.copy(alpha = 0.95f) else Color.Transparent
                val border = if (isSelected) theme.xColor.copy(alpha = 0.6f) else BorderSubtle
                val textColor = if (isSelected) TextPrimary else TextMuted

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(6.dp))
                        .clickable { onSelectPreset(preset) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = when (preset) {
                            CameraPreset.ORBIT -> "3D ORBIT"
                            CameraPreset.ISOMETRIC -> "ИЗОМЕТРИЯ"
                            CameraPreset.TOP_DOWN -> "СВЕРХУ"
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }

        // Undo Button with Vector Icon
        if (canUndo) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .clickable(onClick = onUndo)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UndoVectorIcon(color = theme.xColor, size = 12.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ОТМЕНА",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoScoreboard(state: GameUiState) {
    val labelX = if (state.gameMode == GameMode.VS_AI) "ВЫ (X)" else "PLAYER X"
    val labelO = if (state.gameMode == GameMode.VS_AI) "3D ИИ (O)" else "PLAYER O"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BentoStatCard(
            modifier = Modifier.weight(1f),
            label = labelX,
            score = state.scores.xWins,
            streak = state.winStreakX,
            accentColor = state.theme.xColor,
            victoryColor = state.theme.victoryColor,
            isActive = state.status is GameStatus.InProgress && state.currentPlayer == Player.X
        )

        BentoStatCard(
            modifier = Modifier.weight(0.82f),
            label = "НИЧЬИ",
            score = state.scores.draws,
            streak = 0,
            accentColor = TextMuted,
            victoryColor = state.theme.victoryColor,
            isActive = false
        )

        BentoStatCard(
            modifier = Modifier.weight(1f),
            label = labelO,
            score = state.scores.oWins,
            streak = state.winStreakO,
            accentColor = state.theme.oColor,
            victoryColor = state.theme.victoryColor,
            isActive = state.status is GameStatus.InProgress && state.currentPlayer == Player.O
        )
    }
}

@Composable
private fun BentoStatCard(
    modifier: Modifier = Modifier,
    label: String,
    score: Int,
    streak: Int,
    accentColor: Color,
    victoryColor: Color,
    isActive: Boolean
) {
    val borderColor = if (isActive) accentColor.copy(alpha = 0.6f) else BorderSubtle
    val bgColor = if (isActive) SurfaceCard.copy(alpha = 0.95f) else SurfaceCard

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = accentColor
                )
                if (streak > 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    FlameStreakIcon(color = victoryColor, size = 11.dp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = streak.toString(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = victoryColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = score.toString(),
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ActiveTurnCard(state: GameUiState) {
    val (statusText, statusColor, beamColor) = when (val s = state.status) {
        is GameStatus.InProgress -> {
            if (state.isAiThinking) {
                Triple("ИИ ДУМАЕТ...", state.theme.oColor, state.theme.oColor.copy(alpha = 0.65f))
            } else if (state.currentPlayer == Player.X) {
                val txt = if (state.gameMode == GameMode.VS_AI) "ВАШ ХОД (X)" else "ХОД: ИГРОК 1 (X)"
                Triple(txt, state.theme.xColor, state.theme.xColor.copy(alpha = 0.5f))
            } else {
                val txt = if (state.gameMode == GameMode.VS_AI) "ХОД ИИ (O)" else "ХОД: ИГРОК 2 (O)"
                Triple(txt, state.theme.oColor, state.theme.oColor.copy(alpha = 0.5f))
            }
        }
        is GameStatus.Won -> {
            val winnerName = if (state.gameMode == GameMode.VS_AI) {
                if (s.winner == Player.X) "ВЫ (X)" else "ГРАНДМАСТЕР ИИ (O)"
            } else {
                if (s.winner == Player.X) "ИГРОК 1 (X)" else "ИГРОК 2 (O)"
            }
            Triple("ПОБЕДА: $winnerName", state.theme.victoryColor, state.theme.victoryColor.copy(alpha = 0.65f))
        }
        GameStatus.Draw -> Triple("БОЕВАЯ НИЧЬЯ", TextSecondary, TextMuted)
    }

    BorderBeamContainer(
        modifier = Modifier.fillMaxWidth(),
        colorFrom = beamColor,
        colorTo = Color.Transparent,
        strokeWidth = 2.dp,
        durationMillis = 2400,
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(vertical = 8.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 13.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    onNewGame: () -> Unit,
    onResetScore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerButton(
            onClick = onNewGame,
            shape = RoundedCornerShape(14.dp),
            height = 48.dp
        ) {
            Text(
                text = "НОВАЯ ИГРА",
                letterSpacing = 1.5.sp,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        TextButton(onClick = onResetScore) {
            Text(
                text = "Сбросить счёт",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
    }
}
