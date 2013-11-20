package assignment5;

import glWrapper.GLHalfEdgeStructure;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class CollapseSmallEdges {
	public static void main(String args[]) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/buddha.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();

		hs.init(wf);
		float epsilon = 0.5f;
		
		GLHalfEdgeStructure glBeforeCollapse = collapseSmallEdges(hs, epsilon);
		GLHalfEdgeStructure glAfterCollapse = new GLHalfEdgeStructure(hs);
		
		
		glAfterCollapse.configurePreferredShader(
				"shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");


		MyDisplay d = new MyDisplay();
		d.addToDisplay(glAfterCollapse);
		d.addToDisplay(glBeforeCollapse);
	}

	private static GLHalfEdgeStructure collapseSmallEdges(HalfEdgeStructure hs, float epsilon) {
		GLHalfEdgeStructure collapsedEdgesMarked = new GLHalfEdgeStructure(hs);
		ArrayList<Vector3f> color = new ArrayList<Vector3f>(Collections.nCopies(hs.getVertices().size(), new Vector3f(0,0,1)));
		HalfEdgeCollapse collapse = new HalfEdgeCollapse(hs);
		
		int numberOfCollapsedEdges;
		int totallyCollapsed = 0;
		do{
		numberOfCollapsedEdges = 0;
		for(HalfEdge edge: hs.getHalfEdges()){
			if( 	edge.toVec().length() > epsilon || 
					collapse.isEdgeDead(edge) || 
					collapse.isCollapseMeshInv(edge, edge.end().getPos()) || 
					!HalfEdgeCollapse.isEdgeCollapsable(edge))
			{
				continue;
			}
				
				color.set(edge.end().index, new Vector3f(1,0,0));
				color.set(edge.start().index, new Vector3f(1,0,1));
				
				collapse.collapseEdge(edge);
				numberOfCollapsedEdges ++;
			}
		totallyCollapsed += numberOfCollapsedEdges;
		
		System.out.println("Collapsed " + numberOfCollapsedEdges+" edges in this iteration. "
				+ "\t Totally collapsed " + totallyCollapsed 
				+ " out of " + hs.getHalfEdges().size() 
				+ ",\t remaining " + (hs.getHalfEdges().size()-totallyCollapsed));
		
		} while(numberOfCollapsedEdges > 0);
		
		collapse.finish();
		collapsedEdgesMarked.add(color, "color");
		
		collapsedEdgesMarked.configurePreferredShader(
				"shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		
		return collapsedEdgesMarked;
	}
	
	private static void countFlippables(HalfEdgeStructure hs) {
		  HalfEdgeCollapse c = new HalfEdgeCollapse(hs);
		  int nr = 0;
		  for(HalfEdge e: hs.getHalfEdges()){
		    if((!c.isCollapseMeshInv(e, e.end().getPos())) && c.isEdgeCollapsable(e)){
		      nr++;
		    }
		  }
		  System.out.println(nr);
		}
}
