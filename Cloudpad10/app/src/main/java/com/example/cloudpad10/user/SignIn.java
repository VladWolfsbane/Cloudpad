package com.example.cloudpad10.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cloudpad10.MainActivity;
import com.example.cloudpad10.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignIn extends AppCompatActivity {
    EditText lEmail,lPassword;
    Button loginNow;
    TextView createAcc;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Open Diary");

        lEmail = findViewById(R.id.email);
        lPassword = findViewById(R.id.lPassword);
        loginNow = findViewById(R.id.loginBtn);

        spinner = findViewById(R.id.progressBar3);

        createAcc = findViewById(R.id.createAccount);
        user = FirebaseAuth.getInstance().getCurrentUser();

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        displayWarning();

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail = lEmail.getText().toString();
                String mPassword = lPassword.getText().toString();

                if(mEmail.isEmpty() || mPassword.isEmpty()){
                    Toast.makeText(SignIn.this, "Think you made a mistake there, check again (≖ᴗ≖)", Toast.LENGTH_SHORT).show();
                    return;
                }

                spinner.setVisibility(View.VISIBLE);

                if(fAuth.getCurrentUser().isAnonymous()){
                    FirebaseUser user = fAuth.getCurrentUser();

                    fStore.collection("notes").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(SignIn.this, "Scrapping temporary notes...", Toast.LENGTH_SHORT).show();
                        }
                    });

                    user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // do nothing
                        }
                    });
                }

                fAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(SignIn.this, "Welcome back! (◉‿◉)", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fAuth.signInAnonymously();
                        Toast.makeText(SignIn.this, "Login Failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.GONE);
                    }
                });
            }
        });

        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),CreateAccount.class));
            }
        });
    }

    private void displayWarning() {
        final AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("＼(￣O￣) Hold on there!")
                .setMessage("Opening your diary will scrap all temporary notes! Sure you want to continue?")
                .setPositiveButton("I need them!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(),CreateAccount.class));
                        finish();
                    }
                }).setNegativeButton("I'm sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        warning.show();
    }
}