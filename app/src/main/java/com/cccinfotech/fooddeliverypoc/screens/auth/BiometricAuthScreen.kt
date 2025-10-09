package com.cccinfotech.fooddeliverypoc.screens.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun BiometricAuthScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(context)

    // Check if biometric is available
    val biometricManager = androidx.biometric.BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(
    ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS

    if (!canAuthenticate) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Biometric authentication not available", color = Color.Red)
        }
        return
    }

    val biometricPrompt = remember {
        BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onAuthFailed()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onAuthFailed()
                }
            })
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate to access Home")
            .setNegativeButtonText("Cancel")
            .build()
    }

    // Trigger authentication once
    LaunchedEffect(Unit) {
        biometricPrompt.authenticate(promptInfo)
    }

    // Show fallback UI behind the system prompt
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "Waiting for biometric authentication...",
            modifier = Modifier.align(Alignment.BottomCenter),
            color = Color.Gray
        )
    }
}
