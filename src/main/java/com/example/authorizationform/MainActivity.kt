package com.example.authorizationform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import coil.compose.AsyncImage

private const val LOGIN_ROUTE = "login"
private const val RESULT_ROUTE = "result"
private const val EMAIL_KEY = "email"
private const val ENTERED_PASSWORD_KEY = "entered_password"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AuthorizationApp()
                }
            }
        }
    }
}

@Composable
fun AuthorizationApp() {
    val navController = rememberNavController()

    // Пароль генерируется один раз при создании приложения
    val generatedPassword = remember {
        generatePassword()
    }

    NavHost(
        navController = navController,
        startDestination = LOGIN_ROUTE
    ) {
        composable(LOGIN_ROUTE) {
            AuthorizationScreen(
                onNavigate = { email, enteredPassword ->

                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(EMAIL_KEY, email)

                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            ENTERED_PASSWORD_KEY,
                            enteredPassword
                        )

                    navController.navigate(RESULT_ROUTE)
                }
            )
        }

        composable(RESULT_ROUTE) {
            val previousEntry =
                navController.previousBackStackEntry

            val email = previousEntry
                ?.savedStateHandle
                ?.get<String>(EMAIL_KEY)
                .orEmpty()

            val enteredPassword = previousEntry
                ?.savedStateHandle
                ?.get<String>(ENTERED_PASSWORD_KEY)
                .orEmpty()

            ResultScreen(
                email = email,
                enteredPassword = enteredPassword,
                generatedPassword = generatedPassword,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

fun generatePassword(): String {
    val letters =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    val digits = "0123456789"
    val allCharacters = letters + digits

    val characters = mutableListOf(
        letters.random(),
        digits.random()
    )

    repeat(10) {
        characters.add(allCharacters.random())
    }

    return characters.shuffled().joinToString("")
}

@Composable
fun AuthorizationScreen(
    onNavigate: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember {
        mutableStateOf<Int?>(null)
    }

    var passwordError by remember {
        mutableStateOf<Int?>(null)
    }

    fun validateAndContinue() {
        val checkedEmail = when {
            email.isBlank() ->
                R.string.email_empty_error

            !Regex(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            ).matches(email.trim()) ->
                R.string.email_invalid_error

            else -> null
        }

        val checkedPassword = when {
            password.isBlank() ->
                R.string.password_empty_error

            password.length < 8 ||
                    password.none {
                        it in 'A'..'Z' || it in 'a'..'z'
                    } ||
                    password.none {
                        it in '0'..'9'
                    } ->
                R.string.password_invalid_error

            else -> null
        }

        emailError = checkedEmail
        passwordError = checkedPassword

        if (checkedEmail == null && checkedPassword == null) {
            onNavigate(email.trim(), password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.email_label))
            },
            placeholder = {
                Text(stringResource(R.string.email_hint))
            },
            singleLine = true,
            isError = emailError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        if (emailError != null) {
            Text(
                text = stringResource(emailError!!),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(stringResource(R.string.password_label))
            },
            placeholder = {
                Text(stringResource(R.string.password_hint))
            },
            singleLine = true,
            isError = passwordError != null,
            visualTransformation =
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    validateAndContinue()
                }
            )
        )

        if (passwordError != null) {
            Text(
                text = stringResource(passwordError!!),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                validateAndContinue()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.next_button))
        }
    }
}

@Composable
fun ResultScreen(
    email: String,
    enteredPassword: String,
    generatedPassword: String,
    onBack: () -> Unit
) {
    var passwordVisible by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val copiedMessage = stringResource(
        R.string.password_copied
    )

    val isPasswordCorrect =
        enteredPassword == generatedPassword

    val imageUrl = stringResource(
        if (isPasswordCorrect) {
            R.string.success_image_url
        } else {
            R.string.failure_image_url
        }
    )

    val imageDescription = stringResource(
        if (isPasswordCorrect) {
            R.string.success_image_description
        } else {
            R.string.failure_image_description
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.result_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = email)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(
                if (isPasswordCorrect) {
                    R.string.success_message
                } else {
                    R.string.failure_message
                }
            ),
            style = MaterialTheme.typography.titleMedium,
            color = if (isPasswordCorrect) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AsyncImage(
            model = imageUrl,
            contentDescription = imageDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(
                R.string.generated_password_label
            )
        )

        OutlinedTextField(
            value = generatedPassword,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            }
        )

        Button(
            onClick = {
                passwordVisible = !passwordVisible
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (passwordVisible) {
                        R.string.hide_password
                    } else {
                        R.string.show_password
                    }
                )
            )
        }

        Button(
            onClick = {
                val clipboardManager =
                    context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager

                val clip = ClipData.newPlainText(
                    "Generated password",
                    generatedPassword
                )

                clipboardManager.setPrimaryClip(clip)

                Toast.makeText(
                    context,
                    copiedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.copy_password)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.back_button)
            )
        }
    }
}
