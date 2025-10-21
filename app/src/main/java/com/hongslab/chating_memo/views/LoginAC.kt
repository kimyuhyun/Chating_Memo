package com.hongslab.chating_memo.views

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ActivityLoginBinding
import com.hongslab.chating_memo.dialog.IdPwInputDialog
import com.hongslab.chating_memo.repository.AuthRepository
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import com.hongslab.chating_memo.utils.startActivityForResult2
import kotlinx.coroutines.launch

class LoginAC : BaseAC(TransitionMode.HORIZON), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_policy -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.POLICY_KR))
                startActivity(intent)
            }

            R.id.btn_use_terms -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.USE_TERMS_KR))
                startActivity(intent)
            }

            R.id.btn_id_login -> {
                IdPwInputDialog(this) { isOk, id, pw ->
                    if (isOk) {
                        if (id.isEmpty()) {
                            Toast.makeText(applicationContext, "Please enter your ID.", Toast.LENGTH_LONG).show()
                            return@IdPwInputDialog
                        }

                        if (pw.isEmpty()) {
                            Toast.makeText(applicationContext, "Please enter your password.", Toast.LENGTH_LONG).show()
                            return@IdPwInputDialog
                        }

                        if (id == "tester" && pw == "abc123") {
                            loginOK("tester", "")
                        } else {
                            Toast.makeText(applicationContext, "The ID or password does not match.", Toast.LENGTH_LONG).show()
                        }
                    }
                }.show()
            }

            R.id.btn_google_login -> {
                lifecycleScope.launch {
                    val intent = MyApplication.GOOGLE_SIGN_IN_CLIENT?.signInIntent
                    val activityResult = intent?.let { startActivityForResult2(it) }
                    if (activityResult != null) {
                        if (activityResult.resultCode == Activity.RESULT_OK) {
                            binding.llLoading.visibility = View.VISIBLE

                            val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
                            try {
                                val gsa = task.getResult(ApiException::class.java)
                                val personName = gsa.displayName
                                val personEmail = gsa.email
                                val personId = gsa.id
                                val personPhoto = gsa.photoUrl
                                val serverAuthCode = gsa.serverAuthCode // 🔹 서버 검증용

                                Dlog.d("personName: $personName")
                                Dlog.d("personEmail: $personEmail")
                                Dlog.d("personId: $personId")
                                Dlog.d("personPhoto: $personPhoto")
                                Dlog.d("serverAuthCode: $serverAuthCode")

                                if (personEmail != null && serverAuthCode != null) {
                                    loginOK(personEmail, serverAuthCode)
                                } else {
                                    MyUtils.myToast("There is no email information.")
                                }
                            } catch (e: ApiException) {
                                e.printStackTrace()
                                binding.llLoading.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.click = this



        window.apply {
            statusBarColor = Color.parseColor("#ffe991")
            // 상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true

            navigationBarColor = Color.parseColor("#ffe991")
            // 네비바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = false
        }
    }

    private fun loginOK(email: String, serverAuthCode: String) {
        lifecycleScope.launch {
            val (accessToken, refreshToken) = authRepository.getTokens(email, serverAuthCode)
            Dlog.d("accessToken: $accessToken, refreshToken: $refreshToken")

            SPre.set(SCol.ACCESS_TOKEN.name, accessToken)
            SPre.set(SCol.REFRESH_TOKEN.name, refreshToken)

            startActivity(Intent(this@LoginAC, MainAC::class.java))
            finish()
        }
    }
}