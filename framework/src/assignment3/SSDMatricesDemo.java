package assignment3;

import glWrapper.GLHashtree;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;

import meshes.PointCloud;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;
import openGL.MyDisplay;
import sparse.LinearSystem;
import sparse.SCIPY;
import assignment2.HashOctree;

public class SSDMatricesDemo {
	
	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		
		
		marchingCubesDemo();
		

			
	}
	
	
	public static void marchingCubesDemo() throws MeshNotOrientedException, DanglingTriangleException, IOException{
		
		PointCloud pc = ObjReader.readAsPointCloud("objs/teapot.obj", true);
//		PointCloud pc = PlyReader.readPointCloud("objs/angel_points.ply", true);
		pc.normalizeNormals();
		HashOctree tree = new HashOctree(pc, 8, 1, 1.1f);
		tree.refineTree(2);
		
		float lambda0 = 10;
		float lambda1 = 0.01f;
		float lambdaR = 1;
		
		LinearSystem system = SSDMatrices.ssdSystem(tree, pc, lambda0, lambda1, lambdaR);
		//Test Data: create an octree
		ArrayList<Float> functionByVertex = new ArrayList<Float>();
		SCIPY.solve(system, "whatev", functionByVertex);
		//System.out.println(functionByVertex);
		
		MarchingCubes mc = new MarchingCubes(tree);
		mc.dualMC(functionByVertex);
		WireframeMesh mesh = mc.result;
		GLWireframeMesh glMesh = new GLWireframeMesh(mesh);
		//And show off...
		
		//visualization of the per vertex values (blue = negative, 
		//red = positive, green = 0);
		MyDisplay d = new MyDisplay();
		
		glMesh.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		d.addToDisplay(glMesh);
		
		//discrete approximation of the zero level set: render all
		//tree cubes that have negative values.
		GLHashtree gltree = new GLHashtree(tree);
		gltree.addFunctionValues(functionByVertex);
		gltree.configurePreferredShader("shaders/octree_zro.vert", 
				"shaders/octree_zro.frag", "shaders/octree_zro.geom");
		d.addToDisplay(gltree);
	}
}
