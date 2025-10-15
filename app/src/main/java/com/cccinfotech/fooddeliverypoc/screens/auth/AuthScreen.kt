package com.cccinfotech.fooddeliverypoc.screens.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cccinfotech.fooddeliverypoc.component_design.KUserInputTest
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.SharedPrefManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun AuthScreen(navController: NavController?) {

    var mDeviceToken by remember { mutableStateOf("") }
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {

        GetDeviceTokenComposable { token ->
            mDeviceToken = token
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(100.dp))
            CommonUtils().CommonText(
                "Log In", color = Color.White, fontSize = 20,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            CommonUtils().CommonText(
                "Please sign in to your account",
                color = Color.White,
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(60.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 4.dp
            ) {
                Column(Modifier.padding(20.dp)) {

                    CommonUtils().CommonText(
                        "Email",
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    KUserInputTest().UserTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = "Enter User Id"
                        },
                        hint = "Enter Email"
                    )
                    Spacer(Modifier.height(20.dp))
                    CommonUtils().CommonText(
                        "Password",
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    KUserInputTest().UserTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = "Please Enter Password"
                        },
                        hint = "Enter Password",
                        isPassword = true
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CommonUtils().CommonText(
                            "Don't have an account ? ",
                            fontWeight = FontWeight.Normal,
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        CommonUtils().CommonText(
                            text = "Sign Up",
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                navController?.navigate("SignUp")
                            }
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (email.isEmpty()) {
                                Toast.makeText(context, "Email Can't be empty", Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                            } else if (password.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Password Can't be empty",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isLoading = true

                            FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email.trim(), password.trim())
                                .addOnSuccessListener { res ->
                                    val uid = res.user?.uid ?: return@addOnSuccessListener
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(uid)
                                        .update("deviceToken", mDeviceToken)
                                        .addOnSuccessListener {
                                            Log.d("##FCM", "Token saved: $mDeviceToken")
                                        }
                                    isLoading = false

                                    navController?.navigate("Menu") {
                                        popUpTo("Auth") { inclusive = true }
                                    }
                                    SharedPrefManager.putBoolean("IsLogin", true)
                                    SharedPrefManager.putString("UserId", uid)
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                                    Log.d("#Result", it.message.toString())
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp)
                                    .align(Alignment.CenterVertically),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            CommonUtils().CommonText(
                                "Please wait...",
                                fontWeight = FontWeight.Normal,
                            )
                        } else {
                            CommonUtils().CommonText(
                                "Login",
                                fontWeight = FontWeight.Normal, color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GetDeviceTokenComposable(onTokenReceived: (String) -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("##FCM", "Token: $token")
                onTokenReceived(token)
            }
    }
}
