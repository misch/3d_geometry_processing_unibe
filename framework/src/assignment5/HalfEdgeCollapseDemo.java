package assignment5;

import glWrapper.GLHalfEdgeStructure;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class HalfEdgeCollapseDemo {
	public static void main(String args[]) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/bunny5k.obj", true);
		HalfEdgeStructure hsToKill = new HalfEdgeStructure();
		HalfEdgeStructure hsWillLive = new HalfEdgeStructure();

		hsToKill.init(wf);
		hsWillLive.init(wf);

		HalfEdgeCollapse collapse = new HalfEdgeCollapse(hsToKill);
		for (int i = 0; i < 800; i += 4) {
			HalfEdge deadEdge = hsToKill.getHalfEdges().get(602);
			collapse.collapseEdgeAndDelete(deadEdge);
		}

		GLHalfEdgeStructure glHsKill = new GLHalfEdgeStructure(hsToKill);
		GLHalfEdgeStructure glHsLive = new GLHalfEdgeStructure(hsWillLive);

		glHsKill.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		glHsLive.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");

		MyDisplay d = new MyDisplay();
		d.addToDisplay(glHsKill);
		d.addToDisplay(glHsLive);
	}
}
