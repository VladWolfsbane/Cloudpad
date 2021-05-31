package com.example.cloudpad10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cloudpad10.diary.AddNotes;
import com.example.cloudpad10.diary.EditPage;
import com.example.cloudpad10.diary.NoteDetails;
import com.example.cloudpad10.diary.Page;
import com.example.cloudpad10.user.CreateAccount;
import com.example.cloudpad10.user.SignIn;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    RecyclerView recyclerView;
    String docId;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Page,PageViewHolder> pageAdapter;
    FirebaseUser user;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        recyclerView = findViewById(R.id.notelist);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        Query query = fStore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("title", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Page> allPages = new FirestoreRecyclerOptions.Builder<Page>().setQuery(query,Page.class).build();

        pageAdapter = new FirestoreRecyclerAdapter<Page, PageViewHolder>(allPages) {
            @Override
            protected void onBindViewHolder(@NonNull MainActivity.PageViewHolder pageViewHolder, int i, Page page) {
                pageViewHolder.noteTitle.setText(page.getTitle());
                pageViewHolder.noteContent.setText(page.getContent());
                docId = pageAdapter.getSnapshots().getSnapshot(i).getId();

                pageViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), NoteDetails.class);
                        intent.putExtra("title",page.getTitle());
                        intent.putExtra("content",page.getContent());
                        intent.putExtra("noteId",docId);
                        v.getContext().startActivity(intent);
                    }
                });

                ImageView menuIcon = pageViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String snap = pageAdapter.getSnapshots().getSnapshot(i).getId();
                        PopupMenu menu = new PopupMenu(view.getContext(),view);
                        menu.setGravity(Gravity.END);
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent intent = new Intent(view.getContext(), EditPage.class);
                                intent.putExtra("title",page.getTitle());
                                intent.putExtra("content",page.getContent());
                                intent.putExtra("noteId",snap);
                                startActivity(intent);
                                return false;
                            }
                        });

                        menu.getMenu().add("Scrap").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Toast.makeText(MainActivity.this, "Delete", Toast.LENGTH_SHORT).show();
                                DocumentReference docRef = fStore.collection("notes").document(user.getUid()).collection("myNotes").document(snap);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // deleted
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Oh no! Something went wrong! Let's try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        menu.show();
                    }
                });
            }

            @NonNull
            @Override
            public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);
                return new PageViewHolder(view);
            }
        };

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(pageAdapter);

        View headerView = navigationView.getHeaderView(0);
        TextView username = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);

        userEmail.setText(user.getEmail());
        username.setText(user.getDisplayName());

        if(user.isAnonymous()){
            userEmail.setVisibility(View.GONE);
            username.setText("Mysterious Stranger");
        }else {
            userEmail.setText(user.getEmail());
            username.setText(user.getDisplayName());
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), AddNotes.class));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notes:
                startActivity(new Intent(this,About.class));
                break;

            case R.id.addNote:
                startActivity(new Intent(this,AddNotes.class));
                break;

            case R.id.sync:
                if(user.isAnonymous()){
                    startActivity(new Intent(this, SignIn.class));
                }else {
                    Toast.makeText(this, "Looks like all your notes are already synced (ノ^o^)ノ", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.logout:
                checkUserData();
                break;

            default:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void checkUserData() {
        if(user.isAnonymous()){
            displayAlert();
        }else {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),SplashScreen.class));
            finish();
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("You're anonymous right now!")
                .setMessage("This will get your diary scrapped! Are you sure you want to sign out?")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), CreateAccount.class));
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getApplicationContext(),SplashScreen.class));
                                finish();
                            }
                        });
                    }
                });

        warning.show();
    }

    public class PageViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle,noteContent;
        View view;
        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            view = itemView;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        pageAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pageAdapter != null) {
            pageAdapter.stopListening();
        }
    }

}