package ao.uan.fcn.dam.notes;


import java.util.ArrayList;
import java.util.List;

public class NotesRepository {
    private static final List<Note> notes = new ArrayList<>();

    public static List<Note> getNotes() {
        return notes;
    }

    public static void addNote(Note note) {
        notes.add(note);
    }
}
