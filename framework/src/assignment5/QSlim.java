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
    HalfEdgeCollapse collapse;
	
	public QSlim(HalfEdgeStructure hs){
		this.hs = hs;
		this.collapse = new HalfEdgeCollapse(hs);
		
		for(Vertex vert : hs.getVertices()){
			errorMat.put(vert, costMatrix(vert));
		}
		
		for(HalfEdge edge : hs.getHalfEdges()){
			PotentialCollapse potCollapse = new PotentialCollapse(edge);
			collapses.add(potCollapse);
			currentCollapses.put(edge, potCollapse);
		}
		
	}
	
	public void simplify(int target) {
		System.out.println("Hello!");
		while (target < hs.getVertices().size() - collapse.deadVertices.size()) {
			collapseCheapestEdge();
		}
		collapse.finish();
	}
	
	private boolean collapseCheapestEdge(){
		PotentialCollapse cheapest = collapses.poll();
		int i = 0;
		if(cheapest.isDeleted || collapse.isEdgeDead(cheapest.edge)){
			return false;
		}
		
		if (collapse.isCollapseMeshInv(cheapest.edge, cheapest.newPos) || !HalfEdgeCollapse.isEdgeCollapsable(cheapest.edge)){
			cheapest.increaseCosts();
			return false;
		}
		else{
			i++;
			collapse.collapseEdge(cheapest.edge);
			updateAdjacentCollapseCosts(cheapest.edge.end(),cheapest.error);
		}
		return true;		
	}


	private void updateAdjacentCollapseCosts(Vertex vert, Matrix4f error) {
		errorMat.put(vert, error);
		Iterator<HalfEdge> iter = vert.iteratorVE();
		while(iter.hasNext()){
			HalfEdge edge = iter.next();
			PotentialCollapse potCol = new PotentialCollapse(edge);
			PotentialCollapse potColOppo = new PotentialCollapse(edge.getOpposite());
			
			// delete old potential Collapses
			currentCollapses.get(edge).isDeleted = true;
			currentCollapses.get(edge.getOpposite()).isDeleted = true;
			
			// add new potential Collapses
			collapses.add(potCol);
			collapses.add(potColOppo);
			
			// update Hash Map
			currentCollapses.put(edge, potCol);
			currentCollapses.put(edge, potColOppo);
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
		boolean isDeleted = false;
		Point3f newPos;
		Matrix4f error;
		
		PotentialCollapse(HalfEdge edge) {
			this(edge, 0.f);
		}
		

		public void increaseCosts() {
			float newCost = (this.cost + 0.1f) * 10;
			this.isDeleted = true;
			PotentialCollapse newPotentialCollapse = new PotentialCollapse(this.edge,newCost); 
			collapses.add(newPotentialCollapse);
			currentCollapses.put(edge, newPotentialCollapse);
		}


		PotentialCollapse(HalfEdge edge, float cost) {
			error = new Matrix4f();
            error.add(errorMat.get(edge.start()), errorMat.get(edge.end()));
			this.edge = edge;	
			this.newPos = computeNewPos();
//			this.newPos = computeOptimalNewPos();
			if(cost !=0){
				this.cost = cost;
			}
			else {
				this.cost = computeCost();
			}
		}

        private Point3f computeOptimalNewPos() {
            Point3f newPos = new Point3f();
        	Matrix4f Q = new Matrix4f(error);
            Q.setRow(3, 0, 0, 0, 1);
            if (Q.determinant() != 0) {
                    Q.invert();
                    Point3f optPos = new Point3f();
                    Q.transform(optPos);
                    newPos = optPos;
            } else{
                    newPos = computeNewPos();
            }
            return newPos;
    }
		
		private Point3f computeNewPos() {
			Point3f newPos = new Point3f(edge.start().getPos());
			newPos.add(edge.end().getPos());
			newPos.scale(0.5f);
			
			return newPos;
		}
		
		private float computeCost() {
//            error = new Matrix4f();
//            error.add(errorMat.get(edge.start()), errorMat.get(edge.end()));
            
            Point4f p = new Point4f();
            p.x = newPos.x;
            p.y = newPos.y;
            p.z = newPos.z;
            p.w = 1;
         
            
            Vector4f Qp = new Vector4f(p);
            error.transform(Qp);
            
            Vector4f pT = new Vector4f(p);
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
