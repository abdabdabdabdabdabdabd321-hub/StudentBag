package com.example.studentbag.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentbag.R
import com.example.studentbag.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import android.content.Intent
import androidx.core.net.toUri
import com.example.studentbag.PaymentHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.studentbag.utils.SummaryData

class SummaryActivity : AppCompatActivity() {

    private lateinit var btnFreeSummary: Button
    private lateinit var btnProSummary: Button
    private lateinit var btnBuy: Button
    private lateinit var btnShareSummary: Button
    private lateinit var txtSummary: TextView

    private lateinit var subjectName: String

    private var currentSummary = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_summary)

        btnFreeSummary = findViewById(R.id.btnFreeSummary)
        btnProSummary = findViewById(R.id.btnProSummary)
        btnBuy = findViewById(R.id.btnBuy)
        btnShareSummary = findViewById(R.id.btnShareSummary)
        txtSummary = findViewById(R.id.txtSummary)

        subjectName = intent.getStringExtra("subjectName") ?: ""

        btnBuy.isEnabled = false
        btnBuy.text = "جارٍ التحقق..."

        loadSubscriptionFromFirestore()

        btnFreeSummary.setOnClickListener {
            generateFreeSummary()
        }

        btnProSummary.setOnClickListener {

            if (!isSubscribed()) {

                AlertDialog.Builder(this)
                    .setTitle("التلخيص الذكي")
                    .setMessage(
                        "هذه الميزة متاحة للمشتركين فقط.\n\n" +
                                "يرجى شراء الاشتراك أولاً."
                    )
                    .setPositiveButton("حسناً", null)
                    .show()

                return@setOnClickListener
            }

            txtSummary.text = "⏳ جاري إنشاء الملخص الذكي..."

            lifecycleScope.launch {

                val data = loadContentData()

                if (
                    data.text.isEmpty() &&
                    data.powerpointFiles.isEmpty() &&
                    data.imageFiles.isEmpty()
                ) {

                    txtSummary.text = "لا يوجد محتوى"

                    return@launch
                }

                com.example.studentbag.utils.SummaryGenerator
                    .generateSmartSummary(
                        this@SummaryActivity,
                        data
                    ) { summary ->

                        runOnUiThread {

                            currentSummary = summary

                            txtSummary.text = summary

                            btnShareSummary.isEnabled = true

                        }
                    }
            }
        }

        btnBuy.setOnClickListener {

            if (isSubscribed()) {

                val prefs = getSharedPreferences(
                    "subscription",
                    MODE_PRIVATE
                )

                val endDate =
                    prefs.getLong("endDate", 0)

                val planName =
                    prefs.getString(
                        "planName",
                        "غير معروف"
                    )

                val daysLeft =
                    TimeUnit.MILLISECONDS.toDays(
                        endDate - System.currentTimeMillis()
                    )

                AlertDialog.Builder(this)
                    .setTitle("تفاصيل الاشتراك")
                    .setMessage(
                        "✅ الاشتراك مفعل\n\n" +
                                "الخطة الحالية: $planName\n\n" +
                                "الأيام المتبقية: $daysLeft يوم\n\n" +
                                "سيبقى التلخيص الذكي متاحاً حتى انتهاء المدة."
                    )
                    .setPositiveButton("موافق", null)
                    .show()

            } else {

                showSubscriptionDialog()
            }
        }

        btnShareSummary.setOnClickListener {

            if (currentSummary.isEmpty()) return@setOnClickListener

            com.example.studentbag.utils.SummaryFileHelper
                .shareSummary(
                    this,
                    subjectName,
                    currentSummary
                )
        }

    }

    override fun onResume() {
        super.onResume()

        loadSubscriptionFromFirestore()
    }

    private fun showSubscriptionDialog() {

        val view = layoutInflater.inflate(
            R.layout.dialog_subscription,
            null
        )

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        val btnSubscribeNow =
            view.findViewById<Button>(
                R.id.btnSubscribeNow
            )
        btnSubscribeNow.text = "اشترك الآن"

        btnSubscribeNow.setOnClickListener {

            Thread {

                val paymentHelper = PaymentHelper()

                val email = com.google.firebase.auth.FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.email

                if (email == null) {

                    Toast.makeText(
                        this,
                        "يجب تسجيل الدخول أولاً",
                        Toast.LENGTH_LONG
                    ).show()

                    return@Thread
                }

                val checkoutUrl =
                    paymentHelper.getCheckoutUrl(email)

                runOnUiThread {

                    if (checkoutUrl != null) {

                        dialog.dismiss()

                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            checkoutUrl.toUri()
                        )

                        startActivity(intent)

                    } else {

                        Toast.makeText(
                            this,
                            "فشل الحصول على رابط الدفع",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }.start()
        }

        dialog.show()
    }

    private fun isSubscribed(): Boolean {

        val prefs = getSharedPreferences(
            "subscription",
            MODE_PRIVATE
        )

        val subscribed =
            prefs.getBoolean("subscribed", false)

        if (!subscribed)
            return false

        val endDate =
            prefs.getLong("endDate", 0)

        if (System.currentTimeMillis() > endDate) {

            prefs.edit()
                .putBoolean("subscribed", false)
                .remove("startDate")
                .remove("endDate")
                .remove("planName")
                .apply()

            updateSubscriptionUI()

            return false
        }

        return true
    }

    private fun updateSubscriptionUI() {

        if (isSubscribed()) {

            btnBuy.text = "تم الاشتراك"

            btnBuy.setBackgroundResource(
                R.drawable.bg_button_green
            )

        } else {

            btnBuy.text = "شراء الاشتراك"

            btnBuy.setBackgroundResource(
                R.drawable.bg_button_red
            )
        }
    }

    private fun loadSubscriptionFromFirestore() {

        val user = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val subscribed =
                        document.getBoolean("subscribed") ?: false

                    val endDate =
                        document.getLong("endDate") ?: 0L

                    val planName =
                        document.getString("planName") ?: "غير معروف"

                    val startDate =
                        document.getLong("startDate") ?: 0L

                    getSharedPreferences(
                        "subscription",
                        MODE_PRIVATE
                    )
                        .edit()
                        .putBoolean("subscribed", subscribed)
                        .putLong("startDate", startDate)
                        .putLong("endDate", endDate)
                        .putString("planName", planName)
                        .apply()
                }

                updateSubscriptionUI()
                btnBuy.isEnabled = true
            }
            .addOnFailureListener {

                btnBuy.isEnabled = true
                updateSubscriptionUI()

            }
    }

    private fun generateFreeSummary() {

        txtSummary.text = "⏳ جاري إنشاء ملخص مجاني..."

        lifecycleScope.launch {

            val data = loadContentData()

            val text = data.text

            if (text.isEmpty()) {

                txtSummary.text = "لا يوجد محتوى"

                return@launch
            }

            val summary = simpleSummary(text)

            currentSummary = summary

            txtSummary.text = summary

            btnShareSummary.isEnabled = true
        }
    }

    private suspend fun loadContentData(): SummaryData {

        val db = AppDatabase.getDatabase(this)

        val contents = withContext(Dispatchers.IO) {
            db.subjectContentDao().getAllContents(subjectName)
        }

        return withContext(Dispatchers.IO) {

            com.example.studentbag.utils.ContentExtractor
                .extractAllData(
                    this@SummaryActivity,
                    contents
                )
        }
    }

    private fun simpleSummary(text: String): String {

        val sentences = text.split(".", "؟", "!")

        return if (sentences.size > 5) {

            sentences.take(5)
                .joinToString(". ")

        } else {

            text.take(500)
        }
    }
}