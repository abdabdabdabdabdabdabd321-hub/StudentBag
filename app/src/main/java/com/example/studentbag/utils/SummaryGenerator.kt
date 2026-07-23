package com.example.studentbag.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SummaryGenerator {

    fun generateSmartSummary(

        context: Context,

        data: SummaryData,

        callback: (String) -> Unit

    ) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val summary =
                    ServerApi.generateSummary(
                        context,
                        data
                    )

                callback(summary)

            } catch (e: Exception) {

                callback(
                    "فشل الاتصال بالسيرفر"
                )

            }

        }

    }

}