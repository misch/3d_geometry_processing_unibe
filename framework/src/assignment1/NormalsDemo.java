package assignment1;

import glWrapper.GLHalfEdgeStructure;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class NormalsDemo {
	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/bunny5k.obj", true);
		
		// half-edge structure
		HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}

		// Wrapper for openGL 
		GLHalfEdgeStructure glDragon = new GLHalfEdgeStructure(hs);
		
		// shader
		glDragon.configurePreferredShader(
				"shaders/normals.vert", 
				"shaders/normals.frag", 
				null);
		
		//add the data to the display
		MyDisplay disp = new MyDisplay();
		disp.addToDisplay(glDragon);
	}
}
