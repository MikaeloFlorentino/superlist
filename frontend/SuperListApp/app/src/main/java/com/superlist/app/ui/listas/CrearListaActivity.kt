package com.superlist.app.ui.listas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.CrearListaRequest
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityCrearListaBinding
import kotlinx.coroutines.*

class CrearListaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearListaBinding
    private val api = RetrofitClient.listaApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnCreate.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            if (nombre.isBlank()) return@setOnClickListener
            val supermercado = binding.etSupermercado.text.toString().trim().ifBlank { null }
            crearLista(nombre, supermercado)
        }
    }

    private fun crearLista(nombre: String, supermercado: String?) {
        val familiaId = tokenManager.familiaActualId ?: return
        binding.btnCreate.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.crearLista(tokenManager.getAuthHeader(), familiaId, CrearListaRequest(nombre, supermercado))
                withContext(Dispatchers.Main) {
                    binding.btnCreate.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@CrearListaActivity, R.string.list_created, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CrearListaActivity, "Error al crear lista", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnCreate.isEnabled = true
                    Toast.makeText(this@CrearListaActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
