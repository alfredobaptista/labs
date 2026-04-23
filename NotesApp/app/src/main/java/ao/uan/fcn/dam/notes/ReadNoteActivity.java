package ao.uan.fcn.dam.notes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ReadNoteActivity extends AppCompatActivity {

    private TextView txtTitle, txtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_note);

        txtTitle = findViewById(R.id.txtTitle);
        txtText = findViewById(R.id.txtText);

        int index = getIntent().getIntExtra("noteIndex", -1);
        if (index >= 0) {
            Note note = NotesRepository.getNotes().get(index);
            txtTitle.setText(note.getTitle());
            txtText.setText(note.getText());
        }
    }
}
