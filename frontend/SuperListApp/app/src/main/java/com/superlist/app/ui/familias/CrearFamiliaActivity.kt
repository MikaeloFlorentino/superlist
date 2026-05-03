package com.superlist.app.ui.familias

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.CrearFamiliaRequest
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityCrearFamiliaBinding
import kotlinx.coroutines.*

class CrearFamiliaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearFamiliaBinding
    private val api = RetrofitClient.familiaApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearFamiliaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnCreate.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isBlank()) return@setOnClickListener
            crearFamilia(name)
        }
    }

    private fun crearFamilia(name: String) {
        binding.btnCreate.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.crearFamilia(tokenManager.getAuthHeader(), CrearFamiliaRequest(name))
                withContext(Dispatchers.Main) {
                    binding.btnCreate.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@CrearFamiliaActivity, R.string.family_created, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CrearFamiliaActivity, "Error al crear familia", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnCreate.isEnabled = true
                    Toast.makeText(this@CrearFamiliaActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
