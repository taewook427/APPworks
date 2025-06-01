package com.example.kutil6_metrics;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ManageActivity extends AppCompatActivity {
    private LinearLayout menuLayout, pwLayout;
    private EditText txtTitle, txtContent, pwInput;
    private NoteActivity note;
    private int pageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_main);
        note = new NoteActivity(true, this);

        txtTitle = findViewById(R.id.page_title);
        txtContent = findViewById(R.id.note_content);
        Button prevButton = findViewById(R.id.prev_page_button);
        Button nextButton = findViewById(R.id.next_page_button);
        prevButton.setOnClickListener(v -> goToPreviousPage());
        nextButton.setOnClickListener(v -> goToNextPage());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveText());
        Button newPageButton = findViewById(R.id.new_page_button);
        newPageButton.setOnClickListener(v -> newPage());
        Button delPageButton = findViewById(R.id.del_page_button);
        delPageButton.setOnClickListener(v -> delPage());
        Button resetPWButton = findViewById(R.id.change_pw_button);
        resetPWButton.setOnClickListener(v -> resetPW());

        menuLayout = findViewById(R.id.menu_layout);
        Button menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            if (menuLayout.getVisibility() == View.GONE) {
                menuLayout.setVisibility(View.VISIBLE);
            } else {
                menuLayout.setVisibility(View.GONE);
            }
        });

        pwLayout = findViewById(R.id.pw_layout);
        pwInput = findViewById(R.id.pw_input);
        Button pwButton = findViewById(R.id.pw_button);
        pwButton.setOnClickListener(v -> resetWork());
        updatePageContent();
    }

    private void updatePageContent() { // page update
        if (note.titles.length > pageIndex) {
            txtTitle.setText(note.titles[pageIndex]);
            txtContent.setText(note.contents[pageIndex]);
        }
    }

    private void goToPreviousPage() { // prev button
        if (pageIndex > 0) {
            pageIndex--;
            updatePageContent();
        }
    }

    private void goToNextPage() { // next button
        if (pageIndex < note.titles.length - 1) {
            pageIndex++;
            updatePageContent();
        }
    }

    private void saveText() { // save button
        note.titles[pageIndex] = txtTitle.getText().toString();
        note.contents[pageIndex] = txtContent.getText().toString();
        note.saveNote();
    }

    private void newPage() { // new page button
        String[] newTitles = new String[note.titles.length + 1];
        String[] newContents = new String[note.contents.length + 1];
        pageIndex++;
        for (int i = 0; i < pageIndex; i++) {
            newTitles[i] = note.titles[i];
            newContents[i] = note.contents[i];
        }
        newTitles[pageIndex] = "New page";
        newContents[pageIndex] = "Empty note";
        for (int i = pageIndex; i < note.titles.length; i++) {
            newTitles[i + 1] = note.titles[i];
            newContents[i + 1] = note.contents[i];
        }
        note.titles = newTitles;
        note.contents = newContents;
        updatePageContent();
        note.saveNote();
    }

    private void delPage() { // del page button
        if (note.titles.length != 1) {
            new AlertDialog.Builder(this).setTitle("Page Delete").setMessage("Delete current page?")
                    .setPositiveButton("Confirm", (dialog, which) -> delWork())
                    .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show())
                    .show();
        }
    }
    private void delWork() {
        String[] newTitles = new String[note.titles.length - 1];
        String[] newContents = new String[note.contents.length - 1];
        for (int i = 0; i < pageIndex; i++) {
            newTitles[i] = note.titles[i];
            newContents[i] = note.contents[i];
        }
        for (int i = pageIndex; i < note.titles.length - 1; i++) {
            newTitles[i] = note.titles[i + 1];
            newContents[i] = note.contents[i + 1];
        }
        note.titles = newTitles;
        note.contents = newContents;
        pageIndex--;
        updatePageContent();
        note.saveNote();
    }

    private void resetPW() { // reset pw button
        if (pwLayout.getVisibility() == View.GONE) {
            pwLayout.setVisibility(View.VISIBLE);
        } else {
            pwLayout.setVisibility(View.GONE);
        }
    }
    private void resetWork() {
        note.password = pwInput.getText().toString();
        note.saveNote();
        pwLayout.setVisibility(View.GONE);
    }
}