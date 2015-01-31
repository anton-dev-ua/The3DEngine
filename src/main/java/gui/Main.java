package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

import static engine.LetterABuilder.aLetterA;

public class Main extends Application {


    private Visualizer visualizer;
    private int width = 800;
    private int height = 600;
    private engine.Scene scene;
    private double angle = 0;
    private boolean running = true;

    @Override
    public void start(Stage primaryStage) throws Exception {

        scene = new engine.Scene();
//        scene.setMesh(aCube()
//                        .withEdgeLength(200)
//                        .build()
//        );

//        scene.setMesh(aTorus()
//                        .withBigRadius(150)
//                        .withSmallRadius(60)
//                        .withApproximationNumber(10)
//                        .build()
//        );

        scene.setMesh(aLetterA().withHeight(300).build());

//       scene.setMesh(
//               new Mesh(
//                       new Vertex[]{
//                               new Vertex(100, 150, 0),
//                               new Vertex(70, -50, 0),
//                               new Vertex(-70, -50, 0),
//                               new Vertex(-100, 150, 0),
//                               new Vertex(0, 0, 0)},
//                       new Mesh.Face[] {
//                               new Mesh.Face(0,1,2,3,4)
//                       }
//               )
//       );

        visualizer = new Visualizer(scene, width, height, 90);

        primaryStage.setTitle("3D Engine");
        primaryStage.setScene(new Scene(visualizer.createScenePane(), width, height, Color.BLACK));
        primaryStage.setX(Screen.getPrimary().getVisualBounds().getWidth() - width);
        primaryStage.setY(0);

        sutUpKeyHandlers(primaryStage);

        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> running = false);

        startDrawingThread();
    }

    private void startDrawingThread() {
        new Thread(() -> {
            long fps = 60;
            long redrawSync = 1000000000 / fps;
            long lastTime = 0;
            while (running) {
                if (System.nanoTime() - lastTime > redrawSync) {
                    lastTime = System.nanoTime();
                    waitForDisplaying(() -> visualizer.drawScene());
                }
            }
        }).start();
    }

    private void sutUpKeyHandlers(final Stage primaryStage) {
        primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.LEFT) {
                    angle += 5;
                    scene.getMesh().rotateY(angle);
                }
                if (event.getCode() == KeyCode.RIGHT) {
                    angle -= 5;
                    scene.getMesh().rotateY(angle);
                }
                if (event.getCode() == KeyCode.O) {
                    scene.getMesh().reset();
                    angle = 0;
                }
                if (event.getCode() == KeyCode.T) {
                    scene.getMesh().triangulate();
                }
                if (event.getCode() == KeyCode.V) {
                    visualizer.setShowVertexNumber(!visualizer.isShowVertexNumber());
                }
                if (event.getCode() == KeyCode.A) {
                    visualizer.setShowArrows(!visualizer.isShowArrows());
                }


                if (event.getCode() == KeyCode.Q) {
                    running = false;
                    primaryStage.close();
                }
            }
        });
    }

    private void waitForDisplaying(Runnable operation) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            operation.run();
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
