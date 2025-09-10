package com.cccinfotech.fooddeliverypoc.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cccinfotech.fooddeliverypoc.constant.KUserInputTest

@Composable
fun AuthScreen(navController: NavController?,){

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf("") }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(100.dp))
            Text("Log In", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Please sign in to your account", color = Color.White, fontSize = 16.sp)

            Spacer(Modifier.height(60.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 4.dp
            ) {
                Column(Modifier.padding(20.dp)) {

                    Text("EMAIL")
                    // Email field
                    KUserInputTest().UserTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = "" // clear error on typing
                        },
                        hint = "Enter Email"
                    )

                    Spacer(Modifier.height(20.dp))

                    Text("PASSWORD")
                    // Email field
                    KUserInputTest().UserTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = "" // clear error on typing
                        },
                        hint = "Enter Password"
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(onClick = {
                        when{
                            email.trim().isEmpty() -> emailError = "Please enter email"
                            password.trim().isEmpty() -> passwordError = "Please enter password"

                            else ->{
                                // logic here
                            }
                        }

                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text("Login")
                    }

                }

            }
        }
    }

}