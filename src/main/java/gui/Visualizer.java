package gui;

import engine.Cube;
import engine.Point;
import engine.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import sample.ScreenPoint;

public class Visualizer {

    private final Scene scene;
    private double xSize = 800;
    private double ySize = 600;
    private double fov = 90;
    private double dist = xSize / 2 / Math.tan(fov * Math.PI / 360);
    private Pane root;

    public Visualizer(double xSize, double ySize, double fov) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.fov = fov;
        dist = xSize / 2 / Math.tan(fov * Math.PI / 360);
        scene = new Scene();
        scene.setObject(new Cube(200));
    }

    public Pane createScenePane() {
        root = new Pane();
        return root;
    }

    public void drawScene() {
        root.getChildren().clear();
        engine.Object object = scene.getObject();

        Point[] objPoints = object.getPoints();
        Point[] screenPoints = new Point[objPoints.length];

        for (int i = 0; i < objPoints.length; i++) {
            Point objPoint = objPoints[i];
            screenPoints[i] = toScreenPoint(objPoint);
        }

        for (engine.Object.Face face : object.getFaces()) {
            int[] pointIndices = face.getPointIndices();
            for (int i = 0; i < pointIndices.length; i++) {

                int pointIndex = pointIndices[i];
                int nextPointIndex = i < pointIndices.length - 1 ? pointIndices[i + 1] : pointIndices[0];

                Point startPoint = screenPoints[pointIndex];
                Point endPoint = screenPoints[nextPointIndex];
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

    private void drawHead(Point startPoint, Point endPoint) {

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

    private Point toScreenPoint(Point point) {
        return new Point(
                xSize / 2 + point.getX() * dist / (point.getZ() + dist),
                ySize / 2 - point.getY() * dist / (point.getZ() + dist),
                0
        );
    }

    private Text textPointNumber(Point point, int pointIndex) {
        Text text = new Text(point.getX(), point.getY(), "" + pointIndex);
        text.setStroke(Color.GREEN);
        return text;
    }
}
