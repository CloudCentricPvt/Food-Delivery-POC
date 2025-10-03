package com.cccinfotech.fooddeliverypoc.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.util.Locale

class CommonUtils {

   val  NOTIFICATION_ID = 1001

    @Composable
    fun CommonText(
        text: String,
        fontSize: Int = 12,
        fontWeight: FontWeight = FontWeight.Normal,
        color: Color = Color.Black,
        modifier: Modifier = Modifier

    ) {
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = text,
            fontFamily = Poppins,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun LogoutDialog(
        showDialog: Boolean,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = {
                    CommonText(text = "Info", fontSize = 17, fontWeight = FontWeight.SemiBold)
                },
                text = {
                    CommonText(
                        "Are you sure you want to logout?",
                        fontSize = 15,
                        fontWeight = FontWeight.Medium
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm() }) {
                        CommonText("Logout", fontSize = 15, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onDismiss() }) {
                        CommonText("Cancel", fontSize = 15, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    }

    fun getAddressFromLatLng(context: Context, lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: MutableList<Address>? = geocoder.getFromLocation(lat, lng, 1) // get 1 result
            if (addresses?.isNotEmpty() == true) {
                val address: Address = addresses[0]
                // You can concatenate parts as needed
                val addressLine = address.getAddressLine(0)  // Full address
                addressLine
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }



}