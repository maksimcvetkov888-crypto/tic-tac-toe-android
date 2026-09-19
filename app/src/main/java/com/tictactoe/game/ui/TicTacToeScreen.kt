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
import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.Player
import com.tictactoe.game.ui.components.BorderBeamContainer
import com.tictactoe.game.ui.components.ShimmerButton
import com.tictactoe.game.ui.components3d.Interactive3DBoard
import com.tictactoe.game.ui.theme.AccentVictory
import com.tictactoe.game.ui.theme.AccentVictoryGlow
import com.tictactoe.game.ui.theme.BackgroundObsidian
import com.tictactoe.game.ui.theme.BorderSubtle
import com.tictactoe.game.ui.theme.PlayerOFire
import com.tictactoe.game.ui.theme.PlayerOGlow
import com.tictactoe.game.ui.theme.PlayerXAzure
import com.tictactoe.game.ui.theme.PlayerXGlow
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
                if (state.isHapticsEnabled) soundEffects.vibrate(150)
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
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Editorial Header
            EditorialHeader(
                isSoundEnabled = state.isSoundEnabled,
                isHapticsEnabled = state.isHapticsEnabled,
                onToggleSound = {
                    soundEffects.playClick()
                    viewModel.processIntent(GameIntent.ToggleSound)
                },
                onToggleHaptics = {
                    soundEffects.vibrate(30)
                    viewModel.processIntent(GameIntent.ToggleHaptics)
                }
            )

            // Bento Grid: Score & Stats
            BentoScoreboard(state = state)

            // Active Turn Card with Magic UI Border Beam
            ActiveTurnCard(state = state)

            // Real 3D Kinetic Game Board with Orbit Drag
            Interactive3DBoard(
                state = state,
                onCellClick = { index ->
                    if (state.board[index] == null && !state.isFinished) {
                        if (state.isSoundEnabled) soundEffects.playMove(state.currentPlayer == Player.X)
                        if (state.isHapticsEnabled) soundEffects.vibrate(40)
                        viewModel.processIntent(GameIntent.CellClick(index))
                    }
                }
            )

            Text(
                text = "?? ???????? 3D ???? . ??????? ???: ????? ??????",
                fontSize = 11.sp,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )

            // Bottom Action Bar with Magic UI Shimmer Button
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
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "3D KINETIC EDITION",
                letterSpacing = 2.sp,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = TextMuted
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "????????-?????? 3D",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlPill(
                icon = if (isSoundEnabled) "??" else "??",
                onClick = onToggleSound
            )
            ControlPill(
                icon = if (isHapticsEnabled) "??" else "??",
                onClick = onToggleHaptics
            )
        }
    }
}

@Composable
private fun ControlPill(
    icon: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 16.sp)
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
            accentColor = PlayerXAzure,
            isActive = state.status is GameStatus.InProgress && state.currentPlayer == Player.X
        )

        BentoStatCard(
            modifier = Modifier.weight(0.85f),
            label = "?????",
            score = state.scores.draws,
            accentColor = TextMuted,
            isActive = false
        )

        BentoStatCard(
            modifier = Modifier.weight(1f),
            label = "PLAYER O",
            score = state.scores.oWins,
            accentColor = PlayerOFire,
            isActive = state.status is GameStatus.InProgress && state.currentPlayer == Player.O
        )
    }
}

@Composable
private fun BentoStatCard(
    modifier: Modifier = Modifier,
    label: String,
    score: Int,
    accentColor: Color,
    isActive: Boolean
) {
    val borderColor = if (isActive) accentColor.copy(alpha = 0.5f) else BorderSubtle
    val bgColor = if (isActive) SurfaceCard.copy(alpha = 0.95f) else SurfaceCard

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = score.toString(),
                fontSize = 24.sp,
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
                Triple("???: ????? 1 (X)", PlayerXAzure, PlayerXGlow)
            } else {
                Triple("???: ????? 2 (O)", PlayerOFire, PlayerOGlow)
            }
        }
        is GameStatus.Won -> {
            val winnerName = if (s.winner == Player.X) "????? 1 (X)" else "????? 2 (O)"
            Triple("??????: $winnerName ??", AccentVictory, AccentVictoryGlow)
        }
        GameStatus.Draw -> Triple("?????? ?????! ??", TextSecondary, TextMuted)
    }

    BorderBeamContainer(
        modifier = Modifier.fillMaxWidth(),
        colorFrom = beamColor,
        colorTo = Color.Transparent,
        strokeWidth = 2.dp,
        durationMillis = 2400,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(vertical = 10.dp, horizontal = 16.dp),
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
                    fontSize = 15.sp,
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
            height = 52.dp
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
    }
}
