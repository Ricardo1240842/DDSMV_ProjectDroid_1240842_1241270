package com.example.moviewatchlist;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

/*
 WatchlistActivity displays all movies that the current user has saved
 inside their personal watchlist stored in Firestore.

 Features:
   - Real-time listener: updates automatically when movies are added/removed
   - Uses WatchlistAdapter to show posters, titles, and personal ratings
   - User can edit ratings or remove movies directly from the list
*/
public class WatchlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WatchlistAdapter adapter;
    private List<Movie> watchlist;    // list containing user-saved movies
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        // Sets up toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Watchlist");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // RecyclerView and adapter initialization
        recyclerView = findViewById(R.id.watchlistRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        watchlist = new ArrayList<>();
        adapter = new WatchlistAdapter(this, watchlist); // passes activity context + data list
        recyclerView.setAdapter(adapter);

        // Firestore initialization
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Start loading the user's saved movies
        loadWatchlist();
    }

    /*
     loadWatchlist() sets a real-time Firestore listener on:
         users/{userId}/watchlist

     Anytime a movie is added, updated, or deleted, the RecyclerView updates instantly.
    */
    private void loadWatchlist() {
        db.collection("users")
                .document(userId)
                .collection("watchlist")
                .addSnapshotListener((snapshots, e) -> {

                    // Handle Firestore errors
                    if (e != null) {
                        Toast.makeText(this, "Error loading watchlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update list when data arrives or changes
                    if (snapshots != null) {
                        watchlist.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Movie movie = doc.toObject(Movie.class);

                            // Avoid null objects in the list
                            if (movie != null) {
                                watchlist.add(movie);
                            }
                        }

                        // Refresh UI
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /*
     Handles the back arrow in the toolbar.
    */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
