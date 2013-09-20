package glWrapper;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

/**
 * This class describes how OpenGL should interprete a half-edge data structure.
 * @author Michèle Wyss
 *
 */
public class GLHalfEdgeStructure extends GLDisplayable {

	HalfEdgeStructure myMesh;
	public GLHalfEdgeStructure(HalfEdgeStructure m) {
		
		super(m.getVertices().size());
		myMesh = m;
		
		//Add Vertices
		float[] verts = new float[m.getVertices().size()*3];
		int[] ind = new int[m.getFaces().size()*3];
		
		//copy the data to the allocated arrays
		copyToArrayP3f(m.getVertices(), verts);
		
		copyToArray(m.getFaces(), ind);
		
		
		//The class GLVertexData provides the methods addElement(...) which will
		//cause the passed array to be sent to the graphics card
		//The array passed with the semantic POSITION will always be associated
		//to the position variable in the GL shaders, while arrays passed with the
		//USERSPECIFIED semantic will be associated to the name passed in the last argument
		//
		this.addElement(verts, Semantic.POSITION , 3);
		//Here the position coordinates are passed a second time to the shader as color
		this.addElement(verts, Semantic.USERSPECIFIED , 3, "color");
		
		//pass the index array which has to be conformal to the glRenderflag returned, here GL_Triangles
		this.addIndices(ind);
		
	}
	
	/**
	 * Helper method that copies the face information to the ind array
	 * @param faces
	 * @param ind
	 */
	private void copyToArray(ArrayList<Face> faces, int[] ind) {
		int i = 0, j;
		for(Face f : faces){
			Iterator<Vertex> vertexIterator = f.iteratorFV();
			while (vertexIterator.hasNext()){
				for(j=0; j < 3; j++){
					ind[i*3 + j] = vertexIterator.next().index;
				}
			}
			
			i++;
		}
	}
	
	/**
	 * Helper method that copies the vertices arraylist to the verts array
	 * @param vertices
	 * @param verts
	 */
	private void copyToArrayP3f(ArrayList<Vertex> vertices, float[] verts) {
		int i = 0;
		for(Vertex v: vertices){
			verts[i++] = v.getPos().x;
			verts[i++] = v.getPos().y;
			verts[i++] = v.getPos().z;
		}
	}

	
	/**
	 * Return the gl render flag to inform openGL that the indices/positions describe
	 * triangles
	 */
	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}


	/**
	 * No additional uniform variables are passed to the shader.
	 */
	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		
		//additional uniforms can be loaded using the function
		//glRenderContext.setUniform(name, val);
		
		//Such uniforms can be accessed in the shader by declaring them as
		// uniform <type> name;
		//where type is the appropriate type, e.g. float / vec3 / mat4 etc.
		//this method is called at every rendering pass.
	}

}
