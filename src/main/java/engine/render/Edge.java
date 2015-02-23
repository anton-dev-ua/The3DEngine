package engine.render;

import engine.model.Face;
import engine.model.Vertex;

import static java.lang.Math.round;

public class Edge implements Comparable<Edge> {
    double v;
    double u;
    private double dv;
    private double du;
    private double v0;
    private Vertex v1;
    private Vertex v2;
    private double u0;
    int y;
    double x0, x;
    int dy;
    double dx;
    double w0, dw, w;
    boolean starting;
    public Face face;
    Vertex tv1;
    Vertex tv2;


    public Edge(Vertex v1, Vertex v2, Vertex tv1, Vertex tv2, boolean starting) {
        y = (int) round(v1.y);
        x0 = v1.x;
        dy = (int) round(v2.y) - (int) round(v1.y);
        dx = (v2.x - v1.x) / (v2.y - v1.y);
        w0 = v1.z;
        dw = (v2.z - w0) / (v2.y - v1.y);
        x = x0 - dx;
        w = w0 - dw;
        this.starting = starting;

        if (tv1 != null && tv2 != null) {
            u0 = tv1.x * w0;
            v0 = tv1.y * w0;

            du = (tv2.x * v2.z - u0) / (v2.y - v1.y);
            dv = (tv2.y * v2.z - v0) / (v2.y - v1.y);

            u = u0 - du;
            v = v0 - dv;
        }

        this.v1 = v1;
        this.v2 = v2;
        this.tv1 = tv1;
        this.tv2 = tv2;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("y=").append(y);
        sb.append(", dy=").append(dy);
        sb.append(", v1=").append(v1);
        sb.append(", v2=").append(v2);
        sb.append(", starting=").append(starting);
        sb.append(", x0=").append(x0);
        sb.append(", dx=").append(dx);
        sb.append(", x=").append(x);
        sb.append(", w0=").append(w0);
        sb.append(", dw=").append(dw);
        sb.append(", w=").append(w);
        sb.append(", face=").append(face.index);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Edge o) {
        return x < o.x ? -1 : 1;
    }

    public void nextY() {
        x += dx;
        w += dw;
        u += du;
        v += dv;
        dy--;
    }
}
