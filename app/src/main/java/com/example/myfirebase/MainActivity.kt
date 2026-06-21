package com.example.myfirebase

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myfirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: TarefaAdapter
    private var tarefasRef: DatabaseReference? = null
    private var tarefasListener: ValueEventListener? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            showTasksPanel(user.uid)
        } else {
            showLoginPanel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        setupClickListeners()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authStateListener)
        removeTarefasListener()
    }

    private fun setupRecyclerView() {
        adapter = TarefaAdapter(
            onToggleConcluida = { id, concluida ->
                tarefasRef?.child(id)?.child("concluida")?.setValue(concluida)
            },
            onDelete = { id ->
                tarefasRef?.child(id)?.removeValue()
            }
        )
        binding.recyclerTarefas.layoutManager = LinearLayoutManager(this)
        binding.recyclerTarefas.adapter = adapter
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            Toast.makeText(this, "Clicou em Cadastrar", Toast.LENGTH_SHORT).show()

            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.authStatusText.text = "Preencha o email"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.authStatusText.text = "Preencha a senha"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    binding.authStatusText.text = "Senha deve ter pelo menos 6 caracteres"
                    return@setOnClickListener
                }
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.authStatusText.text = "Usuário cadastrado com sucesso!"
                    } else {
                        binding.authStatusText.text = "Erro: ${task.exception?.message}"
                    }
                }
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.authStatusText.text = "Preencha o email"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.authStatusText.text = "Preencha a senha"
                    return@setOnClickListener
                }
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.authStatusText.text = "Login realizado com sucesso!"
                    } else {
                        binding.authStatusText.text = "Erro: ${task.exception?.message}"
                    }
                }
        }

        binding.addTaskButton.setOnClickListener {
            val nome = binding.taskInput.text.toString().trim()
            if (nome.isEmpty()) {
                Toast.makeText(this, "Digite o nome da tarefa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tarefasRef == null) {
                Toast.makeText(this, "Erro: não conectado ao Firebase", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novaRef = tarefasRef!!.push()
            val tarefa = Tarefa(nome = nome, concluida = false)
            novaRef.setValue(tarefa)
                .addOnSuccessListener {
                    binding.taskInput.text?.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLoginPanel() {
        removeTarefasListener()
        binding.loginPanel.visibility = android.view.View.VISIBLE
        binding.tasksPanel.visibility = android.view.View.GONE
    }

    private fun showTasksPanel(uid: String) {
        binding.loginPanel.visibility = android.view.View.GONE
        binding.tasksPanel.visibility = android.view.View.VISIBLE

        tarefasRef = database.getReference("usuarios").child(uid).child("tarefas")
        carregarTarefas()
    }

    private fun carregarTarefas() {
        tarefasListener?.let {
            tarefasRef?.removeEventListener(it)
        }
        tarefasListener = null

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Tarefa>()
                for (child in snapshot.children) {
                    val tarefa = child.getValue(Tarefa::class.java)
                    tarefa?.id = child.key ?: ""
                    if (tarefa != null) {
                        lista.add(tarefa)
                    }
                }
                adapter.updateList(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        tarefasRef?.addValueEventListener(listener)
        tarefasListener = listener
    }

    private fun removeTarefasListener() {
        tarefasListener?.let {
            tarefasRef?.removeEventListener(it)
        }
        tarefasListener = null
        tarefasRef = null
    }
}
