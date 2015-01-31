package gui;

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

        Vertex[] objVertices = mesh.getVertices();
        Vertex[] screenPoints = new Vertex[objVertices.length];

        for (int i = 0; i < objVertices.length; i++) {
            Vertex objVertex = objVertices[i];
            screenPoints[i] = toScreenPoint(objVertex);
        }

        for (Mesh.Face face : mesh.getFaces()) {
            int[] pointIndices = face.getVertexIndices();
            for (int i = 0; i < pointIndices.length; i++) {

                int pointIndex = pointIndices[i];
                int nextPointIndex = i < pointIndices.length - 1 ? pointIndices[i + 1] : pointIndices[0];

                Vertex startPoint = screenPoints[pointIndex];
                Vertex endPoint = screenPoints[nextPointIndex];
                gc.strokeLine(
                        startPoint.getX(), startPoint.getY(),
                        endPoint.getX(), endPoint.getY()
                );

                if(showArrows) {
                    drawHead(startPoint, endPoint);
                }
                if(showVertexNumber) {
                    gc.fillText(String.valueOf(pointIndex), startPoint.getX(), startPoint.getY());
                }
            }
        }

        frame++;

        if ((spentTime = System.currentTimeMillis() - startTime) > 1000) {
            System.out.printf("\rFPS: %,4.0f", (double) (frame * 1000) / spentTime);
            frame = 0;
            startTime = System.currentTimeMillis();
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
}
