package fx;

import database.JSONToTDB;
import database.SparqlQueries;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import movie.Movie;
import movie.MovieMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MovieController implements Initializable {

    @FXML
    private AnchorPane search;

    @FXML
    private TextField search_box;

    @FXML
    private Pane movieinfo;

    @FXML
    private ImageView cover;

    @FXML
    private Text title;

    @FXML
    private Text country;

    @FXML
    private Text language;

    @FXML
    private Text year;

    @FXML
    private Text age_rating;

    @FXML
    private Text user_rating;

    @FXML
    private Text grossing;

    @FXML
    private Text director;

    @FXML
    private Text actors;

    @FXML
    private Text runtime;

    @FXML
    private Text genres;

    @FXML
    private Text error;

    @FXML
    private Text search_text;

    @FXML
    private Button add_movie;

    @FXML
    private Button next;

    @FXML
    private Button back;

    @FXML
    private Button reset;

    @FXML
    private ListView<String> movieLister;

    @FXML
    private TableView<MovieModel> movielist;

    @FXML
    private TableColumn<MovieModel, String> tab_table;

    public static List<Movie> selectedMovies = new ArrayList<>();
    public static List<Movie> queriedMovieList = new ArrayList<>();

    private ObservableList<MovieModel> observableList = FXCollections.observableArrayList();
    private ObservableList<String> observableMovieList = FXCollections.observableArrayList();

    /**
     * Converts movies.json to a Tripple Database
     * for SPARQL queries
     * @return model
     */
    public static Model firstTimeSetup() {
        JSONToTDB jsontoTDB = new JSONToTDB("movies.json");
        Model model = jsontoTDB.getModel();
        return model;
    }

    public static final HashMap<String, Movie> movieMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final String[] name = new String[1];

        MovieMap.initMovieMap(movieMap);

        // This code allows you to click the movie titles in the table to select em
        // You have to double click for this to work for some unknown reason.
        movielist.setRowFactory( tv -> {
            TableRow<MovieModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    MovieModel rowData = row.getItem();

                    if (rowData.getList() != null)  {
                        name[0] = rowData.getList();

                        Movie movie = movieMap.get(
                             Arrays.toString(name)
                            .replace("[", "")
                            .replace("]", "")
                        );

                        error.setText("");

                        title.setText("Title: " + movie.getTitle());
                        year.setText("Year: " + movie.getYear());
                        genres.setText("Genres: " + movie.getGenres().replace("|", " - "));
                        runtime.setText("Runtime: " + movie.getRuntime() + "min");
                        country.setText("Country: " + movie.getCountry());
                        language.setText("Language: " + movie.getLanguage());
                        age_rating.setText("Age Rating: " + movie.getRating());
                        user_rating.setText("User Rating: " + movie.getScore() + "/10 (" + movie.getVotes() +" votes)");
                        grossing.setText("Grossing: $" + movie.getGross());
                        director.setText("Director: " + movie.getDirector());
                        actors.setText("Actors: " + movie.getActors().toString().replace("[", "").replace("]", "").replace(",", " -"));

                    }
                }
            });
            return row ;
        });

        // See firstSetup() method above
        firstTimeSetup();

        SparqlQueries sparqlQueries = new SparqlQueries();

        // Runs a query of all titles in the database
        ResultSet rs = sparqlQueries.allTitles();

        // Iterates though all titles and adds em to the title display list
        rs.forEachRemaining (
            qsol -> {
                if (qsol.get("?title").toString() != null) {
                    observableList.add(new MovieModel(qsol.get("?title").toString()));
                    queriedMovieList.add(movieMap.get(qsol.get("?title").toString()));
                }
            }
        );

        tab_table.setCellValueFactory(new PropertyValueFactory<>("list"));
        movielist.setItems(observableList);

        // Allows you to search the movie title menu by typing and pressing enter
        search_box.setOnAction(type -> {

            observableList.clear();
            movielist.setItems(observableList);

            ResultSet searchSet = sparqlQueries.searchTitle(search_box.getText());

            // Special query to run searches for titles
            searchSet.forEachRemaining (
                qsol -> {
                    if (qsol.get("?title").toString() != null) {
                        observableList.add(new MovieModel(qsol.get("?title").toString()));
                    }
                }
            );

            tab_table.setCellValueFactory(new PropertyValueFactory<>("list"));
            movielist.setItems(observableList);

        });

        // Reset button - Resets all selected titles
        reset.setOnMouseClicked(clicked -> {
            selectedMovies.clear();
            error.setText("");
            add_movie.setText("Add Movie To List - " + selectedMovies.size() + " / 5");
            observableMovieList.clear();
            movieLister.setItems(observableMovieList);
        });

        // Add title to list - Checks for errors and duplicates
        add_movie.setOnMouseClicked(clicked -> {
            if (selectedMovies.size() < 5) {
                if (title.getText() != null && !title.getText().equals("")) {
                    Movie selectedMovie = movieMap.get(title.getText().replace("Title: ", ""));
                    if (selectedMovie != null && !selectedMovie.getTitle().equals("")) {
                        if (!selectedMovies.contains(selectedMovie)) {
                            selectedMovies.add(selectedMovie);
                            error.setText(selectedMovie.getTitle() + " was added to your list");
                            add_movie.setText("Add Movie To List - " + selectedMovies.size() + " / 5");
                            observableMovieList.add(selectedMovie.getTitle());
                            movieLister.setItems(observableMovieList);
                        } else {
                            error.setText("ERROR: This movie has already been selected");
                            System.out.println("ERROR: This movie has already been selected");
                        }
                    } else {
                        error.setText("ERROR: Unknown movie");
                        System.out.println("ERROR: Unknown movie");
                    }
                } else {
                    error.setText("ERROR: Please select a valid movie");
                    System.out.println("ERROR: Please select a valid movie");
                }
            } else {
                error.setText("ERROR: You can't select more then 5 movies");
                System.out.println("ERROR: You can't select more then 5 movies");
            }

        });

    }

    /**
     * Go back to previous window
     * @param event
     * @throws IOException
     */
    public void backButtonPushed(ActionEvent event) throws IOException {
        selectedMovies.clear();
        Parent welcomeScreenParent = FXMLLoader.load(getClass().getResource("WelcomeScreen.fxml"));
        Scene welcomeScreenScene = new Scene(welcomeScreenParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(welcomeScreenScene);
        window.show();
    }

    /**
     * Go to next window
     * @param event
     * @throws IOException
     */
    public void nextButtonPushed(ActionEvent event) throws IOException {

        if (selectedMovies.size() == 5) {
            Parent welcomeScreenParent = FXMLLoader.load(getClass().getResource("GenreScreen.fxml"));
            Scene welcomeScreenScene = new Scene(welcomeScreenParent);

            //This line gets the Stage information
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

            window.setScene(welcomeScreenScene);
            window.show();
        } else {
            error.setText("You must select 5 movies before continuing");
            System.out.println("You must select 5 movies before continuing");
        }
    }
}
