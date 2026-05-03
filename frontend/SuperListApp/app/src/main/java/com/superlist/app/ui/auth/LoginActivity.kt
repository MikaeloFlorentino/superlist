package com.superlist.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.MensajeResponse
import com.superlist.app.data.model.SolicitarCodigoRequest
import com.superlist.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val api = RetrofitClient.authApi
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendCode.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.isBlank()) {
                binding.tilPhone.error = getString(R.string.phone_error)
                return@setOnClickListener
            }
            binding.tilPhone.error = null
            solicitarCodigo(phone)
        }
    }

    private fun solicitarCodigo(phone: String) {
        binding.btnSendCode.isEnabled = false
        binding.btnSendCode.text = getString(R.string.loading)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.solicitarCodigo(SolicitarCodigoRequest(phone))
                withContext(Dispatchers.Main) {
                    binding.btnSendCode.isEnabled = true
                    binding.btnSendCode.text = getString(R.string.send_code)
                    if (response.isSuccessful) {
                        val intent = Intent(this@LoginActivity, VerifyCodeActivity::class.java)
                        intent.putExtra("phone", phone)
                        startActivity(intent)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val msg = if (errorBody?.contains("error") == true) {
                            parseError(errorBody)
                        } else "Error al enviar código"
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnSendCode.isEnabled = true
                    binding.btnSendCode.text = getString(R.string.send_code)
                    Toast.makeText(this@LoginActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseError(body: String): String {
        return try {
            com.google.gson.Gson().fromJson(body, MensajeResponse::class.java).error ?: "Error"
        } catch (e: Exception) { "Error" }
    }
}
