package com.cccinfotech.fooddeliverypoc.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.screens.splash.MainActivity
import com.cccinfotech.fooddeliverypoc.utils.CommonUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MyForegroundService : Service() {

    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                val orderId = intent.getStringExtra("orderId")
                val productName = intent.getStringArrayListExtra("ProductName")
                val status = intent.getStringExtra("Status")

                val notification = productName?.let {
                    createNotification(
                        it, status,
                    )
                }
                startForeground(CommonUtils().NOTIFICATION_ID, notification)

                if (orderId != null) {
                    orderListener = db.collection("orders")
                        .document(orderId)
                        .addSnapshotListener { snapshot, _ ->
                            val status1 = snapshot?.getString("status")
                            val deliveryTimeOTP = snapshot?.getString("deliveryTimeOTP")
                            val deliveryBoye = snapshot?.getString("deliveryBoye")
                            if (status1.equals("inprogress", ignoreCase = true)) {
                                stopForeground(false)
                                if (deliveryBoye != null) {
                                    showInProgressNotification(deliveryBoye)
                                    showOtpNotification(deliveryTimeOTP,deliveryBoye)
                                }
                            }
                            if (status1.equals("Delivered", ignoreCase = true)) {
                                stopForeground(true)
                                showCompletionNotification(productName)
                                stopSelf()
                            }else if(status1.equals("Cancelled", ignoreCase = true)){
                                stopForeground(true)
                                if (productName != null) {
                                    showCancelledNotification(productName)
                                }
                                stopSelf()
                            }
                        }
                }
            }

            Actions.STOP.toString() -> {
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    enum class Actions {
        START, STOP
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any resources here
    }

    private fun createNotification(
        productName: ArrayList<String>?,
        status: String?,
    ): Notification {
        val channelId = "ForegroundServiceChannelId"
        val channelName = "Order Tracking"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Your Order of $productName is Placed")
            .setContentText("watch your order $status")
            .setSmallIcon(R.drawable.bike)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun showCompletionNotification(productName: ArrayList<String>?) {
        val channelId = "OrderCompleteChannel"
        val channelName = "Order Updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("🎉 Congratulations!")
            .setContentText("Your order $productName has been delivered successfully.")
            .setSmallIcon(R.drawable.bike)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }

    private fun showOtpNotification(otp: String?,deliveryBoy:String) {
        val channelId = "OrderCompleteChannel"
        val channelName = "Order Updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Your Order OTP is $otp")
            .setContentText("Share it with delivery boy $deliveryBoy.")
            .setSmallIcon(R.drawable.bike)
            .setAutoCancel(false)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }


    private fun showCancelledNotification(productName: ArrayList<String>) {
        val channelId = "OrderCompleteChannel"
        val channelName = "Order Updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sorry !!")
            .setContentText("Your order $productName has been cancelled.")
            .setSmallIcon(R.drawable.bike)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }

    private fun showInProgressNotification(deliveryBoy: String?) {
        val channelId = "OrderCompleteChannel"
        val channelName = "Order Updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Your Order going to be ready")
            .setContentText("$deliveryBoy accepted your order")
            .setSmallIcon(R.drawable.bike)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }


}