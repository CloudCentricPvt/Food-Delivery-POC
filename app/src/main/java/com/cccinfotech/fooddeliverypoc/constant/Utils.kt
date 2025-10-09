package com.cccinfotech.fooddeliverypoc.constant

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
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
            // Dismiss previous snackbar if any
            snackbarHostState.currentSnackbarData?.dismiss()
            // Update success/failure state
            currentSnackbarSuccess.value = isSuccess

            // Show snackbar indefinitely
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
        }
    }


}
