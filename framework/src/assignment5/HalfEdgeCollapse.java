package assignment5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;


/**
 * This class handles efficient half-edge collapsing on a 
 * half-edge structure.
 * @author Alf
 *
 */
public class HalfEdgeCollapse {
	
	//collect the obsolete elements
	public HashSet<HalfEdge> deadEdges;
	public HashSet<Vertex> deadVertices;
	public HashSet<Face> deadFaces;
	
	//the half-edge structure we work on
	private HalfEdgeStructure hs;

	//store the original face normals for fold-over prevention
	public HashMap<Face, Vector3f> oldFaceNormals; 
	
	//how strongly the normals are constrained to prevent flips.
	//A flip will be detected, if after a collapse oldNormal.dot(newNormal) < flipConst.
	private static final float flipConst = 0.1f;//-0.8f;
	
	
	/**
	 * 
	 * Initiate class variables...
	 * 
	 * @param hs
	 */
	public HalfEdgeCollapse(HalfEdgeStructure hs){
		this.hs = hs;
		this.deadEdges = new HashSet<>();
		this.deadVertices = new HashSet<>();
		this.deadFaces = new HashSet<>();
		
		this.oldFaceNormals = new HashMap<>();
		for(Face f : hs.getFaces()){
			
			//NAN/infinite Normals.
			//f.normal should be the face normal,
			//simply computed by cross(edge1, edge2).normalize
			if(f.normal().length()*0 != 0){
				oldFaceNormals.put(f, new Vector3f(0,0,0));
			}
			else{
				//f.normal should be the face normal,
				//simply computed by cross(edge1, edge2).normalize
				oldFaceNormals.put(f, f.normal());
			}
		}
		
		for(Face f: hs.getFaces()){
			assert(oldFaceNormals.get(f).length()*0 == 0);
		}
		
	}


	void collapseEdge(HalfEdge e){
		collapseEdge(e, e.end().getPos());
	}
	/**
	 * collapse a single halfedge, but don't remove the dead halfedges faces and vertices from the
	 * halfedges structure.
	 * @param e
	 * @param hs
	 */
	void collapseEdge(HalfEdge e, Point3f newPos){
		
		
		//First step:
		//relink the vertices to safe edges. don't iterate 
		//around e.end() before the collapse is finished.
		makeV2ERefSafe(e);
		
		e.end().setPos(newPos);
		
		deadVertices.add(e.start());
		// Assign new end-vertex to the incident edges at the 
		// soon deleted vertex
		Iterator<HalfEdge> iter = e.start().iteratorVE();
		ArrayList<HalfEdge> edges = new ArrayList<HalfEdge>();
		
		while(iter.hasNext()){
			edges.add(iter.next());
		}
		
		for(HalfEdge toChange : edges){
			toChange.setEnd(e.end());
		}
		
		
		// relink edges
		if (e.hasFace()){
			relink(e);
		}
		else{
			relinkBoundary(e);
		}
		
		if (e.getOpposite().hasFace()){
			relink(e.getOpposite());
		}
		else{
			relinkBoundary(e.getOpposite());
		}
		
		//Do a lot of assertions while debugging, either here
		//or in the calling method... ;-)
		//If something is wrong in the half-edge structure it is awful
		//to detect what it is that is wrong...

	}
	
	private void relink(HalfEdge edge){
		HalfEdge startOut = edge.getPrev().getOpposite();
		HalfEdge endIn = edge.getNext().getOpposite();
		endIn.setOpposite(startOut);
		startOut.setOpposite(endIn);
		
		Face deadFace = edge.getFace();
		deadFaces.add(deadFace);
		Iterator<HalfEdge> iter = deadFace.iteratorFE();
		while(iter.hasNext()){
			deadEdges.add(iter.next());
		}
	}
	
	private void relinkBoundary(HalfEdge edge){
		edge.getNext().setPrev(edge.getPrev());
		edge.getPrev().setNext(edge.getNext());
		deadEdges.add(edge);
	}
	

	/**
	 * collapse a single halfedge, remove all obsolete elements and re-enumerate
	 * the remaining vertices. Inefficient method.
	 * @param e
	 * @param hs
	 */
	public void collapseEdgeAndDelete(HalfEdge e){
		collapseEdge(e);
		finish();
	}
	
	/**
	 * Delete the collected dead vertices, faces, edges and tidy up the Halfedge structure.
	 */
	void finish(){
				
		hs.getFaces().removeAll(deadFaces);
		hs.getVertices().removeAll(deadVertices);
		hs.getHalfEdges().removeAll(deadEdges);
		hs.enumerateVertices();
		
		
		assertEdgesOk(hs);
		assertVerticesOk(hs);
	}
	
	
	
	
	
	
	/**
	 * Tests if an edge is collapsable without producing a invalid mesh
	 * or a mesh with a different topology, as discussed in the exercise session.
	 * @param e
	 * @return
	 */
	public static boolean isEdgeCollapsable(HalfEdge e){
		//1-neighborhood(e.start) \cap 1-neighborhood(e.end)
//		System.out.println("In isEdgeCollapsable");
		int commonNeighbors = 0;
		
		Iterator<Vertex> it_a = e.start().iteratorVV();
		Iterator<Vertex> it_b;
		while(it_a.hasNext()){
			Vertex nb_a = it_a.next();
			Vertex nb_b;
			it_b = e.end().iteratorVV();
			while(it_b.hasNext()){
				nb_b = it_b.next();
//				System.out.println(nb_b.toString());
				commonNeighbors += (nb_b == nb_a ? 1 : 0);
			}
		}
		
		//dont produce dangling edges!
		if(e.start().isOnBoundary() && e.end().isOnBoundary() && ! e.isOnBorder()){
			return false;
		}
		//don't delete the last triangle
		if(!e.hasFace() && e.getNext().getNext() == e.getPrev()){
			return false;
		}
		if(!e.getOpposite().hasFace() && e.getOpposite().getNext().getNext() == e.getOpposite().getPrev()){
			return false;
		}
//		System.out.println("Done with isEdgeCollapsable");
		return commonNeighbors == (e.isOnBorder()? 1: 2);
		
	}
	
	
	
