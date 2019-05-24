package database;

import movie.Movie;

import java.util.List;

public class User {

    private List<Movie> movies;
    private List<String> genres;

    /**
    * User class stores the user inputs in two separate lists; movies & genres
    * @param movies
    * @param genres
    **/
    public User(List<Movie> movies, List<String> genres) {
        this.movies = movies;
        this.genres = genres;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
}
