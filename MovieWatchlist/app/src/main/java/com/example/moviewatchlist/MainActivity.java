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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Movie Watchlist");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        resultsList = findViewById(R.id.resultsList);
        searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);
        Button watchlistButton = findViewById(R.id.watchlistButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button top10Button = findViewById(R.id.top10Button);

        resultsList.setLayoutManager(new LinearLayoutManager(this));
        movieResults = new ArrayList<>();
        adapter = new MovieAdapter(this, movieResults, db, this::updateGlobalRating);
        resultsList.setAdapter(adapter);

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) searchMovies(query);
            else Toast.makeText(this, "Enter a movie title", Toast.LENGTH_SHORT).show();
        });

        watchlistButton.setOnClickListener(v ->
                startActivity(new Intent(this, WatchlistActivity.class)));

        top10Button.setOnClickListener(v ->
                startActivity(new Intent(this, Top10Activity.class)));

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void searchMovies(String title) {
        String apiKey = getString(R.string.tmdb_api_key);
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + title;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "API error", Toast.LENGTH_SHORT).show());
                    return;
                }
                String json = response.body().string();
                runOnUiThread(() -> parseMovies(json));
            }
        });
    }

    private void parseMovies(String json) {
        movieResults.clear();
        try {
            JSONObject root = new JSONObject(json);
            JSONArray results = root.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                Movie movie = new Movie();
                movie.setTitle(obj.getString("title"));
                movie.setTmdbId(obj.getString("id"));
                movie.setPosterUrl(obj.isNull("poster_path") ? null :
                        "https://image.tmdb.org/t/p/w500" + obj.getString("poster_path"));
                movie.setRating(0);
                movieResults.add(movie);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing movie data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Updated rating method: stores full movie object in Firestore
    private void updateGlobalRating(String movieId, double newRating, Movie movie) {
        DocumentReference ref = db.collection("movies").document(movieId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(ref);

            double currentAvg = 0;
            long total = 0;

            if (snapshot.exists()) {
                currentAvg = snapshot.contains("avgRating") && snapshot.getDouble("avgRating") != null
                        ? snapshot.getDouble("avgRating") : 0;
                total = snapshot.contains("totalRatings") && snapshot.getLong("totalRatings") != null
                        ? snapshot.getLong("totalRatings") : 0;
            }

            double newAvg = ((currentAvg * total) + newRating) / (total + 1);
            movie.setAvgRating(newAvg);

            transaction.set(ref, movie, SetOptions.merge());
            return null;
        }).addOnSuccessListener(aVoid ->
                Toast.makeText(this, "Rating updated!", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Error updating rating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
