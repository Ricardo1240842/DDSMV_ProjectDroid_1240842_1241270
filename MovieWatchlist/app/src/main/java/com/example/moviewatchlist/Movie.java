package com.example.moviewatchlist;

/*
 The Movie class represents a movie item used throughout the app.
*/
public class Movie {

    /*
     stored in the user's watchlist,
     saved in the global movies/{id} document for Top 10.
    */
    private String title;

    /*

     The document ID in the global "movies"
     The document ID in each user's watchlist
    */
    private String tmdbId;

    /*
     Loaded using Glide in all RecyclerViews.
     Can be null
    */
    private String posterUrl;

    /*
     stored inside that user’s watchlist.
    */
    private int rating;

    /*
     The global average rating stored in Firestore under "movies/{id}".
    */
    private double avgRating;

    /*
       empty constructor required by firebase
    */
    public Movie() { }

    /* Getters and setters */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /*
     Gets or sets the user's personal rating.
    */
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    /*
     Gets or sets the global average rating.
    */
    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }
}
