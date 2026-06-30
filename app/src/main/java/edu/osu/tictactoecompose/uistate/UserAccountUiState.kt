package edu.osu.tictactoecompose.uistate

sealed class UserAccountUiState {
    object SignedOut: UserAccountUiState()
    object SignedIn: UserAccountUiState()
    object Error: UserAccountUiState()
}