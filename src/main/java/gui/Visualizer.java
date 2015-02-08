package gui;

import engine.Camera;
import engine.Mesh;
import engine.Scene;
import engine.Vertex;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;

import static java.lang.Math.*;

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
    private Vertex moveVector = new Vertex(0, 0, 0);
    private double angleX;
    private Vertex[] screenPoints;
    private PixelWriter pixelWriter;

    private byte buffer[];
    private int rowSize;
    private boolean drawWire;

    public Visualizer(Scene scene, double xSize, double ySize, double fov) {
        this.scene = scene;
        this.xSize = xSize;
        this.ySize = ySize;
        this.fov = fov;
        dist = xSize / 2 / tan(toRadians(fov / 2));
        buffer = new byte[(int) xSize * (int) (ySize + 2) * 3];
        rowSize = (int) xSize * 3;

    }


    public Pane createScenePane() {
        Pane root = new Pane();
        Canvas canvas = new Canvas(xSize, ySize);
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
        gc.setFill(Color.LIGHTGREEN);
        gc.setFont(Font.font("Courier New"));
        pixelWriter = gc.getPixelWriter();
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
        gc.fillText(String.format("FPS  : %,4.0f", fps), 10, 20);
        gc.fillText(String.format("HA   : %s", angleY), 10, 50);
        gc.fillText(String.format("VA   : %s", angleX), 10, 35);
        gc.fillText(String.format("pos  : %s", moveVector), 10, 65);
        gc.fillText(String.format("verts: %s", mesh.getVisibleVerticesCount()), 10, 80);
        gc.fillText(String.format("faces: %s", mesh.getFaces().length), 10, 95);

    }

    private void drawMesh(Mesh mesh) {
        Vertex[] objVertices = mesh.getVertices();
        screenPoints = new Vertex[objVertices.length];

        for (int i = 0; i < objVertices.length; i++) {
            Vertex objVertex = objVertices[i];
            if (objVertex.isInsideCameraPyramid()) {
                screenPoints[i] = toScreenPoint(objVertex);
            }
        }

        Arrays.fill(buffer, (byte) 0);
        for (Mesh.Face face : mesh.getFaces()) {
            drawFace(face);
        }
        pixelWriter.setPixels(0, 0, (int) xSize, (int) ySize, PixelFormat.getByteRgbInstance(), buffer, 0, rowSize);

        if(drawWire) {
            for (Mesh.Face face : mesh.getFaces()) {
                drawFaceStroke(face);
            }
        }
    }

    private void drawFace(Mesh.Face face) {
        int[] vertexIndices = face.getVertexIndices();


        EdgeList[] edgeList = new EdgeList[(int) ySize + 2];

        int minY = (int) ySize + 1;
        int n = vertexIndices.length;
        for (int i = 0; i < n; i++) {
            int v1i = vertexIndices[i];
            int v2i = vertexIndices[(i + 1) % n];

            Vertex v1 = screenPoints[v1i];
            Vertex v2 = screenPoints[v2i];

            if (abs(v1.getY() - v2.getY()) < 1) continue;


            ScreenEdge edge = calcEdge(v1, v2);
            addToEdgeList(edgeList, edge);
            minY = min(minY, edge.y);

        }

        List<ScreenEdge> activeEdges = new LinkedList<>();
        double xlist[] = new double[10];
        int y = minY;
        int yOffset = y * rowSize;
        do {
            if (edgeList[y] != null) activeEdges.addAll(edgeList[y].getAll());
            int xsize = 0;
            for (ScreenEdge edge : activeEdges) {
                xlist[xsize++] = edge.nextX();
                edge.dy--;
            }
            Arrays.sort(xlist, 0, xsize);

            for (int i = 0; i < xsize; i += 2) {
                int startX = (int) xlist[i] * 3;
                int endX = (int) xlist[i + 1] * 3;
                for (int x = startX; x <= endX; x += 3) {
                    buffer[yOffset + x + 0] = face.color.red;
                    buffer[yOffset + x + 1] = face.color.green;
                    buffer[yOffset + x + 2] = face.color.blue;
                }
            }

            activeEdges.removeIf(edge -> edge.dy <= 0);

            y++;
            yOffset += rowSize;

        } while (activeEdges.size() > 0);

    }

    private ScreenEdge calcEdge(Vertex v1, Vertex v2) {
        ScreenEdge edge;
        if (v1.getY() < v2.getY()) {
            edge = new ScreenEdge(
                    (int) round(v1.getY()),
                    v1.getX(),
                    (int) round(v2.getY() - v1.getY()),
                    (v2.getX() - v1.getX()) / (v2.getY() - v1.getY()),
                    true
            );
        } else {
            edge = new ScreenEdge(
                    (int) round(v2.getY()),
                    v2.getX(),
                    (int) round(v1.getY() - v2.getY()),
                    (v1.getX() - v2.getX()) / (v1.getY() - v2.getY()),
                    false
            );
        }
        return edge;
    }

    private void addToEdgeList(EdgeList[] edgeList, ScreenEdge edge) {
        if (edgeList[edge.y] == null) {
            edgeList[edge.y] = new EdgeList();
        }
        edgeList[edge.y].add(edge);
    }

    public void setDrawWire(boolean drawWire) {
        this.drawWire = drawWire;
    }

    public boolean isDrawWire() {
        return drawWire;
    }

    public class ScreenEdge {
        int y;
        double x0, x;
        int dy;
        double dx;
        boolean starting;

        public ScreenEdge(int y, double x0, int dy, double dx, boolean starting) {
            this.y = y;
            this.x0 = x0;
            x = x0 - dx;
            this.dy = dy;
            this.dx = dx;
            this.starting = starting;
        }

        public double nextX() {
            x += dx;
            return x;
        }
    }

    private void drawFaceStroke(Mesh.Face face) {
//        gc.setStroke(new Color(face.color.red, face.color.green, face.color.blue, 1));
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
                vertex.getZ()
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

    public static class EdgeList implements Iterable {
        List<ScreenEdge> edgeList = new ArrayList<>();

        public void add(ScreenEdge edge) {
            edgeList.add(edge);
        }


        @Override
        public Iterator<ScreenEdge> iterator() {
            return edgeList.iterator();
        }

        public int size() {
            return edgeList.size();
        }

        public Collection<? extends ScreenEdge> getAll() {
            return edgeList;
        }
    }
}
