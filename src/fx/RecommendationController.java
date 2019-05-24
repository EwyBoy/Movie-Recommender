package fx;

import database.SparqlQueries;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import movie.Movie;
import movie.MovieRecommender;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RecommendationController implements Initializable {

    @FXML
    private AnchorPane frame;

    @FXML
    private Button exit;

    @FXML
    private Text title_one;

    @FXML
    private Text description_one;

    @FXML
    private Text title_three;

    @FXML
    private Text description_three;

    @FXML
    private Text title_two;

    @FXML
    private Text description_two;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        List<Movie> movies = MovieRecommender.recommendedMovies;
        List<Movie> basedOn = MovieRecommender.usedMovies;

        SparqlQueries queries = new SparqlQueries();
        queries.createMovieObjectsFromMovie(movies);
        queries.createMovieObjectsFromMovie(basedOn);

        List<Movie> sub0 = new ArrayList<>();
        sub0.add(movies.get(0));
        sub0.add(basedOn.get(0));
        List<String> subPred0 = queries.predictSubgenres(sub0);

        List<Movie> sub1 = new ArrayList<>();
        sub1.add(movies.get(1));
        sub1.add(basedOn.get(1));
        List<String> subPred1 = queries.predictSubgenres(sub1);

        List<Movie> sub2 = new ArrayList<>();
        sub2.add(movies.get(2));
        sub2.add(basedOn.get(2));
        List<String> subPred2 = queries.predictSubgenres(sub2);

        queries.insertDescriptionEndpoint(movies);

        String dic0 = queries.getExtraDescription(movies.get(0)).replace("@en", ".");
        String dic1 = queries.getExtraDescription(movies.get(1)).replace("@en", ".");
        String dic2 = queries.getExtraDescription(movies.get(2)).replace("@en", ".");

        if (movies.get(0) != null) title_one.setText(movies.get(0).getTitle());
        if (movies.get(1) != null) title_two.setText(movies.get(1).getTitle());
        if (movies.get(2) != null) title_three.setText(movies.get(2).getTitle());

        description_one.setText(getDescription(0, basedOn, movies) + subPred0.get(0).toLowerCase() + ".\n\n" + dic0);
        description_two.setText(getDescription(1, basedOn, movies) + subPred1.get(0).toLowerCase() + ".\n\n" + dic1);
        description_three.setText(getDescription(2, basedOn, movies) + subPred2.get(0).toLowerCase() + ".\n\n" + dic2);

    }

    private static String getDescription(int i, List<Movie> basedOn, List<Movie> movies) {
            return
                    "Based on " + basedOn.get(i).getTitle() + " from " + basedOn.get(i).getYear() +
                            " we recommend " + movies.get(i).getTitle() + " from " + movies.get(i).getYear() +
                            " since you seem to like "
                    ;
    }

    @FXML
    void exit(ActionEvent event) {
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.close();
    }

}
