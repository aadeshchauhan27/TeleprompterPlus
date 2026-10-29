package com.teleprompter.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teleprompter.app.core.theme.CyanGradient
import com.teleprompter.app.core.theme.DeepNavyDark
import com.teleprompter.app.core.theme.DeepNavySurface
import com.teleprompter.app.core.theme.ElectricBlue
import com.teleprompter.app.core.theme.ElectricBlueLight
import com.teleprompter.app.core.theme.LightBorderSubtle
import com.teleprompter.app.core.theme.PinkGradient
import com.teleprompter.app.core.theme.PurpleGradient
import com.teleprompter.app.core.theme.TextDarkHeading
import com.teleprompter.app.core.theme.TextDarkMuted
import com.teleprompter.app.ui.AppViewModelProvider

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    var showEmailDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Center Branding Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Premium Logo Badge with animated gradient background
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(CyanGradient, ElectricBlue, PurpleGradient)))
                    .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Speech bubble shape representation with play button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(CyanGradient)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(ElectricBlue)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(PurpleGradient)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title: "TelePrompter" with gradient effect
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tele",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkHeading,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "Prompter",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        fontSize = 32.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Sign in to sync your scripts\nacross all your devices",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextDarkMuted,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Premium Social Buttons
            AuthSocialButton(
                icon = {
                    Text("G", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFEA4335))
                },
                text = "Continue with Google",
                onClick = {
                    viewModel.signInWithGoogle(
                        email = "creator@teleprompter.app",
                        name = "Google Creator",
                        onSuccess = {
                            Toast.makeText(context, "Signed in with Google", Toast.LENGTH_SHORT).show()
                            onSignInSuccess()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthSocialButton(
                icon = {
                    Text("", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextDarkHeading)
                },
                text = "Continue with Apple",
                onClick = {
                    viewModel.signInWithEmail(
                        email = "creator@apple.com",
                        name = "Apple Creator",
                        onSuccess = {
                            Toast.makeText(context, "Signed in with Apple ID", Toast.LENGTH_SHORT).show()
                            onSignInSuccess()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthSocialButton(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TextDarkHeading,
                        modifier = Modifier.size(21.dp)
                    )
                },
                text = "Continue with Email",
                onClick = {
                    showEmailDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Guest Mode Option
            TextButton(
                onClick = {
                    viewModel.continueAsGuest(onSuccess = onContinueAsGuest)
                }
            ) {
                Text(
                    text = "Continue as Guest",
                    color = TextDarkMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Email Dialog
        if (showEmailDialog) {
            AlertDialog(
                onDismissRequest = { showEmailDialog = false },
                title = {
                    Text(
                        text = "Sign in with Email",
                        fontWeight = FontWeight.Bold,
                        color = TextDarkHeading
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your email to sync your scripts to the cloud:",
                            color = TextDarkMuted,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            placeholder = { Text("e.g. creator@example.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetEmail = emailInput.trim().ifBlank { "creator@teleprompter.app" }
                            showEmailDialog = false
                            viewModel.signInWithEmail(
                                email = targetEmail,
                                name = targetEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                                onSuccess = {
                                    Toast.makeText(context, "Signed in as $targetEmail", Toast.LENGTH_SHORT).show()
                                    onSignInSuccess()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Sign In", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmailDialog = false }) {
                        Text("Cancel", color = TextDarkMuted)
                    }
                }
            )
        }

        // Legal Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            val termsText = buildAnnotatedString {
                append("By continuing you agree to our ")
                withStyle(SpanStyle(color = ElectricBlue, fontWeight = FontWeight.SemiBold)) {
                    append("Terms of Service")
                }
                append(" and ")
                withStyle(SpanStyle(color = ElectricBlue, fontWeight = FontWeight.SemiBold)) {
                    append("Privacy Policy")
                }
            }

            Text(
                text = termsText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextDarkMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun AuthSocialButton(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = TextDarkHeading
        ),
        border = ButtonDefaults.outlinedButtonBorder(true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE5E5E7))
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextDarkHeading
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {}
        }
    }
}
