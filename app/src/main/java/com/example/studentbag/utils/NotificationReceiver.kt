package com.example.studentbag.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("title") ?: "تنبيه"
        val message = intent.getStringExtra("message") ?: ""

        NotificationHelper.show(context, title, message)
    }
}