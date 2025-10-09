package com.cccinfotech.fooddeliverypoc.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cccinfotech.fooddeliverypoc.screens.splash.SplashScreen
import com.cccinfotech.fooddeliverypoc.ui.theme.FoodDeliveryPOCTheme
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.cccinfotech.fooddeliverypoc.utils.Poppins
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val maxChar = 10
    var isLoading by remember { mutableStateOf(false) }


    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp), verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        CommonUtils().CommonText(
                            "Enter Email",
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    name,
                    { name = it },
                    label = {
                        CommonUtils().CommonText(
                            "Enter Name",
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(phone, {
                    if (it.length <= maxChar) {
                        phone = it
                    }
                }, label = {
                    CommonUtils().CommonText(
                        "Phone Number",
                        fontWeight = FontWeight.Normal,
                    )
                },

                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(role, { role = it }, label = {
                    CommonUtils().CommonText(
                        "Role",
                        fontWeight = FontWeight.Normal,
                    )
                },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    password,
                    { password = it },
                    label = {
                        CommonUtils().CommonText(
                            "Password",
                            fontWeight = FontWeight.Normal,
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    if (email.isEmpty()) {
                        Toast.makeText(ctx, "Enter Email", Toast.LENGTH_SHORT).show()
                        return@Button
                    } else if (name.isEmpty()) {
                        Toast.makeText(ctx, "Enter Name", Toast.LENGTH_SHORT).show()
                        return@Button
                    } else if (phone.isEmpty()) {
                        Toast.makeText(ctx, "Enter Phone", Toast.LENGTH_SHORT).show()
                        return@Button
                    } else if (role.isEmpty()) {
                        Toast.makeText(ctx, "Enter Role", Toast.LENGTH_SHORT).show()
                        return@Button
                    } else if (password.isEmpty()) {
                        Toast.makeText(ctx, "Enter Password", Toast.LENGTH_SHORT).show()
                        return@Button
                    } else {
                        isLoading = true
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                            email.trim(),
                            password.trim()
                        ).addOnSuccessListener { res ->
                            val uid = res.user?.uid ?: return@addOnSuccessListener

                            val user = mapOf(
                                "uid" to uid,
                                "email" to email.trim(),
                                "role" to role.trim(),
                                "name" to name.trim(),
                                "phone" to phone.trim(),
                                "password" to password.trim(),
                                "createdAt" to System.currentTimeMillis()
                            )

                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(ctx, "Account created", Toast.LENGTH_SHORT)
                                        .show()
                                    navController.navigate("Auth") {
                                        popUpTo("SignUp") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(ctx, it.message, Toast.LENGTH_SHORT).show()
                                }

                        }.addOnFailureListener {
                            isLoading = false
                            Toast.makeText(ctx, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        CommonUtils().CommonText(
                            "Please wait...",
                            fontWeight = FontWeight.Normal,
                        )
                    } else {
                        CommonUtils().CommonText(
                            "Sign Up",
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    FoodDeliveryPOCTheme {
        val navController = rememberNavController()
        SignupScreen(navController)
    }
}