import database.SparqlQueries;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import movie.Movie;
import org.apache.jena.rdf.model.Model;

import java.util.List;

public class Main extends Application {

    private static Model model;

    public static void main(String[] args) {
        launch(args);
    }

    private void testQueries(SparqlQueries queries) {

        List<String> list = queries.selectRandomMovies(10);

        //.printAllTriples();

        List<Movie> movies = queries.createMovieObjects(list);
        List<String> topSubgernes = queries.predictSubgenres(movies);

        for (String s : topSubgernes) {
            System.out.print("\nyou seem to like " + s.toLowerCase() + " & ");
        }

        //queries.sparqlEndpoint("Interstellar");
        //queries.sparqlEndpointGetComment("Interstellar");
        //queries.sparqlEndpointGetSubjects("Interstellar");
        //queries.AllMoviesOfDirector("quentin");
        //queries.AllMoviesOfActor("Leonardo DiCaprio");
        //queries.AllMoviesOfActor("Tom Hanks");
        //queries.close();
    }

    @Override
    public void start(Stage stage) throws Exception {

        SparqlQueries sparqlQueries = new SparqlQueries();
        testQueries(sparqlQueries);

        stage.setResizable(true);

        Parent root = FXMLLoader.load(getClass().getResource("fx/WelcomeScreen.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Movie Recommendation System");
        stage.setScene(scene);
        stage.show();
    }
}
