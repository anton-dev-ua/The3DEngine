package gui;

import engine.Camera;
import engine.Mesh;
import engine.Scene;
import engine.Vertex;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

public class Visualizer {

    private final Scene scene;
    private double xSize;
    private double ySize;
    private double fov;
    private double dist;
    private GraphicsContext gc;

    private boolean showVertexNumber;
    private boolean showArrows;
    private double angleY = 0;
    private double fps;
    private Vertex moveVector = new Vertex(0,0,0);
    private double angleX;

    public Visualizer(Scene scene, double xSize, double ySize, double fov) {
        this.scene = scene;
        this.xSize = xSize;
        this.ySize = ySize;
        this.fov = fov;
        dist = xSize / 2 / tan(toRadians(fov / 2));

    }

    public Pane createScenePane() {
        Pane root = new Pane();
        Canvas canvas = new Canvas(xSize, ySize);
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
        gc.setFill(Color.GREEN);
        root.getChildren().add(canvas);
        return root;
    }

    long frame = 0;
    long startTime;
    long spentTime;

    public void drawScene() {
        gc.clearRect(0, 0, xSize, ySize);

        Mesh mesh = scene.getMesh();
        Camera camera = scene.getCamera().transform(moveVector, angleY, angleX);
        mesh.reset();
        mesh.cutByCameraPyramid(camera);
        mesh.alignWithCamera(camera.getPosition(), moveVector, angleY, angleX);
        drawMesh(mesh);

//        Mesh cameraMesh = camera.getMesh();
//        cameraMesh.transform(new Vertex(0,0,0), camera.getPosition().plus(new Vertex(0,0,dist)), 0,0);
//        cameraMesh.cutByCameraPyramid(-dist);
//        drawMesh(cameraMesh);

        frame++;

        if ((spentTime = System.currentTimeMillis() - startTime) > 1000) {
            fps = (double) (frame * 1000) / spentTime;
            frame = 0;
            startTime = System.currentTimeMillis();
        }
        gc.fillText(String.format("FPS: %,4.0f", fps), 10, 20);
        gc.fillText(String.format("x angle: %s", angleX), 10, 35);
        gc.fillText(String.format("y angle: %s", angleY), 10, 50);
        gc.fillText(String.format("move: %s", moveVector), 10, 65);

    }

    private void drawMesh(Mesh mesh) {
        Vertex[] objVertices = mesh.getVertices();
        Vertex[] screenPoints = new Vertex[objVertices.length];

        for (int i = 0; i < objVertices.length; i++) {
            Vertex objVertex = objVertices[i];
            screenPoints[i] = toScreenPoint(objVertex);
        }

        for (Mesh.Face face : mesh.getFaces()) {
            gc.setStroke(new Color(face.color.red, face.color.green, face.color.blue, 1));
            int[] pointIndices = face.getVertexIndices();
            for (int i = 0; i < pointIndices.length - (face.isOpened() ? 1 : 0); i++) {

                int pointIndex = pointIndices[i];
                int nextPointIndex = i < pointIndices.length - 1 ? pointIndices[i + 1] : pointIndices[0];

                Vertex startPoint = screenPoints[pointIndex];
                Vertex endPoint = screenPoints[nextPointIndex];
                gc.strokeLine(
                        startPoint.getX(), startPoint.getY(),
                        endPoint.getX(), endPoint.getY()
                );

                if (showArrows) {
                    drawHead(startPoint, endPoint);
                }
                if (showVertexNumber) {
                    gc.fillText(String.valueOf(pointIndex), startPoint.getX(), startPoint.getY());
                }
            }
        }
    }

    double phi = toRadians(10);
    double barb = 20;

    private void drawHead(Vertex startPoint, Vertex endPoint) {

        double dx = endPoint.getX() - startPoint.getX();
        double dy = endPoint.getY() - startPoint.getY();
        double theta = Math.atan2(dy, dx);
        double rho = theta + phi;

        double x1 = endPoint.getX() - barb * Math.cos(rho);
        double y1 = endPoint.getY() - barb * Math.sin(rho);
        gc.strokeLine(endPoint.getX(), endPoint.getY(), x1, y1);

        rho = theta - phi;
        double x2 = endPoint.getX() - barb * Math.cos(rho);
        double y2 = endPoint.getY() - barb * Math.sin(rho);
        gc.strokeLine(endPoint.getX(), endPoint.getY(), x2, y2);

        gc.strokeLine(x1, y1, x2, y2);
    }

    private Vertex toScreenPoint(Vertex vertex) {
        return new Vertex(
                xSize / 2 + vertex.getX() * dist / (vertex.getZ() + dist),
                ySize / 2 - vertex.getY() * dist / (vertex.getZ() + dist),
                0
        );
    }

    public boolean isShowVertexNumber() {
        return showVertexNumber;
    }

    public void setShowVertexNumber(boolean showVertexNumber) {
        this.showVertexNumber = showVertexNumber;
    }

    public boolean isShowArrows() {
        return showArrows;
    }

    public void setShowArrows(boolean showArrows) {
        this.showArrows = showArrows;
    }

    public void setAngleY(double angleY) {
        this.angleY = angleY;
    }

    public void setMoveVector(Vertex moveVector) {
        this.moveVector = moveVector;
    }

    public void setAngleX(double angleX) {
        this.angleX = angleX;
    }
}
