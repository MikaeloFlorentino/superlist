package com.superlist.app.ui.compras

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.*
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityModoCompraBinding
import kotlinx.coroutines.*

class ModoCompraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModoCompraBinding
    private val listaApi = RetrofitClient.listaApi
    private val compraApi = RetrofitClient.compraApi
    private lateinit var tokenManager: TokenManager
    private var listaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModoCompraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        listaId = intent.getStringExtra("listaId") ?: run { finish(); return }

        supportActionBar?.title = getString(R.string.shopping_mode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cargarModoCompra()
    }

    private fun cargarModoCompra() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = compraApi.obtenerModoCompra(tokenManager.getAuthHeader(), listaId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) mostrarModoCompra(data)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun mostrarModoCompra(data: ModoCompraResponse) {
        val total = data.totalItems ?: 0
        val completados = data.completados ?: 0
        binding.tvProgress.text = "$completados / $total completados"
        binding.progressBar.max = if (total > 0) total else 1
        binding.progressBar.progress = completados

        val areas = data.areas ?: emptyList()
        binding.rvAreas.layoutManager = LinearLayoutManager(this)

        // Build a flat list of items with area headers
        val itemsConArea = mutableListOf<Pair<String?, ItemListaResponse?>>() // null header = area label
        for (area in areas) {
            itemsConArea.add(Pair(area.areaNombre, null)) // header
            for (item in (area.items ?: emptyList())) {
                itemsConArea.add(Pair(null, item))
            }
        }

        binding.rvAreas.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ModoCompraActivity.VH>() {
            override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): VH {
                val card = com.google.android.material.card.MaterialCardView(p.context).apply {
                    layoutParams = android.view.ViewGroup.MarginLayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 4, 16, 4) }
                    radius = 8f; setContentPadding(16, 12, 16, 12)
                }
                val tv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 16f }
                card.addView(tv)
                return VH(card, tv)
            }

            override fun onBindViewHolder(h: VH, pos: Int) {
                val (areaName, item) = itemsConArea[pos]
                if (item == null) {
                    // Area header
                    h.tv.text = "📍 ${areaName ?: "Sin categoría"}"
                    h.tv.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                    h.tv.textSize = 15f
                    h.tv.setTypeface(null, android.graphics.Typeface.BOLD)
                    (h.itemView as com.google.android.material.card.MaterialCardView).setCardBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
                } else {
                    (h.itemView as com.google.android.material.card.MaterialCardView).setCardBackgroundColor(android.graphics.Color.WHITE)
                    val estado = item.estado ?: "PENDIENTE"
                    h.tv.text = "${item.nombre ?: "?"} x${item.cantidad ?: 1}"
                    val color = if (estado == "COMPRADO") android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.parseColor("#212121")
                    h.tv.setTextColor(color)
                    h.tv.textSize = 16f
                    h.tv.setTypeface(null, android.graphics.Typeface.NORMAL)
                    h.itemView.setOnClickListener {
                        // Toggle completed
                        val newEstado = if (estado != "COMPRADO") "COMPRADO" else "PENDIENTE"
                        cambiarEstado(item.id ?: return@setOnClickListener, newEstado, item.id!!)
                    }
                    h.itemView.setOnLongClickListener {
                        marcarNoHay(item.id ?: return@setOnLongClickListener false)
                        true
                    }
                }
            }

            override fun getItemCount() = itemsConArea.size
        }
    }

    private fun cambiarEstado(itemId: String, estado: String, clickItemId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                listaApi.cambiarEstadoItem(tokenManager.getAuthHeader(), itemId, ItemEstadoRequest(estado))
                withContext(Dispatchers.Main) { cargarModoCompra() }
            } catch (_: Exception) {}
        }
    }

    private fun marcarNoHay(itemId: String): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = compraApi.marcarNoHay(tokenManager.getAuthHeader(), listaId, MarcarNoHayRequest(itemId))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ModoCompraActivity, "Marcado como no disponible", Toast.LENGTH_SHORT).show()
                        cargarModoCompra()
                    }
                }
            } catch (_: Exception) {}
        }
        return true
    }

    class VH(v: android.view.View, val tv: com.google.android.material.textview.MaterialTextView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
