package edu.osu.tictactoecompose.model.viewmodel

import android.app.Application
import androidx.annotation.Keep
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.wiley.fordummies.androidsdk.tictactoe.model.Settings
import edu.osu.tictactoecompose.TicTacToeApplication
import edu.osu.tictactoecompose.model.SettingsDataStore
import edu.osu.tictactoecompose.model.UserAccount
import edu.osu.tictactoecompose.model.UserAccountRepository
import edu.osu.tictactoecompose.ui.UserAccountUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * ViewModel for the user account storage.
 */
@Keep
class UserAccountViewModel(var repository: UserAccountRepository, val app: Application) : AndroidViewModel(app) {
    var allUserAccounts: LiveData<List<UserAccount>> = repository.allUserAccounts.asLiveData()

    val dataStore: SettingsDataStore = (app as TicTacToeApplication).dataStore
    var uiState: UserAccountUiState by mutableStateOf(UserAccountUiState.SignedOut)

    var usernameState = TextFieldState()
    var passwordState = TextFieldState()
    var enteredUsernameState = TextFieldState()
    var enteredPasswordState = TextFieldState()

    private val classTag = javaClass.simpleName

    fun checkLogin(): UserAccountUiState {

        val enteredUsername = enteredUsernameState.text.toString()
        val enteredPassword = enteredPasswordState.text.toString()

        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashPassBytes =
                digest.digest(enteredPassword.toByteArray(StandardCharsets.UTF_8))
            val hashPassStr = hashPassBytes.toHexString()

            val userAccount = UserAccount(enteredUsername, hashPassStr)

            uiState =
            if (containsUserAccount(userAccount)) {
                CoroutineScope(Dispatchers.IO).launch {
                    dataStore.putString(
                        Settings.Keys.OPT_NAME, enteredUsername
                    )
                    Timber.tag("$classTag: Coroutine")
                        .d("Wrote username successfully to DataStore")
                }
                UserAccountUiState.SignedIn
            } else {
                UserAccountUiState.Error
            }
        } catch (e: NoSuchAlgorithmException) {
            Timber.tag("UserAccountViewModel").e("No SHA-256 algorithm")
            e.printStackTrace()
        }

        return uiState
    }

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
class UserAccountViewModelFactory(private val repository: UserAccountRepository, val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserAccountViewModel::class.java)) {
            return UserAccountViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}