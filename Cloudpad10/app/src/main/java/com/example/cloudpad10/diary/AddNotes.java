package com.example.cloudpad10.diary;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.cloudpad10.R;

import java.util.HashMap;
import java.util.Map;

public class AddNotes extends AppCompatActivity {
    FirebaseFirestore fStore;
    EditText noteTitle,noteContent;
    ProgressBar progressBarSave;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        noteContent = findViewById(R.id.addNoteContent);
        noteTitle = findViewById(R.id.addNoteTitle);
        progressBarSave = findViewById(R.id.progressBar);

        user = FirebaseAuth.getInstance().getCurrentUser();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle = noteTitle.getText().toString();
                String nContent = noteContent.getText().toString();

                if(nContent.isEmpty()){
                    Toast.makeText(AddNotes.this, "Let's write something first, shall we?", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(nTitle.isEmpty()){
                    Toast.makeText(AddNotes.this, "Let's add a cool title!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBarSave.setVisibility(View.VISIBLE);

                // save data
                DocumentReference docref = fStore.collection("notes").document(user.getUid()).collection("myNotes").document();
                Map<String,Object> note = new HashMap<>();
                note.put("title",nTitle);
                note.put("content",nContent);
                docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNotes.this, "Saved", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNotes.this, "Oh no! Something went wrong! Let's try that again.", Toast.LENGTH_SHORT).show();
                        progressBarSave.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.close){
            Toast.makeText(this,"Note scrapped!", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}