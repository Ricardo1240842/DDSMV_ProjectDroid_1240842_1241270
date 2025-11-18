package com.example.moviewatchlist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView resultsList;
    private EditText searchField;
    private MovieAdapter adapter;
    private List<Movie> movieResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Movie Watchlist");

        // Firebase initialization
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // UI references
        resultsList = findViewById(R.id.resultsList);
        searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);
        Button watchlistButton = findViewById(R.id.watchlistButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button top10Button = findViewById(R.id.top10Button);

        // RecyclerView setup
        resultsList.setLayoutManager(new LinearLayoutManager(this));
        movieResults = new ArrayList<>();

        /*
         Adapter receives:
         - context
         - movie list
         - Firestore reference
         - callback to update the global rating in the "movies" collection
        */
        adapter = new MovieAdapter(this, movieResults, db, this::updateGlobalRating);
        resultsList.setAdapter(adapter);

        // TMDB API request
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchMovies(query);
            } else {
                Toast.makeText(this, "Enter a movie title", Toast.LENGTH_SHORT).show();
            }
        });

        // Opens user watchlist screen
        watchlistButton.setOnClickListener(v ->
                startActivity(new Intent(this, WatchlistActivity.class)));

        // Opens Top 10 global rated movies screen
        top10Button.setOnClickListener(v ->
                startActivity(new Intent(this, Top10Activity.class)));

        // Logout and go to LoginActivity
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /*
     Executes a query to TMDB API using OkHttp.
     Converts the movie title into a search request.
    */
    private void searchMovies(String title) {
        String apiKey = getString(R.string.tmdb_api_key);

        // TMDB search
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + title;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        // Make the HTTP request
        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "API error", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Convert API to string
                String json = response.body().string();

                // Process the results
                runOnUiThread(() -> parseMovies(json));
            }
        });
    }

    /*
     Parses the JSON returned by TMDB.
     Creates Movie objects based on the API's movie list.
    */
    private void parseMovies(String json) {
        movieResults.clear();

        try {
            JSONObject root = new JSONObject(json);
            JSONArray results = root.getJSONArray("results");

            // Loop through TMDB results
            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);

                Movie movie = new Movie();
                movie.setTitle(obj.getString("title"));
                movie.setTmdbId(obj.getString("id"));

                // Builds the full poster URL or sets null if missing
                movie.setPosterUrl(obj.isNull("poster_path") ? null :
                        "https://image.tmdb.org/t/p/w500" + obj.getString("poster_path"));

                // Default user rating is 0 until user selects one
                movie.setRating(0);

                movieResults.add(movie);
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "Error parsing movie data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /*
     Updates global rating inside the "movies" collection.
     Global rating is shared by all users and stored per movie document.

     The method:
     - Reads the current avgRating and totalRatings
     - Recalculates the average based on the new rating
     - Updates the document by merging the fields
    */
    private void updateGlobalRating(String movieId, double newRating, Movie movie) {

        DocumentReference ref = db.collection("movies").document(movieId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {

                    DocumentSnapshot snapshot = transaction.get(ref);

                    double currentAvg = 0;
                    long total = 0;

                    // Extracts existing values
                    if (snapshot.exists()) {
                        currentAvg = snapshot.contains("avgRating") && snapshot.getDouble("avgRating") != null
                                ? snapshot.getDouble("avgRating")
                                : 0;

                        total = snapshot.contains("totalRatings") && snapshot.getLong("totalRatings") != null
                                ? snapshot.getLong("totalRatings")
                                : 0;
                    }

                    // Recalculate the average
                    double newAvg = ((currentAvg * total) + newRating) / (total + 1);

                    // Store new average inside the movie object
                    movie.setAvgRating(newAvg);

                    // Merge keeps all other fields intact
                    transaction.set(ref, movie, SetOptions.merge());

                    return null;
                })

                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Rating updated!", Toast.LENGTH_SHORT).show())

                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error updating rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
