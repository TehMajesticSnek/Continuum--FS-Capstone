package com.continuum.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.continuum.Database
import com.continuum.R
import com.continuum.ui.theme.BluePrimary
import com.continuum.ui.theme.Border
import com.continuum.ui.theme.MutedText
import com.continuum.ui.theme.NavyBackground
import com.continuum.ui.theme.PrimaryText
import com.continuum.ui.theme.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    db: Database,
    toRegister: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.continuum_logo),
            contentDescription = "Continuum Logo",
            modifier = Modifier.height(90.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "CONTINUUM",
            color = PrimaryText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Keep Every Shift Connected.",
            color = BluePrimary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Welcome Back",
            color = PrimaryText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to access your team's handoffs.",
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = Border,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = MutedText,
                cursorColor = BluePrimary,
                focusedPlaceholderColor = MutedText,
                unfocusedPlaceholderColor = MutedText
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = Border,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = MutedText,
                cursorColor = BluePrimary,
                focusedPlaceholderColor = MutedText,
                unfocusedPlaceholderColor = MutedText
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val response = db.login(email, password)
                    if (response == "") {
                        // TODO change to homepage once that exists
                        withContext(Dispatchers.Main) {
                            toRegister()
                        }
                    }
                    else {
                        resultText = response
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BluePrimary,
                contentColor = PrimaryText,
                disabledContainerColor = Border,
                disabledContentColor = MutedText
            )
        ) {
            Text(
                text = "Sign In",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Forgot Password?",
            color = BluePrimary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))


        val signInString = buildAnnotatedString {
            val baseStyle = MaterialTheme.typography.bodyMedium

            withStyle(baseStyle.toSpanStyle().copy(color = MutedText)) {
                append("New to Continuum? ")
            }

            val linkStyles = TextLinkStyles(
                style = baseStyle.toSpanStyle().copy(color = BluePrimary)
            )

            withLink(
                LinkAnnotation.Clickable(
                    tag = "terms",
                    styles = linkStyles,
                    linkInteractionListener = { toRegister() }
                )
            ) {
                append("Create account")
            }
        }

        Text(signInString)
    }
}
