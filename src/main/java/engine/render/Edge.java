package engine.render;

import engine.model.Face;
import engine.model.Vertex;

import static java.lang.Math.round;

public class Edge implements Comparable<Edge> {
    private final Vertex v1;
    private final Vertex v2;
    int y;
    double x0, x;
    int dy;
    double dx;
    double w0, dw, w;
    boolean starting;
    public Face face;


    public Edge(Vertex v1, Vertex v2, boolean starting) {
        y = (int) round(v1.getY());
        x0 = v1.getX();
        dy = (int) round(v2.getY()) - (int) round(v1.getY());
        dx = (v2.getX() - v1.getX()) / (v2.getY() - v1.getY());
        w0 = v1.getZ();
        dw = (v2.getZ() - w0) / (v2.getY() - v1.getY());
        x = x0 - dx;
        w = w0 - dw;
        this.starting = starting;
        this.v1 = v1;
        this.v2 = v2;
    }

    public double nextX() {
        x += dx;
        return x;
    }

    public double nextW() {
        w += dw;
        return w;
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
        return x < o.x ? -1 : (x > o.x ? 1 : 0);
    }
}
