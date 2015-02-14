package engine.scene;

import engine.model.Vertex;

import static java.lang.String.format;

public class Player {

    public Vertex position = new Vertex(-667, 0, 2120);
    public double horizontalAngle = 171;
    public double verticalAngle = 8;
    public double fov;

    public Player(Vertex position, double horizontalAngle, double verticalAngle, double fov) {
        this.position = position;
        this.horizontalAngle = horizontalAngle;
        this.verticalAngle = verticalAngle;
        this.fov = fov;
    }

    @Override
    public String toString() {
        return "Player{" +
                "P=" + position +
                ", HA=" + horizontalAngle +
                ", VA=" + verticalAngle +
                ", FIV=" + fov +
                '}';
    }

    public String getCreationString() {
        return format("new Player(new Vertex(%s, %s, %s,), %s, %s, %s)", position.x, position.y, position.z, horizontalAngle, verticalAngle, fov);
    }

    public String getDataString() {
        return format("%s, %s, %s, %s, %s, %s", position.x, position.y, position.z, horizontalAngle, verticalAngle, fov);
    }
}
