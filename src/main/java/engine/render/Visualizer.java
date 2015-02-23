package engine.render;

import engine.model.*;
import engine.scene.Camera;
import engine.scene.Player;
import engine.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;

import static java.lang.Math.round;

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

    private int buffer[];
    private int rowSize;
    private boolean drawWire;

    double zBuffer[];
    private Set<Integer> showOnlyFace = new HashSet<>();
    private boolean useZBuffer = true;
    private double mouseSceneX;
    private double mouseSceneY;
    private int intensityTable[][] = new int[256][256];

    public Visualizer(Scene scene, double xSize, double ySize, double fov) {
        this.scene = scene;
        setScreen(xSize, ySize, fov);

        for (int color = 0; color <= 255; color++) {
            for (int intensity = 0; intensity <= 255; intensity++) {
                intensityTable[color][intensity] = color * intensity / 255;
            }
        }

//        showOnlyFace.add(7);
//        showOnlyFace.add(21);

    }

    public void setScreen(double xSize, double ySize, double fov) {
        this.xSize = xSize;
        this.ySize = ySize;
        rowSize = (int) xSize;
        this.fov = fov;
        dist = xSize / 2 / Math.tan(Math.toRadians(fov / 2));
        buffer = new int[rowSize * (int) (ySize + 2)];
        zBuffer = new double[rowSize * (int) (ySize + 2)];

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
        if (gc != null) {
            gc.clearRect(0, 0, xSize, ySize);
        }

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
            System.out.printf("\rfps: %,4.0f", fps);
        }

        if (gc != null) {
            gc.fillText(String.format("FPS  : %,4.0f", fps), 10, 20);
            gc.fillText(String.format("HA   : %s", angleY), 10, 50);
            gc.fillText(String.format("VA   : %s", angleX), 10, 35);
            gc.fillText(String.format("pos  : %s", moveVector), 10, 65);
            gc.fillText(String.format("verts: %s", mesh.getVisibleVerticesCount()), 10, 80);
            gc.fillText(String.format("faces: %s", mesh.getFaces().size()), 10, 95);
            gc.fillText(String.format("mouse: %,3.0f, %,3.0f", mouseSceneX, mouseSceneY), 10, 110);
        }

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
        Arrays.fill(buffer, 0);
        Arrays.fill(zBuffer, 0);
        for (Face face : mesh.getFaces()) {
            if (showOnlyFace.isEmpty() || showOnlyFace.contains(face.index))
                drawFace(face);
        }
        if (pixelWriter != null) {
            pixelWriter.setPixels(0, 0, rowSize, (int) ySize, PixelFormat.getIntArgbInstance(), buffer, 0, rowSize);
        }
    }

    private void drawWire(Mesh mesh) {
        for (Face face : mesh.getFaces()) {
            if (showOnlyFace.isEmpty() || showOnlyFace.contains(face.index))
                drawFaceStroke(face);
        }
    }

    private void drawFace(Face face) {

        Intensity intensity = litColor(face);
//        ColorRGB faceColor = face.material.color;
//        ColorRGB color = new ColorRGB(
//                intensityTable[faceColor.red][intensity.red],
//                intensityTable[faceColor.green][intensity.green],
//                intensityTable[faceColor.blue][intensity.blue]
//        );

        int[] vertexIndices = face.getVertexIndices();

        List[] edgeList = new ArrayList[(int) ySize + 2];

        int minY = calcEdges(face, vertexIndices, edgeList);

        if (edgeList[minY] == null) return;

        List<Edge> activeEdges = new ArrayList<>();
        int y = minY;
        int yOffset = y * rowSize;
        do {
            nextRow(edgeList[y], activeEdges);

            drawLine(activeEdges, yOffset, face.material, intensity);

            y++;
            yOffset += rowSize;

        } while (y < xSize && (activeEdges.size() > 0 || edgeList[y] != null));

    }

    private Intensity litColor(Face face) {


        ColorRGB ambient = scene.getAmbient();
        ColorRGB sunColor = scene.getSunColor();

        Vertex cameraLightVector = scene.getCamera().getDirection().normalize().multiply(-1);

        Vertex sunVector = scene.getSunVector();
        float cosAngleToSun = (float) sunVector.dot(face.normal);
        float cosAngleToCamera = (float) cameraLightVector.dot(face.normal);
        ColorRGB cameraColor = new ColorRGB(100, 100, 100);

        ColorRGB lightColors[] = {ambient, sunColor, cameraColor};
        float cosAngleToLight[] = {1, cosAngleToSun, cosAngleToCamera};

        Intensity intensity = new Intensity();
        for (int i = 0; i < lightColors.length; i++) {
            float cosValue = cosAngleToLight[i] >= 0 ? cosAngleToLight[i] : 0;
            intensity.red += (float) lightColors[i].red * cosValue;
            intensity.green += (float) lightColors[i].green * cosValue;
            intensity.blue += (float) lightColors[i].blue * cosValue;
        }

        if (intensity.red > 255) intensity.red = 255;
        if (intensity.green > 255) intensity.green = 255;
        if (intensity.blue > 255) intensity.blue = 255;

        return intensity;
    }

    public static class Intensity {
        int red, green, blue;
    }

    private void drawLine(List<Edge> activeEdges, int yOffset, Material materal, Intensity intensity) {

        Iterator<Edge> activeEdgesIterator = activeEdges.iterator();
        ColorRGB color;
        Texture texture = null;
        if (materal.imageFile != null) {
            texture = scene.getTextureMap().get(materal.imageFile);
        }
        while (activeEdgesIterator.hasNext()) {
            Edge edge1 = activeEdgesIterator.next();
            if (edge1.dy <= 0) activeEdgesIterator.remove();

            Edge edge2 = activeEdgesIterator.next();
            if (edge2.dy <= 0) activeEdgesIterator.remove();

            int startX = (int) round(edge1.x);
            int buffX = yOffset + startX;
            int endBuffX = yOffset + (int) round(edge2.x);

            double w = edge1.w;
            double dwx = (edge2.w - edge1.w) / (edge2.x - edge1.x);

            double u = edge1.u, v = edge1.v;
            double dux = (edge2.u - edge1.u) / (edge2.x - edge1.x);
            double dvx = (edge2.v - edge1.v) / (edge2.x - edge1.x);


            if (endBuffX >= 0 && endBuffX < zBuffer.length && endBuffX < buffer.length) {
                while (buffX < endBuffX) {

                    if (edge1.tv1 != null) {
                        int tu = (int) round(u / w * texture.width) % texture.width;
                        int tv = (int) round(v / w * texture.height) % texture.height;

                        if(tu<0) tu = texture.width + tu;
                        if(tv<0) tv = texture.height + tv;

                        int red = texture.buffer[tv * texture.width * 3 + tu * 3] & 0xFF;
                        int green = texture.buffer[tv * texture.width * 3 + tu * 3 + 1] & 0xFF;
                        int blue = texture.buffer[tv * texture.width * 3 + tu * 3 + 2] & 0xFF;

                        color = new ColorRGB(
                                intensityTable[red][intensity.red],
                                intensityTable[green][intensity.green],
                                intensityTable[blue][intensity.blue]
                        );
//
//                        if ((int) (u / w * 10) % 10 == 0 || (int) (v / w * 20) % 10 == 0) {
//                            color = new ColorRGB(0, 0, 0);
//                        } else {
//                            color = new ColorRGB(
//                                    intensityTable[255][intensity.red],
//                                    intensityTable[255][intensity.green],
//                                    intensityTable[255][intensity.blue]
//                            );
//                        }
                    } else {
                        color = new ColorRGB(
                                intensityTable[materal.color.red][intensity.red],
                                intensityTable[materal.color.green][intensity.green],
                                intensityTable[materal.color.blue][intensity.blue]
                        );
                    }

                    if (w > zBuffer[buffX]) {
                        buffer[buffX] = color.value;
                        zBuffer[buffX] = w;
                    }
                    buffX++;
                    w += dwx;
                    u += dux;
                    v += dvx;
                }
            }

        }
    }

    private void nextRow(List activatedEdges, List<Edge> activeEdges) {
        if (activatedEdges != null) {
            activeEdges.addAll(activatedEdges);

        }
        for (Edge edge : activeEdges) {
            edge.nextY();
        }
        Collections.sort(activeEdges, (e1, e2) -> e1.x < e2.x ? -1 : 1);

    }

    private int calcEdges(Face face, int[] vertexIndices, List[] edgeList) {
        int minY = (int) ySize + 1;
        int n = vertexIndices.length;
        for (int i = 0; i < n; i++) {
            int v1i = vertexIndices[i];
            int v2i = vertexIndices[(i + 1) % n];

            Vertex v1 = screenPoints[v1i];
            Vertex v2 = screenPoints[v2i];


            Vertex tv1 = null, tv2 = null;
            if (face.hasTexCoord()) {
                tv1 = face.textCoord[i];
                tv2 = face.textCoord[(i + 1) % n];
            }

            if ((int) round(v1.y) - (int) round(v2.y) == 0) {
                continue;
            }

            Edge edge = calcEdge(v1, v2, tv1, tv2);
            edge.face = face;

            addToEdgeList(edgeList, edge);
            minY = Math.min(minY, edge.y);

        }
        return minY;
    }

    private Edge calcEdge(Vertex v1, Vertex v2, Vertex tv1, Vertex tv2) {
        if (v1.y < v2.y) {
            return new Edge(v1, v2, tv1, tv2, true);
        } else {
            return new Edge(v2, v1, tv2, tv1, false);
        }
    }

    private void addToEdgeList(List[] edgeList, Edge edge) {
        if (edgeList[edge.y] == null) {
            edgeList[edge.y] = new ArrayList<>();
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

    public Player getPlayer() {
        return new Player(moveVector, angleY, angleX, fov);
    }

    private void drawFaceStroke(Face face) {
        int[] pointIndices = face.getVertexIndices();
        for (int i = 0; i < pointIndices.length; i++) {

            int pointIndex = pointIndices[i];
            int nextPointIndex = i < pointIndices.length - 1 ? pointIndices[i + 1] : pointIndices[0];

            Vertex startPoint = screenPoints[pointIndex];
            Vertex endPoint = screenPoints[nextPointIndex];
            gc.strokeLine(
                    startPoint.x, startPoint.y,
                    endPoint.x, endPoint.y
            );

            if (showArrows) {
                drawHead(startPoint, endPoint);
            }
            if (showVertexNumber) {
                gc.fillText(String.valueOf(pointIndex), startPoint.x, startPoint.y);
            }
        }
    }

    double phi = Math.toRadians(10);
    double barb = 20;

    private void drawHead(Vertex startPoint, Vertex endPoint) {

        double dx = endPoint.x - startPoint.x;
        double dy = endPoint.y - startPoint.y;
        double theta = Math.atan2(dy, dx);
        double rho = theta + phi;

        double x1 = endPoint.x - barb * Math.cos(rho);
        double y1 = endPoint.y - barb * Math.sin(rho);
        gc.strokeLine(endPoint.x, endPoint.y, x1, y1);

        rho = theta - phi;
        double x2 = endPoint.x - barb * Math.cos(rho);
        double y2 = endPoint.y - barb * Math.sin(rho);
        gc.strokeLine(endPoint.x, endPoint.y, x2, y2);

        gc.strokeLine(x1, y1, x2, y2);
    }

    private Vertex toScreenPoint(Vertex vertex) {
        return new Vertex(
                xSize / 2 + vertex.x * dist / (vertex.z),
                ySize / 2 - vertex.y * dist / (vertex.z),
                1 / (vertex.z)
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

    public boolean isUseZBuffer() {
        return useZBuffer;
    }

    public void setUseZBuffer(boolean useZBuffer) {
        this.useZBuffer = useZBuffer;
    }
}
