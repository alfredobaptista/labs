package ao.uan.fcn.dam.notes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class CreateNoteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        EditText editTitle = findViewById(R.id.editNoteTitle);
        EditText editText = findViewById(R.id.editNoteText);
        Button btnOk = findViewById(R.id.btnOk);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(v -> {
            Note note = new Note(editTitle.getText().toString(), editText.getText().toString());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("NEW_NOTE", (Parcelable) note);
            setResult(Activity.RESULT_OK, resultIntent);
            finish(); // Encerra a atividade
        });

        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish(); // Descarta e encerra
        });
    }
}