package com.example.moviewatchlist;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
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
    private Button searchButton;
    private ListView resultsList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> movieTitles = new ArrayList<>();
    private String apiKey;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiKey = getString(R.string.tmdb_api_key);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        searchField = findViewById(R.id.searchField);
        searchButton = findViewById(R.id.searchButton);
        resultsList = findViewById(R.id.resultsList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, movieTitles);
        resultsList.setAdapter(adapter);

        // procurar filmes
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchMovies(query);
            }
        });

        // adicionar a watchlist quando se pesquisa
        resultsList.setOnItemClickListener((parent, view, position, id) -> {
            String title = movieTitles.get(position);
            addToWatchlist(title);
        });
    }

    //  API
    private void searchMovies(String query) {
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + query;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TMDB", "API request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseMovieResults(json));
                }
            }
        });
    }

    // JSON requests
    private void parseMovieResults(String json) {
        movieTitles.clear();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");

            for (JsonElement el : results) {
                JsonObject movie = el.getAsJsonObject();
                String title = movie.get("title").getAsString();
                movieTitles.add(title);
            }

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("TMDB", "Error parsing results", e);
        }
    }

    //  adicionar um filme selecionado a watchlist
    private void addToWatchlist(String title) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String movieId = title.replaceAll("\\s+", "_").toLowerCase();

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("posterUrl", ""); // optional
        data.put("rating", 0);
        data.put("addedAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .collection("watchlist").document(movieId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to watchlist!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
