package assignment6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.solver.Solver;
import assignment4.LMatrices;



/**
 * As rigid as possible deformations.
 * @author Alf
 *
 */
public class RAPS_modelling {

	//ArrayList containing all optimized rotations,
	//keyed by vertex.index
	ArrayList<Matrix3f> rotations;
	
	//A copy of the original half-edge structure. This is needed  to compute the correct
	//rotation matrices.
	private HalfEdgeStructure hs_original;
	//The halfedge structure being deformed
	private HalfEdgeStructure hs_deformed;
	
	//The unnormalized cotan weight matrix, with zero rows for
	//boundary vertices.
	//It can be computed once at setup time and then be reused
	//to compute the matrix needed for position optimization
	CSRMatrix L_cotan;
	//The matrix used when solving for optimal positions
	CSRMatrix L_deform;
	
	//allocate righthand sides and x only once.
	ArrayList<Point3f> b;
	ArrayList<Point3f> x;

	//sets of vertex indices that are constrained.
	private HashSet<Integer> keepFixed;
	private HashSet<Integer> deform;
	
	// weight for user contraints
	private float constraintWeight = 100; 

	// Solver for linear equations system
	Solver solver;
	
	
	
	/**
	 * The mesh to be deformed
	 * @param hs
	 */
	public RAPS_modelling(HalfEdgeStructure hs){
		this.hs_original = new HalfEdgeStructure(hs); //deep copy of the original mesh
		this.hs_deformed = hs;
		
		this.keepFixed = new HashSet<>();
		this.deform = new HashSet<>();
		
		init_b_x(hs);
		
        // initialize rotations with identity
        rotations = new ArrayList<Matrix3f>();
        for (int i = 0; i < hs.getVertices().size(); i++) {
                Matrix3f id = new Matrix3f();
                id.setIdentity();
                rotations.add(id);
        }
		
		L_cotan = LMatrices.mixedCotanLaplacian(hs, false);
		
	}
	
	/**
	 * Set which vertices should be kept fixed. 
	 * @param verts_idx
	 */
	public void keep(Collection<Integer> verts_idx) {
		this.keepFixed.clear();
		this.keepFixed.addAll(verts_idx);

	}
	
	/**
	 * constrain these vertices to the new target position
	 */
	public void target(Collection<Integer> vert_idx){
		this.deform.clear();
		this.deform.addAll(vert_idx);
	}
	
	
	/**
	 * update the linear system used to find optimal positions
	 * for the currently constrained vertices.
	 * Good place to do the cholesky decompositoin
	 */
	public void updateL() {
		
		CSRMatrix userConstraints = getConstraintsMatrix();
		
		// build L_deform as L_deform = (transpose(L_cotan) * L_cotan  + weights^2 * userConstraints) 
		// Eq. (2) on exercise sheet
		L_deform = new CSRMatrix(0,L_cotan.nCols);
		
		CSRMatrix LTransposed = L_cotan.transposed();
		
		CSRMatrix temp = new CSRMatrix(0, L_cotan.nCols);
		LTransposed.multParallel(L_cotan, temp);
		
		L_deform.add(temp, userConstraints);
		
		solver = new Cholesky(L_deform);
	}

	private CSRMatrix getConstraintsMatrix() {
		// create matrix containing weighted user constraints
		CSRMatrix userConstraints = new CSRMatrix(0,L_cotan.nCols);
		
		for (Vertex vert : hs_original.getVertices()){
			int idx = vert.index;
			userConstraints.addRow();
		
			if(keepFixed.contains(idx) || deform.contains(idx)){
				userConstraints.lastRow().add(new col_val(idx,(float)Math.pow(constraintWeight,2)));
			}
		}
		return userConstraints;
	}
	
	/**
	 * The RAPS modelling algorithm.
	 * @param t
	 * @param nRefinements
	 */
	public void deform(Matrix4f t, int nRefinements){
		this.transformTarget(t);
		
		for (int i = 0; i < nRefinements; i++){
			optimalPositions();
			optimalRotations();
		}
	}
	

