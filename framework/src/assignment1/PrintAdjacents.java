package assignment1;

import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.Iterator;

import meshes.Face;
import meshes.HalfEdge;
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
		
		Vertex v = hs.getVertices().get(3);
		printIncidentEdges(v);
		printAdjacentVertices(v);
		printIncidentFaces(v);
	}

	private static void printIncidentFaces(Vertex v) {
		System.out.println("Vertex: " + v.toString());
		Iterator<Face> iter = v.iteratorVF();
		System.out.println("Adjacent faces: \n");
		if (!iter.hasNext())
			System.out.println("No adjacent faces");
		while( iter.hasNext()){
			System.out.println(iter.next());
		}
		System.out.println("=====================================");
	}

	private static void printAdjacentVertices(Vertex v) {
		System.out.println("Vertex: " + v.toString());
		Iterator<Vertex> iter = v.iteratorVV();
		System.out.println("Adjacent vertices: \n");
		if (!iter.hasNext())
			System.out.println("No adjacent vertices");
		while( iter.hasNext()){
			System.out.println(iter.next());
		}
		System.out.println("=====================================");		
	}

	private static void printIncidentEdges(Vertex v) {
		System.out.println("Vertex: " + v.toString());
		Iterator<HalfEdge> iter = v.iteratorVE();
		System.out.println("Incident edges: \n");
		if (!iter.hasNext())
			System.out.println("No incident edges");
		while( iter.hasNext()){
			System.out.println(iter.next());
		}
		System.out.println("=====================================");
		
	}
}
