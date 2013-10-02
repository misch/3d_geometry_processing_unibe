package meshes;

import static org.junit.Assert.*;

import java.util.Iterator;

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
	
	@Test
	public void testGetCurvature(){
		Vertex vertex = hs.getVertices().get(0);
		assertEquals(0.0, vertex.getCurvature(), 0.000001);
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			WireframeMesh sphere = ObjReader.read("./objs/sphere.obj", false);
			hs.init(sphere);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		for (Vertex v: hs.getVertices()) {
			assertEquals(0.5, v.getCurvature(), 0.01);
		
		}
	}
	
	@Test
	public void testGetMixedArea(){		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			WireframeMesh sphere = ObjReader.read("./objs/sphere.obj", false);
			hs.init(sphere);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		float radius = 2;
		float summedVoronoiCells = 0;
		for (Vertex v: hs.getVertices()) {
			summedVoronoiCells += v.getMixedArea();
		
		}
		assertEquals(4*Math.PI*radius*radius, summedVoronoiCells, 0.25);
	}
}
