package engine.builder;

import engine.model.Face;
import engine.model.Mesh;
import engine.model.Vertex;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class TorusBuilder extends MeshBuilder {

    int n = 10;
    double bigRadius = 100;
    double smallRadius = 30;

    public static TorusBuilder aTorus() {
        return new TorusBuilder();
    }

    public TorusBuilder withBigRadius(double bigRadius) {
        this.bigRadius = bigRadius;
        return this;
    }

    public TorusBuilder withSmallRadius(double smallRadius) {
        this.smallRadius = smallRadius;
        return this;
    }

    public TorusBuilder withApproximationNumber(int n) {
        this.n = n;
        return this;
    }

    private TorusBuilder() {

    }

    public Mesh build() {

        List<Vertex> vertices = new ArrayList<>();
        List<Face> faces = new ArrayList<>();

        double delta = 2 * Math.PI / n;

        for (int i = 0; i < n; i++) {
            double alpha = i * delta,
                    cosa = cos(alpha),
                    sina = sin(alpha);
            for (int j = 0; j < n; j++) {
                double beta = j * delta,
                        x = bigRadius + cos(beta) * smallRadius;
                vertices.add(vertex(cosa * x, sin(beta) * smallRadius, sina * x));
            }
        }
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++) {
                int i1 = (i + 1) % n,
                        j1 = (j + 1) % n,
                        a = i * n + j,
                        b = i1 * n + j,
                        c = i1 * n + j1,
                        d = i * n + j1;
                faces.add(face(d, c, b, a));
            }
        }

        return new Mesh(
                vertices.toArray(new Vertex[vertices.size()]),
                faces.toArray(new Face[faces.size()])
        );
    }
}