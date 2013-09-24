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
	 * Get an iterator which iterates over the 1-neighbouhood
	 * @return
	 */
	public Iterator<Vertex> iteratorVV(){
		//Implement this...
		return null;
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
		//Implement this.
		return null;
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
			return current == null ? first : current.next.opposite;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
