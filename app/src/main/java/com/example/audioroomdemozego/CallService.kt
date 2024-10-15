package com.example.audioroomdemozego

import android.annotation.SuppressLint
import android.app.ForegroundServiceTypeException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class CallService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Call the method to start the foreground service
        startForegroundService()
        return START_STICKY
    }

    @SuppressLint("UnspecifiedImmutableFlag") // Use this if your target SDK is less than 31
    private fun startForegroundService() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "call_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Call Service Channel", NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, VoiceCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("roomID", roomid) // Pass any necessary data
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // Request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Voice Call")
            .setContentText("Ongoing Call")
            .setSmallIcon(R.drawable.vcmic)
            .setContentIntent(pendingIntent)
            .build()

        // Start the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL) // Correct method for API 31+
        } else {
            startForeground(1, notification) // For API < 31
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}