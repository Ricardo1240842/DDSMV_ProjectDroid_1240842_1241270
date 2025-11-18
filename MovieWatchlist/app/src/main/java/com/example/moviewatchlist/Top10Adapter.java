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
 this adapter is for UI elements for Top10 page
*/
public class Top10Adapter extends RecyclerView.Adapter<Top10Adapter.ViewHolder> {

    private final List<Movie> movies;

    public Top10Adapter(List<Movie> movies) {
        this.movies = movies;
    }


    @NonNull
    @Override
    public Top10Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top10_movie, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull Top10Adapter.ViewHolder holder, int position) {
        Movie m = movies.get(position);


        holder.title.setText(m.getTitle());


        holder.ratingBar.setRating((float) m.getAvgRating());


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
