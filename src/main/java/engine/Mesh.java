package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static engine.Mesh.Pair.pair;
import static java.lang.Math.*;

public class Mesh {
    public static final int ADD_AS_IS = 0;
    public static final int REMOVE = 1;
    public static final int CUT = 2;
    private Vertex[] originalVertices;
    private Face[] originalFaces;
    private List<Vertex> vertices;
    private List<Face> faces;
    private Triangle[] triangles = new Triangle[0];

    public Mesh(Vertex[] vertices, Face[] face) {
        originalVertices = Arrays.copyOf(vertices, vertices.length);
        originalFaces = Arrays.copyOf(face, face.length);

        this.vertices = new ArrayList<>(originalVertices.length);
        this.vertices.addAll(Arrays.asList(originalVertices));

        this.faces = new ArrayList<>();
        this.faces.addAll(Arrays.asList(originalFaces));
    }

    public Vertex[] getVertices() {
        return vertices.toArray(new Vertex[vertices.size()]);
    }

    public Face[] getFaces() {
        return faces.toArray(new Face[faces.size()]);
    }

    public void transform(Vertex moveVector, double rotateY, double rotateX) {
        vertices = new ArrayList<>(originalVertices.length);
        vertices.addAll(Arrays.asList(originalVertices));
        rotateX(rotateX);
        rotateY(rotateY);
        move(moveVector);
    }

