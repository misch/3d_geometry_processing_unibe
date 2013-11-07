package assignment4;

import glWrapper.GLHalfEdgeStructure;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;

public class LaplacianDemo {
	
	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{


		WireframeMesh m = ObjReader.read("objs/uglySphere.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(m);

		MyDisplay d = new MyDisplay();
		
			CSRMatrix cotanLaplacian = LMatrices.mixedCotanLaplacian(hs);
			CSRMatrix uniformLaplacian = LMatrices.uniformLaplacian(hs);
			CSRMatrix[] laplacians = new CSRMatrix[]{ uniformLaplacian, cotanLaplacian };
			for (CSRMatrix laplacian: laplacians) {
				ArrayList<Vector3f> curvatures = new ArrayList<Vector3f>();
				LMatrices.mult(laplacian, hs, curvatures);
				
				GLHalfEdgeStructure glHs = new GLHalfEdgeStructure(hs);
				glHs.add(curvatures, "curvature");
				
				glHs.configurePreferredShader("shaders/curvature_arrows.vert",
						"shaders/curvature_arrows.frag", 
						"shaders/curvature_arrows.geom");
				d.addToDisplay(glHs);
			}

			GLHalfEdgeStructure glMesh = new GLHalfEdgeStructure(hs);
			glMesh.configurePreferredShader("shaders/trimesh_flat.vert",
					"shaders/trimesh_flat.frag", 
					"shaders/trimesh_flat.geom");
			d.addToDisplay(glMesh);
		}
	}

