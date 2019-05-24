package movie;

import database.User;
import org.apache.commons.lang3.math.NumberUtils;
import fx.MovieController;

import java.util.*;
import java.util.stream.IntStream;

public class MovieRecommender {

    public MovieRecommender() {}

    private static final HashMap<String, Movie> movieMap = new HashMap<>();
    private static Map<Movie, Integer> scoreMap = new HashMap<>();

    public static List<Movie> usedMovies = new ArrayList<>();
    public static List<Movie> recommendedMovies = new ArrayList<>();

    private static int count = 0;

    /**
     * This class takes a User object and goes queries through each movie in the system
     * calculating a "score" for each movie based on how well it matches the user inputs.
     * The highest score gets chosen as the best match and the method uses recursion and
     * performs this action 3 times then stores the results in a list of movies to recommend.
     *
     * @param user
     */
    public static void recommend(User user) {

        // Initializes the movie map - See MovieMap for more info
        MovieMap.initMovieMap(movieMap);

        // Increments the counter - We are using recursion
        count++;

        if (count <= 3) {

            // Triggers the selectMovie method - See selectMovie() below
            Movie selectedMovie = selectMovie(user, usedMovies);

            if (MovieController.queriedMovieList != null) {

                // Grabs the queried list of all movies - had a bug when I tried to query the list here it deleted itself.
                List<Movie> queriedMovieList = MovieController.queriedMovieList;

                // Iterates the queried list of all movies
                for (Movie movie : queriedMovieList) {

                    int score = 0;

                    // Target movie is the current selected movie in the loop
                    Movie targetMovie = movieMap.get(movie.getTitle());

                    if (targetMovie != null) {

                        // Makes sure the selected movie can't be our target movie
                        if (!targetMovie.getTitle().equals(selectedMovie.getTitle())) {

                            // Makes sure the it is non of our movies to prevent duplicates
                            if (!user.getMovies().contains(targetMovie)) {

                                // If target movie shares part of movie title we give lot of points
                                // If you chose Harry Potter its likely to recommend Harry Potter 2
                                if (targetMovie.getTitle().contains(selectedMovie.getTitle())) {
                                    score += 20;
                                }

                                // Gives extra point per shared actor from movies
                                score += IntStream.range(0, targetMovie.getActors().size()).filter(i -> targetMovie.getActors().get(i).contains(selectedMovie.getActors().toString())).map(i -> 10).sum();

                                // Looking for matched genres from user
                                String genre = targetMovie.getGenres();
                                String[] genres = genre.split("\\|");
                                for (String s : genres) {
                                    if (user.getGenres().contains(s)) {
                                        score += 5;
                                    }
                                }

                                // See withinYear() method below
                                // Algorithm prefers movies that are close in year - The closer the better
                                if (withinYear(3, selectedMovie, targetMovie)) {
                                    score += 3;
                                } else if (withinYear(5, selectedMovie, targetMovie)) {
                                    score += 2;
                                } else if (withinYear(10, selectedMovie, targetMovie)){
                                    score += 1;
                                } else {
                                    score -= 3; // if selected move is more then 10 years +/- we take away 3 points
                                }

                                // Same director? Nice, 5 points to you
                                if (targetMovie.getDirector().equals(selectedMovie.getDirector())) {
                                    score += 5;
                                }

                                // If same language we give 10 to boost movies with same language
                                // If not - We take 5 and you don't get the 10 points (so not good match)
                                if (targetMovie.getLanguage().equals(selectedMovie.getLanguage())) {
                                    score += 10;
                                } else {
                                    score -= 5;
                                }

                                // Same here as above but this is for the country the movie was produced
                                if (targetMovie.getCountry().equals(selectedMovie.getCountry())) {
                                    score += 5;
                                } else {
                                    score -= 3;
                                }

                                // Algorithm wants to prioritize recommendation with higher user rating scores
                                // If target movie has 7.7 and selected movie has 5.5 -> It gives you 2.2 points (rounded to 2)
                                // Because 7.7 - 5.5 = 2.2 (rounded to 2)
                                // The else does the same only it subtracts by the score if it is lower
                                if (Double.parseDouble(targetMovie.getScore()) >= Double.parseDouble(selectedMovie.getScore())) {
                                    score += (int) Math.round(Double.parseDouble(targetMovie.getScore()) - Double.parseDouble(selectedMovie.getScore()));
                                } else {
                                    score -= (int) Math.round(Double.parseDouble(selectedMovie.getScore()) - Double.parseDouble(targetMovie.getScore()));
                                }

                                // Checks if not unknown ratings
                                if (!targetMovie.getRating().equalsIgnoreCase("unknown") || !targetMovie.getRating().equalsIgnoreCase("Not Rated")) {

                                    // Awards movies with same rating. The rating is based of the PEGI system
                                    // If both movies has a PEGI-13+ it is good else it is bad.
                                    if (targetMovie.getRating().equals(selectedMovie.getRating())) {
                                        score += 3;
                                    } else {
                                        score -= 3;
                                    }
                                }

                                // Checks for similar keywords in between two movie titles
                                // Keywords for James Bond is Example: "Spy", "Agent", "007" -> if found it is very good
                                if (!Collections.disjoint(targetMovie.getKeywords(), selectedMovie.getKeywords())) {
                                    score += 10;
                                }

                                // Another actor check here in the same fashion as Keywords
                                if (!Collections.disjoint(targetMovie.getActors(), selectedMovie.getActors())) {
                                    score += 5;
                                }

                            }

                        }

                    }

                    // The score is stored in a map where the movie title is the key to the score
                    scoreMap.put(targetMovie, score);

                }

            }

            int biggest = 0;
            Movie recommendedMovie = null;

            // Finds the biggest score in the ScoreMap
            for (Map.Entry<Movie, Integer> entry : scoreMap.entrySet()) {
                if (entry.getValue() >= biggest) {
                    biggest = entry.getValue();
                    recommendedMovie = entry.getKey();
                }
            }

            // Debug code to see what movie was chosen based of XXX
            if (recommendedMovie != null) {
                System.out.println(recommendedMovie.getTitle() + " based on " + selectedMovie.getTitle() + " with a score of " + scoreMap.get(recommendedMovie));
            }

            // Clears the map for new round
            scoreMap.clear();

            // Adds the movie to recommend to the recommendation list
            recommendedMovies.add(recommendedMovie);

            // Jumps back to start and starts all over
            recommend(user);

        }

    }

    /**
     *
     * @param year
     * @param selected
     * @param target
     * @return
     */
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

    /**
     *
     * @param user
     * @param usedMovies
     * @return
     */
    private static Movie selectMovie(User user, List<Movie> usedMovies) {
        Movie selectedMovie = user.getMovies().get(getRandomIntegerInRange(0, 5));

        if (usedMovies.contains(selectedMovie)) {
            return selectMovie(user, usedMovies);
        } else {
            usedMovies.add(selectedMovie);
            return selectedMovie;
        }

    }

    /**
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRandomIntegerInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}
