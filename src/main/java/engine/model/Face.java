package engine.model;

import java.util.List;

public class Face {
    public int vertexIndices[];
    public Material material = new Material(new ColorRGB((byte) 255, (byte) 255, (byte) 255), null);
    public Vertex normal;
    public int index;
    public List<Integer> vertexTypes;
    public Vertex[] textureCoord;

    public Face(int... vertexIndices) {
        this.vertexIndices = vertexIndices;
    }

    public int[] getVertexIndices() {
        return vertexIndices;
    }

    public void setNormal(Vertex normal) {
        this.normal = normal;
    }

    public Vertex getNormal() {
        return normal;
    }
}
