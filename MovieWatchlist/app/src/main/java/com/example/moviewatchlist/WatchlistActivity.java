package com.example.moviewatchlist;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class WatchlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WatchlistAdapter adapter;
    private List<Movie> watchlist = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        recyclerView = findViewById(R.id.watchlistRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new WatchlistAdapter(watchlist);
        recyclerView.setAdapter(adapter);

        loadWatchlist();
    }

    private void loadWatchlist() {
        db.collection("users").document(userId)
                .collection("watchlist")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    watchlist.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Movie movie = doc.toObject(Movie.class);
                        watchlist.add(movie);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load watchlist: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
