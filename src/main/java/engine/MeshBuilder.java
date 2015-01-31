package engine;

public class MeshBuilder {

    protected Vertex vertex(double x, double y, double z) {
        return new Vertex(x, y, z);
    }

    protected Mesh.Face face(int... vertexIndices) {
        return new Mesh.Face(vertexIndices);
    }

    protected Mesh.Face[] faces(Mesh.Face... faces) {
        return faces;
    }

    protected Vertex[] vertices(Vertex... vertices) {
        return vertices;
    }
}
