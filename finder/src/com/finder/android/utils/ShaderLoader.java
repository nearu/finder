package com.finder.android.utils;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderLoader {
	
	public static final String TAG = "ShaderLoader";
	
	public static int loadShader(int type, String code){
		/**
		 		* create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		 		* create a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		 */
		int shader = GLES20.glCreateShader(type);
		
		// add source code to the shader and complie it
		GLES20.glShaderSource(shader, code);
		GLES20.glCompileShader(shader);
		int[] complied = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, complied, 0);
		if (complied[0] != GLES20.GL_TRUE){
			Log.e(TAG, "Could not comple shader");
			Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}
		return shader;
	}
}
