package assignment5;

import java.util.HashMap;
import java.util.Iterator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;


/** 
 * Implement the QSlim algorithm here
 * 
 * @author Alf
 *
 */
public class QSlim {
	
	HalfEdgeStructure hs;
	public HashMap<Vertex, Matrix4f> errorMat = new HashMap<Vertex, Matrix4f>();
	
	public QSlim(HalfEdgeStructure hs){
		this.hs = hs;
		
		for(Vertex vert : hs.getVertices()){
			errorMat.put(vert, costMatrix(vert));
		}
		
	}
	
	/**
	 * Compute per vertex matrices
	 * Compute edge collapse costs,
	 * Fill up the Priority queue/heap or similar
	 */
	private void init(){
		
	}
	
	private Matrix4f costMatrix(Vertex vert){
		Matrix4f cost = new Matrix4f();
		
		Iterator<Face> faces = vert.iteratorVF();
		
		while(faces.hasNext()){
			Matrix4f distanceMatrix = new Matrix4f();
			Face face = faces.next();
			
			Vector3f normal = face.normal();
			Vector4f p = new Vector4f(normal); // consider squared distance of p to plane
			
			Vector3f p0 = new Vector3f(vert.getPos()); // To get a point on the plane just take the position of the vertex from which the iteration started.	
			p.w = -normal.dot(p0);
			
			compute_ppT(p,distanceMatrix);
			
			cost.add(distanceMatrix);
		}
		return cost;
	}
	
	
	/**
	 * The actual QSlim algorithm, collapse edges until
	 * the target number of vertices is reached.
	 * @param target
	 */
	public void simplify(int target){
		
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public void collapsEdge(){
		
	}
	
	/**
	 * helper method that might be useful..
	 * @param p
	 * @param ppT
	 */
	private void compute_ppT(Vector4f p, Matrix4f ppT) {
		Matrix4f pCol = new Matrix4f();
		pCol.setColumn(0, p);
		ppT.mulTransposeRight(pCol, pCol);
	}
	
	
	
	
	
	/**
	 * Represent a potential collapse
	 * @author Alf
	 *
	 */
	protected class PotentialCollapse implements Comparable<PotentialCollapse>{

		@Override
		public int compareTo(PotentialCollapse arg1) {
			return -1;
		}
	}

}
