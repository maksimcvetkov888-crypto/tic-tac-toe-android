package com.tictactoe.game

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.animation.OvershootInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tictactoe.game.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val game = TicTacToeGame()
    private lateinit var cells: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cells = listOf(
            binding.cell0, binding.cell1, binding.cell2,
            binding.cell3, binding.cell4, binding.cell5,
            binding.cell6, binding.cell7, binding.cell8
        )

        setupListeners()
        updateUI()
    }

    private fun setupListeners() {
        cells.forEachIndexed { index, button ->
            button.setOnClickListener {
                onCellClicked(index, button)
            }
        }

        binding.btnNewGame.setOnClickListener {
            vibrate(30)
            game.resetBoard()
            resetCellsUI()
            updateUI()
        }

        binding.btnResetScore.setOnClickListener {
            vibrate(50)
            game.resetAll()
            resetCellsUI()
            updateUI()
        }
    }

    private fun onCellClicked(index: Int, button: Button) {
        val player = game.currentPlayer
        val moved = game.makeMove(index)
        if (!moved) return

        vibrate(30)

        button.text = player
        val colorRes = if (player == "X") R.color.color_x else R.color.color_o
        button.setTextColor(ContextCompat.getColor(this, colorRes))

        button.scaleX = 0f
        button.scaleY = 0f
        button.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(220)
            .setInterpolator(OvershootInterpolator())
            .start()

        updateUI()

        if (game.state != TicTacToeGame.State.IN_PROGRESS) {
            handleGameOver()
        }
    }

    private fun handleGameOver() {
        val winningLine = game.winningLine
        if (winningLine != null) {
            vibrate(120)
            for (idx in winningLine) {
                cells[idx].setBackgroundResource(R.drawable.bg_cell_winning)
                cells[idx].animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .setDuration(180)
                    .withEndAction {
                        cells[idx].animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    }
                    .start()
            }
        } else {
            vibrate(60)
        }

        cells.forEach { it.isEnabled = false }
    }

    private fun resetCellsUI() {
        cells.forEach { button ->
            button.text = ""
            button.setBackgroundResource(R.drawable.bg_cell)
            button.isEnabled = true
            button.scaleX = 1f
            button.scaleY = 1f
        }
    }

    private fun updateUI() {
        binding.tvScoreX.text = game.xScore.toString()
        binding.tvScoreO.text = game.oScore.toString()
        binding.tvScoreDraw.text = game.drawScore.toString()

        when (game.state) {
            TicTacToeGame.State.IN_PROGRESS -> {
                if (game.currentPlayer == "X") {
                    binding.tvStatus.text = getString(R.string.turn_x)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_x))
                } else {
                    binding.tvStatus.text = getString(R.string.turn_o)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_o))
                }
            }
            TicTacToeGame.State.X_WON -> {
                binding.tvStatus.text = getString(R.string.win_x)
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_x))
            }
            TicTacToeGame.State.O_WON -> {
                binding.tvStatus.text = getString(R.string.win_o)
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_o))
            }
            TicTacToeGame.State.DRAW -> {
                binding.tvStatus.text = getString(R.string.game_draw)
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate(ms: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator?.vibrate(ms)
                }
            }
        } catch (_: Exception) {
        }
    }
}
