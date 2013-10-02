package meshes;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class FaceTest {
	private HalfEdgeStructure oneNeigh, simpleArea;
	private ArrayList<Face> facesOneNeigh;
	private Face faceSimpleArea;
	
	
	@Before
	public void setUp() throws Exception {
		WireframeMesh oneNeighWireFrame = ObjReader.read("./objs/oneNeighborhood.obj", true);
		WireframeMesh simpleAreaWireFrame = ObjReader.read("./objs/simpleAreaObject.obj", false);
		oneNeigh = new HalfEdgeStructure();
		simpleArea = new HalfEdgeStructure();
		
		try {
			oneNeigh.init(oneNeighWireFrame);
			simpleArea.init(simpleAreaWireFrame);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		facesOneNeigh = oneNeigh.getFaces();
		faceSimpleArea = simpleArea.getFaces().get(0);
	}
	
	@Test
	public void testArea(){		
		assert(faceSimpleArea.getArea() == 0.5);
	}
}
