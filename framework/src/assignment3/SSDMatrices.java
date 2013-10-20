package assignment3;

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
			
			// interpolation along x
			
			Point3f c_00 = new Point3f();
			c_00.x = c_000.x*(1-relativeCoordinates.x) + cell.getCornerElement(0b100, tree).getPosition().x * relativeCoordinates.x;
			c_00.y = c_000.y*(1-relativeCoordinates.x) + cell.getCornerElement(0b100, tree).getPosition().y * relativeCoordinates.x;
			c_00.z = c_000.z*(1-relativeCoordinates.x) + cell.getCornerElement(0b100, tree).getPosition().z * relativeCoordinates.x;
			
			Point3f c_10 = new Point3f();
			c_10.x = cell.getCornerElement(0b010, tree).getPosition().x*(1-relativeCoordinates.x) + cell.getCornerElement(0b110, tree).getPosition().x * relativeCoordinates.x;
			c_10.y = cell.getCornerElement(0b010, tree).getPosition().y*(1-relativeCoordinates.x) + cell.getCornerElement(0b110, tree).getPosition().y * relativeCoordinates.x;
			c_10.z = cell.getCornerElement(0b010, tree).getPosition().z*(1-relativeCoordinates.x) + cell.getCornerElement(0b110, tree).getPosition().z * relativeCoordinates.x;
			
			Point3f c_01 = new Point3f();
			c_01.x = cell.getCornerElement(0b001, tree).getPosition().x*(1-relativeCoordinates.x) + cell.getCornerElement(0b101, tree).getPosition().x * relativeCoordinates.x;
			c_01.y = cell.getCornerElement(0b001, tree).getPosition().y*(1-relativeCoordinates.x) + cell.getCornerElement(0b101, tree).getPosition().y * relativeCoordinates.x;
			c_01.z = cell.getCornerElement(0b001, tree).getPosition().z*(1-relativeCoordinates.x) + cell.getCornerElement(0b101, tree).getPosition().z * relativeCoordinates.x;
			
			Point3f c_11 = new Point3f();
			c_10.x = cell.getCornerElement(0b011, tree).getPosition().x*(1-relativeCoordinates.x) + cell.getCornerElement(0b111, tree).getPosition().x * relativeCoordinates.x;
			c_10.y = cell.getCornerElement(0b011, tree).getPosition().y*(1-relativeCoordinates.x) + cell.getCornerElement(0b111, tree).getPosition().y * relativeCoordinates.x;
			c_10.z = cell.getCornerElement(0b011, tree).getPosition().z*(1-relativeCoordinates.x) + cell.getCornerElement(0b111, tree).getPosition().z * relativeCoordinates.x;
			
			// interpolation along y
			
			Point3f c_1 = new Point3f();
			c_1.x = c_01.x * (1-relativeCoordinates.y) + c_11.x * relativeCoordinates.y;
			c_1.y = c_01.y * (1-relativeCoordinates.y) + c_11.y * relativeCoordinates.y;
			c_1.z = c_01.z * (1-relativeCoordinates.y) + c_11.z * relativeCoordinates.y;
			
			Point3f c_0 = new Point3f();
			c_0.x = c_00.x * (1-relativeCoordinates.y) + c_10.x * relativeCoordinates.y;
			c_0.y = c_00.y * (1-relativeCoordinates.y) + c_10.y * relativeCoordinates.y;
			c_0.z = c_00.z * (1-relativeCoordinates.y) + c_10.z * relativeCoordinates.y;
			
			// interpolation along z
			
			Point3f c = new Point3f();
			c.x = c_0.x * (1-relativeCoordinates.z) + c_1.x * relativeCoordinates.z;
			c.y = c_0.y * (1-relativeCoordinates.z) + c_1.y * relativeCoordinates.z;
			c.z = c_0.z * (1-relativeCoordinates.z) + c_1.z * relativeCoordinates.z;
			
			
		}
		return null;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 corresponds to the point/normal i/3.
	 * Three consecutant rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {
		
		//TODO
		
		return null;
	}
	
	
	
	public static CSRMatrix RTerm(HashOctree tree){
		
		//TODO
		
		return null;
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
