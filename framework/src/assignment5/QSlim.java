package assignment5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import meshes.Face;
import meshes.HalfEdge;
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
	HashMap<HalfEdge, PotentialCollapse> currentCollapses = new HashMap<HalfEdge, PotentialCollapse>();
    PriorityQueue<PotentialCollapse> collapses = new PriorityQueue<PotentialCollapse>();
	
	public QSlim(HalfEdgeStructure hs){
		this.hs = hs;
		
		for(Vertex vert : hs.getVertices()){
			errorMat.put(vert, costMatrix(vert));
		}
		
		for(HalfEdge edge : hs.getHalfEdges()){
			PotentialCollapse potCollapse = new PotentialCollapse(edge);
			collapses.add(potCollapse);
			currentCollapses.put(edge, potCollapse);
		}
		
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
			
			Vector4f v = new Vector4f(1,1,1, 1);
            distanceMatrix.transform(v);
            
            if (!Float.isNaN(v.lengthSquared()) && !Float.isInfinite(v.lengthSquared())){
            	cost.add(distanceMatrix);
            }
		}
		return cost;
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

		float cost;
		HalfEdge edge;
		boolean isDeleted;
		Point3f targetPosition;
		
		PotentialCollapse(HalfEdge edge) {
			this(edge, 0.f);
		}
		
		PotentialCollapse(HalfEdge edge, float cost) {
			this.edge = edge;	
			if(cost !=0){
				this.cost = cost;
			}
			else {
				this.cost = computeCost();
			}
		}

		private Point3f computeNewPos() {
			Point3f newPos = edge.start().getPos();
			newPos.add(edge.end().getPos());
			newPos.scale(0.5f);
			
			return newPos;
		}
		
		private float computeCost() {
            Matrix4f error = new Matrix4f();
            error.add(errorMat.get(edge.start()), errorMat.get(edge.end()));
            
            Point4f newPos = new Point4f();
            newPos.x = computeNewPos().x;
            newPos.y = computeNewPos().y;
            newPos.z = computeNewPos().z;
            newPos.w = 1;
         
            
            Vector4f Qp = new Vector4f(newPos);
            error.transform(Qp);
            
            Vector4f pT = new Vector4f(newPos);
            return Qp.dot(pT);
		}

		@Override
		public int compareTo(PotentialCollapse potCollapse) {
			int comparison = 0;
			if(this.cost > potCollapse.cost){
				comparison = 1;
			}
			if(this.cost < potCollapse.cost){
				comparison = -1;
			}
			return comparison;
		}
	}
}
