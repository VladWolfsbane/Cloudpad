package com.example.cloudpad10.diary;

import android.content.Intent;
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

import com.example.cloudpad10.MainActivity;
import com.example.cloudpad10.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPage extends AppCompatActivity {
    Intent data;
    EditText editPageTitle,editPageContent;
    FirebaseFirestore fStore;
    ProgressBar spinner;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = fStore.getInstance();
        spinner = findViewById(R.id.progressBar2);
        user = FirebaseAuth.getInstance().getCurrentUser();

        data = getIntent();

        editPageContent = findViewById(R.id.editPageContent);
        editPageTitle = findViewById(R.id.editPageTitle);

        String noteTitle = data.getStringExtra("title");
        String noteContent = data.getStringExtra("content");

        editPageTitle.setText(noteTitle);
        editPageContent.setText(noteContent);

        FloatingActionButton fab = findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nTitle = editPageTitle.getText().toString();
                String nContent = editPageContent.getText().toString();

                if(nContent.isEmpty()){
                    Toast.makeText(EditPage.this, "Let's write something first, shall we?", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(nTitle.isEmpty()){
                    Toast.makeText(EditPage.this, "Let's add a cool title!", Toast.LENGTH_SHORT).show();
                    return;
                }

                spinner.setVisibility(View.VISIBLE);

                DocumentReference docref = fStore.collection("notes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));

                Map<String,Object> note = new HashMap<>();
                note.put("title",nTitle);
                note.put("content",nContent);
                docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditPage.this, "Updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditPage.this, "Oh no! Something went wrong! Let's try that again.", Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.VISIBLE);
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
            Toast.makeText(this,"Putting everything back how it was...", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}