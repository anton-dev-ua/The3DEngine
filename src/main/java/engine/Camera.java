package engine;

import static engine.GeneralMeshBuilder.aMesh;
import static engine.MeshBuilder.face;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

public class Camera {
    private Vertex sp, p;
    private Vertex sq, q;
    private Vertex sr, r;
    private Vertex ss, s;
    private double dist;
    private double width;
    private double height;
    private double fov;

    public Camera(double width, double height, double fov) {
        this.width = width;
        this.height = height;
        this.fov = fov;
        this.dist = width / 2 / tan(toRadians(fov / 2));

        p = sp = new Vertex(0, 0, dist);
        q = sq = new Vertex(width / 2, 0, 0);
        r = sr = new Vertex(0, height / 2, 0);
        s = ss = new Vertex(0, 0, -dist);

    }

    public Mesh getMesh() {
        Vertex dq = q.multiply(1.0/100.0);
        Vertex dr = r.multiply(1.0/100.0);

        Vertex sc = s.plus(p);

        return aMesh()
                .withVertices(
                        s.minus(dq).plus(dr), s.minus(dq).minus(dr), s.plus(dq).minus(dr), s.plus(dq).plus(dr),
                        sc.minus(q).plus(r), sc.minus(q).minus(r), sc.plus(q).minus(r), sc.plus(q).plus(r),
                        s,sc.plus(p)
                )
                .withFaces(
                        face(0, 1, 2, 3),
                        face(7, 6, 5, 4),
                        face(4, 0, 3, 7),
                        face(0, 4, 5, 1),
                        face(1, 5, 6, 2),
                        face(3, 2, 6, 7),
                        face(8,9).opened()
                )
                .build();
    }

    public Camera transform(Vertex moveVector, double angleY, double angleX) {
        p = sp;
        q = sq;
        r = sr;
        s = ss;
        rotateX(toRadians(angleX));
        rotateY(toRadians(angleY));
        move(moveVector);
        return this;
    }

    private void rotateX(double a) {
        p = p.rotateX(a);
        q = q.rotateX(a);
        r = r.rotateX(a);
    }

    private void rotateY(double a) {
        p = p.rotateY(a);
        q = q.rotateY(a);
        r = r.rotateY(a);
    }

    private void move(Vertex moveVector) {
        s = s.plus(moveVector);
    }
}
