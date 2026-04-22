package ao.uan.fcn.dam.notes;


import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReadNoteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_note);

        Note note = (Note) getIntent().getSerializableExtra("SELECTED_NOTE");

        if (note != null) {
            ((TextView) findViewById(R.id.txtTitle)).setText(note.getTitle());
            ((TextView) findViewById(R.id.txtText)).setText(note.getText());
        }
    }
}