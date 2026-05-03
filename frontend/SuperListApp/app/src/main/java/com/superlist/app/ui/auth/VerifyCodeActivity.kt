package com.superlist.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.VerificarCodigoRequest
import com.superlist.app.databinding.ActivityVerifyCodeBinding
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.ui.familias.FamiliasActivity
import kotlinx.coroutines.*

class VerifyCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyCodeBinding
    private val api = RetrofitClient.authApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        val phone = intent.getStringExtra("phone") ?: ""

        binding.tvCodeSent.text = getString(R.string.code_sent, phone)

        binding.btnVerify.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.length != 6) {
                binding.tilCode.error = getString(R.string.code_error)
                return@setOnClickListener
            }
            binding.tilCode.error = null
            verificarCodigo(phone, code)
        }

        binding.tvResend.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun verificarCodigo(phone: String, code: String) {
        binding.btnVerify.isEnabled = false
        binding.btnVerify.text = getString(R.string.loading)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.verificarCodigo(VerificarCodigoRequest(phone, code))
                withContext(Dispatchers.Main) {
                    binding.btnVerify.isEnabled = true
                    binding.btnVerify.text = getString(R.string.verify_code)
                    if (response.isSuccessful) {
                        val body = response.body()
                        body?.token?.let { tokenManager.token = it }
                        body?.usuario?.let {
                            tokenManager.userId = it.id
                            tokenManager.userPhone = it.telefono
                            tokenManager.userName = it.nombre
                        }
                        if (tokenManager.userName.isNullOrBlank()) {
                            startActivity(Intent(this@VerifyCodeActivity, ProfileSetupActivity::class.java))
                        } else {
                            startActivity(Intent(this@VerifyCodeActivity, FamiliasActivity::class.java))
                        }
                        finish()
                    } else {
                        Toast.makeText(this@VerifyCodeActivity, R.string.code_error, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnVerify.isEnabled = true
                    binding.btnVerify.text = getString(R.string.verify_code)
                    Toast.makeText(this@VerifyCodeActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
