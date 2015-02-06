package gui;

import engine.Camera;
import engine.ColladaReader;
import engine.Vertex;
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

import static java.lang.Math.*;

public class Main extends Application {

    private Visualizer visualizer;
    private int width = 800;
    private int height = 600;
    private double fov = 90;
    private engine.Scene scene;
    private double angleY = 0;
    private boolean running = true;
    private Vertex moveVector = new Vertex(0, 0, 0);
    private double angleX;


    @Override
    public void start(Stage primaryStage) throws Exception {

        scene = new engine.Scene();
//        scene.setMesh(aCube()
//                        .withEdgeLength(200)
//                        .build()
//        );

//        scene.setMesh(aCameraPyramid().build());

        scene.setCamera(new Camera(width, height, fov));

//        scene.setMesh(aTorus()
//                        .withBigRadius(150)
//                        .withSmallRadius(60)
//                        .withApproximationNumber(10)
//                        .build()
//        );

//        scene.setMesh(aLetterA().withHeight(300).build());
//        scene.setMesh(aSimpleRoom().build());
        scene.setMesh(new ColladaReader().readFile("/Users/anton/Documents/temp/room-with-corner-stand.dae"));

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

        visualizer = new Visualizer(scene, width, height, fov);

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
                    angleY -= 5;
                    visualizer.setAngleY(angleY);
                }
                if (event.getCode() == KeyCode.RIGHT) {
                    angleY += 5;
                    visualizer.setAngleY(angleY);

                }
                if (event.getCode() == KeyCode.G) {
                    angleX -= 5;
                    visualizer.setAngleX(angleX);
                }
                if (event.getCode() == KeyCode.B) {
                    angleX += 5;
                    visualizer.setAngleX(angleX);

                }
                if (event.getCode() == KeyCode.UP) {
                    double step = 10;
                    double dz = step * cos(toRadians(angleY));
                    double dx = step * sin(toRadians(angleY));
                    moveVector = moveVector.plus(new Vertex(dx, 0, dz));
                    visualizer.setMoveVector(moveVector);

                }
                if (event.getCode() == KeyCode.DOWN) {
                    double step = -10;
                    double dz = step * cos(toRadians(angleY));
                    double dx = step * sin(toRadians(angleY));
                    moveVector = moveVector.plus(new Vertex(dx, 0, dz));
                    visualizer.setMoveVector(moveVector);

                }
                if(event.getCode() == KeyCode.H) {
                    moveVector = moveVector.plus(new Vertex(0, 5, 0));
                    visualizer.setMoveVector(moveVector);
                }
                if(event.getCode() == KeyCode.N) {
                    moveVector = moveVector.plus(new Vertex(0, -5, 0));
                    visualizer.setMoveVector(moveVector);
                }
                if (event.getCode() == KeyCode.O) {
                    scene.getMesh().reset();
                    angleY = 0;
                    angleX = 0;
                    moveVector = new Vertex(0,0,0);
                    visualizer.setAngleY(angleY);
                    visualizer.setAngleX(angleX);
                    visualizer.setMoveVector(moveVector);
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
