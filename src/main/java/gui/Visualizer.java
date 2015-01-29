package gui;

import engine.Mesh;
import engine.Scene;
import engine.Vertex;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class Visualizer {

    private final Scene scene;
    private double xSize = 800;
    private double ySize = 600;
    private double fov = 90;
    private double dist = xSize / 2 / Math.tan(fov * Math.PI / 360);
    private Pane root;

    public Visualizer(Scene scene, double xSize, double ySize, double fov) {
        this.scene = scene;
        this.xSize = xSize;
        this.ySize = ySize;
        this.fov = fov;
        dist = xSize / 2 / Math.tan(fov * Math.PI / 360);

    }

    public Pane createScenePane() {
        root = new Pane();
        return root;
    }

    public void drawScene() {
        root.getChildren().clear();
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
                Line screenLine = new Line(
                        startPoint.getX(), startPoint.getY(),
                        endPoint.getX(), endPoint.getY()
                );

                screenLine.setStroke(Color.WHITE);
                root.getChildren().add(screenLine);

//                drawHead(startPoint, endPoint);

                root.getChildren().add(textPointNumber(startPoint, pointIndex));
            }
        }
    }

    double phi = Math.toRadians(10);
    double barb = 20;

    private void drawHead(Vertex startPoint, Vertex endPoint) {

        Line head1 = new Line();
        Line head2 = new Line();
        head1.setStroke(Color.WHITE);
        head2.setStroke(Color.WHITE);

        double dx = endPoint.getX() - startPoint.getX();
        double dy = endPoint.getY() - startPoint.getY();
        double theta = Math.atan2(dy, dx);
        double x, y, rho = theta + phi;

        x = endPoint.getX() - barb * Math.cos(rho);
        y = endPoint.getY() - barb * Math.sin(rho);
        head1.setStartX(endPoint.getX());
        head1.setStartY(endPoint.getY());
        head1.setEndX(x);
        head1.setEndY(y);
        rho = theta - phi;
        x = endPoint.getX() - barb * Math.cos(rho);
        y = endPoint.getY() - barb * Math.sin(rho);
        head2.setStartX(endPoint.getX());
        head2.setStartY(endPoint.getY());
        head2.setEndX(x);
        head2.setEndY(y);
        root.getChildren().add(head1);
        root.getChildren().add(head2);
    }

    private Vertex toScreenPoint(Vertex vertex) {
        return new Vertex(
                xSize / 2 + vertex.getX() * dist / (vertex.getZ() + dist),
                ySize / 2 - vertex.getY() * dist / (vertex.getZ() + dist),
                0
        );
    }

    private Text textPointNumber(Vertex point, int pointIndex) {
        Text text = new Text(point.getX(), point.getY(), "" + pointIndex);
        text.setStroke(Color.GREEN);
        return text;
    }
}
