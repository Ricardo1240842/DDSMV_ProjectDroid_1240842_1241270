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

/*
 MovieAdapter manages how movie search results are displayed in MainActivity.
 It renders:
 - Poster image
 - Movie title
 - RatingBar for user rating input
 - Button to add movie to watchlist

 It also notifies MainActivity whenever a user changes a movie rating
 so the app can update the global Firestore rating.
*/
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private final Context context;
    private final List<Movie> movies;
    private final FirebaseFirestore db;

    /*
     Listener interface used to notify MainActivity when a user sets a rating.
     This allows MainActivity to update the global rating stored in Firestore.
    */
    private final RatingUpdateListener ratingUpdateListener;

    /*
     Callback the MainActivity must implement.
     movieId = Firestore document ID
     rating = rating the user selected
     movie = full movie object so Firestore can save additional data
    */
    public interface RatingUpdateListener {
        void onRatingUpdate(String movieId, double rating, Movie movie);
    }

    /*
     Constructor initializes:
     - Context
     - List of movies to show
     - Firestore instance (for adding to watchlist)
     - Rating update callback
    */
    public MovieAdapter(Context context, List<Movie> movies, FirebaseFirestore db,
                        RatingUpdateListener listener) {
        this.context = context;
        this.movies = movies;
        this.db = db;
        this.ratingUpdateListener = listener;
    }

    /*
     Creates a new row view using item_movie.xml.
     This is done when RecyclerView needs a new ViewHolder.
    */
    @NonNull
    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    /*
     Called for every visible row.
     Binds the movie data to the UI components in the ViewHolder.
    */
    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.ViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // Set title text
        holder.title.setText(movie.getTitle());

        // Load poster image using Glide
        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.poster);

        // Set pre-existing rating (always zero in search results)
        holder.ratingBar.setRating(movie.getRating());

        /*
         When the user touches the rating bar:
         - Update the local movie object with the user's rating
         - Notify MainActivity (so global avgRating can be recalculated)
        */
        holder.ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                movie.setRating((int) rating);

                if (ratingUpdateListener != null) {
                    ratingUpdateListener.onRatingUpdate(movie.getTmdbId(), rating, movie);
                }
            }
        });

        /*
         Handles adding the movie to the user's watchlist.
         Stored under: users/{userId}/watchlist/{tmdbId}
        */
        holder.addToWatchlistButton.setOnClickListener(v -> addToWatchlist(movie));
    }

    /*
     Adds the selected movie to the current user's watchlist in Firestore.
     The full Movie object is stored, including:
     title, posterUrl, rating, avgRating
    */
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

    /*
     ViewHolder holds references to each row's UI components.
     These references avoid repeated calls to findViewById,
     improving RecyclerView performance.
    */
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
