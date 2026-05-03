package com.superlist.app.ui.historial

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityDetalleHistorialBinding
import kotlinx.coroutines.*

class DetalleHistorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleHistorialBinding
    private val api = RetrofitClient.compraApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        val listaId = intent.getStringExtra("listaId") ?: run { finish(); return }

        supportActionBar?.title = getString(R.string.detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.obtenerDetalleHistorial(tokenManager.getAuthHeader(), listaId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val d = response.body()
                        if (d != null) {
                            supportActionBar?.title = d.listaNombre ?: "Detalle"
                            binding.tvNombre.text = d.listaNombre ?: ""
                            binding.tvSupermercado.text = "Super: ${d.supermercado ?: "-"}"
                            binding.tvCompletadaPor.text = "Completada por: ${d.completadaPor ?: "-"}"
                            binding.tvFecha.text = "Fecha: ${d.fechaCompletada?.take(10) ?: ""}"

                            val items = d.items ?: emptyList()
                            binding.rvItems.layoutManager = LinearLayoutManager(this@DetalleHistorialActivity)
                            binding.rvItems.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<DetalleHistorialActivity.VH>() {
                                override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): VH {
                                    val card = com.google.android.material.card.MaterialCardView(p.context).apply {
                                        layoutParams = android.view.ViewGroup.MarginLayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                                        ).apply { setMargins(0, 4, 0, 4) }
                                        radius = 8f; setContentPadding(16, 12, 16, 12)
                                    }
                                    val ll = android.widget.LinearLayout(p.context).apply { orientation = android.widget.LinearLayout.VERTICAL }
                                    val nameTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 15f }
                                    val qtyTv = com.google.android.material.textview.MaterialTextView(p.context).apply { textSize = 13f; setTextColor(android.graphics.Color.parseColor("#757575")) }
                                    ll.addView(nameTv); ll.addView(qtyTv)
                                    card.addView(ll)
                                    return VH(card, nameTv, qtyTv)
                                }
                                override fun onBindViewHolder(h: VH, pos: Int) {
                                    val i = items[pos]
                                    h.nameTv.text = i.nombre ?: "?"
                                    h.qtyTv.text = "Cant: ${i.cantidad ?: "-"} | Comprado: ${i.cantidadComprada ?: "-"}"
                                    if (i.areaSuperNombre != null) h.qtyTv.append(" | ${i.areaSuperNombre}")
                                }
                                override fun getItemCount() = items.size
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    class VH(v: android.view.View, val nameTv: com.google.android.material.textview.MaterialTextView, val qtyTv: com.google.android.material.textview.MaterialTextView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
