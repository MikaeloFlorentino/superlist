package com.superlist.app.ui.familias

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.UnirseRequest
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityUnirseFamiliaBinding
import kotlinx.coroutines.*

class UnirseFamiliaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUnirseFamiliaBinding
    private val api = RetrofitClient.familiaApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnirseFamiliaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnJoin.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.isBlank()) return@setOnClickListener
            unirse(code)
        }
    }

    private fun unirse(code: String) {
        binding.btnJoin.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.unirsePorCodigo(tokenManager.getAuthHeader(), UnirseRequest(code))
                withContext(Dispatchers.Main) {
                    binding.btnJoin.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@UnirseFamiliaActivity, "Te has unido a la familia", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@UnirseFamiliaActivity, "Código inválido", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnJoin.isEnabled = true
                    Toast.makeText(this@UnirseFamiliaActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
