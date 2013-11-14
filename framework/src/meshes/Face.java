package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Vector3f;

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
	
	public float getArea(){
		Iterator<HalfEdge> iter = iteratorFE();
		Vector3f crossProd = new Vector3f();
		
		HalfEdge edge1 = iter.next().opposite;
		HalfEdge edge2 = iter.next();
		
		crossProd.cross(edge2.toVec(), edge1.toVec());
		
		return crossProd.length()/2.f;
	}

	public float getMixedVoronoiCellArea(Vertex p) {
		
		float area = 0;
		if (!isObtuse()){
			HalfEdge PQ = getOutgoing(p);
			HalfEdge PR = getIncoming(p)/*.getOpposite()*/;
			float angleQ = PR.getOppositeAngle();
			float angleR = PQ.getOppositeAngle();
						
			angleQ = (angleQ>1e-2? angleQ : 1e-2f);
			angleR = (angleQ>1e-2? angleR : 1e-2f);
			area = (1/8.f)*(PR.lengthSquared() * cot(angleQ) + PQ.lengthSquared()* cot(angleR));
		}
		else{
			if (isObtuseVertex(p)){
				area = getArea()/2;
			}
			else{
				area = getArea()/4;
			}
		}
		return area;
	}

	private boolean isObtuseVertex(Vertex p) {
		float angle = (float)(getIncoming(p).getOpposite().toVec().angle(getOutgoing(p).toVec()));
		
		return angle > Math.PI/2;
	}

	private float cot(float angle){
		return 1.f/(float)(Math.tan(angle));
	}
	private HalfEdge getOutgoing(Vertex vertex) {
		Iterator<HalfEdge> iter = iteratorFE();
		HalfEdge outgoing = iter.next();
		
		while(outgoing.start() != vertex){
			outgoing = iter.next();
		}
		
		return outgoing;
	}
	
	private HalfEdge getIncoming(Vertex vertex) {
		Iterator<HalfEdge> iter = iteratorFE();
		HalfEdge incoming = iter.next();
		
		while(incoming.end() != vertex){
			incoming = iter.next();
		}
		
		return incoming;
	}

	private boolean isObtuse() {
		Iterator<Vertex> iter = iteratorFV();
		boolean isObtuse = false;
		while (iter.hasNext()){
			if(isObtuseVertex(iter.next())){
				isObtuse = true;
			}
		}
		return isObtuse;
	}

	public Vector3f normal() {
		Vector3f normal = new Vector3f();
		Iterator<HalfEdge> iter = iteratorFE();
		normal.cross(iter.next().toVec(), iter.next().toVec());
		normal.normalize();
		
		return normal;
	}
}
