package com.example.moviewatchlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/*
 Top10Adapter is responsible for displaying the top 10 globally rated movies.
 It binds movie title, poster, and global average rating (avgRating) to the RecyclerView.
*/
public class Top10Adapter extends RecyclerView.Adapter<Top10Adapter.ViewHolder> {

    private final List<Movie> movies; // list of movies to display

    public Top10Adapter(List<Movie> movies) {
        this.movies = movies;
    }

    /*
     Creates each item view (item_top10_movie) and wraps it in a ViewHolder.
    */
    @NonNull
    @Override
    public Top10Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top10_movie, parent, false);
        return new ViewHolder(view);
    }

    /*
     Binds a Movie object to a ViewHolder.
     Sets:
       - Movie title
       - Poster image (loaded with Glide)
       - Global average rating (read-only RatingBar)
    */
    @Override
    public void onBindViewHolder(@NonNull Top10Adapter.ViewHolder holder, int position) {
        Movie m = movies.get(position);

        // Display movie name
        holder.title.setText(m.getTitle());

        // Display global average rating
        holder.ratingBar.setRating((float) m.getAvgRating());

        // Load poster image using Glide
        Glide.with(holder.itemView.getContext())
                .load(m.getPosterUrl())
                .placeholder(R.drawable.placeholder) // shown while loading / if null
                .into(holder.poster);
    }

    /*
     Returns how many movies are displayed.
    */
    @Override
    public int getItemCount() {
        return movies.size();
    }

    /*
     Holds references to each UI element in the item layout for performance.
    */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            poster = itemView.findViewById(R.id.posterImage);
            title = itemView.findViewById(R.id.movieTitle);
            ratingBar = itemView.findViewById(R.id.movieRatingBar);
        }
    }
}
