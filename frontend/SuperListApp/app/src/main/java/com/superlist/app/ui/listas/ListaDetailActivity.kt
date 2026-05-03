package com.superlist.app.ui.listas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.*
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityListaDetailBinding
import com.superlist.app.ui.compras.ModoCompraActivity
import kotlinx.coroutines.*

class ListaDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListaDetailBinding
    private val listaApi = RetrofitClient.listaApi
    private val compraApi = RetrofitClient.compraApi
    private lateinit var tokenManager: TokenManager
    private var listaId: String = ""
    private var items = listOf<ItemListaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        listaId = intent.getStringExtra("listaId") ?: run { finish(); return }
        val nombre = intent.getStringExtra("listaNombre") ?: ""

        supportActionBar?.title = nombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.fabAdd.setOnClickListener { mostrarDialogoAgregarItem() }

        cargarDatos()
    }

    private fun cargarDatos() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val itemsResponse = listaApi.listarItems(tokenManager.getAuthHeader(), listaId)
                val totalResponse = listaApi.obtenerTotal(tokenManager.getAuthHeader(), listaId)

                withContext(Dispatchers.Main) {
                    if (itemsResponse.isSuccessful) {
                        items = itemsResponse.body() ?: emptyList()
                        mostrarItems()
                    }
                    if (totalResponse.isSuccessful) {
                        val t = totalResponse.body()
                        binding.tvTotalItems.text = "${t?.totalItems ?: 0}"
                        binding.tvCompletados.text = "${t?.completados ?: 0}"
                        binding.tvPendientes.text = "${t?.pendientes ?: 0}"
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun mostrarItems() {
        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ListaDetailActivity.VH>() {
            override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): VH {
                val card = com.google.android.material.card.MaterialCardView(p.context).apply {
                    layoutParams = android.view.ViewGroup.MarginLayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 4, 16, 4) }
                    radius = 8f; setContentPadding(16, 12, 16, 12)
                }
                val ll = android.widget.LinearLayout(p.context).apply { orientation = android.widget.LinearLayout.VERTICAL }
                val nameTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 16f }
                val infoTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 13f }
                ll.addView(nameTv); ll.addView(infoTv)
                card.addView(ll)
                return VH(card, nameTv, infoTv)
            }
            override fun onBindViewHolder(h: VH, pos: Int) {
                val item = items[pos]
                h.nameTv.text = "${item.nombre ?: "?"} x${item.cantidad ?: 1}"
                h.infoTv.text = "Estado: ${item.estado ?: "PENDIENTE"}"
                if (item.notas != null) h.infoTv.append(" | ${item.notas}")
                val estado = item.estado ?: "PENDIENTE"
                val color = when (estado) { "COMPRADO" -> android.graphics.Color.parseColor("#4CAF50"); "NO_HUBO" -> android.graphics.Color.parseColor("#F44336"); else -> android.graphics.Color.parseColor("#212121") }
                h.nameTv.setTextColor(color)
                h.itemView.setOnClickListener { mostrarOpcionesItem(item) }
            }
            override fun getItemCount() = items.size
        }
    }

    private fun mostrarOpcionesItem(item: ItemListaResponse) {
        val opciones = arrayOf(
            if (item.estado != "COMPRADO") "Marcar comprado" else "Desmarcar",
            if (item.estado != "NO_HUBO") "No hay" else "Quitar no hay"
        )

        AlertDialog.Builder(this)
            .setTitle(item.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> cambiarEstado(item.id ?: return@setItems, if (item.estado != "COMPRADO") "COMPRADO" else "PENDIENTE")
                    1 -> {
                        val nuevoEstado = if (item.estado != "NO_HUBO") "NO_HUBO" else "PENDIENTE"
                        if (nuevoEstado == "NO_HUBO") {
                            compraApi.marcarNoHay(tokenManager.getAuthHeader(), listaId, MarcarNoHayRequest(item.id ?: return@setItems))
                            cargarDatos()
                        } else {
                            cambiarEstado(item.id!!, "PENDIENTE")
                        }
                    }
                }
            }.show()
    }

    private fun cambiarEstado(itemId: String, estado: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = listaApi.cambiarEstadoItem(tokenManager.getAuthHeader(), itemId, ItemEstadoRequest(estado))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) cargarDatos()
                }
            } catch (_: Exception) {}
        }
    }

    private fun mostrarDialogoAgregarItem() {
        val input = EditText(this)
        input.hint = "Nombre del artículo"
        val qtyInput = EditText(this)
        qtyInput.hint = "Cantidad"
        qtyInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        val ll = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 16, 48, 16)
            addView(input); addView(qtyInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar item")
            .setView(ll)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = input.text.toString().trim()
                val cantidad = qtyInput.text.toString().trim().toDoubleOrNull() ?: 1.0
                if (nombre.isNotBlank()) agregarItem(nombre, cantidad)
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun agregarItem(nombre: String, cantidad: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = listaApi.agregarItem(tokenManager.getAuthHeader(), listaId, ItemListaRequest(nombreManual = nombre, cantidad = cantidad))
                withContext(Dispatchers.Main) { if (response.isSuccessful) { Toast.makeText(this@ListaDetailActivity, R.string.item_added, Toast.LENGTH_SHORT).show(); cargarDatos() } }
            } catch (_: Exception) {}
        }
    }

    private fun completarLista() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = listaApi.cambiarEstadoLista(tokenManager.getAuthHeader(), listaId, EstadoRequest("COMPLETADA"))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ListaDetailActivity, "Lista completada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Modo compra")
        menu.add(0, 2, 0, "Completar")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> { startActivity(Intent(this, ModoCompraActivity::class.java).putExtra("listaId", listaId)); true }
            2 -> { completarLista(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class VH(v: android.view.View, val nameTv: com.google.android.material.textview.MaterialTextView, val infoTv: com.google.android.material.textview.MaterialTextView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
