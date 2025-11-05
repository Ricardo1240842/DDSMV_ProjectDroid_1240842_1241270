package com.example.moviewatchlist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();
    private EditText searchField;
    private Button searchButton, logoutButton, watchlistButton;
    private ListView resultsList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> movieTitles = new ArrayList<>();
    private String apiKey;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Map<String, String> moviePosterUrls = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Movie Watchlist");

        apiKey = getString(R.string.tmdb_api_key);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        searchField = findViewById(R.id.searchField);
        searchButton = findViewById(R.id.searchButton);
        resultsList = findViewById(R.id.resultsList);
        logoutButton = findViewById(R.id.logoutButton);
        watchlistButton = findViewById(R.id.watchlistButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, movieTitles);
        resultsList.setAdapter(adapter);

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchMovies(query);
            }
        });

        resultsList.setOnItemClickListener((parent, view, position, id) -> {
            String title = movieTitles.get(position);
            String posterUrl = moviePosterUrls.get(title);
            addToWatchlist(title, posterUrl);
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        watchlistButton.setOnClickListener(v -> startActivity(new Intent(this, WatchlistActivity.class)));
    }

    private void searchMovies(String query) {
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + query;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e("TMDB", "API request failed", e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseMovieResults(json));
                }
            }
        });
    }

    private void parseMovieResults(String json) {
        movieTitles.clear();
        moviePosterUrls.clear();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");

            for (JsonElement el : results) {
                JsonObject movie = el.getAsJsonObject();
                String title = movie.get("title").getAsString();
                String posterPath = movie.has("poster_path") && !movie.get("poster_path").isJsonNull()
                        ? "https://image.tmdb.org/t/p/w500" + movie.get("poster_path").getAsString()
                        : "";

                movieTitles.add(title);
                moviePosterUrls.put(title, posterPath);
            }

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("TMDB", "Error parsing results", e);
        }
    }

    private void addToWatchlist(String title, String posterUrl) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String movieId = title.replaceAll("\\s+", "_").toLowerCase();

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("posterUrl", posterUrl);
        data.put("rating", 0);
        data.put("addedAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .collection("watchlist").document(movieId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to watchlist!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
