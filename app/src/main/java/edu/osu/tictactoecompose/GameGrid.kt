package edu.osu.tictactoecompose

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class GameGrid(val grid: List<List<Square>> = listOf(
    listOf(
        Square(0, 0, SYMBOL.Blank),
        Square(1, 0, SYMBOL.Blank),
        Square(2, 0, SYMBOL.Blank)
    ),
    listOf(
        Square(0, 1, SYMBOL.Blank),
        Square(1, 1, SYMBOL.Blank),
        Square(2, 1, SYMBOL.Blank)
    ),
    listOf(
        Square(0, 2, SYMBOL.Blank),
        Square(1, 2, SYMBOL.Blank),
        Square(2, 2, SYMBOL.Blank)
    )
)) : Parcelable {
    val classTag: String = javaClass.simpleName
    val numRows: Int = 3

    fun deepCopy(): GameGrid {
        return GameGrid(listOf(
            listOf(
                Square(0, 0, this.grid[0][0].symbol),
                Square(1, 0, this.grid[0][1].symbol),
                Square(2, 0, this.grid[0][2].symbol)
            ),
            listOf(
                Square(0, 1, this.grid[1][0].symbol),
                Square(1, 1, this.grid[1][1].symbol),
                Square(2, 1, this.grid[1][2].symbol)
            ),
            listOf(
                Square(0, 2, this.grid[2][0].symbol),
                Square(1, 2, this.grid[2][1].symbol),
                Square(2, 2, this.grid[2][2].symbol)
            )
        ))
    }

    // Left diagonal has the same symbol
    val isLeftToRightDiagonalFilled: Boolean
        get() {
            Timber.tag(classTag).d("Entering isLeftToRightDiagonalFilled()")
            var foundMismatch = false
            var index = 0
            while (index < numRows && !foundMismatch) {
                Timber.tag(classTag).d("Row ${index}: ${grid[index]}")
                if (grid[0][0].symbol != grid[index][index].symbol)
                    foundMismatch = true
                index++
            }
            Timber.d(
                classTag,
                "Leaving isLeftToRightDiagonalFilled(); $foundMismatch; index: $index"
            )
            return !foundMismatch && (grid[0][0].symbol != SYMBOL.Blank)
        }

    // Right diagonal has the same symbol
    val isRightToLeftDiagonalFilled: Boolean
        get() {
            var foundIndex = -1
            var foundMismatch = false
            Timber.tag(classTag).d("Entering isRightToLeftDiagonalFilled()")
            var index = numRows - 1
            while (index > 0 && !foundMismatch) {
                Timber.tag(classTag).d("Row ${index}: ${grid[index]}")
                if (grid[0][numRows - 1].symbol != grid[index][numRows - 1 - index].symbol) {
                    foundMismatch = true
                    foundIndex = index
                }
                index--
            }
            Timber.d(
                classTag,
                "Leaving isRightToLeftDiagonalFilled(); $foundMismatch; index: foundIndex; ${grid[0][numRows - 1]}"
            )
            return !foundMismatch && (grid[0][numRows - 1].symbol != SYMBOL.Blank)
        }

    fun isRowFilled(row: Int): Boolean {
        // Entire row has the same symbol
        var foundMismatch = false
        var col = 0
        while (col < numRows && !foundMismatch) {
            if (grid[row][0].symbol != grid[row][col].symbol)
                foundMismatch = true
            col++
        }
        return !foundMismatch && (grid[row][0].symbol !== SYMBOL.Blank)
    }

    fun isColumnFilled(column: Int): Boolean {
        // Entire column has the same symbol
        var foundMismatch = false
        var row = 0
        while (row < numRows && !foundMismatch) {
            if (grid[0][column].symbol !== grid[row][column].symbol)
                foundMismatch = true
            row++
        }
        return !foundMismatch && (grid[0][column].symbol != SYMBOL.Blank)
    }

    fun hasThreeInRow(): Boolean {
        return isRowFilled(0) || isRowFilled(1) || isRowFilled(2) ||
                isColumnFilled(0) || isColumnFilled(1) || isColumnFilled(2) ||
                isLeftToRightDiagonalFilled || isRightToLeftDiagonalFilled
    }

    fun toList(): List<Square> {
        return grid.flatten()
    }

    // Get the unfilled squares
    val emptySquares: List<Square>
        get() {
            val list = ArrayList<Square>()
            for (i in 0 until numRows) {
                (0 until numRows)
                    .asSequence()
                    .filter { grid[i][it].symbol == SYMBOL.Blank }
                    .mapTo(list) { Square(i, it, SYMBOL.Blank) }
            }
            return list
        }

}