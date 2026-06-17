package edu.osu.tictactoecompose.ui

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
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
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.TicTacToeApplication
import edu.osu.tictactoecompose.model.UserAccount
import edu.osu.tictactoecompose.model.viewmodel.UserAccountViewModel
import edu.osu.tictactoecompose.model.viewmodel.UserAccountViewModelFactory
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class AccountActivity : ComponentActivity() {
    private val userAccountViewModel: UserAccountViewModel by viewModels {
        UserAccountViewModelFactory((application as TicTacToeApplication).repository, application)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f),
                ) { innerPadding ->
                    AccountScreen(
                        innerPadding,
                        userAccountViewModel
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    innerPadding: PaddingValues,
    usernameState: TextFieldState,
    passwordState: TextFieldState
) {
    val activity = LocalActivity.current

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .safeDrawingPadding()
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = stringResource(R.string.new_account),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            state = usernameState,
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
                .fillMaxWidth(0.98f)
                .padding(innerPadding),
            Arrangement.spacedBy(22.dp)
        ) {
            OutlinedButton(
                onClick = {
                    Timber.tag("AccountActivity: AccountScreen.Dummy").d("Clearing text fields")
                    usernameState.clearText()
                    passwordState.clearText()
                    Toast.makeText(activity, "Cleared text fields", Toast.LENGTH_SHORT).show()
                },
                content = {
                    Text(stringResource(R.string.clear).uppercase())
                }
            )
            Button(
                onClick = {
                    Timber.tag("AccountActivity: AccountScreen.Dummy")
                        .d("Creating new user account...")

                    val username = usernameState.text.toString()
                    val password = passwordState.text.toString()

                    if (username != "" && password != "") {
                        try {
                            val digest = MessageDigest.getInstance("SHA-256")
                            val hashPassBytes =
                                digest.digest(password.toByteArray(StandardCharsets.UTF_8))
                            val hashPassStr = hashPassBytes.toHexString()

                            val userAccount = UserAccount(username, hashPassStr)
                            Timber.tag("AccountActivity.Dummy").i("Added user account")
                            Toast.makeText(
                                activity,
                                "New UserAccount $username added",
                                Toast.LENGTH_SHORT
                            ).show()

                        } catch (e: NoSuchAlgorithmException) {
                            Toast.makeText(
                                activity,
                                "Error: No SHA-256 algorithm found",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            e.printStackTrace()
                        }
                    } else if (username == "" || password == "") {
                        Timber.tag("AccountActivity: AccountScreen.Dummy").e("Missing entry")
                        Toast.makeText(activity, "Missing entry", Toast.LENGTH_SHORT).show()
                    } else {
                        Timber.tag("AccountActivity: AccountScreen.Dummy")
                            .e("An unknown account creation error occurred.")
                        Toast.makeText(activity, "Error creating account!", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                content = {
                    Text(stringResource(R.string.create).uppercase())
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    innerPadding: PaddingValues,
    accountViewModel: UserAccountViewModel = viewModel()
) {
    val activity = LocalActivity.current

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = stringResource(R.string.new_account),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            state = accountViewModel.usernameState,
            modifier = Modifier.fillMaxWidth(1f),
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Username") }
        )

        OutlinedSecureTextField(
            state = accountViewModel.passwordState,
            modifier = Modifier.fillMaxWidth(1f),
            label = { Text("Password") },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(innerPadding),
            Arrangement.spacedBy(22.dp)
        ) {
            OutlinedButton(
                onClick = {
                    Timber.tag("AccountActivity: AccountScreen()").d("Clearing text fields")
                    accountViewModel.usernameState.clearText()
                    accountViewModel.passwordState.clearText()
                    Toast.makeText(activity, "Cleared text fields", Toast.LENGTH_SHORT).show()
                },
                content = {
                    Text(stringResource(R.string.clear).uppercase())
                }
            )
            Button(
                onClick = {
                    Timber.tag("AccountActivity: AccountScreen()").d("Creating new user account...")

                    val username = accountViewModel.usernameState.text.toString()
                    val password = accountViewModel.passwordState.text.toString()

                    if (username != "" && password != "") {
                        try {
                            val digest = MessageDigest.getInstance("SHA-256")
                            val hashPassBytes =
                                digest.digest(password.toByteArray(StandardCharsets.UTF_8))
                            val hashPassStr = hashPassBytes.toHexString()

                            val userAccount = UserAccount(username, hashPassStr)
                            accountViewModel.insert(userAccount)
                            Toast.makeText(
                                activity,
                                "New UserAccount $username added",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: NoSuchAlgorithmException) {
                            Toast.makeText(
                                activity,
                                "Error: No SHA-256 algorithm found",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            e.printStackTrace()
                        }
                    } else if (username == "" || password == "") {
                        Timber.tag("AccountActivity: AccountScreen").e("Missing entry")
                        Toast.makeText(activity, "Missing entry", Toast.LENGTH_SHORT).show()
                    } else {
                        Timber.e("An unknown account creation error occurred.")
                        Toast.makeText(activity, "Error creating account!", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                content = {
                    Text(stringResource(R.string.create).uppercase())
                }
            )
        }
    }
}

@Preview(apiLevel = 35)
@Composable
fun AccountScreenPreview() {
    TicTacToeComposeTheme {
        AccountScreen(
            innerPadding = PaddingValues(16.dp),
            usernameState = TextFieldState("User"),
            passwordState = TextFieldState("password")
        )
    }
}
