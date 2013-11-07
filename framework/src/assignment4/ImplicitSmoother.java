package assignment4;

import java.util.ArrayList;
import java.util.Iterator;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.LinearSystem;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment3.SSDMatrices;

public class ImplicitSmoother {

	public static void smooth(HalfEdgeStructure hs, float lambda, CSRMatrix laplacian){
		CSRMatrix identity = SSDMatrices.eye(hs.getVertices().size(), hs.getVertices().size());
		
		
		laplacian.scale(-lambda);
		
		CSRMatrix result = new CSRMatrix(0, 0);
		
		result.add(laplacian, identity);
		
		LinearSystem system = new LinearSystem();
		
		ArrayList<Float> xCoord = new ArrayList<Float>();
		ArrayList<Float> yCoord = new ArrayList<Float>();
		ArrayList<Float> zCoord = new ArrayList<Float>();
		
		for (Vertex vert : hs.getVertices()){
			xCoord.add(vert.getPos().x);
			yCoord.add(vert.getPos().y);
			zCoord.add(vert.getPos().z);
		}
		system.b = xCoord;
		system.mat = result;
		
		ArrayList<Float> solvedX = new ArrayList<Float>(); 
		ArrayList<Float> solvedY = new ArrayList<Float>();
		ArrayList<Float> solvedZ = new ArrayList<Float>();
		
		Solver solver = new SciPySolver("");
		solver.solve(system, solvedX);
		
		system.b = yCoord;
		solver.solve(system, solvedY);
		
		system.b = zCoord;
		solver.solve(system, solvedZ);
		
		for(Vertex vert : hs.getVertices()){
			vert.setPos(solvedX.get(vert.index), solvedY.get(vert.index), solvedZ.get(vert.index));
		}
		

	}
}
