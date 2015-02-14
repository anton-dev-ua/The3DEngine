package engine.builder;

import engine.model.Mesh;
import engine.model.Vertex;

public class GeneralMeshBuilder extends MeshBuilder {

    private Vertex[] vertices;
    private Mesh.Face[] faces;

    public static GeneralMeshBuilder aMesh() {
        return new GeneralMeshBuilder();
    }

    public GeneralMeshBuilder withVertices(Vertex... vertices) {
        this.vertices = vertices;
        return this;
    }

    public GeneralMeshBuilder withFaces(Mesh.Face... faces) {
        this.faces = faces;
        return this;
    }

    public Mesh build() {
        Mesh mesh = new Mesh(vertices, faces);
        return mesh;
    }

    private GeneralMeshBuilder() {
    }

}
