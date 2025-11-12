package com.example.moviewatchlist;

public class Movie {

    private String title;
    private String tmdbId;
    private String posterUrl;
    private int rating;       // user's personal rating (1–5)
    private double avgRating; // global Firestore rating

    public Movie() {
        // Required empty constructor for Firestore
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTmdbId() { return tmdbId; }
    public void setTmdbId(String tmdbId) { this.tmdbId = tmdbId; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
}
