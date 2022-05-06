package puzzle.gui;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ui.fxml"));
        stage.setTitle("Sliding Puzzle Game");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
