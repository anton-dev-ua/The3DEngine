package engine.model;

public class Texture {

    public int width;
    public int height;

    public byte[] buffer;

    public Texture(int width, int height, byte[] buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }
}
