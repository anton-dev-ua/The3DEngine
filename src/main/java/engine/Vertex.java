package engine;

public class Vertex {
    private double x, y, z;

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex multiply(double a) {
        return new Vertex(x * a, y * a, z * a);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vertex minus(Vertex v) {
        return new Vertex(x - v.x, y - v.y, z - v.z);
    }

    public Vertex multiply(Vertex v) {
        return new Vertex(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }
}
