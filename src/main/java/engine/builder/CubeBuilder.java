package engine.builder;

import engine.model.Mesh;

public class CubeBuilder extends MeshBuilder {

    private double edgeLength = 200;

    public static CubeBuilder aCube() {
        return new CubeBuilder();
    }

    public CubeBuilder withEdgeLength(double edgeLength) {
        this.edgeLength = edgeLength;
        return this;
    }

    private CubeBuilder() {
    }

    public Mesh build() {
        double halfLength = edgeLength / 2;
        return new Mesh(

                vertices(
                        vertex(-halfLength, halfLength, -halfLength),
                        vertex(halfLength, halfLength, -halfLength),
                        vertex(halfLength, -halfLength, -halfLength),
                        vertex(-halfLength, -halfLength, -halfLength),
                        vertex(-halfLength, halfLength, halfLength),
                        vertex(halfLength, halfLength, halfLength),
                        vertex(halfLength, -halfLength, halfLength),
                        vertex(-halfLength, -halfLength, halfLength)
                ),

                faces(
                        face(3,2,1,0),
                        face(4,5,6,7),
                        face(7,3,0,4),
                        face(1,5,4,0),
                        face(2,6,5,1),
                        face(7,6,2,3)
                )
        );
    }

}
