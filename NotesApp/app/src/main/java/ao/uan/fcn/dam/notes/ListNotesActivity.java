package ao.uan.fcn.dam.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class ListNotesActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_notes);

        listView = findViewById(R.id.listNotes);
        btnNewNote = findViewById(R.id.btnNewNote);

        updateList();

        btnNewNote.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateNoteActivity.class);
            startActivity(intent);
        });

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Intent intent = new Intent(this, ReadNoteActivity.class);
            intent.putExtra("noteIndex", position);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                NotesRepository.getNotes().stream().map(Note::getTitle).toArray(String[]::new)
        );
        listView.setAdapter(adapter);
    }
}

