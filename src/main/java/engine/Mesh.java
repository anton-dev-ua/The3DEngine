package engine;

import java.util.Arrays;

import static java.lang.Math.*;

public class Mesh {
    private Vertex[] vertices, originalVertices;
    private Face[] faces;

    public Mesh(Vertex[] vertices, Face[] face) {
        originalVertices = Arrays.copyOf(vertices, vertices.length);
        this.vertices = Arrays.copyOf(vertices, vertices.length);
        this.faces = face;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public Face[] getFaces() {
        return faces;
    }

    public void rotateY(double a) {
        double ar = toRadians(a);

        for (int i = 0; i < originalVertices.length; i++) {

            Vertex vertex = originalVertices[i];

            double x = vertex.getX() * cos(ar) + vertex.getZ() * sin(ar);
            double y = vertex.getY();
            double z = -vertex.getX() * sin(ar) + vertex.getZ() * cos(ar);
            vertices[i] = new Vertex(x, y, z);
        }
    }

    public void reset() {
        this.vertices = Arrays.copyOf(originalVertices, vertices.length);
    }

    public static class Face {
        int vertexIndices[];

        public Face(int... vertexIndices) {
            this.vertexIndices = vertexIndices;
        }

        public int[] getVertexIndices() {
            return vertexIndices;
        }
    }
}
