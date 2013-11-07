package assignment4;

import java.util.ArrayList;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;

public class ImplicitSmoother {

	public static void smooth(HalfEdgeStructure hs, float lambda, CSRMatrix laplacian){
//		CSRMatrix identity = SSDMatrices.eye(hs.getVertices().size(), hs.getVertices().size());
		
		
		laplacian.scale(-lambda);
		
		
		for (int i = 0; i < laplacian.nRows; i++) {
		    boolean hadEntry = false;
		    for (col_val entry : laplacian.rows.get(i))
		        if (entry.col == i) {
		            entry.val += 1;
		            hadEntry = true;
		        }

		    if (!hadEntry)
		        laplacian.rows.get(i).add(new col_val(i, 1));
		} 
		
		
		
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
		system.mat = laplacian;
		
		ArrayList<Float> solvedX = new ArrayList<Float>(); 
		ArrayList<Float> solvedY = new ArrayList<Float>();
		ArrayList<Float> solvedZ = new ArrayList<Float>();
		
//		Solver solver = new SciPySolver("");
		Solver solver = new JMTSolver();
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
