package edu.osu.tictactoecompose.ui

sealed class UserAccountUiState {
    object SignedOut: UserAccountUiState()
    object SignedIn: UserAccountUiState()
    object Error: UserAccountUiState()
}