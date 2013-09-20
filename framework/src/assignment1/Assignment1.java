package assignment1;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;

import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author Alf
 *
 */
public class Assignment1 {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/teapot.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		
		//... do something with it, display it ....
		MyDisplay disp = new MyDisplay();
		
		//create wrapper which lays out the bunny data in a opengl conform manner 
		GLHalfEdgeStructure glTeapot = new GLHalfEdgeStructure(hs);
		
		//choose the shader for the data
		glTeapot.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", 
				null);
		
		//add the data to the display
		disp.addToDisplay(glTeapot);
		
		//do the same but choose a different shader
		GLHalfEdgeStructure glTeapot2 = new GLHalfEdgeStructure(hs);
		glTeapot2.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glTeapot2);
				
	}
	

}
