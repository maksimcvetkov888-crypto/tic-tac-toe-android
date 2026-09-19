package com.tictactoe.game.ui

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
import com.tictactoe.game.model.BoardTheme
import com.tictactoe.game.model.CameraPreset
import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.Player
import com.tictactoe.game.ui.components.BorderBeamContainer
import com.tictactoe.game.ui.components.MagicMarqueeBar
import com.tictactoe.game.ui.components.ShimmerButton
import com.tictactoe.game.ui.components3d.Interactive3DBoard
import com.tictactoe.game.ui.theme.AccentVictory
import com.tictactoe.game.ui.theme.AccentVictoryGlow
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Editorial Header
            EditorialHeader(
                isSoundEnabled = state.isSoundEnabled,
                isHapticsEnabled = state.isHapticsEnabled,
                currentTheme = state.theme,
                onToggleSound = {
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.ToggleSound)
                },
                onToggleHaptics = {
                    soundEffects.vibrate(30)
                    viewModel.processIntent(GameIntent.ToggleHaptics)
                },
                onSelectTheme = { theme ->
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.SetTheme(theme))
                }
            )

            // 2. Magic UI Marquee Live Commentary Ticker
            MagicMarqueeBar(
                text = state.commentaryText,
                accentColor = state.theme.xColor,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 3. Bento Scoreboard with Win Streak Counters
            BentoScoreboard(state = state)

            // 4. Active Turn Card with Magic UI Border Beam
            ActiveTurnCard(state = state)

            // 5. Camera Angle Presets & Undo Row
            ControlsRow(
                currentPreset = state.cameraPreset,
                canUndo = state.canUndo,
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

            // 6. Real 3D Kinetic Game Board
            Interactive3DBoard(
                state = state,
                onCellClick = { index ->
                    if (state.board[index] == null && !state.isFinished) {
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

            Text(
                text = "?? ???????? 3D ???? . ??????? ???: ????? ??????",
                fontSize = 11.sp,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )

            // 7. Bottom Action Bar with Magic UI Shimmer Button
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
    currentTheme: BoardTheme,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
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
                text = "????????-??????",
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
            // Theme Selector Cycle Pill
            ControlPill(
                label = when (currentTheme) {
                    BoardTheme.OBSIDIAN -> "?? Neo"
                    BoardTheme.CYBER -> "?? Cyber"
                    BoardTheme.SOLAR -> "?? Solar"
                },
                onClick = {
                    val nextTheme = when (currentTheme) {
                        BoardTheme.OBSIDIAN -> BoardTheme.CYBER
                        BoardTheme.CYBER -> BoardTheme.SOLAR
                        BoardTheme.SOLAR -> BoardTheme.OBSIDIAN
                    }
                    onSelectTheme(nextTheme)
                }
            )

            ControlPill(
                label = if (isSoundEnabled) "??" else "??",
                onClick = onToggleSound
            )
            ControlPill(
                label = if (isHapticsEnabled) "??" else "??",
                onClick = onToggleHaptics
            )
        }
    }
}

@Composable
private fun ControlsRow(
    currentPreset: CameraPreset,
    canUndo: Boolean,
    onSelectPreset: (CameraPreset) -> Unit,
    onUndo: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera Presets
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CameraPreset.values().forEach { preset ->
                val isSelected = preset == currentPreset
                val bg = if (isSelected) SurfaceCard.copy(alpha = 0.95f) else Color.Transparent
                val border = if (isSelected) TextSecondary.copy(alpha = 0.5f) else BorderSubtle

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(6.dp))
                        .clickable { onSelectPreset(preset) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = preset.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextMuted
                    )
                }
            }
        }

        // Undo Button
        if (canUndo) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .clickable(onClick = onUndo)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "? ??????",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ControlPill(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BentoScoreboard(state: GameUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BentoStatCard(
            modifier = Modifier.weight(1f),
            label = "PLAYER X",
            score = state.scores.xWins,
            streak = state.winStreakX,
            accentColor = state.theme.xColor,
            isActive = state.status is GameStatus.InProgress && state.currentPlayer == Player.X
        )

        BentoStatCard(
            modifier = Modifier.weight(0.82f),
            label = "?????",
            score = state.scores.draws,
            streak = 0,
            accentColor = TextMuted,
            isActive = false
        )

        BentoStatCard(
            modifier = Modifier.weight(1f),
            label = "PLAYER O",
            score = state.scores.oWins,
            streak = state.winStreakO,
            accentColor = state.theme.oColor,
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
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "??$streak", fontSize = 9.sp, fontWeight = FontWeight.Black)
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
            if (state.currentPlayer == Player.X) {
                Triple("???: ????? 1 (X)", state.theme.xColor, state.theme.xColor.copy(alpha = 0.5f))
            } else {
                Triple("???: ????? 2 (O)", state.theme.oColor, state.theme.oColor.copy(alpha = 0.5f))
            }
        }
        is GameStatus.Won -> {
            val winnerName = if (s.winner == Player.X) "????? 1 (X)" else "????? 2 (O)"
            Triple("??????: $winnerName ??", state.theme.victoryColor, state.theme.victoryColor.copy(alpha = 0.6f))
        }
        GameStatus.Draw -> Triple("?????? ?????! ??", TextSecondary, TextMuted)
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
                .padding(vertical = 9.dp, horizontal = 14.dp),
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
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
            height = 50.dp
        ) {
            Text(
                text = "????? ????",
                letterSpacing = 1.5.sp,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        TextButton(onClick = onResetScore) {
            Text(
                text = "???????? ????",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
    }
}
