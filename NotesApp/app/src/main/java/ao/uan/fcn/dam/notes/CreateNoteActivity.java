package ao.uan.fcn.dam.notes;



import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText edtTitle, edtText;
    private Button btnOk, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        edtTitle = findViewById(R.id.edtTitle);
        edtText = findViewById(R.id.edtText);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(v -> {
            String title = edtTitle.getText().toString();
            String text = edtText.getText().toString();
            if (!title.isEmpty() && !text.isEmpty()) {
                NotesRepository.addNote(new Note(title, text));
            }
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
