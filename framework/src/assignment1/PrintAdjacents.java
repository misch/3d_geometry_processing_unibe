package assignment1;

import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.Iterator;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class PrintAdjacents {

	public static void main(String[] args) throws IOException{
		//load a mesh
		WireframeMesh oneNeighborhood = ObjReader.read("./objs/oneNeighborhood.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		try {
			hs.init(oneNeighborhood);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}		
		 
		GLWireframeMesh glOneNeighborhood = new GLWireframeMesh(oneNeighborhood);
		
		// TODO
		// print that shit out
	}
}
