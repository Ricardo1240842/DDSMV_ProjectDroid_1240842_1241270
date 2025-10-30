package com.example.moviewatchlist;

public class Movie {
    private String title;
    private String posterUrl;
    private int rating;
    private String tmdbId;


    public Movie() {}

    public Movie(String title, String posterUrl, int rating, String tmdbId) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.tmdbId = tmdbId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getTmdbId() { return tmdbId; }
    public void setTmdbId(String tmdbId) { this.tmdbId = tmdbId; }
}
