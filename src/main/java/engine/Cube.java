package engine;

import static engine.ObjectUtils.point;

public class Cube extends Mesh {

    public Cube(double size) {
        super(new Vertex[]{
                        point(-size/2, size/2, -size/2),
                        point(size/2, size/2, -size/2),
                        point(size/2, -size/2, -size/2),
                        point(-size/2, -size/2, -size/2),
                        point(-size/2, size/2, size/2),
                        point(size/2, size/2, size/2),
                        point(size/2, -size/2, size/2),
                        point(-size/2, -size/2, size/2)},
                new Face[]{
                        new Face(0, 1, 2, 3),
                        new Face(7, 6, 5, 4),
                        new Face(4, 0, 3, 7),
                        new Face(0, 4, 5, 1),
                        new Face(1, 5, 6, 2),
                        new Face(3, 2, 6, 7)
                }
        );
    }

}
