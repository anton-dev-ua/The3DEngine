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
import javafx.scene.input.ScrollEvent;
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


    private PlayerPosition predefinedPositions[] = {
            new PlayerPosition(new Vertex(0, 0, 0), 0, 0),
            new PlayerPosition(new Vertex(-667, 0, 2120), 171, 8),
            new PlayerPosition(new Vertex(-203.02, 0, 1247.92), -83, 21),
            new PlayerPosition(new Vertex(-667, 0, 2120), 152.5, 12.6),
            new PlayerPosition(new Vertex(37.17, 0, 330.50), 1.5, 7.2),
            new PlayerPosition(new Vertex(-617.83, 0, 2232.50), 171.5, 50.6),
            new PlayerPosition(new Vertex(-630.25, 0, 2172.95), 158.1, 24.7),
            new PlayerPosition(new Vertex(-32.43, 0, 1026.39), 5.2, 22.7),     //60
            new PlayerPosition(new Vertex(-32.43, 0, 1026.39), 0, 37.1),
            new PlayerPosition(new Vertex(-32.43, 0, 1026.39), 0.6, 37.3),
            new PlayerPosition(new Vertex(-523.73, 0, 2463.28), -170.8, 4.8),
            new PlayerPosition(new Vertex(-523.73, 0, 2463.28), -169.8, 10.1),
            new PlayerPosition(new Vertex(-618.36, 0, 2167.32), -179.7, -2),
            new PlayerPosition(new Vertex(-619.36, -120, 1977.32), -179.7, -2),
            new PlayerPosition(new Vertex(-102.31, -120, 1486.87), -389.7, -12),
    };
    int predefinedPosIndex = 0;

    private PlayerPosition pPosition = predefinedPositions[predefinedPosIndex];

    private boolean running = true;
    private double sensitivity = 0.2;
    private Robot robot;
    private int mouseY;
    private int mouseX;
    private boolean mouseCaptured;
    private double oldY;
    private double oldX;
    private long limitFps = 60;


    @Override
    public void start(Stage primaryStage) throws Exception {
        robot = new Robot();
        scene = new engine.Scene();
        scene.setCamera(new Camera(width, height, fov));

//        scene.setMesh(aCube().withEdgeLength(200).build());
//        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/room-with-corner-stand.dae").getFile()));
        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/few-rooms.dae").getFile()));

        visualizer = new Visualizer(scene, width, height, fov);
        visualizer.setAngleX((int) pPosition.verticalAngle);
        visualizer.setAngleY((int) pPosition.horizontalAngle);
        visualizer.setPosition(pPosition.position);

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
            long redrawSync = 1000000000 / limitFps;
            long lastTime = 0;
            while (running) {
                if (System.nanoTime() - lastTime > redrawSync) {
                    lastTime = System.nanoTime();
                    waitForDisplaying(visualizer::drawScene);
                }
            }
        }).start();
    }

    private void sutUpKeyHandlers(final Stage primaryStage) {
        primaryStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.LEFT) {
                    if (event.isShiftDown()) {
                        pPosition.horizontalAngle -= 0.1;
                    } else {
                        pPosition.horizontalAngle -= 5;
                    }
                    visualizer.setAngleY((int) (pPosition.horizontalAngle * 10) / 10.0);
                }
                if (event.getCode() == KeyCode.RIGHT) {
                    if (event.isShiftDown()) {
                        pPosition.horizontalAngle += 0.1;
                    } else {
                        pPosition.horizontalAngle += 5;
                    }
                    visualizer.setAngleY((int) (pPosition.horizontalAngle * 10) / 10.0);

                }
                if (event.getCode() == KeyCode.G) {
                    pPosition.verticalAngle -= 5;
                    visualizer.setAngleX((int) (pPosition.verticalAngle * 10) / 10.0);
                }
                if (event.getCode() == KeyCode.B) {
                    pPosition.verticalAngle += 5;
                    visualizer.setAngleX((int) (pPosition.verticalAngle * 10) / 10.0);

                }


                if (event.getCode() == KeyCode.W) {
                    double step = 10;
                    double dz = step * cos(toRadians(pPosition.horizontalAngle));
                    double dx = step * sin(toRadians(pPosition.horizontalAngle));
                    pPosition.position = pPosition.position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(pPosition.position);

                }
                if (event.getCode() == KeyCode.S) {
                    double step = -10;
                    double dz = step * cos(toRadians(pPosition.horizontalAngle));
                    double dx = step * sin(toRadians(pPosition.horizontalAngle));
                    pPosition.position = pPosition.position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(pPosition.position);

                }
                if (event.getCode() == KeyCode.A) {
                    double step = -10;
                    double dz = step * -sin(toRadians(pPosition.horizontalAngle));
                    double dx = step * cos(toRadians(pPosition.horizontalAngle));
                    pPosition.position = pPosition.position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(pPosition.position);

                }
                if (event.getCode() == KeyCode.D) {
                    double step = 10;
                    double dz = step * -sin(toRadians(pPosition.horizontalAngle));
                    double dx = step * cos(toRadians(pPosition.horizontalAngle));
                    pPosition.position = pPosition.position.plus(new Vertex(dx, 0, dz));
                    visualizer.setPosition(pPosition.position);

                }


                if (event.getCode() == KeyCode.H) {
                    pPosition.position = pPosition.position.plus(new Vertex(0, 5, 0));
                    visualizer.setPosition(pPosition.position);
                }
                if (event.getCode() == KeyCode.N) {
                    pPosition.position = pPosition.position.plus(new Vertex(0, -5, 0));
                    visualizer.setPosition(pPosition.position);
                }
                if (event.getCode() == KeyCode.O) {
                    scene.getMesh().reset();
                    pPosition.horizontalAngle = 0;
                    pPosition.verticalAngle = 0;
                    pPosition.position = new Vertex(0, 0, -400);
                    fov = 90;
                    visualizer.setScreen(width, height, fov);
                    visualizePosition();
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
                if (event.getCode() == KeyCode.Z) {
                    visualizer.setUseZBuffer(!visualizer.isUseZBuffer());
                }

                if (event.getCode() == KeyCode.P) {
                    predefinedPosIndex++;
                    if (predefinedPosIndex >= predefinedPositions.length) predefinedPosIndex = 0;
                    pPosition = predefinedPositions[predefinedPosIndex];
                    System.out.printf("predefined position: %s\n", predefinedPosIndex);
                    visualizePosition();
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
                    pPosition.horizontalAngle += deltaX * sensitivity;
                    pPosition.verticalAngle += deltaY * sensitivity;
                    visualizer.setAngleY((int) (pPosition.horizontalAngle * 10) / 10.0);
                    visualizer.setAngleX((int) (pPosition.verticalAngle * 10) / 10.0);
                    robot.mouseMove(mouseX, mouseY);
                }

                visualizer.setMousePositionInfo(event.getSceneX(), event.getSceneY());

            }
        });

        primaryStage.getScene().setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
//                System.out.println("scroll: "+event.getDeltaY());
                fov += event.getDeltaY() / 10;
                if (fov > 120) fov = 120;
                if (fov < 10) fov = 10;
                visualizer.setScreen(width, height, fov);
            }
        });
    }

    private void visualizePosition() {
        visualizer.setAngleY(pPosition.horizontalAngle);
        visualizer.setAngleX(pPosition.verticalAngle);
        visualizer.setPosition(pPosition.position);
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
