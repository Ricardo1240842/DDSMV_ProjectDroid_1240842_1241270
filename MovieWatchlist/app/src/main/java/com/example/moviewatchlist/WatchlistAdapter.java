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

/*
 WatchlistAdapter displays all movies saved by the user in their personal watchlist.
 It allows:
   - Viewing stored movie title, poster, and personal rating
   - Updating the rating inside the watchlist
   - Removing movies from the user's watchlist

 Each movie shown in this adapter comes from Firestore under:
   users/{userId}/watchlist/{movieId}
*/
public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> movies;       // movies stored in the user's watchlist
    private final FirebaseFirestore db;

    public WatchlistAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
        this.db = FirebaseFirestore.getInstance();
    }

    /*
     Creates the view for each watchlist item (item_watchlist_movie.xml)
    */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_watchlist_movie, parent, false);
        return new ViewHolder(view);
    }

    /*
     Binds data from a Movie object into the UI elements:
       - Title
       - Poster
       - User rating (editable)
       - Remove button
    */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie m = movies.get(position);

        // Set movie title
        holder.title.setText(m.getTitle());

        // Display personal rating saved in watchlist
        holder.ratingBar.setRating(m.getRating());

        // Load poster using Glide
        Glide.with(context)
                .load(m.getPosterUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.poster);

        /*
         When the user changes their watchlist rating, update Firestore.
         Only triggers when the change is made by the user, not programmatically.
        */
        holder.ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                m.setRating((int) rating);
                updateRatingInFirestore(m);
            }
        });

        // Remove a movie from the user's watchlist
        holder.removeButton.setOnClickListener(v -> removeFromWatchlist(m, position));
    }

    /*
     Updates only the "rating" field in the Firestore watchlist document.
     This does not affect global ratings.
    */
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

    /*
     Removes a movie document from the user's watchlist in Firestore
     and updates the local RecyclerView list accordingly.
    */
    private void removeFromWatchlist(Movie movie, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference ref = db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movie.getTmdbId());

        ref.delete()
                .addOnSuccessListener(aVoid -> {
                    movies.remove(position); // remove locally
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Removed from Watchlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /*
     Returns the number of movies in the watchlist.
    */
    @Override
    public int getItemCount() {
        return movies.size();
    }

    /*
     Holds references to UI components for each watchlist item,
     improving performance by avoiding repeated findViewById calls.
    */
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
