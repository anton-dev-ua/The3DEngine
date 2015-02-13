package engine;

import org.w3c.dom.Document;
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

import static engine.Mesh.Face;
import static java.lang.Math.random;
import static java.lang.String.format;

public class ColladaReader {

    public static final String VERTICES_INPUT_ID = "//mesh//vertices/input[@semantic='POSITION']/@source";
    public static final String VERTICES_LINE = "//mesh/source[@id='%s']/float_array/text()";
    public static final String FACES_VERTEX_COUNT_LINE = "//mesh/polylist/vcount/text()";
    public static final String FACES_VERTICES_LINE = "//mesh/polylist/p/text()";
    private XPath xPath;
    private static double SCALE = 200.0 / 78.7401575;

    public Mesh readFile(String fileName) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document document = documentBuilder.parse(fileName);

        XPathFactory xPathFactory = XPathFactory.newInstance();

        xPath = xPathFactory.newXPath();

        String verticesInputId = extractValue(document, VERTICES_INPUT_ID);
        String verticesCoordLine = extractValue(document, format(VERTICES_LINE, verticesInputId.substring(1)));

        String verticesCoord[] = verticesCoordLine.split(" ");

        List<Vertex> vertices = new ArrayList<>();
        int[] vertexRemap = new int[verticesCoord.length / 3];
        Map<String, Integer> vertexMap = new HashMap<>();

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

        String facesVertexCountLine = extractValue(document, FACES_VERTEX_COUNT_LINE);
        String facesVerticesLine = extractValue(document, FACES_VERTICES_LINE);

        int[] facesVerticesCount = Arrays.stream(facesVertexCountLine.split(" ")).mapToInt(Integer::valueOf).toArray();
        int[] facesVertices = Arrays.stream(facesVerticesLine.split(" ")).mapToInt(Integer::valueOf).toArray();

        int position = 0;
        int faceIndex = 0;
        Face[] faces = new Face[facesVerticesCount.length];
        for (int faceVertexCount : facesVerticesCount) {
            int[] vertexIndices = new int[faceVertexCount];
            for (int i = 0; i < faceVertexCount; i++) {
                vertexIndices[i] = vertexRemap[facesVertices[position]];
                position++;
            }
            faces[faceIndex] = new Face(vertexIndices);
            byte r = (byte) (50 + random()*150);// * faceIndex / facesVerticesCount.length);
            byte g = (byte) (50 + random()*150);// * faceIndex / facesVerticesCount.length);
            byte b = (byte) (50 + random()*150);// * faceIndex / facesVerticesCount.length);
            faces[faceIndex].color = new ColorRGB(r, g, b);
            faces[faceIndex].index = faceIndex;


            faceIndex++;
        }

        return new Mesh(vertices.toArray(new Vertex[vertices.size()]), faces).scale(SCALE);
//        return new Mesh(vertices.toArray(new Vertex[vertices.size()]), new Face[]{faces[13]}).scale(SCALE);

    }

    private String extractValue(Document document, String xpath) throws XPathExpressionException {
        XPathExpression expression = xPath.compile(xpath);
        return expression.evaluate(document);
    }
}
