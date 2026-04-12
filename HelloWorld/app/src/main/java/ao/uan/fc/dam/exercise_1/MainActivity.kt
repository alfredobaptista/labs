package ao.uan.fc.dam.exercise_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

// Constante para a chave da mensagem (equivalente ao static final em Java)
const val EXTRA_MESSAGE = "ao.uan.fc.dam.MESSAGE"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSend = findViewById<Button>(R.id.BtnSend)
        val editText = findViewById<EditText>(R.id.editTextMessage)

        btnSend.setOnClickListener {
            val message = editText.text.toString()

            // Criar a Intent para a DisplayMessageActivity
            val intent = Intent(this, DisplayMessageActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, message)
            }
            startActivity(intent)
        }
    }

    /** Chamado quando o usuário clica no botão Send */
    fun sendMessage(view: View) {
        val editText = findViewById<EditText>(R.id.editTextMessage)
        val message = editText.text.toString()

        // Intent para iniciar a segunda Activity
        val intent = Intent(this, DisplayMessageActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }
}