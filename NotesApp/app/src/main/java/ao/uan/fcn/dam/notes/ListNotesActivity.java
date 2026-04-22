package ao.uan.fcn.dam.notes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ListNotesActivity extends AppCompatActivity {
    private ArrayList<Note> notesList = new ArrayList<>();
    private ArrayList<String> titlesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    // Callback para receber a nota da CreateNoteActivity
    private final ActivityResultLauncher<Intent> createNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Note newNote = (Note) result.getData().getSerializableExtra("NEW_NOTE");
                    if (newNote != null) {
                        notesList.add(newNote);
                        titlesList.add(newNote.getTitle());
                        adapter.notifyDataSetChanged();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_notes);

        ListView listView = findViewById(R.id.listViewNotes);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titlesList);
        listView.setAdapter(adapter);

        // Botão "New Note" conforme o wireframe
        Button btnNewNote = findViewById(R.id.btnNewNote);
        btnNewNote.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateNoteActivity.class);
            createNoteLauncher.launch(intent);
        });

        // Clique num item da lista para ler (ReadNoteActivity)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Note selected = notesList.get(position);
            Intent intent = new Intent(this, ReadNoteActivity.class);
            intent.putExtra("SELECTED_NOTE", (Parcelable) selected);
            startActivity(intent);
        });
    }
}