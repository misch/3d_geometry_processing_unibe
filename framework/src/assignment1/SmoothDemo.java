package assignment1;

import glWrapper.GLHalfEdgeStructure;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class SmoothDemo {
	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
				
		//create wrapper which lays out the bunny data in a opengl conform manner 
		GLHalfEdgeStructure glObject = new GLHalfEdgeStructure(hs);
		GLHalfEdgeStructure glSmoothed1 = new GLHalfEdgeStructure(hs);
		GLHalfEdgeStructure glSmoothed2 = new GLHalfEdgeStructure(hs);
		GLHalfEdgeStructure glSmoothed3 = new GLHalfEdgeStructure(hs);
		GLHalfEdgeStructure glSmoothed4 = new GLHalfEdgeStructure(hs);
		GLHalfEdgeStructure glSmoothed5 = new GLHalfEdgeStructure(hs);
		GLHalfEdgeStructure glSmoothed40 = new GLHalfEdgeStructure(hs);
		
		// smoothing:	always just 1 because the values of the vertex position
		// 				get overwritten during the smoothing
		glSmoothed1.smooth(1);
		glSmoothed2.smooth(1);
		glSmoothed3.smooth(1);
		glSmoothed4.smooth(1);
		glSmoothed5.smooth(1);
		glSmoothed40.smooth(35);
		
		
		// shaders
		setShader("trimesh_flat", glObject);
		setShader("trimesh_flat", glSmoothed1);
		setShader("trimesh_flat", glSmoothed2);
		setShader("trimesh_flat", glSmoothed3);
		setShader("trimesh_flat", glSmoothed4);
		setShader("trimesh_flat", glSmoothed5);
		setShader("trimesh_flat", glSmoothed40);
		
		
		
		//add the data to the display
		MyDisplay disp = new MyDisplay();
		
		disp.addToDisplay(glObject);
		disp.addToDisplay(glSmoothed1);
		disp.addToDisplay(glSmoothed2);
		disp.addToDisplay(glSmoothed3);
		disp.addToDisplay(glSmoothed4);
		disp.addToDisplay(glSmoothed5);
		disp.addToDisplay(glSmoothed40);
				
	}
	
	private static void setShader(String shader, GLHalfEdgeStructure obj){
		obj.configurePreferredShader("shaders/"+shader+".vert", "shaders/"+shader+".frag", "shaders/"+shader+".geom");
	}

}