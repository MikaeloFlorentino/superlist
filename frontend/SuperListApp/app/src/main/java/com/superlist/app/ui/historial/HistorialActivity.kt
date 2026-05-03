package com.superlist.app.ui.historial

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.ListaResponse
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityHistorialBinding
import kotlinx.coroutines.*

class HistorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialBinding
    private val api = RetrofitClient.compraApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.title = getString(R.string.history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.srlHistorial.setOnRefreshListener { cargarHistorial() }

        cargarHistorial()
    }

    private fun cargarHistorial() {
        val familiaId = tokenManager.familiaActualId ?: return
        binding.srlHistorial.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.obtenerHistorial(tokenManager.getAuthHeader(), familiaId)
                withContext(Dispatchers.Main) {
                    binding.srlHistorial.isRefreshing = false
                    if (response.isSuccessful) {
                        val data = response.body()
                        val listas = data?.completadas ?: emptyList()
                        mostrarHistorial(listas)
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { binding.srlHistorial.isRefreshing = false }
            }
        }
    }

    private fun mostrarHistorial(listas: List<ListaResponse>) {
        binding.rvHistorial.visibility = if (listas.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        binding.tvEmpty.visibility = if (listas.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        binding.rvHistorial.layoutManager = LinearLayoutManager(this)
        binding.rvHistorial.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<HistorialActivity.VH>() {
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
                val l = listas[pos]
                h.nameTv.text = l.nombre ?: ""
                h.infoTv.text = "${l.supermercado ?: "Sin super"} | ${l.itemsCompletados ?: 0}/${l.itemsCount ?: 0} items"
                h.itemView.setOnClickListener {
                    startActivity(Intent(this@HistorialActivity, DetalleHistorialActivity::class.java).putExtra("listaId", l.id))
                }
            }
            override fun getItemCount() = listas.size
        }
    }

    class VH(v: android.view.View, val nameTv: com.google.android.material.textview.MaterialTextView, val infoTv: com.google.android.material.textview.MaterialTextView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
