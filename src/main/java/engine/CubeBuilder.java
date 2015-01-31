package engine;

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
                        face(0, 1, 2, 3),
                        face(7, 6, 5, 4),
                        face(4, 0, 3, 7),
                        face(0, 4, 5, 1),
                        face(1, 5, 6, 2),
                        face(3, 2, 6, 7)
                )
        );
    }

}
