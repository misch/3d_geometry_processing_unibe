package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

/**
 * Implementation of a vertex for the {@link HalfEdgeStructure}
 */
public class Vertex extends HEElement{
	
	/**position*/
	Point3f pos;
	/**adjacent edge: this vertex is startVertex of anEdge*/
	HalfEdge anEdge;
	
	/**The index of the vertex, mainly used for toString()*/
	public int index;

	public Vertex(Point3f v) {
		pos = v;
		anEdge = null;
	}
	
	
	public Point3f getPos() {
		return pos;
	}

	public void setHalfEdge(HalfEdge he) {
		anEdge = he;
	}
	
	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	/**
	 * Get an iterator which iterates over the 1-neighborhood
	 * @return
	 */
	public Iterator<Vertex> iteratorVV(){
		return new IteratorVV(anEdge);
	}
	
	/**
	 * Iterate over the incident edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorVE(){
		return new IteratorVE(anEdge);
	}
	
	/**
	 * Iterate over the neighboring faces
	 * @return
	 */
	public Iterator<Face> iteratorVF(){
		return new IteratorVF(anEdge);
	}
	
	
	public String toString(){
		return "" + index;
	}
	
	

	public boolean isAdjascent(Vertex w) {
		boolean isAdj = false;
		Vertex v = null;
		Iterator<Vertex> it = iteratorVV();
		for( v = it.next() ; it.hasNext(); v = it.next()){
			if( v==w){
				isAdj=true;
			}
		}
		return isAdj;
	}
	
	public final class IteratorVE implements Iterator<HalfEdge>{
		
		private HalfEdge first, current;
		
		public IteratorVE(HalfEdge anEdge){
			// This Vertex is the starting point of anEdge
			// and therefore the opposite of anEdge is the incident edge.
			first = anEdge.opposite;
			current = null;
		}

		@Override
		public boolean hasNext() {
			return current == null || current.next.opposite != first;
		}

		@Override
		public HalfEdge next() {
			if (!hasNext()){
				throw new NoSuchElementException();
			}
			
			current = (current == null ? first : current.next.opposite);
			return current;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public final class IteratorVV implements Iterator<Vertex>{

		private Iterator<HalfEdge> edgeIterator;
		
		public IteratorVV(HalfEdge anEdge){
			edgeIterator = new IteratorVE(anEdge);
		}
		
		@Override
		public boolean hasNext() {
			return edgeIterator.hasNext();
		}

		@Override
		public Vertex next() {
			if (!hasNext()){
				throw new NoSuchElementException();
			}
			
			// Go through all the incident edges and take their starting points.
			return edgeIterator.next().start();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
	}
	
	public final class IteratorVF implements Iterator<Face>{
		
		private Iterator<HalfEdge> edgeIterator;
		private Face next;
		
		public IteratorVF(HalfEdge anEdge){
			edgeIterator = iteratorVE();
		}
		
		@Override
		public boolean hasNext() {		
	
			while (next == null && edgeIterator.hasNext()){
					next = edgeIterator.next().getFace();
			}
			
			return next == null ? false : true;
		}

		@Override
		public Face next() {
			if (!hasNext()){
				throw new NoSuchElementException();
			}
			Face nextFace = next;
			next = null;
			return nextFace;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
