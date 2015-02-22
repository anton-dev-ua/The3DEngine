package engine.scene;

import engine.model.*;

import java.util.Map;

public class Scene {
    private Mesh mesh;
    private Camera camera;
    private ColorRGB ambient = new ColorRGB(60, 60, 60);
    private Vertex sunVector = new Vertex(0, 1000, -200).normalize();
    private ColorRGB sunColor = new ColorRGB(180, 180, 180);
    private Map<String, Texture> textureMap;

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        MeshPreProcessor meshPreProcessor = new MeshPreProcessor(mesh);
        meshPreProcessor.calculateNormals();
        textureMap = meshPreProcessor.processTextures();

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

    public ColorRGB getAmbient() {
        return ambient;
    }

    public void setAmbient(ColorRGB ambient) {
        this.ambient = ambient;
    }

    public Vertex getSunVector() {
        return sunVector;
    }

    public void setSunVector(Vertex sunVector) {
        this.sunVector = sunVector;
    }

    public ColorRGB getSunColor() {
        return sunColor;
    }

    public void setSunColor(ColorRGB sunColor) {
        this.sunColor = sunColor;
    }
}
