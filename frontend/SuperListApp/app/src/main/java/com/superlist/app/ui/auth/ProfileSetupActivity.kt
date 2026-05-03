package com.superlist.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.ActualizarPerfilRequest
import com.superlist.app.databinding.ActivityProfileSetupBinding
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.ui.familias.FamiliasActivity
import kotlinx.coroutines.*

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private val api = RetrofitClient.authApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isBlank()) {
                binding.tilName.error = "Ingresa tu nombre"
                return@setOnClickListener
            }
            binding.tilName.error = null
            guardarPerfil(name)
        }

        binding.tvSkip.setOnClickListener {
            irAFamilias()
        }
    }

    private fun guardarPerfil(name: String) {
        binding.btnSave.isEnabled = false
        binding.btnSave.text = getString(R.string.loading)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.actualizarPerfil(tokenManager.getAuthHeader(), ActualizarPerfilRequest(name))
                withContext(Dispatchers.Main) {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = getString(R.string.save)
                    if (response.isSuccessful) {
                        tokenManager.userName = name
                        irAFamilias()
                    } else {
                        Toast.makeText(this@ProfileSetupActivity, "Error al guardar perfil", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = getString(R.string.save)
                    Toast.makeText(this@ProfileSetupActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun irAFamilias() {
        startActivity(Intent(this, FamiliasActivity::class.java))
        finish()
    }
}
