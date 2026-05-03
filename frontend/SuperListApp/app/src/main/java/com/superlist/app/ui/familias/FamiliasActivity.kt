package com.superlist.app.ui.familias

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.superlist.app.R
import com.superlist.app.data.api.RetrofitClient
import com.superlist.app.data.model.FamiliaResponse
import com.superlist.app.data.model.InvitacionResponse
import com.superlist.app.data.preferences.TokenManager
import com.superlist.app.databinding.ActivityFamiliasBinding
import com.superlist.app.ui.auth.LoginActivity
import com.superlist.app.ui.catalogo.CatalogoActivity
import com.superlist.app.ui.compras.PendientesActivity
import com.superlist.app.ui.historial.HistorialActivity
import com.superlist.app.ui.listas.ListasActivity
import com.superlist.app.ui.common.adapters.SimpleAdapter
import kotlinx.coroutines.*

class FamiliasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFamiliasBinding
    private val familiaApi = RetrofitClient.familiaApi
    private lateinit var tokenManager: TokenManager
    private var familias = listOf<FamiliaResponse>()
    private var invitaciones = listOf<InvitacionResponse>()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFamiliasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.my_families)

        binding.btnCreateFamily.setOnClickListener {
            startActivity(Intent(this, CrearFamiliaActivity::class.java))
        }

        binding.btnJoinFamily.setOnClickListener {
            startActivity(Intent(this, UnirseFamiliaActivity::class.java))
        }

        binding.srlFamilias.setOnRefreshListener { cargarDatos() }

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        binding.srlFamilias.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val famResponse = familiaApi.listarFamilias(tokenManager.getAuthHeader())
                val invResponse = familiaApi.listarInvitaciones(tokenManager.getAuthHeader())

                withContext(Dispatchers.Main) {
                    binding.srlFamilias.isRefreshing = false

                    if (famResponse.isSuccessful) {
                        familias = famResponse.body() ?: emptyList()
                        actualizarFamilias()
                    }

                    if (invResponse.isSuccessful) {
                        invitaciones = invResponse.body()?.filter {
                            it.estado == "PENDIENTE"
                        } ?: emptyList()
                        if (invitaciones.isNotEmpty()) {
                            binding.tvInvitacionesTitle.visibility = android.view.View.VISIBLE
                            binding.rvInvitaciones.visibility = android.view.View.VISIBLE
                            actualizarInvitaciones()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.srlFamilias.isRefreshing = false
                    if (familias.isEmpty()) {
                        binding.tvEmpty.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
    }

    private fun actualizarFamilias() {
        if (familias.isEmpty()) {
            binding.rvFamilias.visibility = android.view.View.GONE
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            return
        }
        binding.rvFamilias.visibility = android.view.View.VISIBLE
        binding.tvEmpty.visibility = android.view.View.GONE

        val adapter = SimpleAdapter(
            items = familias.map { it.nombre },
            onItemClick = { position ->
                val familia = familias[position]
                tokenManager.familiaActualId = familia.id
                tokenManager.familiaActualNombre = familia.nombre
                mostrarMenuFamilia(familia)
            }
        )
        binding.rvFamilias.layoutManager = LinearLayoutManager(this)
        binding.rvFamilias.adapter = adapter
    }

    private fun actualizarInvitaciones() {
        val adapter = SimpleAdapter(
            items = invitaciones.map {
                "Invitación de: ${it.familiaNombre ?: it.familiaId}"
            },
            onItemClick = { position ->
                val inv = invitaciones[position]
                inv.id?.let { aceptarInvitacion(it) }
            }
        )
        binding.rvInvitaciones.layoutManager = LinearLayoutManager(this)
        binding.rvInvitaciones.adapter = adapter
    }

    private fun aceptarInvitacion(invitacionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = familiaApi.aceptarInvitacion(tokenManager.getAuthHeader(), invitacionId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FamiliasActivity, "Invitación aceptada", Toast.LENGTH_SHORT).show()
                        cargarDatos()
                    } else {
                        Toast.makeText(this@FamiliasActivity, "Error al aceptar", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FamiliasActivity, R.string.network_error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarMenuFamilia(familia: FamiliaResponse) {
        val options = arrayOf(
            "Listas de compras",
            "Catálogo",
            "Miembros",
            "Pendientes",
            "Historial"
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(familia.nombre)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, ListasActivity::class.java))
                    1 -> startActivity(Intent(this, CatalogoActivity::class.java))
                    2 -> {
                        val intent = Intent(this, MiembrosActivity::class.java)
                        intent.putExtra("familiaId", familia.id)
                        intent.putExtra("familiaNombre", familia.nombre)
                        startActivity(intent)
                    }
                    3 -> startActivity(Intent(this, PendientesActivity::class.java))
                    4 -> startActivity(Intent(this, HistorialActivity::class.java))
                }
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Cerrar sesión")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            tokenManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
