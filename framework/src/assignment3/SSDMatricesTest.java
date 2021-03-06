package assignment3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import meshes.PointCloud;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import assignment2.HashOctree;
import assignment2.HashOctreeVertex;

public class SSDMatricesTest {

	private PointCloud pointCloud;
	private HashOctree tree;
	private CSRMatrix D_0, D_1, R;
	
	@Before
	public void setUp() throws Exception {
        pointCloud = ObjReader.readAsPointCloud("objs/teapot.obj", true);
        tree = new HashOctree(pointCloud,8,1,1f);
        D_0 = SSDMatrices.D0Term(tree, pointCloud);
        D_1 = SSDMatrices.D1Term(tree, pointCloud);
        R = SSDMatrices.RTerm(tree);
	}

    @Test
    public void D0RowShouldSumUpToOne() {
            for (ArrayList<col_val> row: D_0.rows) {
                    float sum = 0;
                    for (col_val entry: row) {
                            sum += entry.val;
                    }
                    assertEquals(1, sum, 0.0001);
            }
    }

    @Test
    public void D0multipliedByPoints() {        
            ArrayList<Point3f> vertexPos = new ArrayList<Point3f>();
            for (HashOctreeVertex v: tree.getVertices()){
                    vertexPos.add(v.getPosition());
            }
            ArrayList<Point3f> result = new ArrayList<Point3f>();
            assertEquals(tree.getVertices().size(), D_0.nCols);
            D_0.multPoints(vertexPos, result);                

            for (int i = 0; i < result.size(); i++) {
                    Point3f computed = result.get(i);
                    Point3f original = pointCloud.points.get(i);
                    assertTrue(original.distance(computed) < 0.00001f);
            }
    }
    
    @Test
    public void D1shouldHaveRightSize() {
            assertEquals(D_1.nCols, tree.getVertices().size());
            assertEquals(D_1.nRows, pointCloud.points.size()*3);
    }
    
    @Test
    public void D1onLinearFunctionShouldYieldCoefficiants(){
    	ArrayList<Float> f = new ArrayList<Float>();
    	float a = 7, b = 3, c = 1;
    	
    	// D1 has n columns if n is the number of tree vertices
    	// ==> f will have n rows.
    	for (MarchableCube treeVertex : tree.getVertices()){
    		Point3f pos = treeVertex.getPosition();
    		float linFunc = a*pos.x + b*pos.y + c*pos.z;
    		f.add(linFunc);
    	}
    	
    	ArrayList<Float> result = new ArrayList<Float>();
		D_1.mult(f, result);
		for (int i = 0; i < result.size()/3; i++ ){
			assertEquals(a, result.get(i*3), 0.001f);
			assertEquals(b, result.get(i*3+1), 0.001f);
			assertEquals(c, result.get(i*3+2), 0.001f);
		}
    }
    
    @Test
    public void RtimesLinearFunctionShouldBeZero(){
    	ArrayList<Float> f = new ArrayList<Float>();
    	float a = 7, b = 3, c = 1;
    	
    	// D1 has n columns if n is the number of tree vertices
    	// ==> f will have n rows.
    	for (MarchableCube treeVertex : tree.getVertices()){
    		Point3f pos = treeVertex.getPosition();
    		float linFunc = a*pos.x + b*pos.y + c*pos.z;
    		f.add(linFunc);
    	}
    	
    	ArrayList<Float> result = new ArrayList<Float>();
		R.mult(f, result);
		for (int i = 0; i < result.size()/3; i++ ){
			assertEquals(0, result.get(i*3), 0.001f);
			assertEquals(0, result.get(i*3+1), 0.001f);
			assertEquals(0, result.get(i*3+2), 0.001f);
		}
    }
}
