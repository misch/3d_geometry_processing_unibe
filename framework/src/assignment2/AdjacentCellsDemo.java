package assignment2;

import glWrapper.GLHashtree;
import glWrapper.GLHashtreeAdjacentCells;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;

import java.io.IOException;

import meshes.PointCloud;
import meshes.reader.PlyReader;
import openGL.MyDisplay;


public class AdjacentCellsDemo {
	
	public static void main(String[] args) throws IOException{
		hashTreeDemo(PlyReader.readPointCloud("./objs/octreeTest.ply", true));
	}
		
	public static void hashTreeDemo(PointCloud pc){

		HashOctree tree = new HashOctree(pc,4,1,1f);
		MyDisplay display = new MyDisplay();
		GLPointCloud glPC = new GLPointCloud(pc);
		GLHashtreeAdjacentCells glOT = new GLHashtreeAdjacentCells(tree);
		GLHashtree glOT2 = new GLHashtree(tree);

		glOT.configurePreferredShader("shaders/octree.vert",
		"shaders/octree.frag",
		"shaders/octree_adjacent_cells.geom");
		
		glOT2.configurePreferredShader("shaders/octree.vert",
				"shaders/octree.frag",
				"shaders/octree.geom");

		GLHashtree_Vertices glOTv = new GLHashtree_Vertices(tree);

		display.addToDisplay(glOT);
		display.addToDisplay(glOT2);
		display.addToDisplay(glOTv);

		display.addToDisplay(glPC);

		}
}
