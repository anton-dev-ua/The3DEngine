package prof;

import engine.model.Vertex;
import engine.render.Visualizer;
import engine.scene.Camera;
import engine.scene.Player;
import engine.scene.Scene;
import org.xml.sax.SAXException;
import util.ColladaReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main3 {

    private static int width = 800;
    private static int height = 600;
    private static engine.scene.Scene scene;
    private static Player pPosition = new Player(new Vertex(0, 0, -400), 0, 0, 90);
    private static Visualizer visualizer;
    static int measures = 0;

    static Map<String, Stats> calls = new HashMap<>();
    private static boolean drawing = true;


    public static void main(String[] args) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException, InterruptedException {

        scene = new Scene();
        scene.setCamera(new Camera(width, height, pPosition.fov));

        scene.setMesh(new ColladaReader().readFile(Main3.class.getResource("/few-rooms-textured.dae").getFile()));

        visualizer = new Visualizer(scene, width, height, pPosition.fov);
        visualizer.setAngleX((int) pPosition.verticalAngle);
        visualizer.setAngleY((int) pPosition.horizontalAngle);
        visualizer.setPosition(pPosition.position);

        Thread thread = new Thread() {

            public void run() {
                while (drawing) {
                    visualizer.drawScene();
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                List<Stats> collect = calls.values().stream()
                        .map(stats -> {
                            stats.totalPercent = (float) stats.total * 100 / measures;
                            stats.selfPercent = (float) stats.self * 100 / measures;
                            return stats;
                        })
                        .sorted((s1, s2) -> s1.total > s2.total ? -1 : s1.total < s2.total ? 1 : 0)
                        .collect(Collectors.toList());

                System.out.printf("%-70s | %-9s | %-6s | %-9s | %-6s |\n", "[name]", "[total]", "[%t]", "[self]", "[%s]");
                collect.forEach(stats -> {
                    System.out.printf("%-70s | %,9d | %,6.2f | %,9d | %,6.2f |\n",
                            stats.name, stats.total, stats.totalPercent, stats.self, stats.selfPercent);
                });
            }
        });

        thread.start();

        Thread.sleep(5000);

        StackTraceElement[][] stacks = new StackTraceElement[100000][];
        int index = 0;

        long timeStart = System.nanoTime();
        while (index < stacks.length) {
            stacks[index++] = thread.getStackTrace();
            measures += 1;
        }

        for (int i = 0; i < stacks.length; i++) {

            Stats stats = getStatsFor(buildKey(stacks[i][0]));
            stats.self += 1;
            stats.total += 1;

            for (int j = 1; j < stacks[i].length; j++) {
                stats = getStatsFor(buildKey(stacks[i][j]));
                stats.total += 1;
            }
        }

        long timeSpent = (System.nanoTime() - timeStart) / 1000000;

        System.out.printf("\n\ntime: %,7d msec, %,10.2f samples/msec\n\n", timeSpent, (double) stacks.length / timeSpent);


        drawing = false;

    }

    private static String buildKey(StackTraceElement stackTraceElement) {
        StackTraceElement stackLine = stackTraceElement;
        return stackLine.getClassName() + "." + stackLine.getMethodName();
    }

    private static Stats getStatsFor(String key) {
        Stats stats = calls.get(key);
        if (stats == null) {
            stats = new Stats(key);
            calls.put(key, stats);
        }
        return stats;
    }

    static class Stats {
        long self;
        long total;
        float selfPercent;
        float totalPercent;
        String name;

        public Stats(String name) {

            this.name = name;
        }
    }
}
