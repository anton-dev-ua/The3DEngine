package engine;


import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ColladaReaderTest {

    @Test
    public void readsMeshFromFile() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        Mesh mesh = new ColladaReader().readFile(getClass().getResource("/box.dae").getFile());

        assertThat(mesh.getOriginalVertices()).hasSize(8);
        assertThat(mesh.getOriginalFaces()).hasSize(6);

    }

    private Vertex v(double x, double y, double z) {
        return new Vertex(x,y,z);
    }

}