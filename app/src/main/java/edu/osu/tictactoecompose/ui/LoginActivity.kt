package edu.osu.tictactoecompose.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wiley.fordummies.androidsdk.tictactoe.model.Settings
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.TicTacToeApplication
import edu.osu.tictactoecompose.model.UserAccount
import edu.osu.tictactoecompose.model.viewmodel.UserAccountViewModel
import edu.osu.tictactoecompose.model.viewmodel.UserAccountViewModelFactory
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.CopyOnWriteArrayList


class LoginActivity : ComponentActivity() {
    private val userAccountViewModel: UserAccountViewModel by viewModels {
        UserAccountViewModelFactory((application as TicTacToeApplication).repository)
    }

    private var userAccountList = CopyOnWriteArrayList<UserAccount>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f)
                )
                { innerPadding ->
                    LoginScreen(innerPadding, userAccountViewModel)
                }
            }
        }

        userAccountViewModel.allUserAccounts.observe(
            this
        ) { newUserAccountList ->
            Timber.tag(localClassName)
                .i("List of user accounts changed: ${newUserAccountList.size} accounts")
            userAccountList.clear()
            userAccountList.addAll(newUserAccountList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userAccountViewModel.allUserAccounts.removeObservers(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    nameState: TextFieldState,
    passwordState: TextFieldState
) {
    val activity = LocalActivity.current

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            state = nameState,
            modifier = Modifier.fillMaxWidth(1f),
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Username") }
        )

        OutlinedSecureTextField(
            state = passwordState,
            modifier = Modifier.fillMaxWidth(1f),
            label = { Text("Password") },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(innerPadding),
            Arrangement.spacedBy(20.dp)
        ) {
            Button(
                onClick = {
                    Timber.tag("LoginActivity: LoginScreen.Dummy").d("Logging in...")

                    val enteredUsername = nameState.text.toString()
                    val enteredPassword = passwordState.text.toString()

                    try {
                        val digest = MessageDigest.getInstance("SHA-256")
                        val hashPassBytes =
                            digest.digest(enteredPassword.toByteArray(StandardCharsets.UTF_8))
                        val hashPassStr = hashPassBytes.toHexString()

                        val userAccount = UserAccount(enteredUsername, hashPassStr)
                    } catch (e: NoSuchAlgorithmException) {
                        Timber.tag("LoginActivity: LoginScreen.Dummy").e("No SHA-256 algorithm")
                        e.printStackTrace()
                    }
                },
                content = {
                    Text(
                        text = stringResource(R.string.login).uppercase()
                    )
                }
            )
            OutlinedButton(
                onClick = {
                    activity?.finish()
                    Timber.tag("LoginActivity: LoginScreen.Dummy").d("Finishing activity")
                },
                content = {
                    Text(
                        text = stringResource(R.string.exit).uppercase()
                    )
                }
            )
            OutlinedButton(
                onClick = {
                    Timber.tag("LoginActivity: LoginScreen.Dummy").d("Starting AccountActivity")
                    val intent = Intent(activity?.applicationContext, AccountActivity::class.java)
                    activity?.startActivity(intent)
                },
                content = {
                    Text(
                        text = stringResource(R.string.new_user).uppercase()
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    accountViewModel: UserAccountViewModel = viewModel()
) {
    val context = LocalActivity.current

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            state = accountViewModel.enteredUsernameState,
            modifier = Modifier.fillMaxWidth(1f),
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Username") }
        )

        OutlinedSecureTextField(
            state = accountViewModel.enteredPasswordState,
            modifier = Modifier.fillMaxWidth(1f),
            label = { Text("Password") },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(innerPadding),
            Arrangement.spacedBy(20.dp)
        ) {
            Button(
                onClick = {
                    Timber.tag("LoginActivity: LoginScreen()").d("Logging in...")

                    val enteredUsername = accountViewModel.enteredUsernameState.text.toString()
                    val enteredPassword = accountViewModel.enteredPasswordState.text.toString()

                    try {
                        val digest = MessageDigest.getInstance("SHA-256")
                        val hashPassBytes =
                            digest.digest(enteredPassword.toByteArray(StandardCharsets.UTF_8))
                        val hashPassStr = hashPassBytes.toHexString()

                        val userAccount = UserAccount(enteredUsername, hashPassStr)
                        if (accountViewModel.containsUserAccount(userAccount)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                (context?.application as TicTacToeApplication).dataStore.putString(
                                    Settings.Keys.OPT_NAME, enteredUsername
                                )
                                Timber.tag("LoginActivity: LoginScreen: Coroutine")
                                    .d("Wrote username successfully to DataStore")
                            }
                            Timber.tag("LoginActivity: LoginScreen")
                                .d("Starting GameOptionsActivity")
                            val intent = Intent(context, GameOptionsActivity::class.java)
                            context?.startActivity(intent)
                            context?.finish()
                        } else {
                            Timber.tag("LoginActivity: LoginScreen").e("Login error")
                            Toast.makeText(context, "Login error, please try again", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: NoSuchAlgorithmException) {
                        Timber.tag("LoginActivity: LoginScreen").e("No SHA-256 algorithm")
                        e.printStackTrace()
                    }
                },
                content = {
                    Text(
                        text = stringResource(R.string.login).uppercase()
                    )
                }
            )
            OutlinedButton(
                onClick = {
                    context?.finish()
                    Timber.tag("LoginActivity: LoginScreen()").d("Finishing activity")
                },
                content = {
                    Text(
                        text = stringResource(R.string.exit).uppercase()
                    )
                }
            )
            OutlinedButton(
                onClick = {
                    Timber.tag("LoginActivity: LoginScreen").d("Starting AccountActivity")
                    val intent = Intent(context, AccountActivity::class.java)
                    context?.startActivity(intent)
                },
                content = {
                    Text(
                        text = stringResource(R.string.new_user).uppercase()
                    )
                }
            )
        }
    }
}

@Preview(apiLevel = 35)
@Composable
fun LoginScreenPreview() {
    TicTacToeComposeTheme {
        LoginScreen(
            PaddingValues(16.dp),
            nameState = TextFieldState("User"),
            passwordState = TextFieldState("password")
        )
    }
}
