package engine.builder;

import engine.model.Face;
import engine.model.Vertex;

public class MeshBuilder {

    public static Vertex vertex(double x, double y, double z) {
        return new Vertex(x, y, z);
    }

    public static Face face(int... vertexIndices) {
        return new Face(vertexIndices);
    }

    protected Face[] faces(Face... faces) {
        return faces;
    }

    protected Vertex[] vertices(Vertex... vertices) {
        return vertices;
    }
}
