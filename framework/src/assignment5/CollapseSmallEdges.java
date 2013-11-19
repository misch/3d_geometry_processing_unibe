package assignment5;

import glWrapper.GLHalfEdgeStructure;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class CollapseSmallEdges {
	public static void main(String args[]) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/bunny_ear.obj", true);
		HalfEdgeStructure hsToKill = new HalfEdgeStructure();
		HalfEdgeStructure hsWillLive = new HalfEdgeStructure();

		hsToKill.init(wf);
		hsWillLive.init(wf);
		float epsilon = 1;
		
//		HalfEdge deadEdge = hsToKill.getHalfEdges().get(4);
		//mark the halfedge on untouched object
		ArrayList<Vector3f> color = new ArrayList<Vector3f>(Collections.nCopies(hsWillLive.getVertices().size(), new Vector3f(0,0,1)));
		HalfEdgeCollapse collapse = new HalfEdgeCollapse(hsToKill);
		
		int counter = 0;
		int checkCounter = 0;
		for(HalfEdge edge: hsToKill.getHalfEdges()){
			checkCounter++;
			System.out.print("Checking edge No. " + checkCounter +": ");
			if( edge.length() > epsilon || !HalfEdgeCollapse.isEdgeCollapsable(edge) || collapse.isEdgeDead(edge)){
				System.out.print("Stays.\n");
				continue;
			}
			color.set(edge.end().index, new Vector3f(1,0,0));
			color.set(edge.start().index, new Vector3f(1,0,1));
			
			collapse.collapseEdge(edge);
			counter++;
			
			System.out.print("Deleted.\n");
		}
		
		System.out.println("Done with collapsing");


		GLHalfEdgeStructure glHsKill = new GLHalfEdgeStructure(hsToKill);
		GLHalfEdgeStructure glHsLive = new GLHalfEdgeStructure(hsWillLive);
		
		glHsLive.add(color, "color");
		
		glHsKill.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		glHsLive.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");

		MyDisplay d = new MyDisplay();
		d.addToDisplay(glHsKill);
		d.addToDisplay(glHsLive);
	}
}
