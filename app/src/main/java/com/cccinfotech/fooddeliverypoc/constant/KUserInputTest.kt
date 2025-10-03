package com.cccinfotech.fooddeliverypoc.constant

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cccinfotech.fooddeliverypoc.R


import com.cccinfotech.fooddeliverypoc.utils.Poppins

class KUserInputTest {
    @Composable
    fun UserTextField(
        value: String,
        onValueChange: (String) -> Unit,
        hint: String,
        isPassword: Boolean = false,
        isError: Boolean = false,
        textColor: Color = Color.Black,
        hintColor: Color = Color.Gray,
        letterSpacing: TextUnit = 1.sp,
        fontSize: TextUnit = 16.sp,
        fontWeight: FontWeight = FontWeight.Normal,
        keyboardType: KeyboardType = KeyboardType.Text,
        shape: RoundedCornerShape = RoundedCornerShape(12.dp),
        shapeColor: Color = Color(LocalContext.current.getColor(R.color.purple_200))
    ) {
        var passwordVisible by remember { mutableStateOf(false) } // ✅ state for toggle

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = hint,
                    color = hintColor,
                    fontSize = fontSize,
                    fontFamily = Poppins,
                    fontWeight = fontWeight,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) {
                        Icons.Default.Visibility     // 👀 show icon
                    } else {
                        Icons.Default.VisibilityOff  // 🚫 hide icon
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password")
                    }
                }
            }
        )
    }

}