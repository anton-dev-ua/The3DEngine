package util;

import engine.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.max;
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
    private Map<String, Material> materialMap = new HashMap<>();
    private Map<String, String> materialBindMap = new HashMap<>();
    private Map<String, Material> effectMap = new HashMap<>();
    private Map<String, String> imageMap = new HashMap<>();

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
//        parseImages();
        parseEffects();


        NodeList materials = evaluateNodeset("//library_materials/material");

        for (int materialIndex = 0; materialIndex < materials.getLength(); materialIndex++) {
            Node material = materials.item(materialIndex);
            String materialId = extractValue(material, "@id");
            String effectId = extractValue(material, ".//instance_effect/@url").substring(1);
            materialMap.put(materialId, effectMap.get(effectId));
        }

    }

    private void parseImages() throws XPathExpressionException {
        iterateNodes("//library_images/image", image -> {
            String imageId = extractValue(image, "@id");
            String imageFile = extractValue(image, "init_from/text()");
            imageMap.put(imageId, imageFile);
        });
    }

    private void parseEffects() throws XPathExpressionException {
        iterateNodes("//library_effects/effect", effect -> {
            String id = extractValue(effect, "@id");
            String colorLine = extractValue(effect, ".//profile_COMMON/technique/lambert/diffuse/color");
            String textureId = extractValue(effect, ".//profile_COMMON/technique/lambert/diffuse/texture/@texture");

            ColorRGB color;
            String imageFile = null;

            if (!colorLine.trim().isEmpty()) {
                String[] colorParts = colorLine.split(" ");
                color = new ColorRGB(
                        (int) (Float.valueOf(colorParts[0]) * 255),
                        (int) (Float.valueOf(colorParts[1]) * 255),
                        (int) (Float.valueOf(colorParts[2]) * 255));
            } else {
                color = new ColorRGB(255, 255, 255);
            }

            if (!textureId.trim().isEmpty()) {
                String samplerSourceId = extractValue(effect, ".//*[@sid='%s']/sampler2D/source/text()", textureId);
                String imageId = extractValue(effect, ".//*[@sid='%s']/surface/init_from/text()", samplerSourceId);
                imageFile = extractValue(document, "//library_images/image[@id='%s']/init_from/text()", imageId);
            }

            effectMap.put(id, new Material(color, imageFile));
        });
    }

    private NodeList evaluateNodeset(String expression) throws XPathExpressionException {
        return (NodeList) xPath.compile(expression).evaluate(document, NODESET);
    }

    private NodeList evaluateNodeset(Node node, String expression) throws XPathExpressionException {
        return (NodeList) xPath.compile(expression).evaluate(node, NODESET);
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

        String id = extractValue(geometry, "@id");
        Material material = materialMap.get(materialBindMap.get(id));

        String verticesInputId = extractValue(geometry, VERTICES_INPUT_ID);
        String verticesCoordLine = extractValue(geometry, format(VERTICES_LINE, verticesInputId.substring(1)));

        NodeList inputs = evaluateNodeset(geometry, ".//polylist/input");

        OffsetData offsetData = new OffsetData();

        iterateNodes(inputs, node -> {
            String offsetName = extractValue(node, "@semantic");
            Integer offset = Integer.valueOf(extractValue(node, "@offset"));
            String sourceId = extractValue(node, "@source").substring(1);

            if ("VERTEX".equals(offsetName)) {
                offsetData.vertexOffset = offset;
            }
            if ("TEXCOORD".equals(offsetName)) {
                offsetData.texCoordOffset = offset;
                offsetData.texCoordSourceId = sourceId;
            }

            offsetData.maxOffset = max(offsetData.maxOffset, offset);
        });

        int vStride = offsetData.maxOffset + 1;

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

        Vertex[] texCoordVertex = {};
        if (offsetData.containsTextureCoord()) {
            String facesTextureVerticesLine = extractValue(geometry, format(".//mesh/source[@id=\"%s\"]/float_array/text()", offsetData.texCoordSourceId));
            Integer texCoordVertexCount = Integer.valueOf(extractValue(geometry, format(".//mesh/source[@id=\"%s\"]//accessor/@count", offsetData.texCoordSourceId)));

            texCoordVertex = new Vertex[texCoordVertexCount];
            String[] facesTextureVertices = facesTextureVerticesLine.split(" ");
            for (int texCoordIndex = 0; texCoordIndex < texCoordVertexCount; texCoordIndex++) {
                texCoordVertex[texCoordIndex] = new Vertex(
                        Double.valueOf(facesTextureVertices[texCoordIndex * 2]),
                        -Double.valueOf(facesTextureVertices[texCoordIndex * 2 + 1]),
                        0
                );
                System.out.println(texCoordVertex[texCoordIndex]);
            }
        }

        int[] facesVerticesCount = Arrays.stream(facesVertexCountLine.split(" ")).mapToInt(Integer::valueOf).toArray();
        int[] facesVertices = Arrays.stream(facesVerticesLine.split(" ")).mapToInt(Integer::valueOf).toArray();

        int position = 0;
        for (int faceVertexCount : facesVerticesCount) {

            int[] vertexIndices = new int[faceVertexCount];
            Vertex[] textureCoord = null;
            if (offsetData.containsTextureCoord()) {
                textureCoord = new Vertex[faceVertexCount];
            }
            for (int i = 0; i < faceVertexCount; i++) {
                vertexIndices[i] = vertexRemap[facesVertices[position + offsetData.vertexOffset]];
                if (offsetData.containsTextureCoord()) {
                    textureCoord[i] = texCoordVertex[facesVertices[position + offsetData.texCoordOffset]];
                }
                position += vStride;
            }

            Face face = new Face(vertexIndices);
            faces.add(face);
            face.textCoord = textureCoord;
            face.material = material;
            face.index = faces.size() - 1;
        }
    }

    public static class OffsetData {
        int maxOffset;
        int vertexOffset;
        int texCoordOffset;

        public String texCoordSourceId;

        private boolean containsTextureCoord() {
            return texCoordSourceId != null;
        }
    }

    private void iterateNodes(String expression, Consumer<Node> consumer) throws XPathExpressionException {
        iterateNodes(evaluateNodeset(expression), consumer);
    }

    private void iterateNodes(NodeList nodeList, Consumer<Node> consumer) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            consumer.accept(nodeList.item(i));
        }
    }

    private String extractValue(Node node, String xpath, Object... args) {
        try {
            return xPath.compile(format(xpath, args)).evaluate(node);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
