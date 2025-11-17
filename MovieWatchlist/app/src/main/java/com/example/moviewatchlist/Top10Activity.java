package com.example.moviewatchlist;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

/*
 Top10Activity displays the global top 10 movies in the Firestore "movies" collection.
 Movies are ordered by avgRating (descending), so the highest-rated movies appear first.
*/
public class Top10Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Top10Adapter adapter;
    private List<Movie> topMovies = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10);

        /*

          SETUP TOOLBAR
          Adds a title and back arrow in the action bar

        */
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Top 10 Movies");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /*

          SETUP RECYCLER VIEW

        */
        recyclerView = findViewById(R.id.top10Recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Attach adapter with empty list initially
        adapter = new Top10Adapter(topMovies);
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load top 10 movies from Firestore
        loadTopMovies();
    }

    /*

      Load top 10 movies from Firestore
      Sorts by "avgRating" descending and limits to 10

    */
    private void loadTopMovies() {
        db.collection("movies")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    topMovies.clear(); // reset list before adding new results

                    // Convert Firestore docs into Movie objects
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Movie movie = doc.toObject(Movie.class);
                        topMovies.add(movie);
                    }

                    // Refresh RecyclerView
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load top 10: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /*
     Enables the back arrow to return to the previous screen
    */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