	/**
	 * Method to transform the target positions and do nothing else.
	 * @param t
	 */
	public void transformTarget(Matrix4f t) {
		for(Vertex v : hs_deformed.getVertices()){
			if(deform.contains(v.index)){
				t.transform(v.getPos());
			}
		}
	}
	
	
	/**
	 * ArrayList keyed with the vertex indices.
	 * @return
	 */
	public ArrayList<Matrix3f> getRotations() {
		return rotations;
	}

	/**
	 * Getter for undeformed version of the mesh
	 * @return
	 */
	public HalfEdgeStructure getOriginalCopy() {
		return hs_original;
	}
	
	

	/**
	 * initialize b and x
	 * @param hs
	 */
	private void init_b_x(HalfEdgeStructure hs) {
		b = new ArrayList<Point3f>();
		x = new ArrayList<Point3f>();
			for(int j = 0; j < hs.getVertices().size(); j++){
				b.add(new Point3f(0,0,0));
				x.add(new Point3f(0,0,0));
			}
	}
	
	
	
	/**
	 * Compute optimal positions for the current rotations.
	 */
	public void optimalPositions(){
		compute_b();

		solver.solveTuple(L_deform, b, x);
		
		for (Vertex vert : hs_deformed.getVertices()){
			vert.setPos(x.get(vert.index));
		}
	}
	

	/**
	 * compute the righthand side for the position optimization
	 */
	private void compute_b() {
		CSRMatrix LTransposed = L_cotan.transposed();
		ArrayList<Point3f> Ltb = new ArrayList<Point3f>();
		
		reset_b();
		
		for( Vertex vert : hs_original.getVertices()){
			Iterator<HalfEdge> iter = vert.iteratorVE();
			while(iter.hasNext()){
				HalfEdge edge = iter.next();
				Matrix3f rotation = new Matrix3f(rotations.get(edge.start().index));
				rotation.add(rotations.get(edge.end().index));
				Vector3f vec = edge.toVec();
				rotation.transform(vec);
				
				float alpha = Math.max(edge.getOppositeAngle(),1e-2f);
				float beta = Math.max(edge.getOpposite().getOppositeAngle(),1e-2f);
				float cotanWeight = (cot(alpha) + cot(beta));
				vec.scale(cotanWeight*-0.5f);
				b.get(vert.index).add(vec);
			}
		}
		
		LTransposed.multPoints(b, Ltb);
		
		CSRMatrix constraintMatrix = getConstraintsMatrix();
		
		ArrayList<Point3f> constrainedPoints = new ArrayList<Point3f>();
		
		ArrayList<Point3f> points = new ArrayList<Point3f>();
		for(Vertex vert : hs_deformed.getVertices()){
			points.add(vert.getPos());
		}
		constraintMatrix.multPoints(points, constrainedPoints);
		
		for (int i = 0; i<b.size(); i++){
			Ltb.get(i).add(constrainedPoints.get(i));
		}
		
		b = Ltb;
	}

	// TODO: refactor!
	private static float cot(float angle){
		return 1.f/(float)(Math.tan(angle));
	}

	/**
	 * helper method
	 */
	private void reset_b() {
		for(Point3f p : b){
			p.set(0, 0, 0);
		}
	}


	/**
	 * Compute the optimal rotations for 1-neighborhoods, given
	 * the original and deformed positions.
	 */
	public void optimalRotations() {
		//for the svd.
		Linalg3x3 l = new Linalg3x3(10);// argument controls number of iterations for ed/svd decompositions 
										//3 = very low precision but high speed. 3 seems to be good enough
			
		//Note: slightly better results are achieved when the absolute of cotangent
		//weights w_ij are used instead of plain cotangent weights.		
			
		//TODO
		
	}

	


	
	

	private void compute_ppT(Vector3f p, Vector3f p2, Matrix3f pp2T) {
		assert(p.x*0==0);
		assert(p.y*0==0);
		assert(p.z*0==0);

		pp2T.m00 = p.x*p2.x; pp2T.m01 = p.x*p2.y; pp2T.m02 = p.x*p2.z; 
		pp2T.m10 = p.y*p2.x; pp2T.m11 = p.y*p2.y; pp2T.m12 = p.y*p2.z; 
		pp2T.m20 = p.z*p2.x; pp2T.m21 = p.z*p2.y; pp2T.m22 = p.z*p2.z; 

	}


	
	




}
