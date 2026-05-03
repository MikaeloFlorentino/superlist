package com.superlist.app.ui.catalogo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.ArticuloResponse
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityCatalogoBinding
import kotlinx.coroutines.*

class CatalogoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCatalogoBinding
    private val api = RetrofitClient.catalogoApi
    private lateinit var tokenManager: TokenManager
    private var articulos = listOf<ArticuloResponse>()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.title = getString(R.string.catalog)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.fabAdd.setOnClickListener {
            com.superlist.app.ui.catalogo.CrearArticuloActivity.start(this, tokenManager.familiaActualId ?: "")
        }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(300)
                    s?.toString()?.let { buscarArticulos(it) }
                }
            }
        })

        binding.srlCatalogo.setOnRefreshListener { cargarArticulos() }

        cargarArticulos()
    }

    private fun cargarArticulos() {
        val familiaId = tokenManager.familiaActualId ?: return
        binding.srlCatalogo.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.listarArticulos(tokenManager.getAuthHeader(), familiaId)
                withContext(Dispatchers.Main) {
                    binding.srlCatalogo.isRefreshing = false
                    if (response.isSuccessful) {
                        articulos = response.body() ?: emptyList()
                        mostrarArticulos(articulos)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.srlCatalogo.isRefreshing = false
                }
            }
        }
    }

    private fun buscarArticulos(query: String) {
        val familiaId = tokenManager.familiaActualId ?: return
        if (query.length < 2) {
            cargarArticulos()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.buscarArticulos(tokenManager.getAuthHeader(), familiaId, query)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        articulos = response.body() ?: emptyList()
                        mostrarArticulos(articulos)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun mostrarArticulos(articulos: List<ArticuloResponse>) {
        if (articulos.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvArticulos.visibility = android.view.View.GONE
            return
        }
        binding.tvEmpty.visibility = android.view.View.GONE
        binding.rvArticulos.visibility = android.view.View.VISIBLE

        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<CatalogoActivity.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
                val card = com.google.android.material.card.MaterialCardView(parent.context).apply {
                    layoutParams = android.view.ViewGroup.MarginLayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 8, 16, 8) }
                    radius = 12f
                    cardElevation = 2f
                    setContentPadding(24, 16, 24, 16)
                }
                val ll = android.widget.LinearLayout(parent.context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                }
                val nameTv = com.google.android.material.textview.MaterialTextView(parent.context).apply {
                    textSize = 16f
                }
                ll.addView(nameTv)
                val skuTv = com.google.android.material.textview.MaterialTextView(parent.context).apply {
                    textSize = 14f
                    setTextColor(0xFF757575.toInt())
                }
                ll.addView(skuTv)
                card.addView(ll)
                return ViewHolder(card, nameTv, skuTv)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val a = articulos[position]
                holder.nameTv.text = a.nombre ?: "Sin nombre"
                holder.skuTv.text = "SKU: ${a.sku ?: "-"}"
            }

            override fun getItemCount() = articulos.size
        }

        binding.rvArticulos.layoutManager = LinearLayoutManager(this)
        binding.rvArticulos.adapter = adapter
    }

    class ViewHolder(
        card: android.view.View,
        val nameTv: com.google.android.material.textview.MaterialTextView,
        val skuTv: com.google.android.material.textview.MaterialTextView
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(card)

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
