package glWrapper;

import java.util.ArrayList;

import javax.media.opengl.GL;

import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;

/**
 * Simple GLWrapper for the {@link HashOctree}.
 * The octree is sent to the Gpu as set of cell-center points and
 * side lengths. 
 * @author Alf
 *
 */
public class GLHashtreeAdjacentCells extends GLDisplayable {

	private HashOctree myTree;
	public GLHashtreeAdjacentCells(HashOctree tree) {
		
		super(6*tree.numberOfLeafs());
		this.myTree = tree;
		// Add Vertices
		// Multiplication by 6 because every vertex will be sent into the shader 6 times,
		// with one of the possibly 6 neighbor cells.
		float[] verts = new float[6*myTree.numberOfLeafs()*3];
		float[] neighborCells = new float[6*myTree.numberOfLeafs()*3];
		
		
		int idx = 0;
		for(HashOctreeCell n : tree.getLeafs()){
			 for (HashOctreeCell neighborCell : myTree.getNeighborCells(n)){
			 		verts[idx] = n.center.x;
					verts[idx+1] = n.center.y;
					verts[idx+2] = n.center.z;
			 		neighborCells[idx] = neighborCell.center.x;
					neighborCells[idx+1] = neighborCell.center.y;
			 		neighborCells[idx+2] = neighborCell.center.z;
			 		
			 		idx += 3;
			 }
		}
		
		int[] ind = new int[myTree.numberOfLeafs()*6];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		this.addElement(verts, Semantic.POSITION , 3);
		
		// Call the variable "pointTo" (instead of e.g. "neighbor" in order to use 
		// the standard octree vertex shader.
		this.addElement(neighborCells, Semantic.USERSPECIFIED , 3, "pointTo");
		
		this.addIndices(ind);
		
	}
	
	/**
	 * values are given by OctreeVertex
	 * @param values
	 */
	public void addFunctionValues(ArrayList<Float> values){
		float[] vals = new float[myTree.numberOfLeafs()];
		
		for(HashOctreeCell n: myTree.getLeafs()){
			for(int i = 0; i <=0b111; i++){
				vals[n.leafIndex] += values.get(myTree.getNbr_c2v(n, i).index);//*/Math.signum(values.get(myTree.getVertex(n, i).index));
			}
			vals[n.leafIndex] /=8;
			//vals[n.leafIndex] = Math.abs(vals[n.leafIndex]) < 5.99 ? -1: 1;
		}
		
		this.addElement(vals, Semantic.USERSPECIFIED , 1, "func");
	}

	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		
	}
}
