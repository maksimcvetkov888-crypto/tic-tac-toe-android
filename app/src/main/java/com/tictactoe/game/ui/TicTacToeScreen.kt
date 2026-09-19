package com.tictactoe.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tictactoe.game.audio.SoundEffects
import com.tictactoe.game.model.GameIntent
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.Player
import com.tictactoe.game.ui.theme.AccentPurple
import com.tictactoe.game.ui.theme.BackgroundDark
import com.tictactoe.game.ui.theme.CardDark
import com.tictactoe.game.ui.theme.CellBorder
import com.tictactoe.game.ui.theme.CellDark
import com.tictactoe.game.ui.theme.CellWinning
import com.tictactoe.game.ui.theme.CellWinningBorder
import com.tictactoe.game.ui.theme.PlayerOCoral
import com.tictactoe.game.ui.theme.PlayerXCyan
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
            .background(BackgroundDark)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            HeaderSection(
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

            // Scoreboard Card
            ScoreboardSection(scores = state.scores)

            // Status Banner (Turn / Winner)
            StatusBanner(state = state)

            // 3x3 Game Board
            GameBoard(
                state = state,
                onCellClick = { index ->
                    if (state.board[index] == null && !state.isFinished) {
                        if (state.isSoundEnabled) soundEffects.playMove(state.currentPlayer == Player.X)
                        if (state.isHapticsEnabled) soundEffects.vibrate(35)
                        viewModel.processIntent(GameIntent.CellClick(index))
                    }
                }
            )

            // Action Buttons
            ActionButtonsSection(
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

        // Win celebration confetti
        if (state.status is GameStatus.Won) {
            ConfettiEffect()
        }
    }
}

@Composable
private fun HeaderSection(
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
                text = "Крестики-Нолики",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "2 игрока на одном устройстве",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isSoundEnabled) "🔊" else "🔇",
                fontSize = 22.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleSound)
                    .padding(4.dp)
            )
            Text(
                text = if (isHapticsEnabled) "📳" else "📴",
                fontSize = 22.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleHaptics)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun ScoreboardSection(scores: com.tictactoe.game.model.Scores) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreColumn(title = "Игрок X", score = scores.xWins, color = PlayerXCyan)
            ScoreDivider()
            ScoreColumn(title = "Ничьи", score = scores.draws, color = TextSecondary)
            ScoreDivider()
            ScoreColumn(title = "Игрок O", score = scores.oWins, color = PlayerOCoral)
        }
    }
}

@Composable
private fun ScoreDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(CellBorder)
    )
}

@Composable
private fun ScoreColumn(title: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
    }
}

@Composable
private fun StatusBanner(state: GameUiState) {
    val (statusText, statusColor) = when (val s = state.status) {
        is GameStatus.InProgress -> {
            if (state.currentPlayer == Player.X) {
                "Ход: Игрок 1 (X)" to PlayerXCyan
            } else {
                "Ход: Игрок 2 (O)" to PlayerOCoral
            }
        }
        is GameStatus.Won -> {
            if (s.winner == Player.X) {
                "Победил Игрок 1 (X)! 🎉" to PlayerXCyan
            } else {
                "Победил Игрок 2 (O)! 🎉" to PlayerOCoral
            }
        }
        GameStatus.Draw -> "Боевая ничья! 🤝" to TextSecondary
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = statusText,
            color = statusColor,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun GameBoard(
    state: GameUiState,
    onCellClick: (Int) -> Unit
) {
    val winningLine = (state.status as? GameStatus.Won)?.line

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val isWinningCell = winningLine?.contains(index) == true
                        val symbol = state.board[index]

                        Cell(
                            modifier = Modifier.weight(1f),
                            player = symbol,
                            isWinning = isWinningCell,
                            onClick = { onCellClick(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(
    modifier: Modifier = Modifier,
    player: Player?,
    isWinning: Boolean,
    onClick: () -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isWinning) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cellScale"
    )

    val bgColor = if (isWinning) CellWinning else CellDark
    val borderColor = if (isWinning) CellWinningBorder else CellBorder

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(scaleAnim)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isWinning) 3.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = player != null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            if (player != null) {
                Text(
                    text = player.symbol,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = if (player == Player.X) PlayerXCyan else PlayerOCoral
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onNewGame: () -> Unit,
    onResetScore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onNewGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Text(
                text = "Новая игра",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(onClick = onResetScore) {
            Text(
                text = "Сброс счёта",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}
