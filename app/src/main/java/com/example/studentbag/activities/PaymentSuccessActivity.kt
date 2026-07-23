package com.example.studentbag.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaymentSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDate = System.currentTimeMillis()

        val endDate =
            startDate + (30L * 24 * 60 * 60 * 1000)

        getSharedPreferences(
            "subscription",
            MODE_PRIVATE
        )
            .edit()
            .putBoolean("subscribed", true)
            .putString(
                "planName",
                "الخطة الشهرية"
            )
            .putLong(
                "startDate",
                startDate
            )
            .putLong(
                "endDate",
                endDate
            )
            .apply()

        val user =
            FirebaseAuth.getInstance().currentUser

        if (user != null) {

            val subscriptionData = hashMapOf(

                "email" to user.email,
                "subscribed" to true,
                "planName" to "الخطة الشهرية",
                "startDate" to startDate,
                "endDate" to endDate

            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .set(subscriptionData)
        }

        Toast.makeText(
            this,
            "تم تفعيل الاشتراك بنجاح ✅",
            Toast.LENGTH_LONG
        ).show()

        finish()
    }
}