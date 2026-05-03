package com.superlist.app.ui.catalogo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.CrearArticuloRequest
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityCrearArticuloBinding
import kotlinx.coroutines.*

class CrearArticuloActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearArticuloBinding
    private val api = RetrofitClient.catalogoApi
    private lateinit var tokenManager: TokenManager
    private var familiaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearArticuloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        familiaId = intent.getStringExtra("familiaId") ?: tokenManager.familiaActualId ?: ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSave.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val sku = binding.etSku.text.toString().trim()
            if (nombre.isBlank() || sku.isBlank()) {
                Toast.makeText(this, "Nombre y SKU requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val codigoBarras = binding.etCodigoBarras.text.toString().trim().ifBlank { null }
            crearArticulo(nombre, sku, codigoBarras)
        }
    }

    private fun crearArticulo(nombre: String, sku: String, codigoBarras: String?) {
        binding.btnSave.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = CrearArticuloRequest(nombre = nombre, sku = sku, codigoBarras = codigoBarras, areaSuperId = null, areaCasaId = null, cantidadDefecto = null)
                val response = api.crearArticulo(tokenManager.getAuthHeader(), familiaId, request)
                withContext(Dispatchers.Main) {
                    binding.btnSave.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@CrearArticuloActivity, R.string.article_created, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CrearArticuloActivity, "Error al crear artículo", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this@CrearArticuloActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    companion object {
        fun start(context: Context, familiaId: String) {
            context.startActivity(Intent(context, CrearArticuloActivity::class.java).putExtra("familiaId", familiaId))
        }
    }
}
