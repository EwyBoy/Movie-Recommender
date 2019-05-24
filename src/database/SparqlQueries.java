package database;

import movie.Movie;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparqlQueries {

    private Dataset dataset;
    private Model model;
    private ArrayList<Movie> movies;

    public SparqlQueries() {
        this.dataset = TDBFactory.createDataset("movie_tdb");
        this.model = dataset.getDefaultModel();
        this.movies = new ArrayList<>();
    }

    public ArrayList<Movie> getMovies() {
        return movies;
    }

    public List<Movie> createMovieObjectsFromMovie(List<Movie> titles) {
        List<Movie> movies = new ArrayList<>();
        for(Movie title : titles) {
            String query = ""
                    + "PREFIX info216: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/page/> PREFIX dbo: <http://dbpedia.org/ontology/> "
                    + "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> "
                    + "SELECT ?title ?year ?genres ?runtime ?language ?country ?gross ?votes ?rating ?score ?link ?id ?directorName WHERE {"
                        + "?movie info216:title ?title ;"
                        + "dbo:year ?year ;"
                        + "info216:genres ?genres ;"
                        + "dbo:filmRuntime ?runtime ;"
                        + "dbo:language ?language ;"
                        + "dbo:country ?country ;"
                        + "dbo:gross ?gross ;"
                        + "info216:imdb_votes ?votes ;"
                        + "dbo:content_rating ?rating ;"
                        + "dbo:rating ?score ;"
                        + "dbp:Hyperlink ?link ;"
                        + "info216:imdbId ?id ;"
                        + "dbo:director ?director."
                        + "?director vcard:FN ?directorName."
                        + "FILTER regex(?title, \"^" + title.getTitle() + "$\", \"i\")."
                    + "}";

            ResultSet resultSet = QueryExecutionFactory
                    .create(query, model)
                    .execSelect()
            ;

            while (resultSet.hasNext()) {

                QuerySolution qsol = resultSet.next();

                ArrayList<String> actors = actorsOfTitle(qsol.get("?title").toString());
                ArrayList<String> keywords = keywordsOfTitle(qsol.get("?title").toString());

                // Adds all properties returned from query as parameters for the movie object.
                if (qsol.get("?title") != null) {
                    Movie movie = new Movie(
                        qsol.get("?title").toString(),
                        qsol.get("?year").toString(),
                        qsol.get("?genres").toString(),
                        qsol.get("?runtime").toString(),
                        qsol.get("?language").toString(),
                        qsol.get("?country").toString(),
                        qsol.get("?gross").toString(),
                        qsol.get("?votes").toString(),
                        qsol.get("?rating").toString(),
                        qsol.get("?score").toString(),
                        qsol.get("?link").toString(),
                        qsol.get("?id").toString(),
                        qsol.get("?directorName").toString(),
                        actors,
                        keywords
                    );
                    movies.add(movie);
                }
            }
        }
        //  return list of movie-objects.
        return movies;
    }

    public List<Movie> createMovieObjects(List<String> titles) {
        List<Movie> movies = new ArrayList<>();
        for(String title : titles) {
            String query = ""
                    + "PREFIX info216: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/page/> PREFIX dbo: <http://dbpedia.org/ontology/> "
                    + "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> "
                    + "SELECT ?title ?year ?genres ?runtime ?language ?country ?gross ?votes ?rating ?score ?link ?id ?directorName WHERE {"
                    + "?movie info216:title ?title ;"
                    + "dbo:year ?year ;"
                    + "info216:genres ?genres ;"
                    + "dbo:filmRuntime ?runtime ;"
                    + "dbo:language ?language ;"
                    + "dbo:country ?country ;"
                    + "dbo:gross ?gross ;"
                    + "info216:imdb_votes ?votes ;"
                    + "dbo:content_rating ?rating ;"
                    + "dbo:rating ?score ;"
                    + "dbp:Hyperlink ?link ;"
                    + "info216:imdbId ?id ;"
                    + "dbo:director ?director."
                    + "?director vcard:FN ?directorName."
                    + "FILTER regex(?title, \"^" + title + "$\", \"i\")."
                    + "}";

            ResultSet resultSet = QueryExecutionFactory
                    .create(query, model)
                    .execSelect()
                    ;

            while (resultSet.hasNext()) {

                QuerySolution qsol = resultSet.next();

                ArrayList<String> actors = actorsOfTitle(qsol.get("?title").toString());
                ArrayList<String> keywords = keywordsOfTitle(qsol.get("?title").toString());

                // Adds all properties returned from query as parameters for the movie object.
                if (qsol.get("?title") != null) {
                    Movie movie = new Movie(
                            qsol.get("?title").toString(),
                            qsol.get("?year").toString(),
                            qsol.get("?genres").toString(),
                            qsol.get("?runtime").toString(),
                            qsol.get("?language").toString(),
                            qsol.get("?country").toString(),
                            qsol.get("?gross").toString(),
                            qsol.get("?votes").toString(),
                            qsol.get("?rating").toString(),
                            qsol.get("?score").toString(),
                            qsol.get("?link").toString(),
                            qsol.get("?id").toString(),
                            qsol.get("?directorName").toString(),
                            actors,
                            keywords
                    );
                    movies.add(movie);
                }
            }
        }
        //  return list of movie-objects.
        return movies;
    }

    // This method takes a parameter of several movies, and then queries the dbpedia endpoint to make a big list of all the subjects that occur in all of them.
    // Then we find which of these subjects occur most often and return the top three of them. An example of a subject is: Films_about_sharks or Space-Adventure films and more.
    public List<String> predictSubgenres(List<Movie> movies) {

        if(movies.isEmpty()) {
            return null;
        }

        // dbpedia has pretty inconsistent url's unfortunately for their movies.
        // Therefore we often need to attempt several different versions to make a sucessfull query.
        List<String> queryAttempts = prepareURLs(movies);

        // movies that were sucessfully queried.

        List<String> succesQueries = new ArrayList<>();

        // top-subjects that will be returned.
        List<String> subjects = new ArrayList<>();

        int attempt = 1;
        boolean skipNext = false;

        for (String title : queryAttempts) {
            //skips queries for movies that already had a successful query.
            if(!(skipNext)) {
                String query = "PREFIX dc: <http://purl.org/dc/terms/> PREFIX dbo: <http://dbpedia.org/ontology/>" +
                    "SELECT DISTINCT ?subject" +
                    " WHERE {" +
                        "<http://dbpedia.org/resource/" + title + "> dc:subject ?subject." +
                        //" FILTER regex(str(?subject), \"about\", \"i\")." +
                    "}";

                ResultSet resultSet = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query).execSelect();

                if (resultSet.hasNext()) {
                    succesQueries.add(title);
                    // add all subjects to a big list, and format away the uri leaving only the subjects itself.
                    resultSet.forEachRemaining(
                            qsol -> subjects.add(qsol.get("?subject").toString().split(":")[2].replace("_", " "))
                    );
                    // if we made the query on first or second attempt, we dont have to any more until next movie.
                    if (attempt == 1 || attempt == 2) {
                        skipNext = true;
                    }
                }
            } else {
                skipNext = false;
            }

            attempt++;

            // time for next movie.
            if(attempt > 3) {
                attempt = 1;
            }
        }

        // Let's sort the map of frequencies, so that we can easily find the top subjects.
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String s : subjects) {
            Integer count = frequencyMap.get(s);
            if (count == null)
                count = 0;
            frequencyMap.put(s, count + 1);
        }

        Sorting sorting = new Sorting();
        frequencyMap = sorting.sortByValue(frequencyMap);
        // The top three subjects we want are exactly the three first entries in the map.
        int i = 0;
        ArrayList<String> favorite_subgenres = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            boolean skip = false;
            // skip some boring and generic sub-genres.
            if(entry.getKey().contains("language") || entry.getKey().contains("American") || entry.getKey().contains("Studio") || entry.getKey().contains("Company")) {
                skip = true;
            }
            if (i > 3) {
                break;
            }
            if(!skip) {
                favorite_subgenres.add(entry.getKey());
                i++;
            }
        }
        // print some information about the common subjects found....
        System.out.println("Based on the movies: ");

        for(String title : succesQueries) {
            System.out.print(title + "  | ");
        }

        return favorite_subgenres;
    }

    // private method used by method above.
    // This method takes a list of movies, and makes returns a list of titles that are suitable for when querying the DBpedia sparqø-endpoint.
    private List<String> prepareURLs(List<Movie> movies) {
        ArrayList<String> queryAttempts = new ArrayList<>();
        for (Movie m : movies) {
            String year = m.getYear().split("\\^\\^")[0];
            //url's cant have whitespace
            String originalTitle = m.getTitle().replace(" ", "_");
            // normal title-url
            queryAttempts.add(originalTitle);
            // title-url + _(film)
            String titlePlusFilm = originalTitle + "_(film)";
            queryAttempts.add(titlePlusFilm);
            String titlePlusYear = originalTitle +  "_(" + year + "_film)";
            queryAttempts.add(titlePlusYear);
        }
        return queryAttempts;
    }

    // This method queries the dbpedia.org sparql endpoint for descriptions that we want to add to our own TDB. Essentially combining data from two datasets.
    public boolean insertDescriptionEndpoint(List<Movie> movies) {

        if(movies.isEmpty()) {
            return false;
        }

        List<String> queryAttempts = prepareURLs(movies);
        int attempt = 1;
        boolean skipNext = false;
        System.out.println("\nRetrieveing descriptions from dbpedia.org....\n");

        for (String title : queryAttempts) {
            if(!(skipNext)) {
                String query = "PREFIX dc: <http://purl.org/dc/terms/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>PREFIX dbo: <http://dbpedia.org/ontology/>" +
                        " SELECT DISTINCT ?subject ?comment" +
                        " WHERE {" +
                        " <http://dbpedia.org/resource/" + title + ">  rdfs:comment ?comment." +
                        "FILTER (lang(?comment) = 'en')." +
                        "}";
                ResultSet resultSet = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query).execSelect();
                if (resultSet.hasNext()) {
                    String originalTitle = title.split("_\\(")[0].replace("_"," ");
                    resultSet.forEachRemaining(
                            qsol -> insertDescription(originalTitle, qsol.get("?comment").toString()));
                    if (attempt == 1 || attempt == 2) {
                        skipNext = true;
                    }
                }
            }
            else {
                skipNext = false;
            }
            attempt++;
            // time for next movie.
            if(attempt > 3) {
                attempt = 1;
            }
        }
        return true;
    }

    // private method used by method above to insert descriptions into our TDB.
    private void insertDescription(String title, String description) {
        String query = ""
                + "PREFIX info216: <http://info216.no/v2019/vocabulary/>"
                + " INSERT {"
                + "    ?extra info216:comment \""+description+"\". "
                + "} "
                + " WHERE { ?movie info216:title ?title; "
                + "  info216:extra ?extra. "
                + " FILTER regex(?title, \"^" + title + "$\", \"i\")."
                // + "}"
                + "} ";
        UpdateAction.parseExecute(query, model);
    }

    public String getExtraDescription(Movie title) {
        String result = "";
        String query =
                "PREFIX info216: <http://info216.no/v2019/vocabulary/> " +
                        "SELECT DISTINCT ?comment" +
                        " WHERE { " +
                        "?movie info216:title ?title . " +
                        "?movie info216:extra ?extra . " +
                        "?extra info216:comment ?comment ." +
                        " FILTER regex(?title, \"^" + title.getTitle() + "$\", \"i\")." +
                        "}";
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if (resultSet.hasNext()) {
            result = resultSet.next().get("?comment").toString();
        }
        return result;
    }

    public boolean sparqlEndpointGetSubjects(String title) {
        boolean match = false;
        title = title.replace(" ", "_");
        title += "_(film)";
        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbp: <http://dbpedia.org/page/> PREFIX dc: <http://purl.org/dc/terms/>" +
                "SELECT DISTINCT ?subject " +
                "WHERE {" +
                  //"<http://dbpedia.org/resource/" + title + "> rdfs:label ?title." +
                    "<http://dbpedia.org/resource/" + title + "> dc:subject ?subject." +
                    //"FILTER (lang(?subject) = 'en')." +
                "}";
        System.out.println("QUERY: " + query);
        ResultSet resultSet = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query).execSelect();
        if(resultSet.hasNext()) {
            match = true;
            resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?subject")));
        }
        return match;
    }

    public Boolean searchForAMovie(String title) {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> SELECT DISTINCT ?movie ?property ?value WHERE { ?movie m:title ?title .?movie ?property ?value .FILTER regex(str(?title), \""+title+"\") .}";
        System.out.println("QUERY: " + query);

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {
            match = true;
        }

        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?movie").toString()+ qsol.get("?property").toString() + qsol.get("?value").toString()));

        return match;
    }

    public ArrayList<String> actorsOfTitle(String title) {
        ArrayList<String> list = new ArrayList<>();
        boolean match = false;
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/> PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> "+
                "SELECT DISTINCT ?name WHERE { ?movie info216:title ?value .?movie info216:actors ?actors. ?actors dbp:starring ?actor. ?actor vcard:FN ?name." +
                "FILTER regex(str(?value), \"^" + title + "$\", \"i\") .}";
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        resultSet.forEachRemaining(
                querySolution -> {
                    list.add(querySolution.get("?name").toString());
                }
        );

        return list;
    }

    public ArrayList<String> keywordsOfTitle(String title) {
        ArrayList<String> list = new ArrayList<>();
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
                "SELECT DISTINCT ?keywordLabel WHERE { ?movie info216:title ?value .?movie info216:keywords ?keywords. ?keywords info216:keyword ?keyword. ?keyword rdfs:label ?keywordLabel." +
                "FILTER regex(str(?value), \"^" + title + "$\", \"i\") .}";

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        resultSet.forEachRemaining(qsol ->
                list.add(qsol.get("?keywordLabel").toString())
        );
        return list;
    }

    public Boolean allMoviesOfDirector(String director) {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/> PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>" +
                "SELECT DISTINCT ?title WHERE { ?movie m:title ?title .?movie dbp:director ?director. ?director vcard:FN ?name."+
                "FILTER regex(str(?name), \""+director+"\", \"i\") .}";
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {
            match = true;
        }
        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?title")));

        return match;
    }

    public ResultSet allMoviesOfActor(String actor) {
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/> PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>" +
                "SELECT DISTINCT ?title WHERE { ?movie m:title ?title .?movie m:actors ?actors. ?actors dbp:starring ?actor. ?actor vcard:FN ?name." +
                "FILTER regex(str(?name), \"" + actor + "\",\"i\").}";
        //System.out.println(query);

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {}

        //resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?title")));

        return resultSet;
    }

    public ResultSet allTitles() {
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/>" +
                "SELECT DISTINCT ?title" +
                " WHERE { ?movie info216:title ?title " +
                ".}" +
                "LIMIT 4830";
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        //resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?title")));
        return resultSet;
    }

    public ResultSet allGenres() {
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/>" +
                "SELECT DISTINCT ?genre" +
                " WHERE { ?movie info216:genres ?genre " +
                ".}" +
                "LIMIT 4830";

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        //resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?genre")));
        return resultSet;
    }

    public ResultSet searchTitle(String search) {
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/>" +
                "SELECT DISTINCT ?title" +
                " WHERE { ?movie info216:title ?title. " +
                "FILTER regex(str(?title), \"" + search + "\", \"i\")." +
                "}" +
                "LIMIT 4830";

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        return resultSet;
    }

    public ResultSet searchGenre(String search) {
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/>" +
                "SELECT DISTINCT ?genre" +
                " WHERE { ?movie info216:genres ?genre. " +
                "FILTER regex(str(?genre), \"" + search + "\", \"i\")." +
                "}" +
                "LIMIT 4830";
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        return resultSet;
    }

    public Boolean personDirectorAndActor() {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/>" +
                "SELECT DISTINCT ?title WHERE { ?movie m:title ?title.?movie dbp:director ?director.?movie m:actors ?actors. ?actors dbp:starring ?actor." +
                "FILTER (?director = ?actor).}";
        System.out.println(query);

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {
            match = true;
        }

        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("title")));

        return match;
    }

    public void printAllTriples() {
        ResultSet resultSet = QueryExecutionFactory
                .create(""
                    + "SELECT ?s ?p ?o WHERE {"
                    + "?s ?p ?o ."
                    + "}", model)
                .execSelect();
        resultSet.forEachRemaining(qsol -> System.out.println(qsol.toString()));
    }

    // This method prints x number of randomly selected mobies from out TDB. Mostly used for testing/simulation ourposes.
    public List<String> selectRandomMovies(int queryLimit) {
        List<String> titles = new ArrayList<>();
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/> " +
                "SELECT DISTINCT ?title " +
                " WHERE { " +
                "?movie info216:title ?title. " +
                "}" +
                "ORDER BY RAND()" +
                "LIMIT " + queryLimit;
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        while(resultSet.hasNext()) {
            QuerySolution qsol = resultSet.next();
            if(qsol.get("?title") != null){
                titles.add(qsol.get("?title").toString());
            }
        }
        return titles;
    }


    public Model getModel() {
        return this.model;
    }

    public void close() {
        this.dataset.close();
    }
}
