package assignment5;

import glWrapper.GLHalfEdgeStructure;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class QSlimDemo {
	public static void main(String args[]) throws Exception {
		WireframeMesh wf = ObjReader.read("objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();

		hs.init(wf);
		GLHalfEdgeStructure glBeforeCollapse = new GLHalfEdgeStructure(hs);
		
		QSlim qSlim = new QSlim(hs);
		qSlim.simplify(1500);
		
		GLHalfEdgeStructure glAfterCollapse = new GLHalfEdgeStructure(hs);
		
		glAfterCollapse.configurePreferredShader(
				"shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		
		glBeforeCollapse.configurePreferredShader(
				"shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");


		MyDisplay d = new MyDisplay();
		d.addToDisplay(glAfterCollapse);
		d.addToDisplay(glBeforeCollapse);
	}
}
