package engine.model;

public class ColorRGB {
    public int red, green, blue;
    public int value;

    public ColorRGB(int red, int green, int blue) {
        this.red = red <= 255 ? red : 255;
        this.green = green <= 255 ? green : 255;
        this.blue = blue <= 255 ? blue : 255;
        value = calcValue();
    }

    private int calcValue() {
        return (255 << 24) + (red << 16) + (green << 8) + blue;
    }

    @Override
    public String toString() {
        return "ColorRGB{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                '}';
    }
}
