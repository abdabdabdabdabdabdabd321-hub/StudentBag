package com.example.studentbag.utils

import android.app.*
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.studentbag.R
import android.content.Context.MODE_PRIVATE

object NotificationHelper {

    private const val CHANNEL_ID = "study_channel"

    fun show(context: Context, title: String, message: String) {

        val prefs = context.getSharedPreferences("studentbag", MODE_PRIVATE)

        if (!prefs.getBoolean("notifications_enabled", true)) {
            return
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "تنبيهات حقيبة الطالب",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }

            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("🎒 StudentBag")
            .setContentText("$title: $message")
            .setSmallIcon(R.mipmap.ic_launcher) // ✅ أيقونة التطبيق
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL) // 🔊 صوت + 📳 اهتزاز
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}