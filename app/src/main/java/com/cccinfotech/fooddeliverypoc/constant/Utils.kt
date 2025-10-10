package com.cccinfotech.fooddeliverypoc.constant

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object CommonUtil {

    var currentSnackbarSuccess = mutableStateOf(false)

    fun showSnackbar(
        message: String,
        isSuccess: Boolean,
        snackbarHostState: SnackbarHostState,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            currentSnackbarSuccess.value = isSuccess
            snackbarHostState.showSnackbar(message)
        }
    }

    fun showSnackbarForInternet(
        message: String,
        isSuccess: Boolean,
        snackbarHostState: SnackbarHostState,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            currentSnackbarSuccess.value = isSuccess

            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    @Composable
    fun OrderCompletedDialog(
        productNames: List<String>,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                CommonUtils().CommonText(
                    text = "🎉 Congratulations!",
                    fontWeight = FontWeight.Medium, fontSize = 15
                )
            },
            text = {
                CommonUtils().CommonText(
                    "Your order is completed for:\n $productNames", fontSize = 14
                )
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    CommonUtils().CommonText(
                        "Okay",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold, fontSize = 15
                    )
                }
            }
        )
    }
}
