package glWrapper;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import meshes.Face;
import meshes.HEData3d;
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
		
		sendToOpenGL();
		
	}

	public void sendToOpenGL(){
		//Add Vertices
				float[] verts = new float[myMesh.getVertices().size()*3];
				float[] valences = new float[myMesh.getVertices().size()];
				int[] ind = new int[myMesh.getFaces().size()*3];
				
				//copy the data to the allocated arrays
				
				copyToArrayP3f(myMesh.getVertices(), verts);
				
				copyToArrayValence(myMesh.getVertices(), valences);
				
				copyToArray(myMesh.getFaces(), ind); 
				
					
				
				//The class GLVertexData provides the methods addElement(...) which will
				//cause the passed array to be sent to the graphics card
				//The array passed with the semantic POSITION will always be associated
				//to the position variable in the GL shaders, while arrays passed with the
				//USERSPECIFIED semantic will be associated to the name passed in the last argument
				//
				this.addElement(verts, Semantic.POSITION , 3);
				//Here the position coordinates are passed a second time to the shader as color
				this.addElement(verts, Semantic.USERSPECIFIED , 3, "color");
				
				this.addElement(valences, Semantic.USERSPECIFIED, 1, "valence");
				
				//pass the index array which has to be conformal to the glRenderflag returned, here GL_Triangles
				this.addIndices(ind);
	}
	/**
	 * Helper method that copies the valence information to the valences array
	 * @param vertices
	 * @param valences
	 */
	private void copyToArrayValence(ArrayList<Vertex> vertices, float[] valences) {
		int i = 0;
		for(Vertex v: vertices){
			valences[i++] = v.getValence();
		}
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
	
	public void smooth(int iterations){
		ArrayList<Vertex> vertices = myMesh.getVertices();
		HEData3d smoothedVerts = new HEData3d(myMesh);
		
		for (int i = 0; i<iterations; i++){
			for (Vertex v: vertices){
				Iterator<Vertex> iter = v.iteratorVV();
	
				Point3f sum = new Point3f(0, 0, 0);
				int numberOfVertices = 0;
				
				while (iter.hasNext()){
					sum.add(iter.next().getPos());
					numberOfVertices++;
				}
				
				sum.scale(1.f/(float)(numberOfVertices));
				smoothedVerts.put(v, sum);
			}	
			// This will overwrite the actual HalfEdgeStructure.
			// Maybe I'll stop ignoring that being funky later.
			// Because. Funky. Is. Okay. Too.
			for (Vertex v : myMesh.getVertices()){
				Tuple3f smoothed = smoothedVerts.get(v);
				v.setPos(smoothed.x,smoothed.y,smoothed.z);
			}
		}
		sendToOpenGL();
	}

}
