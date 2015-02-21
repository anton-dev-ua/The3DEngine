package util;

import engine.model.ColorRGB;
import engine.model.Face;
import engine.model.Mesh;
import engine.model.Vertex;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.random;
import static java.lang.String.format;
import static javax.xml.xpath.XPathConstants.NODESET;

public class ColladaReader {

    public static final String VERTICES_INPUT_ID = ".//mesh//vertices/input[@semantic='POSITION']/@source";
    public static final String VERTICES_LINE = ".//mesh/source[@id='%s']/float_array/text()";
    public static final String FACES_VERTEX_COUNT_LINE = ".//mesh/polylist/vcount/text()";
    public static final String FACES_VERTICES_LINE = ".//mesh/polylist/p/text()";
    private XPath xPath;
    private static double SCALE = 200.0 / 78.7401575;
    private Map<String, Integer> vertexMap = new HashMap<>();
    private List<Face> faces = new ArrayList<>();
    private List<Vertex> vertices = new ArrayList<>();
    private Document document;
    private Map<String, ColorRGB> materialMap = new HashMap<>();
    private Map<String, String> materialBindMap = new HashMap<>();

    public Mesh readFile(String fileName) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        init(fileName);


        parseMaterials();
        parseMaterialBaindings();

        NodeList geometries = geometries();

        for (int meshIndex = 0; meshIndex < geometries.getLength(); meshIndex++) {
            parseGeometry(geometries.item(meshIndex));
        }

        return new Mesh(vertices.toArray(new Vertex[vertices.size()]), faces.toArray(new Face[faces.size()])).scale(SCALE);

    }

    private void parseMaterialBaindings() throws XPathExpressionException {
        NodeList bindMaterials = evaluateNodeset("//instance_geometry");

        for (int bindIndex = 0; bindIndex < bindMaterials.getLength(); bindIndex++) {
            Node bind = bindMaterials.item(bindIndex);
            String geometryId = extractValue(bind, "@url").substring(1);
            String materialId = extractValue(bind, ".//instance_material/@target").substring(1);
            materialBindMap.put(geometryId, materialId);
        }
    }

    private void parseMaterials() throws XPathExpressionException {
        NodeList effects = evaluateNodeset("//library_effects/effect");

        Map<String, ColorRGB>  effectMap = new HashMap<>();
        for (int effectIndex = 0; effectIndex < effects.getLength(); effectIndex++) {
            Node effect = effects.item(effectIndex);
            String id = extractValue(effect, "@id");
            String colorLine = extractValue(effect, ".//color");
            String[] colorParts = colorLine.split(" ");
            effectMap.put(id, new ColorRGB(
                    (int) (Float.valueOf(colorParts[0]) * 255),
                    (int) (Float.valueOf(colorParts[1]) * 255),
                    (int) (Float.valueOf(colorParts[2]) * 255)
            ));
        }


        NodeList materials = evaluateNodeset("//library_materials/material");

        for(int materialIndex = 0; materialIndex < materials.getLength(); materialIndex++) {
            Node material = materials.item(materialIndex);
            String materialId = extractValue(material, "@id");
            String effectId = extractValue(material, ".//instance_effect/@url").substring(1);
            materialMap.put(materialId, effectMap.get(effectId));
        }

    }

    private NodeList evaluateNodeset(String expression) throws XPathExpressionException {
        return (NodeList) xPath.compile(expression).evaluate(document, NODESET);
    }

    private NodeList geometries() throws XPathExpressionException {
        return evaluateNodeset("//geometry");
    }

    private void init(String fileName) throws ParserConfigurationException, SAXException, IOException {
        readDocument(fileName);

        XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
    }

    private void readDocument(String fileName) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        document = documentBuilder.parse(fileName);
    }

    private void parseGeometry(Node geometry) throws XPathExpressionException {

        String id = xPath.compile("@id").evaluate(geometry);
        ColorRGB color = materialMap.get(materialBindMap.get(id));

        String verticesInputId = extractValue(geometry, VERTICES_INPUT_ID);
        String verticesCoordLine = extractValue(geometry, format(VERTICES_LINE, verticesInputId.substring(1)));

        String verticesCoord[] = verticesCoordLine.split(" ");

        int[] vertexRemap = new int[verticesCoord.length / 3];

        for (int index = 0; index < verticesCoord.length / 3; index++) {
            String x = verticesCoord[index * 3];
            String y = verticesCoord[index * 3 + 2];
            String z = verticesCoord[index * 3 + 1];

            String vertexKey = x + "|" + y + "|" + z;

            if (!vertexMap.containsKey(vertexKey)) {
                Vertex vertex = new Vertex(Double.valueOf(x), Double.valueOf(y), Double.valueOf(z));
                vertices.add(vertex);
                int vertexIndex = vertices.size() - 1;
                vertexMap.put(vertexKey, vertexIndex);
                vertexRemap[index] = vertexIndex;
            } else {
                vertexRemap[index] = vertexMap.get(vertexKey);
            }

        }

        String facesVertexCountLine = extractValue(geometry, FACES_VERTEX_COUNT_LINE);
        String facesVerticesLine = extractValue(geometry, FACES_VERTICES_LINE);

        int[] facesVerticesCount = Arrays.stream(facesVertexCountLine.split(" ")).mapToInt(Integer::valueOf).toArray();
        int[] facesVertices = Arrays.stream(facesVerticesLine.split(" ")).mapToInt(Integer::valueOf).toArray();

        int position = 0;
        for (int faceVertexCount : facesVerticesCount) {
            int[] vertexIndices = new int[faceVertexCount];
            for (int i = 0; i < faceVertexCount; i++) {
                vertexIndices[i] = vertexRemap[facesVertices[position]];
                position++;
            }
            Face face = new Face(vertexIndices);
            faces.add(face);
            if (color == null) {
                int r = (int) (50 + random() * 150);// * faceIndex / facesVerticesCount.length);
                int g = (int) (50 + random() * 150);// * faceIndex / facesVerticesCount.length);
                int b = (int) (50 + random() * 150);// * faceIndex / facesVerticesCount.length);
                face.color = new ColorRGB(r, g, b);
            } else {
                face.color = color;
            }
            face.index = faces.size() - 1;
        }
    }

    private String extractValue(Node node, String xpath) throws XPathExpressionException {
        XPathExpression expression = xPath.compile(xpath);
        return expression.evaluate(node);
    }
}
