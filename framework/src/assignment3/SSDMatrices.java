package assignment3;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import meshes.PointCloud;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
import assignment2.MortonCodes;


public class SSDMatrices {
	
	
	/**
	 * Example Matrix creation:
	 * Create an identity matrix, clamped to the provided format.
	 */
	public static CSRMatrix eye(int nRows, int nCols){
		CSRMatrix eye = new CSRMatrix(0, nCols);
		
		//initialize the identity matrix part
		for(int i = 0; i< Math.min(nRows, nCols); i++){
			eye.addRow();
			eye.lastRow().add(
						//column i, value 1
					new col_val(i,1));
		}
		//fill up the matrix with empty rows.
		for(int i = Math.min(nRows, nCols); i < nRows; i++){
			eye.addRow();
		}
		
		return eye;
	}
	
	
	/**
	 * Example matrix creation:
	 * Identity matrix restricted to boundary per vertex values.
	 */
	public static CSRMatrix Eye_octree_boundary(HashOctree tree){
		
		CSRMatrix result = new CSRMatrix(0, tree.numberofVertices());
				
		for(HashOctreeVertex v : tree.getVertices()){
			if(MortonCodes.isVertexOnBoundary(v.code, tree.getDepth())){
				result.addRow();
				result.lastRow().add(new col_val(v.index,1));
			}
		}
		
		return result;
	}
	
	/**
	 * One line per point, One column per vertex,
	 * enforcing that the interpolation of the Octree vertex values
	 * is zero at the point position.
	 *
	 */
	public static CSRMatrix D0Term(HashOctree tree, PointCloud cloud){
		
		CSRMatrix result = new CSRMatrix(0, tree.numberofVertices());
		
		for (Point3f point : cloud.points){
			HashOctreeCell cell = tree.getCell(point);
			
			Point3f relativeCoordinates = new Point3f(point);
			Point3f c_000 = new Point3f(cell.getCornerElement(0, tree).getPosition()); 	// cf. slide #37 of lecture notes "04 - Surface Reconstruction"
			relativeCoordinates.sub(c_000); 	// (x-x0,y-y0,z-z0), cf. slide #37 again
			relativeCoordinates.scale(1/cell.side); 	// = (x1-x0) = (y1-y0) = (z1-z0), cf. slide #37
			
			result.addRow();
			for (int i = 0; i<8; i++){
				float weight = (0b100 & i) == 0b100 ? relativeCoordinates.x : 1-relativeCoordinates.x;
				weight *= (0b010 & i) == 0b010 ? relativeCoordinates.y : 1-relativeCoordinates.y;
				weight *= (0b001 & i) == 0b001 ? relativeCoordinates.z : 1-relativeCoordinates.z;
				
				result.lastRow().add(new col_val(cell.getCornerElement(i, tree).getIndex(),weight));
			}			
		}
		return result;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 corresponds to the point/normal i/3.
	 * Three consecutant rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {

		CSRMatrix result = new CSRMatrix(0, tree.numberofVertices());

		for (Point3f point : cloud.points) {
			
			HashOctreeCell cell = tree.getCell(point);
			
			// Gradients:
			// The D1-Term gets multiplied by the function values later -
			// therefore only put -1 or 1 to decide which function values
			// have to be subtracted from each other.
			result.addRow();
			ArrayList<col_val> xRow =  result.lastRow();
			result.addRow();
			ArrayList<col_val> yRow =  result.lastRow();
			result.addRow();
			ArrayList<col_val> zRow =  result.lastRow();
			
			for(int i = 0; i<8; i++){
				
				int index = cell.getCornerElement(i, tree).getIndex();
				
				float gradX = ((i & 0b100) == 0b100 ? 1 : -1)/(4*cell.side);
				xRow.add(new col_val(index,gradX));
				
				
				float gradY = ((i & 0b010) == 0b010 ? 1 : -1)/(4*cell.side);
				yRow.add(new col_val(index,gradY));
				
				float gradZ = ((i & 0b001) == 0b001 ? 1 : -1)/(4*cell.side);
				zRow.add(new col_val(index,gradZ));				
			}
		}
		return result;
	}
	
	
	
	public static CSRMatrix RTerm(HashOctree tree){
		CSRMatrix result = new CSRMatrix(0, tree.numberofVertices());
		float totalScale = 0;
		
		for (HashOctreeVertex vertex : tree.getVertices()){
			for (int mask = 0b100; mask > 0; mask>>=1){
				HashOctreeVertex positiveNeigh = tree.getNbr_v2v(vertex, mask);
				HashOctreeVertex negativeNeigh = tree.getNbr_v2vMinus(vertex, mask);
				
				// if no neighbor vertex triple, go to the next direction
				if (positiveNeigh == null || negativeNeigh == null){
					continue;
				}
				
				result.addRow();
				ArrayList<col_val> row =  result.lastRow();
				
				// add the values to the row (slide 24 of Peter's explanations)
				//
				// [ ... 1 ... -dist_ij/(dist_ij+dist_kj) ... -(dist_jk)/(dist_ij+dist_kj) ]
				//       ^					^								^
				//	   col j			  col k							  col i
				//
				// j = vertex
				// k = positiveNeigh
				// i = negativeNeigh
				row.add(new col_val(vertex.getIndex(),1));
				
				float dist_ij = negativeNeigh.getPosition().distance(vertex.getPosition());
				float dist_kj = positiveNeigh.getPosition().distance(vertex.getPosition());
				
				row.add(new col_val(positiveNeigh.getIndex(),-dist_ij/(dist_ij + dist_kj)));
				row.add(new col_val(negativeNeigh.getIndex(),-dist_kj/(dist_ij + dist_kj)));
				
				totalScale += dist_ij * dist_kj;	
			}

//			int xMask = 0b100, yMask = 0b010, zMask = 0b001;
//			
//			HashOctreeVertex positiveNeighX = tree.getNbr_v2v(vertex, xMask);
//			HashOctreeVertex negativeNeighX = tree.getNbr_v2vMinus(vertex, xMask);
//			
//			HashOctreeVertex positiveNeighY = tree.getNbr_v2v(vertex, yMask);
//			HashOctreeVertex negativeNeighY = tree.getNbr_v2vMinus(vertex, yMask);
//			
//			HashOctreeVertex positiveNeighZ = tree.getNbr_v2v(vertex, zMask);
//			HashOctreeVertex negativeNeighZ = tree.getNbr_v2vMinus(vertex, zMask);
			
			
		}
		
		result.scale(1/totalScale);
		return result;
	}

	
	


	/**
	 * Set up the linear system for ssd: append the three matrices, 
	 * appropriately scaled. And set up the appropriate right hand side, i.e. the
	 * b in Ax = b
	 * @param tree
	 * @param pc
	 * @param lambda0
	 * @param lambda1
	 * @param lambda2
	 * @return
	 */
	public static LinearSystem ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda0,
			float lambda1,
			float lambda2){
		
				
		LinearSystem system = new LinearSystem();
		system.mat = null;
		system.b = null;
		return system;
	}

}