    private void rotateY(double a) {
        double ar = toRadians(a);

        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, vertices.get(i).rotateY(ar));
        }
    }

    private void rotateX(double a) {
        double ar = toRadians(a);

        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, vertices.get(i).rotateX(ar));
        }
    }

    private void move(Vertex moveVector) {
        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, vertices.get(i).plus(moveVector));
        }
    }

    public void cutByCameraPyramid(double z) {
        faces = new ArrayList<>();
        double cutPlane = z + 1;

        for (Vertex vertex : vertices) {
            if (vertex.getZ() < cutPlane) {
                vertex.setBehindCamera(true);
            }
        }

        for (Triangle triangle : triangles) {
            cutFace(cutPlane, triangle.getFace());
        }

        for (Face face : originalFaces) {
            cutFace(cutPlane, face);
        }

    }

    private void cutFace(double cutPlane, Face face) {
        int[] vertexIndices = face.getVertexIndices();
        int transformMode = ADD_AS_IS;
        for (int j = 0; j < vertexIndices.length; j++) {
            Vertex v = vertices.get(vertexIndices[j]);
            if (v.isBehindCamera()) {
                if (j > 0 && transformMode == ADD_AS_IS) {
                    transformMode = CUT;
                    break;
                } else {
                    transformMode = REMOVE;
                }
            } else if (transformMode == REMOVE) {
                transformMode = CUT;
                break;
            }
        }

        if (ADD_AS_IS == transformMode) {
            faces.add(face);
        } else if (CUT == transformMode) {

            List<Integer> newVertexIndices = new ArrayList<>(20);
            for (int j = 0; j < vertexIndices.length; j++) {
                int vi1 = vertexIndices[j];
                int vi2 = vertexIndices[j < vertexIndices.length - 1 ? j + 1 : 0];
                Vertex v1 = vertices.get(vi1);
                Vertex v2 = vertices.get(vi2);

                if (!v1.isBehindCamera()) {
                    newVertexIndices.add(vi1);
                }

                if (v1.isBehindCamera() != v2.isBehindCamera()) {
                    double x = v1.getX() + (v2.getX() - v1.getX()) * (cutPlane - v1.getZ()) / (v2.getZ() - v1.getZ());
                    double y = v1.getY() + (v2.getY() - v1.getY()) * (cutPlane - v1.getZ()) / (v2.getZ() - v1.getZ());

                    vertices.add(new Vertex(x, y, cutPlane));

                    newVertexIndices.add(vertices.size() - 1);
                }

            }

            int[] newVertexIndicesForFace = new int[newVertexIndices.size()];
            for (int t = 0; t < newVertexIndices.size(); t++) newVertexIndicesForFace[t] = newVertexIndices.get(t);

            Face newFace = new Face(newVertexIndicesForFace);
            newFace.color = face.color;
            faces.add(newFace);
        }
    }

    public void reset() {
        this.vertices = new ArrayList<>(Arrays.asList(originalVertices));
        triangles = new Triangle[0];
    }

    public Mesh scale(double scale) {
        Vertex scaled[] = new Vertex[originalVertices.length];
        for (int i = 0; i < originalVertices.length; i++) {
            scaled[i] = originalVertices[i].multiply(scale);
        }
        return new Mesh(scaled, originalFaces);
    }

    public void triangulate() {
        List<Triangle> trianglesList = new ArrayList<>();

        for (Face face : originalFaces) {
            Vertex[] projXYPoints = getProjection(face, vertex -> new Vertex(vertex.getX(), vertex.getY(), 0));
            Vertex[] projXZPoints = getProjection(face, vertex -> new Vertex(vertex.getX(), vertex.getZ(), 0));
            Vertex[] projYZPoints = getProjection(face, vertex -> new Vertex(vertex.getY(), vertex.getZ(), 0));

            double xyProjectionArea = abs(IntStream.range(0, projXYPoints.length).mapToDouble(i -> projXYPoints[i].multiply(projXYPoints[i < projXYPoints.length - 1 ? i + 1 : 0]).getZ()).sum());
            double xzProjectionArea = abs(IntStream.range(0, projXZPoints.length).mapToDouble(i -> projXZPoints[i].multiply(projXZPoints[i < projXZPoints.length - 1 ? i + 1 : 0]).getZ()).sum());
            double yzProjectionArea = abs(IntStream.range(0, projYZPoints.length).mapToDouble(i -> projYZPoints[i].multiply(projYZPoints[i < projYZPoints.length - 1 ? i + 1 : 0]).getZ()).sum());

            Vertex[] projPoints;
            if (xyProjectionArea > xzProjectionArea && xyProjectionArea > yzProjectionArea) {
                projPoints = projXYPoints;
            } else if (xzProjectionArea > xyProjectionArea && xzProjectionArea > yzProjectionArea) {
                projPoints = projXZPoints;
            } else {
                projPoints = projYZPoints;
            }
            int minPointIndex = IntStream.range(0, projPoints.length).mapToObj(i -> pair(i, projPoints[i].getX())).min((p1, p2) -> p1.v < p2.v ? -1 : 1).get().i;

            double sign;
            {
                int nextPointIndex = minPointIndex < projPoints.length - 1 ? minPointIndex + 1 : 0;
                int prevPointIndex = minPointIndex > 0 ? minPointIndex - 1 : projPoints.length - 1;

                Vertex a = projPoints[nextPointIndex].minus(projPoints[minPointIndex]);
                Vertex b = projPoints[prevPointIndex].minus(projPoints[minPointIndex]);

                sign = signum(a.multiply(b).getZ());
            }

            List<Integer> pointIndices = IntStream.range(0, projPoints.length).mapToObj(Integer::valueOf).collect(Collectors.toList());


            int offset = 0;

            while (pointIndices.size() > 3) {
                Integer i1 = pointIndices.get(0 + offset);
                Integer i2 = pointIndices.get(1 + offset);
                Integer i3 = pointIndices.get(2 + offset);

                Vertex p1 = projPoints[i1];
                Vertex p2 = projPoints[i2];
                Vertex p3 = projPoints[i3];

                Vertex a = p3.minus(p2);
                Vertex b = p1.minus(p2);

                boolean found = true;
                if (signum(a.multiply(b).getZ()) == sign) {
                    TriangleHelper triangleHelper = new TriangleHelper(p1, p2, p3);
                    for (int pi : pointIndices) {
                        if (face.vertexIndices[i1] == face.vertexIndices[pi] ||
                                face.vertexIndices[i2] == face.vertexIndices[pi] ||
                                face.vertexIndices[i3] == face.vertexIndices[pi]) continue;
                        if (triangleHelper.insideTriangle(projPoints[pi])) {
                            found = false;
                            break;
                        }
                    }
                } else {
                    found = false;
                }

                if (found) {
                    trianglesList.add(new Triangle(face.vertexIndices[i1], face.vertexIndices[i2], face.vertexIndices[i3]));
                    pointIndices.remove(1 + offset);
                } else {
                    offset++;
                }
                if (offset > pointIndices.size() - 3) {
                    offset = 0;
                }

            }

            trianglesList.add(new Triangle(face.vertexIndices[pointIndices.get(0)], face.vertexIndices[pointIndices.get(1)], face.vertexIndices[pointIndices.get(2)]));
        }

        triangles = trianglesList.toArray(new Triangle[trianglesList.size()]);
    }

    public Vertex[] getProjection(Face face, Function<Vertex, Vertex> project) {
        Vertex projection[] = new Vertex[face.vertexIndices.length];
        for (int i = 0; i < face.vertexIndices.length; i++) {
            projection[i] = project.apply(originalVertices[face.vertexIndices[i]]);
        }
        return projection;
    }

    public Triangle[] getTriangles() {
        return triangles;
    }

    static class Pair {
        private int i;
        private double v;

        private Pair(int i, double v) {
            this.i = i;
            this.v = v;
        }


        public static Pair pair(int i, double v) {
            return new Pair(i, v);
        }
    }

    public static class Face {
        int vertexIndices[];
        public ColorRGB color = new ColorRGB(1, 1, 1);
        private boolean opened;

        public Face(int... vertexIndices) {
            this.vertexIndices = vertexIndices;
        }

        public int[] getVertexIndices() {
            return vertexIndices;
        }

        public Face opened() {
            opened = true;
            return this;
        }

        public boolean isOpened() {
            return opened;
        }
    }

    public static class Triangle {
        private int i1, i2, i3;

        public Triangle(int i1, int i2, int i3) {
            this.i1 = i1;
            this.i2 = i2;
            this.i3 = i3;
        }

        public int getI1() {
            return i1;
        }

        public int getI2() {
            return i2;
        }

        public int getI3() {
            return i3;
        }

        public Face getFace() {
            Face face = new Face(i1, i2, i3);
            face.color = new ColorRGB(.5, .5, .5);
            return face;
        }
    }

    public Vertex[] getOriginalVertices() {
        return originalVertices;
    }

    public Face[] getOriginalFaces() {
        return originalFaces;
    }
}
