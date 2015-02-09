package gui;

import engine.Camera;
import engine.ColladaReader;
import engine.Vertex;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.util.concurrent.CountDownLatch;

import static java.lang.Math.*;

public class Main extends Application {

    private Visualizer visualizer;
    private int width = 800;
    private int height = 600;
    private double fov = 90;
    private engine.Scene scene;
    private Vertex position = new Vertex(0, 0, 0);
    private double angleX;
    private double angleY = 0;
    private boolean running = true;
    private double sensitivity = 0.2;
    private Robot robot;
    private int mouseY;
    private int mouseX;
    private boolean mouseCaptured;
    private double oldY;
    private double oldX;
    private long fps = 60;


    @Override
    public void start(Stage primaryStage) throws Exception {
        robot = new Robot();
        scene = new engine.Scene();
        scene.setCamera(new Camera(width, height, fov));

//        scene.setMesh(aCube().withEdgeLength(200).build());
//        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/room-with-corner-stand.dae").getFile()));
        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/few-rooms.dae").getFile()));

        visualizer = new Visualizer(scene, width, height, fov);
        visualizer.setAngleX((int) angleX);
        visualizer.setAngleY((int) angleY);
        visualizer.setPosition(position);

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
            long redrawSync = 1000000000 / fps;
            long lastTime = 0;
            while (running) {
                if (System.nanoTime() - lastTime > redrawSync) {
                    lastTime = System.nanoTime();
                    waitForDisplaying(visualizer::drawScene);
                }
                Thread.yield();
            }
        }).start();
    }

    private void sutUpKeyHandlers(final Stage primaryStage) {
        primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.LEFT) {
                    angleY -= 5;
                    visualizer.setAngleY((int) angleY);
                }
                if (event.getCode() == KeyCode.RIGHT) {
                    angleY += 5;
                    visualizer.setAngleY((int) angleY);

                }
                if (event.getCode() == KeyCode.G) {
                    angleX -= 5;
                    visualizer.setAngleX((int) angleX);
                }
                if (event.getCode() == KeyCode.B) {
                    angleX += 5;
                    visualizer.setAngleX((int) angleX);

                }


                if (event.getCode() == KeyCode.W) {
                    double step = 10;
                    double dz = step * cos(toRadians(angleY));
                    double dx = step * sin(toRadians(angleY));
                    position = position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(position);

                }
                if (event.getCode() == KeyCode.S) {
                    double step = -10;
                    double dz = step * cos(toRadians(angleY));
                    double dx = step * sin(toRadians(angleY));
                    position = position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(position);

                }
                if (event.getCode() == KeyCode.A) {
                    double step = -10;
                    double dz = step * -sin(toRadians(angleY));
                    double dx = step * cos(toRadians(angleY));
                    position = position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(position);

                }
                if (event.getCode() == KeyCode.D) {
                    double step = 10;
                    double dz = step * -sin(toRadians(angleY));
                    double dx = step * cos(toRadians(angleY));
                    position = position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(position);

                }


                if (event.getCode() == KeyCode.H) {
                    position = position.plus(new Vertex(0, 5, 0));
                    visualizer.setPosition(position);
                }
                if (event.getCode() == KeyCode.N) {
                    position = position.plus(new Vertex(0, -5, 0));
                    visualizer.setPosition(position);
                }
                if (event.getCode() == KeyCode.O) {
                    scene.getMesh().reset();
                    angleY = 0;
                    angleX = 0;
                    position = new Vertex(0, 0, 0);
                    visualizer.setAngleY(angleY);
                    visualizer.setAngleX(angleX);
                    visualizer.setPosition(position);
                }
                if (event.getCode() == KeyCode.T) {
                    scene.getMesh().triangulate();
                }
                if (event.getCode() == KeyCode.V) {
                    visualizer.setShowVertexNumber(!visualizer.isShowVertexNumber());
                }
                if (event.getCode() == KeyCode.F) {
                    visualizer.setShowArrows(!visualizer.isShowArrows());
                }

                if (event.getCode() == KeyCode.L) {
                    visualizer.setDrawWire(!visualizer.isDrawWire());
                }


                if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                    visualizer.setShowOnlyFace(visualizer.getShowOnlyFace() + 1);
                }

                if (event.getCode() == KeyCode.OPEN_BRACKET) {
                    visualizer.setShowOnlyFace(visualizer.getShowOnlyFace() - 1);
                }

                if (event.getCode() == KeyCode.Q) {
                    running = false;
                    primaryStage.close();
                }
            }
        });

        primaryStage.getScene().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!mouseCaptured) {
                    mouseY = (int) event.getScreenY();
                    mouseX = (int) event.getScreenX();
                    oldX = event.getScreenX();
                    oldY = event.getScreenY();
                    primaryStage.getScene().setCursor(new ImageCursor(new Image("empty_cursor.png")));
                    mouseCaptured = true;
                } else {
                    primaryStage.getScene().setCursor(Cursor.DEFAULT);
                    mouseCaptured = false;
                }

            }
        });
        primaryStage.getScene().setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (mouseCaptured) {
                    double newX = event.getScreenX();
                    double newY = event.getScreenY();
                    double deltaX = newX - oldX;
                    double deltaY = newY - oldY;
                    angleY += deltaX * sensitivity;
                    angleX += deltaY * sensitivity;
                    visualizer.setAngleY((int) angleY);
                    visualizer.setAngleX((int) angleX);
                    robot.mouseMove(mouseX, mouseY);
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
