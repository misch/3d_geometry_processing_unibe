package assignment3;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point3f;

import meshes.Point2i;
import meshes.WireframeMesh;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;


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
	
	// this prevents the algorithm(s) to generete some vertices more than once
	private HashMap<Point2i, Integer> createdVertices = new HashMap<Point2i, Integer>();
	
	
	
		
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
				
		for (HashOctreeCell cell : tree.getLeafs()){
				pushCube(cell);
		}
	}
	
	
	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(ArrayList<Float> byVertex) {
		this.val = new ArrayList<Float>(byVertex);
		this.result = new WireframeMesh();
		
		
		for (HashOctreeCell cell: tree.getLeafs()) {
            float cellValue = 0;
            for(int i = 0; i < 8; i++) {
                    MarchableCube corner = cell.getCornerElement(i, tree);
                    cellValue += byVertex.get(corner.getIndex())/8;
            }
            this.val.set(cell.getIndex(), cellValue);
    }
		
		
		for (HashOctreeVertex vertex : tree.getVertices()){
			if(tree.isOnBoundary(vertex)){
				continue;
			}
			pushCube(vertex);
		}
	}
	
	/**
	 * March a single cube: compute the triangles and add them to the wireframe model
	 * @param n
	 */

	// TODO: Refactor that bullshit down there! OMG!
	// This method is so fat that it would kill the ducks in the lake to get their bread!
	private void pushCube(MarchableCube c){
		Point2i[] triangles_to_generate = new Point2i[15];
		
		for (int i = 0; i < triangles_to_generate.length; i++){
			triangles_to_generate[i] = new Point2i();
		}
		
		float[] values = new float[8];
		for (int i = 0; i < 8; i++){
			MarchableCube corner = c.getCornerElement(i, tree); 
			values[i] =  val.get(corner.getIndex());
		}
		MCTable.resolve(values, triangles_to_generate);
		
		
		int addedVertices = 0;
		int[] triangleIndices = new int[3];
		
		for(Point2i point : triangles_to_generate){
			
			// If three new vertices are available, add a new triangle to WireframeMesh.
			if (addedVertices == 3) {
				if (!degeneratedTriangle(triangleIndices)) {
					result.faces.add(triangleIndices);
				}
				addedVertices = 0;
				triangleIndices = new int[3];
			}
			
			
			// If we already added the last vertex, break
			if (point.x == -1){
				break;
				
			}
			
			// If the vertex in question has already been computed in a previous step,
			// just add the index of it to the indices for the new triangle.
			if (alreadyGenerated(c,point)){
				triangleIndices[addedVertices] = createdVertices.get(key(c,point));
			}
			else{
				MarchableCube  cornerElementA = c.getCornerElement(point.x, tree);
				MarchableCube  cornerElementB = c.getCornerElement(point.y, tree);
			
				Point3f pos = interpolatePosition(cornerElementA, cornerElementB);

				result.vertices.add(pos);
				int newIndex = result.vertices.size()-1;
				triangleIndices[addedVertices] = newIndex;
			
				createdVertices.put(key(c,point), newIndex);
			}
			addedVertices++ ;
		}
	}

	
	private boolean degeneratedTriangle(int[] triangleIndices) {
		return triangleIndices[0] == triangleIndices[1]
				|| triangleIndices[1] == triangleIndices[2]
				|| triangleIndices[0] == triangleIndices[2];
	}

	private boolean alreadyGenerated(MarchableCube c, Point2i point) {
		return createdVertices.containsKey(key(c,point));
	}

	private Point3f interpolatePosition(MarchableCube cornerElementA, MarchableCube cornerElementB) {
		Point3f pos_a = new Point3f(cornerElementA.getPosition());
		Point3f pos_b = new Point3f(cornerElementB.getPosition());
		
		float a = val.get(cornerElementA.getIndex());
		float b = val.get(cornerElementB.getIndex());			
		
		float alpha = a/(a-b);
		
		pos_a.scale(1-alpha);
		pos_b.scale(alpha);
		
		Point3f pos = pos_a;
		pos.add(pos_b);
		
		return pos;
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
