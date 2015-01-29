package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {


    private Visualizer visualizer;
    private int width;
    private int height;

    @Override
    public void start(Stage primaryStage) throws Exception {

        width = 800;
        height = 600;
        visualizer = new Visualizer(width, height, 90);

        primaryStage.setTitle("3D Engine");
        Scene value = new Scene(visualizer.createScenePane(), width, height, Color.BLACK);
        primaryStage.setScene(value);
        primaryStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - width);
        primaryStage.setY(0);

        visualizer.drawScene();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
