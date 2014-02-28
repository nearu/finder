package com.finder.android.entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Tile {
	
	private FloatBuffer vertexBuffer = null;
	private ShortBuffer drawListBuffer = null;
	
	// num of coordinates per vertex
	public static final int FLOAT_BYTES_SIZE = 4;
	public static final int COORD_PER_VERTEX = 3;
	public static final int TILE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_BYTES_SIZE;
	public static final int ELEMENTS_NUM = 6;
	/**
	 * width of the tile
	 */
	public static final float l = 0.05f;
	static float squareCoords[] = {
		  -l,  l, 0.0f,  1.0f, 0.0f,   // top left
		  -l, -l, 0.0f,  1.0f, 1.0f,  	// bottom left
		   l, -l, 0.0f,  0.0f, 1.0f, 		// bottom right
		   l,  l, 0.0f,  0.0f, 0.0f }; // top right
	private short []drawOrder = { 0, 1, 2, 0, 2, 3 };
	//private short []drawOrder = { 0, 1, 1, 2, 2, 3, 3,0 };
	private float []color = { 0.0f, 0.0f, 255.0f, 1.0f };
	
	public Tile(){
		// initialize vertex buffer
		ByteBuffer bb = ByteBuffer.allocateDirect( squareCoords.length * 4 );
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(squareCoords);
		vertexBuffer.position(0);
		
		// initialize drawListBuffer
		ByteBuffer dlb = ByteBuffer.allocateDirect( drawOrder.length * 2 );
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
	}
	
	public FloatBuffer getVertexBuffer(){
		return vertexBuffer;
	}
	
	public ShortBuffer getDrawListBuffer(){
		return drawListBuffer;
	}
	
	public float[] getColor(){
		return color;
	}
}
