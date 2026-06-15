package edu.osu.tictactoecompose.model.viewmodel

import androidx.annotation.Keep
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import edu.osu.tictactoecompose.model.UserAccount
import edu.osu.tictactoecompose.model.UserAccountRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the user account storage.
 */
@Keep
class UserAccountViewModel(var repository: UserAccountRepository) : ViewModel() {
    var allUserAccounts: LiveData<List<UserAccount>> = repository.allUserAccounts.asLiveData()

    var usernameState = TextFieldState()
    var passwordState = TextFieldState()
    var enteredUsernameState = TextFieldState()
    var enteredPasswordState = TextFieldState()

    fun containsUserAccount(userAccount: UserAccount): Boolean {
        var accountInList = false
        val userAccountList: List<UserAccount>? = allUserAccounts.value
        if (userAccountList?.contains(userAccount) == true) {
            accountInList = true
        }
        return accountInList
    }

    fun getUserAccount(userAccount: UserAccount): LiveData<UserAccount> {
        return repository.findUserAccountByName(userAccount)
    }

    fun insert(userAccount: UserAccount) = viewModelScope.launch {
        repository.insert(userAccount)
    }

}

@Suppress("UNCHECKED_CAST")
class UserAccountViewModelFactory(private val repository: UserAccountRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserAccountViewModel::class.java)) {
            return UserAccountViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}