package assignment2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MortonCodesTest {

	long hash = 		0b1000101000100;
	
	//the hashes of its parent and neighbors
	long parent = 		0b1000101000;
	long nbr_plus_x = 	0b1000101100000;
	long nbr_plus_y =   0b1000101000110;
	long nbr_plus_z =   0b1000101000101;
	
	long nbr_minus_x = 	0b1000101000000;
	long nbr_minus_y =  -1; //invalid: the vertex lies on the boundary and an underflow should occur
	long nbr_minus_z =  0b1000100001101;
	
	
	//example of a a vertex morton code in a multigrid of
	//depth 4. It lies on the level 3 and 4 grids
	long vertexHash = 0b1000110100000;

	@Test
	public void testParent() {
		assertEquals(parent,MortonCodes.parentCode(hash));
	}
	
	@Test
	public void testNeighborPlus(){
		assertEquals(nbr_plus_x, MortonCodes.nbrCode(hash, 4, 0b100));
		assertEquals(nbr_plus_y, MortonCodes.nbrCode(hash, 4, 0b010));
		assertEquals(nbr_plus_z, MortonCodes.nbrCode(hash, 4, 0b001));
		assertEquals(-1L, MortonCodes.nbrCode(0b1111, 1, 0b100));
	}
	
	@Test
	public void testNeighborMinus(){
		assertEquals(nbr_minus_x, MortonCodes.nbrCodeMinus(hash, 4, 0b100));
		assertEquals(nbr_minus_y, MortonCodes.nbrCodeMinus(hash, 4, 0b010));
		assertEquals(nbr_minus_z, MortonCodes.nbrCodeMinus(hash, 4, 0b001));
		assertEquals(-1L, MortonCodes.nbrCodeMinus(0b1000000, 2, 0b100));
	}
	
	@Test
	public void isCellOnLevelXGrid(){
		assert(MortonCodes.isCellOnLevelXGrid(0b1000, 1));
		assert(MortonCodes.isCellOnLevelXGrid(0b1000100, 2));
		assert(MortonCodes.isCellOnLevelXGrid(0b1100000000, 3));
		assertFalse(MortonCodes.isCellOnLevelXGrid(0b110000000, 1));
		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000, 2));
	}
	
	@Test
	public void isVertexOnLevelXGrid(){
		assertFalse(MortonCodes.isVertexOnLevelXGrid(0b1000000010000000,1,5));
		assertFalse(MortonCodes.isVertexOnLevelXGrid(0b1000000010000000,2,5));
		assert(MortonCodes.isVertexOnLevelXGrid(0b1000000010000000,3,5));
		assert(MortonCodes.isVertexOnLevelXGrid(0b1000000010000000,4,5));
		assert(MortonCodes.isVertexOnLevelXGrid(0b1000000010000000,5,5));
		
		assertFalse(MortonCodes.isVertexOnLevelXGrid(0b1000111001, 1, 3));
		assert(MortonCodes.isVertexOnLevelXGrid(0b1000111000, 2, 3));
		assert(MortonCodes.isVertexOnLevelXGrid(0b1000111000, 3, 3));
	}
}
