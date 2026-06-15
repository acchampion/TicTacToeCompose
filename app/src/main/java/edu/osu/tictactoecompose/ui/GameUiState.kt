package edu.osu.tictactoecompose.ui

import android.os.Parcelable
import edu.osu.tictactoecompose.GameGrid
import edu.osu.tictactoecompose.SYMBOL
import kotlinx.parcelize.Parcelize

@Parcelize
enum class STATE : Parcelable { Inactive, Active, Won, Draw };

enum class PLAYER { Player1, Player2 }

data class GameUiState(
    val gameGrid: GameGrid = GameGrid(),
    val firstPlayerName: String = "",
    val secondPlayerName: String = "",
    val currentPlayer: PLAYER = PLAYER.Player1,
    val winningPlayer: PLAYER = PLAYER.Player1,
    val winningPlayerName: String = "",
    val currentPlayerName: String = "",
    val currentPlayerSymbol: SYMBOL = SYMBOL.X,
    val scorePlayerOne: Int = 0,
    val scorePlayerTwo: Int = 0,
    val gameState: STATE = STATE.Active,
    val playCount: Int = 0
) {

}