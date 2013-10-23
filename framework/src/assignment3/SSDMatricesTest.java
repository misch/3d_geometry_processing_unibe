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
	private CSRMatrix D_0, D_1;
	
	@Before
	public void setUp() throws Exception {
        pointCloud = ObjReader.readAsPointCloud("objs/teapot.obj", true);
        tree = new HashOctree(pointCloud,8,1,1f);
        D_0 = SSDMatrices.D0Term(tree, pointCloud);
        D_1 = SSDMatrices.D1Term(tree, pointCloud);
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
}
