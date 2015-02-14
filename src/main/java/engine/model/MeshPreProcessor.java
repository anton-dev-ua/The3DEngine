package engine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static engine.model.Pair.pair;
import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class MeshPreProcessor {
    public static final int LESS_180 = 1;
    public static final int GREATER_180 = -1;

    public static void calculateNormals(Mesh mesh) {
        for (Face face : mesh.originalFaces) {
            {
                Vertex[] projXYPoints = getProjection(mesh.originalVertices, face, vertex -> new Vertex(vertex.getX(), vertex.getY(), 0));
                Vertex[] projXZPoints = getProjection(mesh.originalVertices, face, vertex -> new Vertex(vertex.getX(), vertex.getZ(), 0));
                Vertex[] projYZPoints = getProjection(mesh.originalVertices, face, vertex -> new Vertex(vertex.getY(), vertex.getZ(), 0));

                double xyProjectionArea = abs(IntStream.range(0, projXYPoints.length).mapToDouble(i -> projXYPoints[i].cross(projXYPoints[i < projXYPoints.length - 1 ? i + 1 : 0]).getZ()).sum());
                double xzProjectionArea = abs(IntStream.range(0, projXZPoints.length).mapToDouble(i -> projXZPoints[i].cross(projXZPoints[i < projXZPoints.length - 1 ? i + 1 : 0]).getZ()).sum());
                double yzProjectionArea = abs(IntStream.range(0, projYZPoints.length).mapToDouble(i -> projYZPoints[i].cross(projYZPoints[i < projYZPoints.length - 1 ? i + 1 : 0]).getZ()).sum());

                Vertex[] projPoints;
                if (xyProjectionArea > xzProjectionArea && xyProjectionArea > yzProjectionArea) {
                    projPoints = projXYPoints;
                } else if (xzProjectionArea > xyProjectionArea && xzProjectionArea > yzProjectionArea) {
                    projPoints = projXZPoints;
                } else {
                    projPoints = projYZPoints;
                }
                int minPointIndex = IntStream.range(0, projPoints.length).mapToObj(i -> pair(i, projPoints[i].getX())).min((p1, p2) -> p1.v < p2.v ? -1 : 1).get().i;

                int nextPointIndex = minPointIndex < projPoints.length - 1 ? minPointIndex + 1 : 0;
                int prevPointIndex = minPointIndex > 0 ? minPointIndex - 1 : projPoints.length - 1;

                Vertex v3 = mesh.originalVertices[face.getVertexIndices()[nextPointIndex]];
                Vertex v2 = mesh.originalVertices[face.getVertexIndices()[minPointIndex]];
                Vertex v1 = mesh.originalVertices[face.getVertexIndices()[prevPointIndex]];
                Vertex a = v1.minus(v2);
                Vertex b = v3.minus(v2);

                Vertex normal = a.cross(b).normalize();
                face.setNormal(normal);
            }


            List<Integer> vertexTypes = new ArrayList<>();
            int[] vertexIndices = face.getVertexIndices();
            for (int i = 0; i < vertexIndices.length; i++) {
                int nextI = i < vertexIndices.length - 1 ? i + 1 : 0;
                int prevI = i > 0 ? i - 1 : vertexIndices.length - 1;

                Vertex v1 = mesh.originalVertices[vertexIndices[prevI]];
                Vertex v2 = mesh.originalVertices[vertexIndices[i]];
                Vertex v3 = mesh.originalVertices[vertexIndices[nextI]];

                Vertex a = v1.minus(v2);
                Vertex b = v3.minus(v2);

                Vertex check = a.cross(b);
                Vertex normal = face.getNormal();
                vertexTypes.add(signum(check.dot(normal)) < 0 ? GREATER_180 : LESS_180);

            }

            face.vertexTypes = vertexTypes;
        }


    }

    static List<Face> triangulateFace(Mesh mesh, Face face) {
        List<Face> faceTrianglesList = new ArrayList<>();
        Vertex[] projXYPoints = getProjection(mesh.originalVertices, face, vertex -> new Vertex(vertex.getX(), vertex.getY(), 0));
        Vertex[] projXZPoints = getProjection(mesh.originalVertices, face, vertex -> new Vertex(vertex.getX(), vertex.getZ(), 0));
        Vertex[] projYZPoints = getProjection(mesh.originalVertices, face, vertex -> new Vertex(vertex.getY(), vertex.getZ(), 0));

        double xyProjectionArea = abs(IntStream.range(0, projXYPoints.length).mapToDouble(i -> projXYPoints[i].cross(projXYPoints[i < projXYPoints.length - 1 ? i + 1 : 0]).getZ()).sum());
        double xzProjectionArea = abs(IntStream.range(0, projXZPoints.length).mapToDouble(i -> projXZPoints[i].cross(projXZPoints[i < projXZPoints.length - 1 ? i + 1 : 0]).getZ()).sum());
        double yzProjectionArea = abs(IntStream.range(0, projYZPoints.length).mapToDouble(i -> projYZPoints[i].cross(projYZPoints[i < projYZPoints.length - 1 ? i + 1 : 0]).getZ()).sum());

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

            sign = signum(a.cross(b).getZ());
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
            if (signum(a.cross(b).getZ()) == sign) {
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
                Face triangle = new Face(face.vertexIndices[i1], face.vertexIndices[i2], face.vertexIndices[i3]);
                triangle.normal = face.normal;
                triangle.color = face.color;
                triangle.index = face.index;
                faceTrianglesList.add(triangle);
                pointIndices.remove(1 + offset);
            } else {
                offset++;
            }
            if (offset > pointIndices.size() - 3) {
                offset = 0;
            }

        }

        Face triangle = new Face(face.vertexIndices[pointIndices.get(0)], face.vertexIndices[pointIndices.get(1)], face.vertexIndices[pointIndices.get(2)]);
        triangle.normal = face.normal;
        triangle.color = face.color;
        triangle.index = face.index;
        faceTrianglesList.add(triangle);
        return faceTrianglesList;
    }

    public static Face[] triangulate(Mesh mesh) {
        List<Face> trianglesList = new ArrayList<>();

        for (Face face : mesh.originalFaces) {
            trianglesList.addAll(triangulateFace(mesh, face));
        }

        return trianglesList.toArray(new Face[trianglesList.size()]);
    }

    public static Vertex[] getProjection(Vertex[] originalVertices, Face face, Function<Vertex, Vertex> project) {
        Vertex projection[] = new Vertex[face.vertexIndices.length];
        for (int i = 0; i < face.vertexIndices.length; i++) {
            projection[i] = project.apply(originalVertices[face.vertexIndices[i]]);
        }
        return projection;
    }
}
