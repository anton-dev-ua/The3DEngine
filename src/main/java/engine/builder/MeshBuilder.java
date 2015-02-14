package engine.builder;

import engine.model.Mesh;
import engine.model.Vertex;

public class MeshBuilder {

    public static Vertex vertex(double x, double y, double z) {
        return new Vertex(x, y, z);
    }

    public static Mesh.Face face(int... vertexIndices) {
        return new Mesh.Face(vertexIndices);
    }

    protected Mesh.Face[] faces(Mesh.Face... faces) {
        return faces;
    }

    protected Vertex[] vertices(Vertex... vertices) {
        return vertices;
    }
}
