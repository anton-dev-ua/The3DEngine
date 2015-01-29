package engine;

public class Object {
    private Point[] points;
    private Face[] faces;

    public Object(Point[] points, Face[] face) {
        this.points = points;
        this.faces = face;
    }

    public Point[] getPoints() {
        return points;
    }

    public Face[] getFaces() {
        return faces;
    }

    public static class Face {
        int pointIndices[];

        public Face(int... pointIndices) {
            this.pointIndices = pointIndices;
        }

        public int[] getPointIndices() {
            return pointIndices;
        }
    }
}
