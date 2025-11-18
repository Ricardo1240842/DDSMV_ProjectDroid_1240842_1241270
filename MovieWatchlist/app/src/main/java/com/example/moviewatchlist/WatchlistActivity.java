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
 WatchlistActivity is for the users watchlist
*/
public class WatchlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WatchlistAdapter adapter;
    private List<Movie> watchlist;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        // toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Watchlist");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        recyclerView = findViewById(R.id.watchlistRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        watchlist = new ArrayList<>();
        adapter = new WatchlistAdapter(this, watchlist); // passes activity context + data list
        recyclerView.setAdapter(adapter);

        // Firestore
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        loadWatchlist();
    }

    /*
     update watchlist
    */
    private void loadWatchlist() {
        db.collection("users")
                .document(userId)
                .collection("watchlist")
                .addSnapshotListener((snapshots, e) -> {


                    if (e != null) {
                        Toast.makeText(this, "Error loading watchlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update list
                    if (snapshots != null) {
                        watchlist.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Movie movie = doc.toObject(Movie.class);


                            if (movie != null) {
                                watchlist.add(movie);
                            }
                        }


                        adapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
