package gui;

import engine.model.Vertex;
import engine.render.Visualizer;
import engine.scene.Camera;
import engine.scene.Player;
import org.xml.sax.SAXException;
import util.ColladaReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class Main2 {

    private static int width = 800;
    private static int height = 600;
    private static engine.scene.Scene scene;
    private static Player pPosition = new Player(new Vertex(0,0,-400), 0, 0, 90);
    private static Visualizer visualizer;



    public static void main(String[] args) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        scene = new engine.scene.Scene();
        scene.setCamera(new Camera(width, height, pPosition.fov));

//        scene.setMesh(aCube().withEdgeLength(200).build());
//        scene.setMesh(new ColladaReader().readFile(getClass().getResource("/room-with-corner-stand.dae").getFile()));
        scene.setMesh(new ColladaReader().readFile(Main2.class.getResource("/few-rooms.dae").getFile()));

        visualizer = new Visualizer(scene, width, height, pPosition.fov);
        visualizer.setAngleX((int) pPosition.verticalAngle);
        visualizer.setAngleY((int) pPosition.horizontalAngle);
        visualizer.setPosition(pPosition.position);

        while(true) {
            visualizer.drawScene();
        }

    }
}
