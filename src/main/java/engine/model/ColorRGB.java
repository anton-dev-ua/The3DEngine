package engine.model;

public class ColorRGB {
    public byte red, green, blue;
    public int value;

    public ColorRGB(byte red, byte green, byte blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        value = (255 << 24) + (red << 16) + (green << 8) + blue;
    }
}
