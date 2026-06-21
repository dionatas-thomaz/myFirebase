package com.example.myfirebase

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TarefaAdapter(
    private val onToggleConcluida: (String, Boolean) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<TarefaAdapter.ViewHolder>() {

    private val tarefas = mutableListOf<Tarefa>()

    fun updateList(novaLista: List<Tarefa>) {
        tarefas.clear()
        tarefas.addAll(novaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarefa, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tarefas[position])
    }

    override fun getItemCount() = tarefas.size

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val checkConcluida: CheckBox = itemView.findViewById(R.id.checkConcluida)
        private val textNome: TextView = itemView.findViewById(R.id.textNome)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(tarefa: Tarefa) {
            textNome.text = tarefa.nome

            checkConcluida.setOnCheckedChangeListener(null)
            checkConcluida.isChecked = tarefa.concluida
            checkConcluida.setOnCheckedChangeListener { _, isChecked ->
                onToggleConcluida(tarefa.id, isChecked)
            }

            if (tarefa.concluida) {
                textNome.paintFlags = textNome.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textNome.alpha = 0.6f
            } else {
                textNome.paintFlags = textNome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                textNome.alpha = 1.0f
            }

            deleteButton.setOnClickListener {
                onDelete(tarefa.id)
            }
        }
    }
}
