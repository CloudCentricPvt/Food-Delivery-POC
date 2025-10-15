package com.cccinfotech.fooddeliverypoc.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service.NOTIFICATION_SERVICE
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cccinfotech.fooddeliverypoc.R
import com.cccinfotech.fooddeliverypoc.screens.splash.MainActivity

class MyWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "OrderCompleteChannel"
        val channelName = "Order Updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(appContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(appContext, channelId)
            .setContentTitle("Try New Food")
            .setContentText("Its Your day and try new food")
            .setSmallIcon(R.drawable.food)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }
}