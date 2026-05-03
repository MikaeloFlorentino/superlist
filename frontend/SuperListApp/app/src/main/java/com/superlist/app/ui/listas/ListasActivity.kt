package com.superlist.app.ui.listas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.ListaResponse
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityListasBinding
import kotlinx.coroutines.*

class ListasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListasBinding
    private val api = RetrofitClient.listaApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.title = getString(R.string.shopping_lists)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.fabCreate.setOnClickListener {
            startActivity(Intent(this, CrearListaActivity::class.java))
        }

        binding.srlListas.setOnRefreshListener { cargarListas() }

        cargarListas()
    }

    override fun onResume() {
        super.onResume()
        cargarListas()
    }

    private fun cargarListas() {
        val familiaId = tokenManager.familiaActualId ?: return
        binding.srlListas.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.listarListas(tokenManager.getAuthHeader(), familiaId)
                withContext(Dispatchers.Main) {
                    binding.srlListas.isRefreshing = false
                    if (response.isSuccessful) {
                        val listas = response.body() ?: emptyList()
                        mostrarListas(listas)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { binding.srlListas.isRefreshing = false }
            }
        }
    }

    private fun mostrarListas(listas: List<ListaResponse>) {
        binding.rvListas.visibility = if (listas.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        binding.tvEmpty.visibility = if (listas.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        binding.rvListas.layoutManager = LinearLayoutManager(this)
        binding.rvListas.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ListasActivity.VH>() {
            override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): VH {
                val card = com.google.android.material.card.MaterialCardView(p.context).apply {
                    layoutParams = android.view.ViewGroup.MarginLayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(16, 8, 16, 8) }
                    radius = 12f; cardElevation = 2f; setContentPadding(24, 16, 24, 16)
                }
                val ll = android.widget.LinearLayout(p.context).apply { orientation = android.widget.LinearLayout.VERTICAL }
                val nameTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 16f }
                val statusTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 14f }
                ll.addView(nameTv); ll.addView(statusTv)
                card.addView(ll)
                return VH(card, nameTv, statusTv)
            }
            override fun onBindViewHolder(h: VH, pos: Int) {
                val l = listas[pos]
                h.nameTv.text = l.nombre ?: ""
                val estado = l.estado ?: ""
                val count = "${l.itemsCompletados ?: 0}/${l.itemsCount ?: 0}"
                h.statusTv.text = "$estado ($count)"
                h.statusTv.setTextColor(if (estado == "COMPLETADA") android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.parseColor("#FF9800"))
                h.itemView.setOnClickListener {
                    val intent = Intent(this@ListasActivity, ListaDetailActivity::class.java)
                    intent.putExtra("listaId", l.id)
                    intent.putExtra("listaNombre", l.nombre)
                    startActivity(intent)
                }
            }
            override fun getItemCount() = listas.size
        }
    }

    class VH(v: android.view.View, val nameTv: com.google.android.material.textview.MaterialTextView, val statusTv: com.google.android.material.textview.MaterialTextView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
