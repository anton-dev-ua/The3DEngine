package engine;

public class Mesh {
    private Vertex[] vertices;
    private Face[] faces;

    public Mesh(Vertex[] vertices, Face[] face) {
        this.vertices = vertices;
        this.faces = face;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public Face[] getFaces() {
        return faces;
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
