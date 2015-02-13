package gui;

import engine.Vertex;

public class PlayerPosition {

    public Vertex position = new Vertex(-667, 0, 2120);
    public double horizontalAngle = 171;
    public double verticalAngle = 8;
    public double fov;

    public PlayerPosition(Vertex position, double horizontalAngle, double verticalAngle, double fov) {
        this.position = position;
        this.horizontalAngle = horizontalAngle;
        this.verticalAngle = verticalAngle;
        this.fov = fov;
    }
}
