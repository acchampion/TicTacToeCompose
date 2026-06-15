package edu.osu.tictactoecompose

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class SYMBOL {
    X, O, Blank
}

@Parcelize
data class Square(val x: Int, val y: Int, var symbol: SYMBOL): Parcelable {
    override fun toString(): String {
        return when (symbol) {
            SYMBOL.X -> "X"
            SYMBOL.O -> "O"
            SYMBOL.Blank -> "blank"
        }
    }
}