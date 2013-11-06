package assignment4;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

public class Assignment4_1_Tests {
	
	// A sphere of radius 2.
	private HalfEdgeStructure hs; 
	// An ugly sphere of radius 1, don't expect the Laplacians 
	//to perform accurately on this mesh.
	private HalfEdgeStructure hs2; 
	@Before
	public void setUp(){
		try {
			WireframeMesh m = ObjReader.read("objs/sphere.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
			
			m = ObjReader.read("objs/uglySphere.obj", false);
			hs2 = new HalfEdgeStructure();
			hs2.init(m);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	
	@Test 
	public void uniformLaplacianRowShouldSumUpToZero(){
		CSRMatrix uniformLaplacian = LMatrices.uniformLaplacian(hs);
		
		for (ArrayList<col_val> row : uniformLaplacian.rows){
			float sum = 0;
			for (col_val weight : row){
				sum += weight.val;
			}
			assertEquals(0, sum, 0.00001);
		}
	}
	
	@Test
	public void mixedCotanLaplacianRowShouldSumUpToZero(){
		CSRMatrix uniformLaplacian = LMatrices.mixedCotanLaplacian(hs);
		
		for (ArrayList<col_val> row : uniformLaplacian.rows){
			float sum = 0;
			for (col_val weight : row){
				sum += weight.val;
			}
			assertEquals(0, sum, 0.00001);
		}
	}
	
    @Test
    public void sphereMeanCurvatureMixedCotanLaplacian() {
            CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs);
            // Call it "res" because thats the very beautiful name of this
            // variable in he LMatrices.mult method. Is this a short version of "result?"
            // Cow, oh holy one...!
            ArrayList<Vector3f> res = new ArrayList<Vector3f>();
            LMatrices.mult(laplacian, hs, res);
            for (Vector3f meanCurvNormal: res)
                    assertEquals(0.5f, meanCurvNormal.length()/2, 0.01);
    }
}
