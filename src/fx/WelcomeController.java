package fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    /**
     * When these methods are called, they will change the Scenes
     **/
    public void startButtonPushed(ActionEvent event) throws IOException {
        Parent movieScreenParent = FXMLLoader.load(getClass().getResource("MovieScreen.fxml"));
        Scene movieScreenScene = new Scene(movieScreenParent);

        //This line gets the Stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();

        window.setScene(movieScreenScene);
        window.show();
    }

    public void exitButtonPushed(ActionEvent event) {
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.close();
    }

}
