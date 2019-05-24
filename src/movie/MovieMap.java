package movie;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MovieMap {

    public MovieMap() {}

    /**
     *
     * This class takes ALL movies in JSON and generates a Movie object for each movie
     * and puts em in a map so I can easily get movie object from a movie title String.
     *
     * Map key : String Title
     * Map entry : Movie movie
     *
     * @param movieMap
     * @return movieMap
     */
    public static HashMap<String, Movie> initMovieMap(HashMap<String, Movie> movieMap) {

        JsonParser parser = new JsonParser();

        JsonReader file = null;
        try {
            file = new JsonReader(new FileReader("movies.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonElement element = parser.parse(file);
        JsonArray array = element.getAsJsonArray();

        for(JsonElement movies : array) {

            JsonObject movieObj = movies.getAsJsonObject();

            ArrayList<String> actorList = new ArrayList<>();
            ArrayList<String> keywordList = new ArrayList<>();

            JsonObject actorsJson = movieObj.getAsJsonObject("actors");
            Set<Map.Entry<String, JsonElement>> entrySet = actorsJson.entrySet();
            for(Map.Entry<String,JsonElement> entry : entrySet) {
                actorList.add(actorsJson.get(entry.getKey()).getAsString());
            }

            JsonObject keywordsJson = movieObj.getAsJsonObject("keywords");
            Set<Map.Entry<String, JsonElement>> keywordEntrySet = keywordsJson.entrySet();
            for(Map.Entry<String,JsonElement> entry : keywordEntrySet){
                keywordList.add(keywordsJson.get(entry.getKey()).getAsString());
            }

            Movie movie = new Movie(
                    movieObj.get("title").getAsString(),
                    movieObj.get("year").getAsString(),
                    movieObj.get("genres").getAsString(),
                    movieObj.get("runtime").getAsString(),
                    movieObj.get("language").getAsString(),
                    movieObj.get("country").getAsString(),
                    movieObj.get("gross").getAsString(),
                    movieObj.get("num_votes").getAsString(),
                    movieObj.get("content_rating").getAsString(),
                    movieObj.get("imdb_rating").getAsString(),
                    movieObj.get("imdb_link").getAsString(),
                    movieObj.get("imdb_id").getAsString(),
                    movieObj.get("director").getAsString(),
                    actorList,
                    keywordList
            );

            movieMap.put(movieObj.get("title").getAsString(), movie);

        }

        return movieMap;
    }
}
