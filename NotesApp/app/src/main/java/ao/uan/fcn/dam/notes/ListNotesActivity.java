package ao.uan.fcn.dam.notes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListNotesActivity extends AppCompatActivity {

    private ArrayList<Note> notes = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayList<String> titles = new ArrayList<>();

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_notes);

        ListView listView = findViewById(R.id.listNotes);
        Button btnNew = findViewById(R.id.btnNewNote);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);

        btnNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateNoteActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Note note = notes.get(position);

            Intent intent = new Intent(this, ReadNoteActivity.class);
            intent.putExtra("title", note.getTitle());
            intent.putExtra("content", note.getContent());
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");

            Note note = new Note(title, content);
            notes.add(note);
            titles.add(title);

            adapter.notifyDataSetChanged();
        }
    }
}