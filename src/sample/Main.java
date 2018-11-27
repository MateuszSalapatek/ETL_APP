package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("ETL Application");
        root.getStylesheets().add("Resources/style.css");
        primaryStage.setScene(new Scene(root, 600 , 350));
        primaryStage.show();


    }
}
