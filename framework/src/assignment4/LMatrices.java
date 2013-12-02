package assignment4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

/**
 * Methods to create different flavours of the cotangent and uniform laplacian.
 * @author Alf
 *
 */
public class LMatrices {
	
	/**
	 * The uniform Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix uniformLaplacian(HalfEdgeStructure hs){
		CSRMatrix laplace = new CSRMatrix(0,hs.getVertices().size());
		
		for(Vertex vert : hs.getVertices()){
			int valence = vert.getValence();
			
			laplace.addRow();
			laplace.lastRow().add(new col_val(vert.index,-1));
			
			Iterator<Vertex> iter = vert.iteratorVV();
			while(iter.hasNext()){
				laplace.lastRow().add(new col_val(iter.next().index,1.f/valence));
			}
			Collections.sort(laplace.lastRow());
		}
		return laplace;
	}
	
	/**
	 * The cotangent Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs, boolean weighted){
		CSRMatrix laplace = new CSRMatrix(0,hs.getVertices().size());
		
		for(Vertex vert : hs.getVertices()){
			Iterator<HalfEdge> iter = vert.iteratorVE();
			laplace.addRow();
			
			if(vert.isOnBoundary()){
				continue;
			}
			float area = vert.getMixedArea();
			float sum = 0;
			while(iter.hasNext()){
				HalfEdge edge = iter.next();
				
					float alpha = Math.max(edge.getOppositeAngle(),1e-2f);
					float beta = Math.max(edge.getOpposite().getOppositeAngle(),1e-2f);
					Vector3f v = edge.toVec();
					
					float weight = (cot(alpha) + cot(beta));
					
					if (weighted){
						weight /= (2*area);
					}
					sum += weight;
					laplace.lastRow().add(new col_val(edge.start().index,weight));
			}
			
			laplace.lastRow().add(new col_val(vert.index,-sum));
			Collections.sort(laplace.lastRow());
		}
		
		return laplace;
	}
	
	
	/**
	 * The cotangent Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs){
		return mixedCotanLaplacian(hs,true);
	}
		
	// TODO: refactor!
	private static float cot(float angle){
		return 1.f/(float)(Math.tan(angle));
	}
	
	/**
	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
	 * @param hs
	 * @return
	 */
	public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs){
		return null;
	}
	
	
	/**
	 * helper method to multiply x,y and z coordinates of the halfedge structure at once
	 * @param m
	 * @param s
	 * @param res
	 */
	public static void mult(CSRMatrix m, HalfEdgeStructure s, ArrayList<Vector3f> res){
		ArrayList<Float> x = new ArrayList<>(), b = new ArrayList<>(s.getVertices().size());
		x.ensureCapacity(s.getVertices().size());
		
		res.clear();
		res.ensureCapacity(s.getVertices().size());
		for(Vertex v : s.getVertices()){
			x.add(0.f);
			res.add(new Vector3f());
		}
		
		for(int i = 0; i < 3; i++){
			
			//setup x
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					x.set(v.index, v.getPos().x);	
					break;
				case 1:
					x.set(v.index, v.getPos().y);	
					break;
				case 2:
					x.set(v.index, v.getPos().z);	
					break;
				}
				
			}
			
			m.mult(x, b);
			
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					res.get(v.index).x = b.get(v.index);	
					break;
				case 1:
					res.get(v.index).y = b.get(v.index);	
					break;
				case 2:
					res.get(v.index).z = b.get(v.index);	
					break;
				}
				
			}
		}
	}
}
