package engine.render;

import engine.model.Face;
import engine.model.Mesh;
import engine.model.Vertex;
import engine.scene.Camera;
import engine.scene.Scene;
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

    double zBuffer[];
    private Set<Integer> showOnlyFace = new HashSet<>();
    private boolean useZBuffer = true;
    private double mouseSceneX;
    private double mouseSceneY;

    public Visualizer(Scene scene, double xSize, double ySize, double fov) {
        this.scene = scene;
        setScreen(xSize, ySize, fov);

//        showOnlyFace.add(7);
//        showOnlyFace.add(21);

    }

    public void setScreen(double xSize, double ySize, double fov) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.fov = fov;
        dist = xSize / 2 / tan(toRadians(fov / 2));
        buffer = new byte[(int) xSize * (int) (ySize + 2) * 3];
        rowSize = (int) xSize * 3;
        zBuffer = new double[(int) xSize * (int) (ySize + 2)];

        scene.setCamera(new Camera(xSize, ySize, fov));
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
        mesh.alignWithCamera(camera);
        drawMesh(mesh);

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
        gc.fillText(String.format("mouse: %,3.0f, %,3.0f", mouseSceneX, mouseSceneY), 10, 110);

    }

    private void drawMeshWire(Mesh mesh) {
        calculateProjection(mesh);
        drawWire(mesh);
    }

    private void drawMesh(Mesh mesh) {
        calculateProjection(mesh);

        drawFaces(mesh);

        if (drawWire) {
            drawWire(mesh);
        }
    }

    private void calculateProjection(Mesh mesh) {
        Vertex[] objVertices = mesh.getVertices();
        screenPoints = new Vertex[objVertices.length];

        for (int i = 0; i < objVertices.length; i++) {
            Vertex objVertex = objVertices[i];
            if (objVertex.isInsideCameraPyramid()) {
                screenPoints[i] = toScreenPoint(objVertex);
            }
        }
    }

    private void drawFaces(Mesh mesh) {
        Arrays.fill(buffer, (byte) 0);
        Arrays.fill(zBuffer, 0);
        for (Face face : mesh.getFaces()) {
            if (showOnlyFace.isEmpty() || showOnlyFace.contains(face.index))
                drawFace(face);
        }
        pixelWriter.setPixels(0, 0, (int) xSize, (int) ySize, PixelFormat.getByteRgbInstance(), buffer, 0, rowSize);
    }

    private void drawWire(Mesh mesh) {
        for (Face face : mesh.getFaces()) {
            if (showOnlyFace.isEmpty() || showOnlyFace.contains(face.index))
                drawFaceStroke(face);
        }
    }

    private void drawFace(Face face) {
        int[] vertexIndices = face.getVertexIndices();

        EdgeList[] edgeList = new EdgeList[(int) ySize + 2];

        List<ScreenEdge> edges = new ArrayList<>();

        int minY = (int) ySize + 1;
        int n = vertexIndices.length;
        for (int i = 0; i < n; i++) {
            int v1i = vertexIndices[i];
            int v2i = vertexIndices[(i + 1) % n];

            Vertex v1 = screenPoints[v1i];
            Vertex v2 = screenPoints[v2i];

            if ((int) round(v1.getY()) - (int) round(v2.getY()) == 0) {
                continue;
            }


            ScreenEdge edge = calcEdge(v1, v2);
            edge.face = face;

            addToEdgeList(edgeList, edge);
            edges.add(edge);
            minY = min(minY, edge.y);

        }

        if (edgeList[minY] == null) return;

        List<ScreenEdge> activeEdges = new LinkedList<>();
        int y = minY;
        int yOffset = y * rowSize;
        int zBufYOffset = y * (int) xSize;
        do {
            if (edgeList[y] != null) {
                activeEdges.addAll(edgeList[y].getAll());
            }
            int xLength = 0;
            for (ScreenEdge edge : activeEdges) {
                edge.nextX();
                edge.nextW();
                edge.dy--;
            }
            Collections.sort(activeEdges);

            for (int i = 0; i < activeEdges.size(); i += 2) {
                int startX = (int) round(activeEdges.get(i).x) * 3;
                int zBuffX = (int) round(activeEdges.get(i).x);
                int endX = (int) round(activeEdges.get(i + 1).x) * 3;

                double w = activeEdges.get(i).w;
                double dwx = (activeEdges.get(i + 1).w - activeEdges.get(i).w) / (activeEdges.get(i + 1).x - activeEdges.get(i).x);
                for (int x = startX; x < endX; x += 3) {
                    if (w > zBuffer[zBufYOffset + zBuffX] || !useZBuffer) {
                        buffer[yOffset + x + 0] = face.color.red;
                        buffer[yOffset + x + 1] = face.color.green;
                        buffer[yOffset + x + 2] = face.color.blue;

                        zBuffer[zBufYOffset + zBuffX] = w;
                    }
                    w += dwx;
                    zBuffX++;
                }
            }

            activeEdges.removeIf(edge -> edge.dy <= 0);

            y++;
            yOffset += rowSize;
            zBufYOffset += (int) xSize;

        } while (y < 600 && (activeEdges.size() > 0 || edgeList[y] != null));

    }

    private ScreenEdge calcEdge(Vertex v1, Vertex v2) {
        ScreenEdge edge;
        if (v1.getY() < v2.getY()) {
            edge = new ScreenEdge(v1, v2, true);
        } else {
            edge = new ScreenEdge(v2, v1, false);
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

    public void setShowOnlyFace(int showOnlyFace) {
        System.out.println("show face: " + showOnlyFace);
        this.showOnlyFace.clear();
        if (showOnlyFace >= 0) {
            this.showOnlyFace.add(showOnlyFace);
        }
    }

    public int getShowOnlyFace() {
        return showOnlyFace.isEmpty() ? -1 : showOnlyFace.iterator().next();
    }

    public void setMousePositionInfo(double sceneX, double sceneY) {

        this.mouseSceneX = sceneX;
        this.mouseSceneY = sceneY;
    }

    public class ScreenEdge implements Comparable<ScreenEdge> {
        private final Vertex v1;
        private final Vertex v2;
        int y;
        double x0, x;
        int dy;
        double dx;
        double w0, dw, w;
        boolean starting;
        public Face face;


        public ScreenEdge(Vertex v1, Vertex v2, boolean starting) {
            y = (int) round(v1.getY());
            x0 = v1.getX();
            dy = (int) round(v2.getY()) - (int) round(v1.getY());
            dx = (v2.getX() - v1.getX()) / (v2.getY() - v1.getY());
            w0 = v1.getZ();
            dw = (v2.getZ() - w0) / (v2.getY() - v1.getY());
            x = x0 - dx;
            w = w0 - dw;
            this.starting = starting;
            this.v1 = v1;
            this.v2 = v2;
        }

        public double nextX() {
            x += dx;
            return x;
        }

        public double nextW() {
            w += dw;
            return w;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("y=").append(y);
            sb.append(", dy=").append(dy);
            sb.append(", v1=").append(v1);
            sb.append(", v2=").append(v2);
            sb.append(", starting=").append(starting);
            sb.append(", x0=").append(x0);
            sb.append(", dx=").append(dx);
            sb.append(", x=").append(x);
            sb.append(", w0=").append(w0);
            sb.append(", dw=").append(dw);
            sb.append(", w=").append(w);
            sb.append(", face=").append(face.index);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public int compareTo(ScreenEdge o) {
            return x < o.x ? -1 : (x > o.x ? 1 : 0);
        }
    }

    private void drawFaceStroke(Face face) {
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
                xSize / 2 + vertex.getX() * dist / (vertex.getZ()),
                ySize / 2 - vertex.getY() * dist / (vertex.getZ()),
                1 / (vertex.getZ())
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

    public void setPosition(Vertex moveVector) {
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

        public ScreenEdge get(int index) {
            return edgeList.get(index);
        }
    }

    public boolean isUseZBuffer() {
        return useZBuffer;
    }

    public void setUseZBuffer(boolean useZBuffer) {
        this.useZBuffer = useZBuffer;
    }
}
