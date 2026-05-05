package ao.uan.fcn.dam

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class TodoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        val editTextTodo = findViewById<EditText>(R.id.editTextTodo)
        val listViewTodo = findViewById<ListView>(R.id.listViewTodo)

        val todoItems = ArrayList<String>()
        val aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, todoItems)
        listViewTodo.adapter = aa

        editTextTodo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val task = editTextTodo.text.toString()
                if (task.isNotBlank()) {
                    todoItems.add(0, task) // Adiciona no topo
                    aa.notifyDataSetChanged()
                    editTextTodo.setText("")
                }
                true
            } else {
                false
            }
        }
    }
}