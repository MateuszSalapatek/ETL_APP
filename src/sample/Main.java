package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        ///////////////////////////////
        //to maximalize window
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        primaryStage.setWidth(bounds.getWidth()/2);
        primaryStage.setHeight(bounds.getHeight()/2);
        /////////////////////////////////

        Parent root = FXMLLoader.load(getClass().getResource("ETLForm.fxml"));
        primaryStage.setTitle("ETL Application");
        root.getStylesheets().add("Resources/style.css");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
