package com.example.studentbag.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.studentbag.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnGoogleSignIn: Button

    private lateinit var btnTogglePassword: ImageButton
    private lateinit var tvForgotPassword: TextView

    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        tvForgotPassword.setOnClickListener {

            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {

                etEmail.error = "أدخل بريدك الإلكتروني أولاً"
                etEmail.requestFocus()

                Toast.makeText(
                    this,
                    "أدخل بريدك الإلكتروني لإرسال رابط تغيير كلمة المرور",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            tvForgotPassword.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->

                    tvForgotPassword.isEnabled = true

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "تم إرسال رابط تغيير كلمة المرور إلى بريدك الإلكتروني",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.message
                                ?: "تعذر إرسال رابط تغيير كلمة المرور",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnTogglePassword.setOnClickListener {

            passwordVisible = !passwordVisible

            if (passwordVisible) {

                etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()

                btnTogglePassword.setImageResource(
                    android.R.drawable.ic_menu_close_clear_cancel
                )

            } else {

                etPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()

                btnTogglePassword.setImageResource(
                    android.R.drawable.ic_menu_view
                )
            }

            etPassword.setSelection(etPassword.text.length)
        }

        btnRegister.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "أدخل البريد الإلكتروني وكلمة المرور",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val user = auth.currentUser

                        if (user != null) {

                            val data = hashMapOf(
                                "email" to user.email,
                                "subscribed" to false,
                                "planName" to "",
                                "startDate" to 0L,
                                "endDate" to 0L
                            )

                            db.collection("users")
                                .document(user.uid)
                                .set(data)
                        }

                        Toast.makeText(
                            this,
                            "تم إنشاء الحساب بنجاح",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.message
                                ?: "فشل إنشاء الحساب",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "أدخل البريد الإلكتروني وكلمة المرور",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    btnLogin.isEnabled = true

                    if (task.isSuccessful) {

                        openMainActivity()

                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.message
                                ?: "فشل تسجيل الدخول",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnGoogleSignIn.setOnClickListener {
            startGoogleSignIn()
        }
    }
    private fun startGoogleSignIn() {

        btnGoogleSignIn.isEnabled = false

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(
                getString(R.string.default_web_client_id)
            )
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {

            try {

                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(
                            credential.data
                        )

                    firebaseAuthWithGoogle(
                        googleCredential.idToken
                    )

                } else {

                    btnGoogleSignIn.isEnabled = true

                    Toast.makeText(
                        this@LoginActivity,
                        "تعذر قراءة حساب Google",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (exception: GoogleIdTokenParsingException) {

                btnGoogleSignIn.isEnabled = true

                Toast.makeText(
                    this@LoginActivity,
                    "تعذر قراءة بيانات حساب Google",
                    Toast.LENGTH_LONG
                ).show()

            } catch (exception: GetCredentialException) {

                btnGoogleSignIn.isEnabled = true

                Toast.makeText(
                    this@LoginActivity,
                    exception.message
                        ?: "تم إلغاء تسجيل الدخول أو تعذر فتح حسابات Google",
                    Toast.LENGTH_LONG
                ).show()

            } catch (exception: Exception) {

                btnGoogleSignIn.isEnabled = true

                Toast.makeText(
                    this@LoginActivity,
                    exception.message
                        ?: "حدث خطأ أثناء تسجيل الدخول باستخدام Google",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val firebaseCredential =
            GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    saveGoogleUserIfNeeded()

                } else {

                    btnGoogleSignIn.isEnabled = true

                    Toast.makeText(
                        this,
                        task.exception?.message
                            ?: "فشل تسجيل الدخول باستخدام Google",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveGoogleUserIfNeeded() {

        val user = auth.currentUser

        if (user == null) {

            btnGoogleSignIn.isEnabled = true

            Toast.makeText(
                this,
                "تعذر الحصول على بيانات المستخدم",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val userDocument = db.collection("users")
            .document(user.uid)

        userDocument.get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    openMainActivity()

                } else {

                    val data = hashMapOf(
                        "email" to user.email,
                        "name" to (user.displayName ?: ""),
                        "subscribed" to false,
                        "planName" to "",
                        "startDate" to 0L,
                        "endDate" to 0L
                    )

                    userDocument.set(data)
                        .addOnSuccessListener {
                            openMainActivity()
                        }
                        .addOnFailureListener { exception ->

                            btnGoogleSignIn.isEnabled = true

                            Toast.makeText(
                                this,
                                exception.message
                                    ?: "تعذر حفظ بيانات المستخدم",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->

                btnGoogleSignIn.isEnabled = true

                Toast.makeText(
                    this,
                    exception.message
                        ?: "تعذر التحقق من بيانات المستخدم",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openMainActivity() {

        getSharedPreferences(
            "subscription",
            MODE_PRIVATE
        ).edit().clear().apply()

        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )

        finish()
    }
}
