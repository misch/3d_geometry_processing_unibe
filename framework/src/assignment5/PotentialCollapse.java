package assignment5;

import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector4f;

import meshes.HalfEdge;
import meshes.Vertex;

public class PotentialCollapse implements Comparable<PotentialCollapse> {
	
	private HalfEdge halfEdge;
	private Point3f newPos;
	private float cost;
	private boolean isDeleted;
	
	
	public PotentialCollapse(HalfEdge e, HashMap<Vertex, Matrix4f> errorMat){
		this.halfEdge = e;
		this.newPos = computeNewPos(e);
		this.cost = computeCost(e, errorMat);
		
	}
	
	private float computeCost(HalfEdge edge, HashMap<Vertex, Matrix4f> errorMat) {
            Matrix4f error = new Matrix4f();
            error.add(errorMat.get(edge.start()), errorMat.get(edge.end()));
            
            Point4f newPos = new Point4f();
            newPos.x = computeNewPos(edge).x;
            newPos.y = computeNewPos(edge).y;
            newPos.z = computeNewPos(edge).z;
            newPos.w = 1;
         
            
            Vector4f Qp = new Vector4f(newPos);
            error.transform(Qp);
            
            Vector4f pT = new Vector4f(newPos);
            return Qp.dot(pT);
	}

	private Point3f computeNewPos(HalfEdge e) {
		Point3f newPos = e.start().getPos();
		newPos.add(e.end().getPos());
		newPos.scale(0.5f);
		
		return newPos;
	}

	public HalfEdge getEdge(){
		return this.halfEdge;
	}
	
	public Point3f getNewPos(){
		return this.newPos;
	}
	
	public float getCost(){
		return this.cost;
	}
	
	public boolean isDeleted(){
		return this.isDeleted;
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
