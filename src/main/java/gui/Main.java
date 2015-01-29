package gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static engine.CubeBuilder.aCube;

public class Main extends Application {


    private Visualizer visualizer;
    private int width = 800;
    private int height = 600;
    private engine.Scene scene;
    private double angle = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {

        scene = new engine.Scene();
        scene.setMesh(aCube()
                        .withEdgeLength(200)
                        .build()
        );
        visualizer = new Visualizer(scene, width, height, 90);

        primaryStage.setTitle("3D Engine");
        primaryStage.setScene(new Scene(visualizer.createScenePane(), width, height, Color.BLACK));
        primaryStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - width);
        primaryStage.setY(0);

        sutupKeyHundlers(primaryStage);

        visualizer.drawScene();

        primaryStage.show();
    }

    private void sutupKeyHundlers(final Stage primaryStage) {
        primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.OPEN_BRACKET) {
                    angle += 5;
                    scene.getMesh().rotateY(angle);
                }
                if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                    angle -= 5;
                    scene.getMesh().rotateY(angle);
                }
                if (event.getCode() == KeyCode.O) {
                    scene.getMesh().reset();
                    angle = 0;
                }
                if (event.getCode() == KeyCode.Q) {
                    primaryStage.close();
                }
                visualizer.drawScene();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}
