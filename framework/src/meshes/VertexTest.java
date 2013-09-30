package meshes;

import static org.junit.Assert.*;

import javax.vecmath.Vector3f;

import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class VertexTest {

	private HalfEdgeStructure hs;
	@Before
	public void setUp() throws Exception {
		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
		hs = new HalfEdgeStructure();
		
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Test
	public void testGetValence() {
		Vertex center = hs.getVertices().get(0);
		
		assertEquals(5, center.getValence());
		assertEquals(3, hs.getVertices().get(2).getValence());
	}
	
	@Test
	public void testNormalsAreVertical(){
		
		for (Vertex vertex : hs.getVertices()){
			Vector3f normal = vertex.getNormal();
			assert(normal.x == 0 && normal.y == 0);
		}
	}
	
	@Test
	public void testNormalsHaveUnitLength(){
		for (Vertex vertex : hs.getVertices()){
			Vector3f normal = vertex.getNormal();
			assert(normal.length() == 1);
		}
	}
	
	@Test
	public void testNormalsArePositivelyOriented(){
		for (Vertex vertex : hs.getVertices()){
			Vector3f normal = vertex.getNormal();
			assert(normal.z > 0);
		}
	}
}
