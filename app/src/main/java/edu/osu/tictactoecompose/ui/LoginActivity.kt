package edu.osu.tictactoecompose.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.TicTacToeApplication
import edu.osu.tictactoecompose.model.UserAccount
import edu.osu.tictactoecompose.uistate.UserAccountUiState
import edu.osu.tictactoecompose.model.viewmodel.UserAccountViewModel
import edu.osu.tictactoecompose.model.viewmodel.UserAccountViewModelFactory
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.CopyOnWriteArrayList


class LoginActivity : ComponentActivity() {
    private val userAccountViewModel: UserAccountViewModel by viewModels {
        UserAccountViewModelFactory((application as TicTacToeApplication).repository, application)
    }

    private var userAccountList = CopyOnWriteArrayList<UserAccount>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen().setOnExitAnimationListener { splashScreenView: SplashScreenViewProvider ->
                val iconView = splashScreenView.iconView
                if (iconView != null) {
                    val slideUp = ObjectAnimator.ofFloat(
                        splashScreenView, View.TRANSLATION_Y.toString(),
                        0f,
                        splashScreenView.iconView.height
                            .toFloat()
                    )
                    slideUp.interpolator = AnticipateInterpolator()
                    slideUp.duration = 200L

                    // Call SplashScreenView.remove at the end of your custom animation.
                    slideUp.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            splashScreenView.remove()
                        }
                    })

                    // Run your animation.
                    slideUp.start()
                }
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f)
                )
                { innerPadding ->
                    LoginScreen(
                        innerPadding,
                        userAccountViewModel.uiState,
                        userAccountViewModel
                    )
                }
            }
        }

        userAccountViewModel.allUserAccounts.observe(
            this
        ) { newUserAccountList ->
            Timber.tag(localClassName)
                .i("List of user accounts changed: ${newUserAccountList.size} accounts")
            Timber.tag(localClassName)
                .i("User accounts: ${newUserAccountList}")
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
    uiState: UserAccountUiState,
    accountViewModel: UserAccountViewModel = viewModel()
) {
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()

    when (uiState) {
        UserAccountUiState.SignedOut -> {
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
                            accountViewModel.uiState = accountViewModel.checkLogin()
                            if (accountViewModel.uiState == UserAccountUiState.SignedIn) {
                                val intent = Intent(activity, GameOptionsActivity::class.java)
                                activity?.startActivity(intent)
                            } else {
                                // UserAccountUiState is Error, so show LoginErrorScreen
                                Timber.tag("LoginScreen").e("Invalid login")
                                // Toast.makeText(activity, "Invalid login, please try again", Toast.LENGTH_SHORT).show()
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
                            val intent = Intent(activity, AccountActivity::class.java)
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

        UserAccountUiState.Error -> {
            LoginErrorScreen(
                onDismissRequest = {
                    accountViewModel.uiState = UserAccountUiState.SignedOut
                },
                accountViewModel
            )
        }

        UserAccountUiState.SignedIn -> {
            val intent = Intent(activity, GameOptionsActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginErrorScreen(
    onDismissRequest: () -> Unit,
    viewModel: UserAccountViewModel
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Login Error") },
        text = { Text("An error occurred during login. Please try again.") },
        confirmButton = {
            TextButton(
                onClick = {
                    Timber.tag("LoginErrorScreen").i("Clicked OK button")
                    onDismissRequest()
                }
            ) {
                Text("OK")
            }
        }
    )
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
