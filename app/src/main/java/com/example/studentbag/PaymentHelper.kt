package com.example.studentbag

import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PaymentHelper {

    fun getCheckoutUrl(email: String): String? {

        return try {

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val formBody = FormBody.Builder()
                .add("email", email)
                .build()

            val request = Request.Builder()
                .url(
                    "https://studentbag-backend-production.up.railway.app/create-checkout-session"
                )
                .post(formBody)
                .build()

            Log.d(
                "STRIPE_PAYMENT",
                "Sending payment request for: $email"
            )

            client.newCall(request).execute().use { response ->

                val responseBody =
                    response.body?.string().orEmpty()

                Log.d(
                    "STRIPE_PAYMENT",
                    "Response code: ${response.code}"
                )

                Log.d(
                    "STRIPE_PAYMENT",
                    "Response body: $responseBody"
                )

                if (!response.isSuccessful) {

                    Log.e(
                        "STRIPE_PAYMENT",
                        "Server returned error ${response.code}: $responseBody"
                    )

                    return null
                }

                if (responseBody.isBlank()) {

                    Log.e(
                        "STRIPE_PAYMENT",
                        "Server returned an empty response"
                    )

                    return null
                }

                val json = JSONObject(responseBody)

                val checkoutUrl =
                    json.optString("url", "")

                if (checkoutUrl.isBlank()) {

                    Log.e(
                        "STRIPE_PAYMENT",
                        "The response does not contain the field url: $responseBody"
                    )

                    null

                } else {

                    Log.d(
                        "STRIPE_PAYMENT",
                        "Checkout URL received successfully"
                    )

                    checkoutUrl
                }
            }

        } catch (e: Exception) {

            Log.e(
                "STRIPE_PAYMENT",
                "Payment request failed: ${e.message}",
                e
            )

            null
        }
    }
}