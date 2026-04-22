package ao.uan.fcn.dam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent

import android.widget.Button
import android.widget.EditText


const val EXTRA_MESSAGE = "ao.uan.fc.dam.MESSAGE"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSend = findViewById<Button>(R.id.BtnSend)
        val btnTodo = findViewById<Button>(R.id.btnGoToTodo)
        val editText = findViewById<EditText>(R.id.editTextMessage)

        btnSend.setOnClickListener {
            val intent = Intent(this, DisplayMessageActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, editText.text.toString())
            }
            startActivity(intent)
        }

        // Abrir Lista de Tarefas (Ex III)
        btnTodo.setOnClickListener {
            startActivity(Intent(this, TodoActivity::class.java))
        }
    }
}