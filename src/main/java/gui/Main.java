package gui;

import engine.model.MeshPreProcessor;
import engine.model.Vertex;
import engine.render.Visualizer;
import engine.scene.Camera;
import engine.scene.Player;
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
import util.ColladaReader;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.lang.Math.*;

public class Main extends Application {

    public static final String PATH_TXT = "path.txt";
    private Visualizer visualizer;
    private int width = 800;
    private int height = 600;
    private engine.scene.Scene scene;

    private long limitFps = 120;

    private Player predefinedPositions[] = {
            new Player(new Vertex(0, 0, -400), 0, 0, 90),
            new Player(new Vertex(27.136970204695142, 0.0, -293.0038611309138), 7.400000000000007, 3.600000000000026, 90.0),
            new Player(new Vertex(27.136970204695142, 0.0, -293.0038611309138), 5.200000000000003, 2.400000000000026, 90.0),
            new Player(new Vertex(-647.0700080776569, 0.0, 1037.4655184934118), 123.80000000000037, 16.799999999999997, 90.0),

            new Player(new Vertex(-640.1065836836125, 0.0, 849.642890192125), 110.00000000000044, 18.999999999999996, 90.0),
            new Player(new Vertex(-620.6425173683409, 0.0, 938.0072501898347), 116.60000000000043, 20.59999999999989, 90.0),
            new Player(new Vertex(-107.52776544268067, 0.0, 713.1287569393371), 151.9999999999993, 3.399999999999917, 90.0),
            new Player(new Vertex(-184.63111976815813, 0.0, 894.0414400821707), -27.29999999999987, 7.799999999999988, 90.0),
            new Player(new Vertex(-228.0832943613, 0.0, 844.9915097168952), -7.299999999999869, 7.799999999999988, 90.0),
            new Player(new Vertex(-650.2050378343187, 0.0, 840.6246662007345), 81.80000000000018, 0.0, 90.0),
            new Player(new Vertex(-648.1600773159007, 0.0, 850.4133400883513), 101.80000000000018, 0.0, 90.0),
            new Player(new Vertex(-44.56121611459678, 0.0, 690.9253423771717), -22.999999999999954, 6.3999999999999995, 90.0),
            new Player(new Vertex(-648.5168381350969, 0.0, 1714.643637871075), 158.20000000000024, 25.399999999999963, 90.0),
            new Player(new Vertex(-649.8395924009379, 0.0, 1125.150437875747), 179.19999999999973, 22.59999999999994, 90.0),
            new Player(new Vertex(-649.6999705975464, 0.0, 1115.151412635654), 179.19999999999973, 22.59999999999994, 90.0),
            new Player(new Vertex(-69.52136475844637, 0.0, 643.4631916585137), 156.9999999999993, 18.399999999999917, 90.0),
            new Player(new Vertex(-69.52136475844637, 0.0, 643.4631916585137), 157.39999999999915, 15.999999999999913, 90.0),
            new Player(new Vertex(-36.146648750840626, 0.0, -103.80299001267035), 346.5999999999988, 16.599999999999874, 90.0),
            new Player(new Vertex(-6.405199980249866, 0.0, 354.4009287278233), 358.39999999999867, 29.79999999999986, 48.0),
            new Player(new Vertex(-6.405199980249866, 0.0, 354.4009287278233), 358.9999999999985, 28.799999999999862, 48.0),
            new Player(new Vertex(-6.405199980249866, 0.0, 354.4009287278233), 358.59999999999854, 28.999999999999872, 48.0),
            new Player(new Vertex(-184.63111976815813, 0.0, 894.0414400821707), -32.29999999999987, 7.799999999999988, 90.0),
    };
    int predefinedPosIndex = 0;

    private Player pPosition = predefinedPositions[predefinedPosIndex];

    private boolean running = true;
    private double sensitivity = 0.2;
    private Robot robot;
    private int mouseY;
    private int mouseX;
    private boolean mouseCaptured;
    private double oldY;
    private double oldX;
    private PrintWriter pathWriter;
    private boolean recording;
    private int pathIndex;
    private List<Player> path;
    private boolean playing;


