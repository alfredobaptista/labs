package ao.uan.fc.dam.exercise_1

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisplayMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_message)

        // Obtém a mensagem enviada pela Intent
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        // Exibe no TextView do layout
        val textView = findViewById<TextView>(R.id.text_display)
        textView.text = message
    }
}