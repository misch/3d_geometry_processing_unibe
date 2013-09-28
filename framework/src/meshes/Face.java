package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of a face for the {@link HalfEdgeStructure}
 *
 */
public class Face extends HEElement {

	//an adjacent edge, which is positively oriented with respect to the face.
	private HalfEdge anEdge;
	
	public Face(){
		anEdge = null;
	}

	public void setHalfEdge(HalfEdge he) {
		this.anEdge = he;
	}

	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	
	/**
	 * Iterate over the vertices on the face.
	 * @return
	 */
	public Iterator<Vertex> iteratorFV(){
		return new IteratorFV(anEdge);
	}
	
	/**
	 * Iterate over the adjacent edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorFE(){
		return new IteratorFE(anEdge);
	}
	
	public String toString(){
		if(anEdge == null){
			return "f: not initialized";
		}
		String s = "f: [";
		Iterator<Vertex> it = this.iteratorFV();
		while(it.hasNext()){
			s += it.next().toString() + " , ";
		}
		s+= "]";
		return s;
		
	}
	
	

	/**
	 * Iterator to iterate over the vertices on a face
	 * @author Alf
	 *
	 */
	public final class IteratorFV implements Iterator<Vertex> {
		
		
		private HalfEdge first, current;

		public IteratorFV(HalfEdge anEdge) {
			first = anEdge;
			current = null;
		}

		@Override
		public boolean hasNext() {
			return current == null || current.next != first;
		}

		@Override
		public Vertex next() {
			//make sure eternam iteration is impossible
			if(!hasNext()){
				throw new NoSuchElementException();
			}

			//update what edge was returned last
			current = (current == null?
						first:
						current.next);
			return current.incident_v;
		}
		
		@Override
		public void remove() {
			//we don't support removing through the iterator.
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Iterator to iterate over the edges of a face
	 * @author Michèle
	 *
	 */
	public final class IteratorFE implements Iterator<HalfEdge> {
		
		private HalfEdge first, current;
		
		public IteratorFE(HalfEdge anEdge){
			first = anEdge;
			current = null;
		}
		
		@Override
		public boolean hasNext() {
			return current == null || current.next != first;
		}

		@Override
		public HalfEdge next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			
			current = (current == null ? first : current.next);
			return current;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * return the face this iterator iterates around
		 * @return
		 */
		public Face face() {
			return first.incident_f;
		}
	}

}
