package com.example.studentbag.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.studentbag.R
import com.google.firebase.auth.FirebaseAuth
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import android.content.res.Configuration
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var txtEmail: TextView
    private lateinit var txtLanguage: TextView
    private lateinit var txtVersion: TextView

    private lateinit var prefs: SharedPreferences

    private lateinit var switchDarkMode: Switch
    private lateinit var switchNotifications: Switch

    private lateinit var layoutPrivacy: LinearLayout
    private lateinit var layoutLogout: LinearLayout

    private lateinit var layoutLanguage: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("studentbag", MODE_PRIVATE)

        txtEmail = findViewById(R.id.txtEmail)
        txtLanguage = findViewById(R.id.txtLanguage)
        txtVersion = findViewById(R.id.txtVersion)

        switchDarkMode = findViewById(R.id.switchDarkMode)
        switchNotifications = findViewById(R.id.switchNotifications)

        layoutPrivacy = findViewById(R.id.layoutPrivacy)
        layoutLogout = findViewById(R.id.layoutLogout)
        layoutLanguage = findViewById(R.id.layoutLanguage)

        layoutLanguage.setOnClickListener {

            val languages = arrayOf(
                "العربية",
                "English"
            )

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("اختر لغة التطبيق")
                .setItems(languages) { _, which ->

                    when (which) {

                        0 -> {

                            txtLanguage.text = "العربية"

                            Toast.makeText(
                                this,
                                "سيتم دعم العربية بالكامل قريبًا",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                        1 -> {

                            txtLanguage.text = "English"

                            Toast.makeText(
                                this,
                                "English support will be available soon",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }

                }
                .setNegativeButton("إلغاء", null)
                .show()

        }

        val user = FirebaseAuth.getInstance().currentUser

        txtEmail.text = user?.email ?: "غير مسجل"

        txtLanguage.text = "العربية"

        txtVersion.text = "Version 1.0"

        val settingsPrefs = getSharedPreferences("settings", MODE_PRIVATE)

        switchDarkMode.isChecked =
            settingsPrefs.getBoolean("darkMode", false)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->

            settingsPrefs.edit()
                .putBoolean("darkMode", isChecked)
                .apply()

            if (isChecked) {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

            } else {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )

            }

        }

        // تحميل آخر حالة محفوظة
        switchNotifications.isChecked =
            prefs.getBoolean("notifications_enabled", true)

// عند تغيير المفتاح
// قراءة آخر حالة محفوظة
        switchNotifications.isChecked =
            prefs.getBoolean("notifications_enabled", true)

// عند تغيير المفتاح
        // قراءة آخر حالة محفوظة
        switchNotifications.isChecked =
            prefs.getBoolean("notifications_enabled", true)

// عند تغيير المفتاح
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->

            prefs.edit()
                .putBoolean("notifications_enabled", isChecked)
                .apply()

            if (isChecked) {

                Toast.makeText(
                    this,
                    "تم تشغيل الإشعارات",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "تم إيقاف الإشعارات",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

        layoutPrivacy.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    PrivacyPolicyActivity::class.java
                )
            )

        }

        layoutLogout.setOnClickListener {

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("تسجيل الخروج")
                .setMessage("هل تريد تسجيل الخروج من الحساب؟")
                .setPositiveButton("نعم") { _, _ ->

                    FirebaseAuth.getInstance().signOut()

                    startActivity(
                        Intent(
                            this,
                            LoginActivity::class.java
                        )
                    )

                    finishAffinity()

                }
                .setNegativeButton("إلغاء", null)
                .show()

        }

    }

    private fun changeLanguage(languageCode: String) {

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        prefs.edit()
            .putString("language", languageCode)
            .apply()

        val locale = Locale(languageCode)

        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(
            config,
            resources.displayMetrics
        )

        recreate()
    }

}