	/**
	 * Simple, unrefined, heuristic mesh inversion detection
	 * @param e
	 * @param newPos
	 * @return
	 */
	public boolean isCollapseMeshInv(HalfEdge e, Point3f newPos){
		return isFaceFlipStart(e, newPos) || isFaceFlipStart(e.getOpposite(), newPos);
	}

	
	
	/**
	 * Helper method, will check for face flips around e.start().
	 * @param e
	 * @param newPos
	 * @return
	 */
	private boolean isFaceFlipStart(HalfEdge e, Point3f newPos) {
		HalfEdge current, next, first; 
		Vector3f e1 = new Vector3f(), e2 = new Vector3f(), n1 = new Vector3f(), n2 = new Vector3f();

		first = current = e.start().getHalfEdge();
		next= null;
		while(next!= first){
		  //current = it.next();
		  next = current.getPrev().getOpposite();
		  if(next == e || current == e || ! current.hasFace()){
		    current = next;
		    continue;
		  }
		  n1.set(oldFaceNormals.get(current.getFace()));

		  e1.set(newPos); e1.negate();
		  e1.add(current.end().getPos());
		  e2.set(newPos); e2.negate();
		  e2.add(next.end().getPos());
		  n2.cross(e1, e2);

		  //Degenerated faces have 0 normals and flips on
		  //them will not be detected.
		  if(n1.length() > 0 && n2.length() > 0 &&
		    n1.dot(n2)/n1.length()/n2.length() < flipConst){
		    return true;
		  }
		  current = next;
		}
		return false;

		}
	
	
	/**
	 * This helper method will make sure that none of the vertices reference an edge
	 * that becomes obsolete during the (half) edge collapse e.start() -> e.end().
	 * <p>
	 * Note that in some cases there exists no non-obsolete-becoming edge at the vertex e.end() that points away
	 * from e.end(), namely in the case of an edge collapse on a boundary triangle: __/\__
	 * In that case, e.end() is set to reference an appropriate edge from e.start(); this means that
	 * after calling this method, until the edge is completely collapsed, iterators around e.end() will behave
	 * irregularly.
	 * </p>
	 * @param e
	 */
	private void makeV2ERefSafe(HalfEdge e){
		
		HalfEdge e_opp = e.getOpposite();
		Vertex b = e.end();
		Vertex c = e.getNext().end();
		Vertex d = e_opp.getNext().end();
		
		//find an edge at vertex b which does not become obsolete,
		//if this is impossible, an edge from a is taken!
		if(e.hasFace()){
			b.setHalfEdge(e.getNext().getOpposite().getNext());
			if(e.getNext().getOpposite().getNext() == e.getOpposite()){
				b.setHalfEdge(e_opp.getNext().getOpposite().getNext());
			}
		}
		//handle the case of e being on the outer boundary 
		else{
			b.setHalfEdge(e.getNext());
		}
		
		//find non-obsolete edges at the vertices c and d
		if(e.hasFace()){
			c.setHalfEdge(e.getNext().getOpposite());
		}
		if(e_opp.hasFace()){
			d.setHalfEdge(e.getOpposite().getNext().getOpposite());
		}
	}
	

	
	/**
	 * is v obsolete?
	 * @param v
	 * @return
	 */
	public boolean isVertexDead(Vertex v){
		return deadVertices.contains(v);
	}
	
	/**
	 * is e obsolete?
	 * @param e
	 * @return
	 */
	public boolean isEdgeDead(HalfEdge e){
		return deadEdges.contains(e);
	}
	
	/**
	 * is f obsolete?
	 * @param f
	 * @return
	 */
	public boolean isFaceDead(Face f) {
		return deadFaces.contains(f);
	}

	/**
	 * Valueable assertion
	 * @param vs
	 */
	private void assertVerticesDontRefZombies(Vertex... vs ) {
		for(Vertex v : vs){
			assert(!deadEdges.contains(v.getHalfEdge()));
		}
	}
	
	
	/**
	 * Valueable assertions.
	 * @param h
	 */
	public void assertEdgesOk(HalfEdgeStructure h) {
		for(HalfEdge e: h.getHalfEdges()){
			if(!isEdgeDead(e)){
				assert(e.start() != e.end());
				assert(e.getOpposite().getOpposite() == e);
				assert(e.getPrev().end() == e.start());
				assert(e.getNext().start() == e.end());
				assert(e.getPrev().end() != e.getNext().start());
				
				assert(e == e.getNext().getPrev());
				assert(e == e.getPrev().getNext());
				
				assert(e.getFace() == e.getNext().getFace());
				assert(e.getFace() == e.getPrev().getFace());
				
				assert(!deadEdges.contains(e.getPrev()));
				assert(!deadEdges.contains(e.getNext()));
				assert(!deadEdges.contains(e.getOpposite()));
				
				assert(e.getFace() == null ||!isFaceDead(e.getFace()));
				assert(!isVertexDead(e.end()));
				assert(!isVertexDead(e.start()));
			}
		}
	}
	
	/**
	 * Valueable assertions.
	 * @param h
	 */
	public  void assertVerticesOk(HalfEdgeStructure h) {
		for(Vertex v: h.getVertices()){
			if(!isVertexDead(v)){
				assert(!isEdgeDead(v.getHalfEdge()));
				assert(v.getHalfEdge().start() == v);
			}
		}
	}

}
