package com.superlist.app.ui.familias

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.InvitacionRequest
import com.superlist.app.data.model.MiembroResponse
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityMiembrosBinding
import kotlinx.coroutines.*

class MiembrosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMiembrosBinding
    private val api = RetrofitClient.familiaApi
    private lateinit var tokenManager: TokenManager
    private var familiaId: String = ""
    private var familiaNombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMiembrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)
        familiaId = intent.getStringExtra("familiaId") ?: return
        familiaNombre = intent.getStringExtra("familiaNombre") ?: ""

        supportActionBar?.title = familiaNombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.fabInvite.setOnClickListener { mostrarDialogoInvitar() }

        binding.srlMiembros.setOnRefreshListener { cargarMiembros() }

        cargarMiembros()
    }

    private fun cargarMiembros() {
        binding.srlMiembros.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.listarMiembros(tokenManager.getAuthHeader(), familiaId)
                withContext(Dispatchers.Main) {
                    binding.srlMiembros.isRefreshing = false
                    if (response.isSuccessful) {
                        val miembros = response.body() ?: emptyList()
                        mostrarMiembros(miembros)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.srlMiembros.isRefreshing = false
                }
            }
        }
    }

    private fun mostrarMiembros(miembros: List<MiembroResponse>) {
        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<MiembrosActivity.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
                val tv = com.google.android.material.textview.MaterialTextView(parent.context)
                tv.setPadding(24, 16, 24, 16)
                tv.textSize = 16f
                return ViewHolder(tv)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val m = miembros[position]
                val name = m.nombre ?: m.telefono ?: "Usuario"
                val role = if (m.rol == "ADMIN") " 👑" else ""
                holder.textView.text = "$name$role"
            }

            override fun getItemCount() = miembros.size
        }

        binding.rvMiembros.layoutManager = LinearLayoutManager(this)
        binding.rvMiembros.adapter = adapter
    }

    class ViewHolder(val textView: com.google.android.material.textview.MaterialTextView) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(textView)

    private fun mostrarDialogoInvitar() {
        val input = EditText(this).apply {
            hint = getString(R.string.invite_phone_hint)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        AlertDialog.Builder(this)
            .setTitle("Invitar miembro")
            .setView(input)
            .setPositiveButton(getString(R.string.send_invite)) { _, _ ->
                val phone = input.text.toString().trim()
                if (phone.isNotBlank()) invitar(phone)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun invitar(phone: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.invitarMiembro(
                    tokenManager.getAuthHeader(), familiaId, InvitacionRequest(phone)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MiembrosActivity, R.string.invitation_sent, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MiembrosActivity, "Error al invitar", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MiembrosActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
