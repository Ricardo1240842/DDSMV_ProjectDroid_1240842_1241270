package com.example.moviewatchlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> movies;
    private final FirebaseFirestore db;
    private final RatingUpdateListener ratingUpdateListener;

    public interface RatingUpdateListener {
        void onRatingUpdate(String movieId, double rating, Movie movie);
    }

    public MovieAdapter(Context context, List<Movie> movies, FirebaseFirestore db,
                        RatingUpdateListener listener) {
        this.context = context;
        this.movies = movies;
        this.db = db;
        this.ratingUpdateListener = listener;
    }

    @NonNull
    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.title.setText(movie.getTitle());
        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.poster);

        holder.ratingBar.setRating(movie.getRating());

        holder.ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                movie.setRating((int) rating);
                if (ratingUpdateListener != null) {
                    ratingUpdateListener.onRatingUpdate(movie.getTmdbId(), rating, movie);
                }
            }
        });

        holder.addToWatchlistButton.setOnClickListener(v -> addToWatchlist(movie));
    }

    private void addToWatchlist(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movie.getTmdbId());

        ref.set(movie)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Added to Watchlist", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to add: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        RatingBar ratingBar;
        Button addToWatchlistButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.posterImage);
            title = itemView.findViewById(R.id.movieTitle);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            addToWatchlistButton = itemView.findViewById(R.id.addToWatchlistButton);
        }
    }
}
