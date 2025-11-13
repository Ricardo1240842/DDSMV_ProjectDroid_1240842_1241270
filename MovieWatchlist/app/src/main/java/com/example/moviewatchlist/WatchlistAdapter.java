package com.example.moviewatchlist;

import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> movies;
    private final FirebaseFirestore db;

    public WatchlistAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_watchlist_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie m = movies.get(position);

        holder.title.setText(m.getTitle());
        holder.ratingBar.setRating(m.getRating()); // show user rating

        Glide.with(context)
                .load(m.getPosterUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.poster);

        // Update user rating
        holder.ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                m.setRating((int) rating);
                updateRatingInFirestore(m);
            }
        });

        // Remove movie
        holder.removeButton.setOnClickListener(v -> removeFromWatchlist(m, position));
    }

    private void updateRatingInFirestore(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movie.getTmdbId());

        ref.update("rating", movie.getRating())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Rating updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to update rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeFromWatchlist(Movie movie, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movie.getTmdbId());

        ref.delete()
                .addOnSuccessListener(aVoid -> {
                    movies.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Removed from Watchlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        RatingBar ratingBar;
        Button removeButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.posterImage);
            title = itemView.findViewById(R.id.movieTitle);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
