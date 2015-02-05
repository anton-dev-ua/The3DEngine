package engine;

import static java.lang.Math.*;

public class Vertex {
    private double x, y, z;
    private boolean behindCamera;

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

    public Vertex plus(Vertex moveVector) {
        return new Vertex(x + moveVector.x, y + moveVector.y, z + moveVector.z);
    }

    public Vertex minus(Vertex v) {
        return new Vertex(x - v.x, y - v.y, z - v.z);
    }

    public Vertex multiply(Vertex v) {
        return new Vertex(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }

    public void setBehindCamera(boolean behindCamera) {
        this.behindCamera = behindCamera;
    }

    public boolean isBehindCamera() {
        return behindCamera;
    }

    Vertex rotateY(double ar) {
        double nx = x * cos(ar) + z * sin(ar);
        double ny = y;
        double nz = -x * sin(ar) + z * cos(ar);
        return new Vertex(nx, ny, nz);
    }

    Vertex rotateX(double ar) {
        double nx = x;
        double ny = y * cos(ar) + z * sin(ar);
        double nz = -y * sin(ar) + z * cos(ar);
        return new Vertex(nx, ny, nz);
    }

    public Vertex norm() {
        double l = sqrt(x * x + y * y + z * z);
        return new Vertex(x / l, y / l, z / l);
    }

    @Override
    public String toString() {
        return String.format("[%,7.2f,%,7.2f,%,7.2f]", x, y, z);
//        "Vertex{" +
//                "x=" + x +
//                ", y=" + y +
//                ", z=" + z +
//                '}';
    }
}
