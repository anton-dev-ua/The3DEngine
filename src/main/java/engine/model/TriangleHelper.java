package engine.model;

class TriangleHelper {
    private Vertex C;
    private double a1, a2, b1, b2, c1, c2, d1, d2, det;

    public TriangleHelper(Vertex A, Vertex B, Vertex C) {
        this.C = C;
        a1 = A.getX() - C.getX();
        a2 = A.getY() - C.getY();
        b1 = B.getX() - C.getX();
        b2 = B.getY() - C.getY();
        det = a1 * b2 - b1 * a2;
        if (det != 0) {
            c1 = b2 / det;
            c2 = -a2 / det;
            d1 = -b1 / det;
            d2 = a1 / det;
        }
    }

    public boolean insideTriangle(Vertex P) {
        double p1 = P.getX() - C.getX(), p2 = P.getY() - C.getY(),
                lambda, mu;
        return (lambda = p1 * c1 + p2 * d1) >= 0 &&
                (mu = p1 * c2 + p2 * d2) >= 0 &&
                lambda + mu <= 1;
    }
}
