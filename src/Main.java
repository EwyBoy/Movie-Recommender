import Database.SparqlQueries;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.jena.rdf.model.Model;

public class Main extends Application {

    private static Model model;

    public static void main(String[] args) {
        launch(args);
    }

    private void testQueries(SparqlQueries queries) {
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

        Parent root = FXMLLoader.load(getClass().getResource("sample/WelcomeScreen.fxml"));
        //Parent root = FXMLLoader.load(getClass().getResource("sample/RecommendationScreen.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Movie Recommendation System");
        stage.setScene(scene);
        stage.show();
    }
}
