package engine.model;

public class Pair {
    public int i;
    public double v;

    private Pair(int i, double v) {
        this.i = i;
        this.v = v;
    }


    public static Pair pair(int i, double v) {
        return new Pair(i, v);
    }
}
