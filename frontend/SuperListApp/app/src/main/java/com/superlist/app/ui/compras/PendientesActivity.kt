package com.superlist.app.ui.compras

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.*
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityPendientesBinding
import kotlinx.coroutines.*

class PendientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPendientesBinding
    private val compraApi = RetrofitClient.compraApi
    private val listaApi = RetrofitClient.listaApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.title = getString(R.string.pending_items)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.srlPendientes.setOnRefreshListener { cargarPendientes() }

        cargarPendientes()
    }

    override fun onResume() {
        super.onResume()
        cargarPendientes()
    }

    private fun cargarPendientes() {
        val familiaId = tokenManager.familiaActualId ?: return
        binding.srlPendientes.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = compraApi.obtenerPendientes(tokenManager.getAuthHeader(), familiaId)
                withContext(Dispatchers.Main) {
                    binding.srlPendientes.isRefreshing = false
                    if (response.isSuccessful) {
                        val data = response.body()
                        val items = data?.items ?: emptyList()
                        mostrarPendientes(items)
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { binding.srlPendientes.isRefreshing = false }
            }
        }
    }

    private fun mostrarPendientes(items: List<ItemPendienteResponse>) {
        binding.rvPendientes.visibility = if (items.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        binding.tvEmpty.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        binding.rvPendientes.layoutManager = LinearLayoutManager(this)
        binding.rvPendientes.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<PendientesActivity.VH>() {
            override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): VH {
                val card = com.google.android.material.card.MaterialCardView(p.context).apply {
                    layoutParams = android.view.ViewGroup.MarginLayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 8, 16, 8) }
                    radius = 12f; setContentPadding(24, 16, 24, 16)
                }
                val ll = android.widget.LinearLayout(p.context).apply { orientation = android.widget.LinearLayout.VERTICAL }
                val nameTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 16f }
                val infoTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 13f; setTextColor(android.graphics.Color.parseColor("#757575")) }
                ll.addView(nameTv); ll.addView(infoTv)
                card.addView(ll)
                return VH(card, nameTv, infoTv)
            }
            override fun onBindViewHolder(h: VH, pos: Int) {
                val item = items[pos]
                h.nameTv.text = "${item.nombre ?: "?"} x${item.cantidad ?: "1"}"
                h.infoTv.text = "De: ${item.listaOrigenNombre ?: "?"}"
                h.itemView.setOnClickListener { mostrarOpciones(item) }
            }
            override fun getItemCount() = items.size
        }
    }

    private fun mostrarOpciones(item: ItemPendienteResponse) {
        val familiaId = tokenManager.familiaActualId ?: return

        // Get available lists
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = listaApi.listarListas(tokenManager.getAuthHeader(), familiaId)
                val listas = response.body() ?: emptyList()
                val listaNombres = listas.filter { it.estado != "COMPLETADA" }.map { it.nombre ?: "?" }
                val listaIds = listas.filter { it.estado != "COMPLETADA" }.mapNotNull { it.id }

                withContext(Dispatchers.Main) {
                    if (listaNombres.isEmpty()) {
                        Toast.makeText(this@PendientesActivity, "No hay listas activas", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }

                    AlertDialog.Builder(this@PendientesActivity)
                        .setTitle("Mover a lista")
                        .setItems(listaNombres.toTypedArray()) { _, which ->
                            val listaId = listaIds[which]
                            resolverPendiente(item.id ?: return@setItems, listaId)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            } catch (_: Exception) {}
        }
    }

    private fun resolverPendiente(pendienteId: String, listaId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = compraApi.resolverPendiente(
                    tokenManager.getAuthHeader(), pendienteId,
                    ResolverPendienteRequest(listaId)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PendientesActivity, "Pendiente resuelto", Toast.LENGTH_SHORT).show()
                        cargarPendientes()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    class VH(v: android.view.View, val nameTv: com.google.android.material.textview.MaterialTextView, val infoTv: com.google.android.material.textview.MaterialTextView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
