package com.example.moviewatchlist;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class Top10Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Top10Adapter adapter;
    private List<Movie> topMovies = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10);

        // setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Top 10 Movies");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.top10Recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Top10Adapter(topMovies);
        recyclerView.setAdapter(adapter);


        db = FirebaseFirestore.getInstance();
        loadTopMovies();
    }

    private void loadTopMovies() {
        db.collection("movies")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    topMovies.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Movie movie = doc.toObject(Movie.class);
                        topMovies.add(movie);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load top 10: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
