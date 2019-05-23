package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RecommendationController implements Initializable {

    @FXML
    private AnchorPane frame;

    @FXML
    private AnchorPane panel_one;

    @FXML
    private AnchorPane panel_two;

    @FXML
    private AnchorPane panel_three;

    @FXML
    private Button exit;

    @FXML
    private Button restart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void exit(ActionEvent event) {
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.close();
    }

    @FXML
    void restart(ActionEvent event) {

    }
}
