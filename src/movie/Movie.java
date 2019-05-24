package movie;
import java.util.ArrayList;

public class Movie {

    private String title;
    private String year;
    private String genres;
    private String runtime;
    private String language;
    private String country;
    private String gross;
    private String votes;
    private String rating;
    private String score;
    private String link;
    private String id;
    private String director;
    private ArrayList<String> actors;
    private ArrayList<String> keywords;

    /**
     * @param title
     * @param year
     * @param genres
     * @param runtime
     * @param language
     * @param country
     * @param gross
     * @param votes
     * @param rating
     * @param score
     * @param link
     * @param id
     * @param director
     * @param actors
     * @param keywords
     * 
     * This class is to create movie objects
     */
    public Movie (
            String title,
            String year,
            String genres,
            String runtime,
            String language,
            String country,
            String gross,
            String votes,
            String rating,
            String score,
            String link,
            String id,
            String director,
            ArrayList<String> actors,
            ArrayList<String> keywords
    ) {
        this.title = title;
        this.year = year;
        this.genres = genres;
        this.runtime = runtime;
        this.language = language;
        this.country = country;
        this.gross = gross;
        this.votes = votes;
        this.rating = rating;
        this.score = score;
        this.link = link;
        this.id = id;
        this.director = director;
        this.actors = actors;
        this.keywords = keywords;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
    }

    public String getVotes() {
        return votes;
    }

    public void setVotes(String votes) {
        this.votes = votes;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public ArrayList<String> getActors() {
        return actors;
    }

    public void setActors(ArrayList<String> actors) {
        this.actors = actors;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

}
