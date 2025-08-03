package com.example.kutil6_unitconv;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {
    private EditText txtTitle, txtContent;
    private ImageButton saveButton;
    public int pos;
    public NoteIO note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        pos = getIntent().getIntExtra("position", -1);
        note = new NoteIO(true, this);
        setResult(Activity.RESULT_CANCELED);

        txtTitle = findViewById(R.id.page_title);
        txtContent = findViewById(R.id.note_content);
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> onSaveClick());

        if (0 <= pos && pos < note.titles.length) {
            txtTitle.setText(note.titles[pos]);
            txtContent.setText(note.contents[pos]);
        } else {
            note.showMsg("invalid note position");
        }
    }

    public void onSaveClick() {
        note.titles[pos] = txtTitle.getText().toString();
        note.contents[pos] = txtContent.getText().toString();
        note.saveNote();
        setResult(Activity.RESULT_OK);
    }
}