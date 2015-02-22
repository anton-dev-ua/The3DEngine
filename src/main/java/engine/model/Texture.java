package engine.model;

public class Texture {

    int width;
    int height;

    byte[] buffer;

    public Texture(int width, int height, byte[] buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }
}
