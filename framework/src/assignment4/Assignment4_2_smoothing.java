package assignment4;

import glWrapper.GLHalfEdgeStructure;

import java.io.IOException;

import openGL.MyDisplay;
import sparse.CSRMatrix;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;


/**
 * Smoothing
 * @author Alf
 *
 */
public class Assignment4_2_smoothing {

	
	public static void main(String args[]) throws IOException, MeshNotOrientedException, DanglingTriangleException{
	
		WireframeMesh m = ObjReader.read("objs/head.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(m);
		
		CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs);
		
		float vol_old = hs.getVolume();
		
		ImplicitSmoother.smooth(hs, 0.05f, laplacian);
		
		float vol_new = hs.getVolume();
		System.out.println(vol_old/vol_new);
		
		MyDisplay disp = new MyDisplay();

		for(Vertex vert : hs.getVertices()){
			vert.getPos().scale((float)Math.pow(vol_old/vol_new,(1f/3)));
		}
		
		GLHalfEdgeStructure glHs = new GLHalfEdgeStructure(hs);
		

		
		glHs.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glHs);

		
	}
	 

}
