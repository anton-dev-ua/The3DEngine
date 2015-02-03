package engine;

public class SimpleRoomBuilder extends MeshBuilder {

    private SimpleRoomBuilder() {
    }

    public static SimpleRoomBuilder aSimpleRoom() {
        return new SimpleRoomBuilder();
    }

    public Mesh build() {
        return new Mesh(
                vertices(
                        vertex(-300, 200, -600),
                        vertex(-300, 200, 500),
                        vertex(-300, -100, 500),
                        vertex(-300, -100, 250),
                        vertex(-300, -200, 250),
                        vertex(-300, -200, -600),

                        vertex(300, 200, -600),
                        vertex(300, 200, 500),
                        vertex(300, -100, 500),
                        vertex(300, -100, 250),
                        vertex(300, -200, 250),
                        vertex(300, -200, -600)
                ),
                faces(
                        face(0, 1, 2, 3, 4, 5),
                        face(11, 10, 9, 8, 7, 6),
                        face(0, 5, 11, 6),
                        face(0, 6, 7, 1),
                        face(5, 4, 10, 11),
                        face(4, 3, 9, 10),
                        face(3, 2, 8, 9)
                )
        );
    }
}
