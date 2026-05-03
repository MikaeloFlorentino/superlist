package com.superlist.app.ui.familias

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.InvitacionResponse
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityInvitacionesBinding
import kotlinx.coroutines.*

class InvitacionesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvitacionesBinding
    private val api = RetrofitClient.familiaApi
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvitacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.pending_invitations)

        cargarInvitaciones()
    }

    private fun cargarInvitaciones() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.listarInvitaciones(tokenManager.getAuthHeader())
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val invitaciones = response.body()?.filter { it.estado == "PENDIENTE" } ?: emptyList()
                        binding.rvInvitaciones.layoutManager = LinearLayoutManager(this@InvitacionesActivity)
                        binding.rvInvitaciones.adapter = InvitacionAdapter(invitaciones) { id, aceptar ->
                            if (aceptar) aceptarInvitacion(id) else rechazarInvitacion(id)
                        }
                        binding.tvEmpty.visibility =
                            if (invitaciones.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InvitacionesActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun aceptarInvitacion(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.aceptarInvitacion(tokenManager.getAuthHeader(), id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@InvitacionesActivity, "Invitación aceptada", Toast.LENGTH_SHORT).show()
                        cargarInvitaciones()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InvitacionesActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun rechazarInvitacion(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.rechazarInvitacion(tokenManager.getAuthHeader(), id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@InvitacionesActivity, "Invitación rechazada", Toast.LENGTH_SHORT).show()
                        cargarInvitaciones()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InvitacionesActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class InvitacionAdapter(
    private val invitaciones: List<InvitacionResponse>,
    private val onAction: (String, Boolean) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<InvitacionAdapter.ViewHolder>() {

    class ViewHolder(val card: com.google.android.material.card.MaterialCardView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val ctx = parent.context
        val card = com.google.android.material.card.MaterialCardView(ctx).apply {
            layoutParams = android.view.ViewGroup.MarginLayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 8, 16, 8) }
            radius = 12f
            cardElevation = 2f
            setContentPadding(24, 16, 24, 16)
        }

        val linearLayout = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
        }

        val nameTv = com.google.android.material.textview.MaterialTextView(ctx).apply {
            textSize = 16f
        }
        linearLayout.addView(nameTv)

        val btnLayout = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }

        val acceptBtn = com.google.android.material.button.MaterialButton(ctx).apply {
            text = "Aceptar"
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        btnLayout.addView(acceptBtn)

        val rejectBtn = com.google.android.material.button.MaterialButton(ctx).apply {
            text = "Rechazar"
            isEnabled = false
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        btnLayout.addView(rejectBtn)

        linearLayout.addView(btnLayout)
        card.addView(linearLayout)

        return ViewHolder(card)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val inv = invitaciones[position]
        // Could add more details but for MVP just show info
    }

    override fun getItemCount() = invitaciones.size
}
