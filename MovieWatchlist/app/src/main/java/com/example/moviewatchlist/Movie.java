package com.example.moviewatchlist;

/*
 The Movie class represents a movie item used throughout the app.
 It is stored in multiple places:
 - TMDB API search results
 - User watchlists (in Firestore under users/{userId}/watchlist)
 - Global movie ratings collection (movies/{movieId})

 This model must include:
 - A no-argument constructor (required by Firestore)
 - Public getters and setters (Firestore uses reflection to map fields)
*/
public class Movie {

    /*
     The title of the movie.
     Retrieved from the TMDB API and stored in the user's watchlist,
     and also saved in the global movies/{id} document for display in Top 10.
    */
    private String title;

    /*
     The unique TMDB movie ID.
     This is used as:
     - The document ID in the global "movies" Firestore collection
     - The document ID in each user's watchlist subcollection
    */
    private String tmdbId;

    /*
     URL to the movie poster.
     Loaded using Glide in all RecyclerViews.
     Can sometimes be null when TMDB API returns no poster.
    */
    private String posterUrl;

    /*
     The user’s personal rating (1–5 stars).
     This value is only stored inside that user’s watchlist.
     It does NOT affect the global avgRating.
    */
    private int rating;

    /*
     The global average rating stored in Firestore under "movies/{id}".
     This is computed based on all user ratings using a transaction.
     It is displayed in the Top 10 movies page.
    */
    private double avgRating;

    /*
     Firestore requires a public empty constructor.
     Without this, Firestore cannot create Movie objects
     when retrieving documents.
    */
    public Movie() { }

    // --- Getters and setters ---

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
     This value only lives inside the watchlist and is not used globally.
    */
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    /*
     Gets or sets the global average rating.
     This is updated whenever any user rates the movie in MainActivity.
    */
    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }
}
