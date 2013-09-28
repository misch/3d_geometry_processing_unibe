package meshes;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class FaceTest {
	private HalfEdgeStructure hs;
	ArrayList<Face> faces;
	
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
		faces = hs.getFaces();
	}
}
