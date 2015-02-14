package engine.model;

import engine.scene.Camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.toRadians;

public class Mesh {
    public Vertex[] originalVertices;
    public Face[] originalFaces;
    public List<Vertex> vertices;
    public List<Face> faces;

    public Mesh(Vertex[] vertices, Face[] face) {
        originalVertices = vertices;
        originalFaces = face;
    }

    public Vertex[] getVertices() {
        return vertices.toArray(new Vertex[vertices.size()]);
    }

    public Face[] getFaces() {
        return faces.toArray(new Face[faces.size()]);
    }

    public void alignWithCamera(Camera camera) {
        move(camera.getPosition());
        rotateY(camera.getAngleY());
        rotateX(camera.getAngleX());
    }

    private void rotateY(double a) {
        double ar = toRadians(a);

        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).isInsideCameraPyramid()) {
                vertices.set(i, vertices.get(i).rotateY(-ar));
            }
        }
    }

    private void rotateX(double a) {
        double ar = toRadians(a);

        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).isInsideCameraPyramid()) {
                vertices.set(i, vertices.get(i).rotateX(-ar));
            }
        }
    }

    private void move(Vertex moveVector) {
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).isInsideCameraPyramid()) {
                vertices.set(i, vertices.get(i).minus(moveVector));
            }
        }
    }


    public void cutByCameraPyramid(Camera camera) {
        Vertex[] cutPlanes = camera.getCutPlanes();

        faces = new ArrayList<>();
        for (Face face : originalFaces) {
            double d = -originalVertices[face.getVertexIndices()[0]].dot(face.normal);
            if (camera.getPosition().dot(face.getNormal()) + d > 0) {
                faces.add(face);
            }
        }
        vertices = new ArrayList<>(originalVertices.length);
        for (Vertex vertex : originalVertices) {
            vertices.add(new Vertex(vertex));
        }

        for (int i = 0; i < cutPlanes.length; i += 2) {
            cutByPlane(cutPlanes[i], cutPlanes[i + 1]);
        }
    }

    private void cutByPlane(Vertex n, Vertex o) {

        for (Vertex vertex : vertices) {
            if (vertex.minus(o).dot(n) < 0) {
                vertex.setBehindCamera(true);
            }
        }

        List<Face> tempFaces = new ArrayList<>();
        tempFaces.addAll(faces);
        faces = new ArrayList<>();
        for (Face face : tempFaces) {
            cutFace(n, o, face);
        }
    }

    private void cutFace(Vertex n, Vertex o, Face face) {
        int[] vertexIndices = face.getVertexIndices();
        boolean hasOutside = false;
        boolean hasInside = false;
        for (int vertexIndex : vertexIndices) {
            Vertex v = vertices.get(vertexIndex);
            hasOutside |= v.isOutsideCameraPyramid();
            hasInside |= v.isInsideCameraPyramid();
        }

        if (hasInside && !hasOutside) {
            faces.add(face);
        } else if (hasInside && hasOutside) {

            List<Integer> newVertexIndices = new ArrayList<>(20);
            for (int j = 0; j < vertexIndices.length; j++) {
                int vi1 = vertexIndices[j];
                int vi2 = vertexIndices[j < vertexIndices.length - 1 ? j + 1 : 0];
                Vertex v1 = vertices.get(vi1);
                Vertex v2 = vertices.get(vi2);

                if (!v1.isOutsideCameraPyramid()) {
                    newVertexIndices.add(vi1);
                }

                if (v1.isOutsideCameraPyramid() != v2.isOutsideCameraPyramid()) {
                    double k = n.dot(o.minus(v1)) / n.dot(v2.minus(v1));
                    Vertex v = v1.plus(v2.minus(v1).multiply(k));

                    vertices.add(v);

                    newVertexIndices.add(vertices.size() - 1);
                }

            }

            int[] newVertexIndicesForFace = new int[newVertexIndices.size()];
            for (int t = 0; t < newVertexIndices.size(); t++) newVertexIndicesForFace[t] = newVertexIndices.get(t);

            Face newFace = new Face(newVertexIndicesForFace);
            newFace.color = face.color;
            newFace.normal = face.normal;
            newFace.index = face.index;
            faces.add(newFace);
        }
    }

    public void reset() {
        this.vertices = new ArrayList<>(Arrays.asList(originalVertices));
        this.faces = new ArrayList<>(Arrays.asList(originalFaces));
    }

    public Mesh scale(double scale) {
        Vertex scaled[] = new Vertex[originalVertices.length];
        for (int i = 0; i < originalVertices.length; i++) {
            scaled[i] = originalVertices[i].multiply(scale);
        }
        return new Mesh(scaled, originalFaces);
    }

    public int getVisibleVerticesCount() {
        int count = 0;
        for (Vertex v : vertices) {
            if (v.isInsideCameraPyramid()) {
                count++;
            }
        }
        return count;
    }

    public Vertex[] getOriginalVertices() {
        return originalVertices;
    }

    public Face[] getOriginalFaces() {
        return originalFaces;
    }
}
