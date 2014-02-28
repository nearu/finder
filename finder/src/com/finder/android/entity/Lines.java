package com.finder.android.entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Lines {
	
	private FloatBuffer vertexBuffer = null;
	
	static float[] linesCoords = {
		-1.0f, 1.0f, 0.0f,
		 1.0f, 1.0f, 0.0f,
		-1.0f, 0.0f, 0.0f,
		 1.0f, 0.0f, 0.0f,
		 0.0f, 1.0f, 0.0f,
		 0.0f,-1.0f, 0.0f
	};
	public static final int FLOAT_BYTES_SIZE = 4;
	public static final int COORD_PER_VERTEX = 3;
	public static final int LINES_VERTICES_DATA_STRIDE_BYTES = 3 * FLOAT_BYTES_SIZE;
	public static final int VERTEX_COUNT = 6;
	static float[] color = { 0.0f, 0.0f, 0.0f, 1.0f };
	public Lines(){
		ByteBuffer bb = ByteBuffer.allocateDirect( linesCoords.length * 4 );
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(linesCoords);
		vertexBuffer.position(0);
	}
	
	public FloatBuffer getVertexBuffer(){
		return vertexBuffer;
	}
	
	public float[] getColor(){
		return color;
	}

}