    @Override
    public void start(Stage primaryStage) throws Exception {
        robot = new Robot();
        scene = new engine.scene.Scene();
        scene.setCamera(new Camera(width, height, pPosition.fov));

//        scene.setMesh(aCube().withEdgeLength(200).build());
//        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/room-with-corner-stand.dae").getFile()));
//        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/few-rooms.dae").getFile()));
        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/few-rooms-colored.dae").getFile()));

        visualizer = new Visualizer(scene, width, height, pPosition.fov);
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

//            startRecording();
//            startPlaying();

            while (running) {
                if (System.nanoTime() - lastTime > redrawSync) {
                    lastTime = System.nanoTime();

                    recording();
                    play();

                    waitForDisplaying(visualizer::drawScene);
                }

                try {
                    long sleepTime = redrawSync / 1000000 - (System.currentTimeMillis() - lastTime / 1000000);
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            stopRecording();
            stopPlaying();

        }).start();
    }

    private void stopPlaying() {
        playing = false;
    }

    private void play() {
        if (playing && path.size() > 0) {
            pPosition = path.get(pathIndex);
            pathIndex++;
            if (pathIndex >= path.size()) pathIndex = 0;
            applyNewPosition();
        }
    }

    private void startPlaying() {
        stopRecording();
        pathIndex = 0;
        path = new ArrayList<>();
        try {
            File fileR = new File(PATH_TXT);
            BufferedReader br = new BufferedReader(new FileReader(fileR));
            String line = br.readLine();
            while (line != null) {
                String[] nums = line.split(",");
                path.add(new Player(new Vertex(Double.valueOf(nums[0]), Double.valueOf(nums[1]), Double.valueOf(nums[2])),
                        Double.valueOf(nums[3]), Double.valueOf(nums[4]), Double.valueOf(nums[5])));
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        playing = true;
    }

    private void stopRecording() {
        if (recording && pathWriter != null) {
            pathWriter.close();
            recording = false;
        }
    }

    private void recording() {
        if (recording && pathWriter != null) {
            pathWriter.println(visualizer.getPlayer().getDataString());
        }
    }

    private void startRecording() {
        stopPlaying();
        File fileW = new File(PATH_TXT);
        pathWriter = null;
        try {
            pathWriter = new PrintWriter(fileW);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        recording = true;
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
                    pPosition.fov = 90;
                    applyNewPosition();
                }
                if (event.getCode() == KeyCode.T) {
                    scene.getMesh().originalFaces = MeshPreProcessor.triangulate(scene.getMesh());
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

                if (event.getCode() == KeyCode.P && !event.isShortcutDown()) {
                    predefinedPosIndex++;
                    if (predefinedPosIndex >= predefinedPositions.length) predefinedPosIndex = 0;
                    pPosition = predefinedPositions[predefinedPosIndex];
                    System.out.printf("predefined position: %s\n", predefinedPosIndex);
                    applyNewPosition();
                }

                if (event.getCode() == KeyCode.I) {
                    System.out.println("new PlayerPosition(new Vertex(" + pPosition.position.x + ", " + pPosition.position.y + ", " + pPosition.position.z + "), " + pPosition.horizontalAngle + ", " + pPosition.verticalAngle + ", " + pPosition.fov + "),");
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


                if(event.getCode() == KeyCode.P && event.isShortcutDown()) {
                    startPlaying();
                }

                if(event.getCode() == KeyCode.ESCAPE) {
                    stopPlaying();
                    stopRecording();
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
                pPosition.fov += event.getDeltaY() / 10;
                if (pPosition.fov > 120) pPosition.fov = 120;
                if (pPosition.fov < 10) pPosition.fov = 10;
                visualizer.setScreen(width, height, pPosition.fov);
            }
        });
    }

    private void applyNewPosition() {
        visualizer.setScreen(width, height, pPosition.fov);
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
