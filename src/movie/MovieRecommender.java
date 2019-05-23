package movie;

import Database.SparqlQueries;
import Database.User;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.query.ResultSet;

import java.util.*;
import java.util.stream.IntStream;

public class MovieRecommender {

    public MovieRecommender() {}

    public static final HashMap<String, Movie> movieMap = new HashMap<>();

    public static void main(String[] args) {

        MovieMap.initMovieMap(movieMap);

        List<Movie> testMovies = new ArrayList<>();
        testMovies.add(movieMap.get("Avatar"));
        testMovies.add(movieMap.get("The Following"));
        testMovies.add(movieMap.get("Shanghai Calling"));
        testMovies.add(movieMap.get("Skyfall"));
        testMovies.add(movieMap.get("Tangled"));

        List<String> testGenres = new ArrayList<>();
        testGenres.add("Comedy");
        testGenres.add("Drama");
        testGenres.add("Action");

        recommend(new User(testMovies, testGenres));
    }

    public static Map<Movie, Integer> scoreMap = new HashMap<>();
    private static List<Movie> usedMovies = new ArrayList<>();
    private static List<Movie> recommendedMovies = new ArrayList<>();

    private static int count = 0;

    private static SparqlQueries sparqlQueries = new SparqlQueries();

    public static void recommend(User user) {

        count++;

        if (count <= 3) {
            Movie selectedMovie = selectMovie(user, usedMovies);

            ResultSet allMovies = sparqlQueries.allTitles();

            allMovies.forEachRemaining(

                querySolution -> {

                    if (querySolution.get("?title").toString() != null) {

                        int score = 0;

                        Movie targetMovie = movieMap.get(querySolution.get("?title").toString());

                        if (!user.getMovies().contains(targetMovie)) {

                            if (targetMovie.getTitle().contains(selectedMovie.getTitle())) {
                                score += 20;
                            }

                            score += IntStream.range(0, targetMovie.getActors().size()).filter(i -> targetMovie.getActors().get(i).contains(selectedMovie.getActors().toString())).map(i -> 10).sum();

                            String genre = targetMovie.getGenres();
                            String[] genres = genre.split("\\|");
                            for (String s : genres) {
                                if (user.getGenres().contains(s)) {
                                    score += 5;
                                }
                            }

                            if (withinYear(3, selectedMovie, targetMovie)) {
                                score += 3;
                            } else if (withinYear(5, selectedMovie, targetMovie)) {
                                score += 2;
                            } else if (withinYear(10, selectedMovie, targetMovie)){
                                score += 1;
                            } else {
                                score -= 3;
                            }

                            if (targetMovie.getDirector().equals(selectedMovie.getDirector())) {
                                score += 5;
                            }

                            if (targetMovie.getLanguage().equals(selectedMovie.getLanguage())) {
                                score += 10;
                            } else {
                                score -= 5;
                            }

                            if (targetMovie.getCountry().equals(selectedMovie.getCountry())) {
                                score += 5;
                            } else {
                                score -= 3;
                            }

                            if (Double.parseDouble(targetMovie.getScore()) >= Double.parseDouble(selectedMovie.getScore())) {
                                score += (int) Math.round(Double.parseDouble(targetMovie.getScore()) - Double.parseDouble(selectedMovie.getScore()));
                            } else {
                                score -= (int) Math.round(Double.parseDouble(selectedMovie.getScore()) - Double.parseDouble(targetMovie.getScore()));
                            }

                            if (!targetMovie.getRating().equalsIgnoreCase("unknown") || !targetMovie.getRating().equalsIgnoreCase("Not Rated")) {
                                if (targetMovie.getRating().equals(selectedMovie.getRating())) {
                                    score += 3;
                                } else {
                                    score -= 3;
                                }
                            }

                            if (!Collections.disjoint(targetMovie.getKeywords(), selectedMovie.getKeywords())) {
                                score += 10;
                            }

                            if (!Collections.disjoint(targetMovie.getActors(), selectedMovie.getActors())) {
                                score += 5;
                            }

                        }

                        scoreMap.put(targetMovie, score);

                    }
                }
            );

            int biggest = 0;
            Movie recommendedMovie = null;

            for (Map.Entry<Movie, Integer> entry : scoreMap.entrySet()) {
                if (entry.getValue() >= biggest) {
                    biggest = entry.getValue();
                    recommendedMovie = entry.getKey();
                }
            }

            System.out.println(recommendedMovie.getTitle() + " based on " + selectedMovie.getTitle() + " with a score of " + scoreMap.get(recommendedMovie));

            recommendedMovies.add(recommendedMovie);

            recommend(user);
        }

    }

    private static Boolean withinYear(int year, Movie selected, Movie target) {
        if (NumberUtils.isNumber(target.getYear())) {

            if (NumberUtils.isNumber(selected.getYear())) {

                int targetYear = Integer.parseInt(target.getYear());
                int selectedYear = Integer.parseInt(selected.getYear());

                int targetPlusMargin =  Math.addExact(targetYear, year);
                int targetMinusMargin =  Math.subtractExact(targetYear, year);

                return selectedYear <= targetPlusMargin && selectedYear >= targetMinusMargin;
            }
        }
        return false;
    }

    private static Movie selectMovie(User user, List<Movie> usedMovies) {
        Movie selectedMovie = user.getMovies().get(getRandomIntegerInRange(0, 5));

        if (usedMovies.contains(selectedMovie)) {
            return selectMovie(user, usedMovies);
        } else {
            usedMovies.add(selectedMovie);
            return selectedMovie;
        }

    }

    public static int getRandomIntegerInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}
