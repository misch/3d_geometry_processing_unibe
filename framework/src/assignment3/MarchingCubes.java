package assignment3;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import meshes.Point2i;
import meshes.WireframeMesh;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;


/**
 * Implement your Marching cubes algorithms here.
 * @author bertholet
 *
 */
public class MarchingCubes {
	
	//the reconstructed surface
	public WireframeMesh result;
	

	//the tree to march
	private HashOctree tree;
	//per marchable cube values
	private ArrayList<Float> val;
	
	
	
	
		
	/**
	 * Implementation of the marching cube algorithm. pass the tree
	 * and either the primary values associated to the trees edges
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree){
		this.tree = tree;
		
		
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.result = new WireframeMesh();
		
		ArrayList<HashOctreeCell> cells = tree.getLeafs();
		Point2i[] triangles_to_generate = new Point2i[15];
		
		for (HashOctreeCell cell : cells){
			float[] values = new float[8];
			for (int i = 0; i < 8; i++){
				MarchableCube corner = cell.getCornerElement(i, tree); 
				values[i] =  val.get(corner.getIndex());
			}
			MCTable.resolve(values, triangles_to_generate);
			
			
			int addedVertices = 0;
			int[] triangleIndices = new int[3];
			
			for(Point2i point : triangles_to_generate){
				if(addedVertices ==3 ){
					result.faces.add(triangleIndices);
					addedVertices = 0;
					triangleIndices = new int[3];
				}
				
				MarchableCube  cornerElementA = cell.getCornerElement(point.x, tree);
				MarchableCube  cornerElementB = cell.getCornerElement(point.y, tree);
				
				Point3f pos_a = new Point3f(cornerElementA.getPosition());
				Point3f pos_b = new Point3f(cornerElementB.getPosition());
				
				float a = val.get(cornerElementA.getIndex());
				float b = val.get(cornerElementB.getIndex());			
				
				float alpha = a/(a-b);
				
				pos_a.scale(1-alpha);
				pos_b.scale(alpha);
				
				Point3f pos = pos_a;
				pos.add(pos_b);
				
				result.vertices.add(pos);
				triangleIndices[addedVertices] = result.vertices.size()-1;
				
				addedVertices++;
			}
		}
		
		
		
	}
	
	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(ArrayList<Float> byVertex) {
		
		//TODO: do your stuff
	}
	
	/**
	 * March a single cube: compute the triangles and add them to the wireframe model
	 * @param n
	 */
	private void pushCube(MarchableCube n){
		
		//TODO: do your stuff
		
		
	}

	
	/**
	 * Get a nicely marched wireframe mesh...
	 * @return
	 */
	public WireframeMesh getResult() {
		return this.result;
	}


	/**
	 * compute a key from the edge description e, that can be used to
	 * uniquely identify the edge e of the cube n. See Assignment 3 Exerise 1-5
	 * @param n
	 * @param e
	 * @return
	 */
	private Point2i key(MarchableCube n, Point2i e) {
		Point2i p = new Point2i(n.getCornerElement(e.x, tree).getIndex(),
				n.getCornerElement(e.y, tree).getIndex());
		if(p.x > p.y) {
			int temp = p.x;
			p.x= p.y; p.y = temp;
		}
		return p;
	}
	

}
