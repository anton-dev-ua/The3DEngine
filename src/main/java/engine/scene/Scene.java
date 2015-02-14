package engine.scene;

import engine.model.Mesh;

public class Scene {
    private Mesh mesh;
    private Camera camera;

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }
}
