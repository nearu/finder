package com.finder.android.utils;

import android.opengl.Matrix;

public class CommonUtils {
	public static float dist(float x1, float y1, float x2, float y2){
		return (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	public static int alignment(long bytes){
		int log2 = (int)(Math.log((double)bytes) / Math.log((double)2)) + 1; 
		return (int)Math.pow((double)2, (double)log2);
	}
	public static boolean unProject(float winx, float winy, float winz,
            float[] modelMatrix, int moffset,
            float[] projMatrix, int poffset,
            int[] viewport, int voffset,
            float[] obj, int ooffset) {
		float[] finalMatrix = new float[16];
		float[] in = new float[4];
		float[] out = new float[4];
		
		Matrix.multiplyMM(finalMatrix, 0, projMatrix, poffset, 
		modelMatrix, moffset);
		if (!Matrix.invertM(finalMatrix, 0, finalMatrix, 0))
		return false;
		
		in[0] = winx;
		in[1] = winy;
		in[2] = winz;
		in[3] = 1.0f;
		
		// Map x and y from window coordinates
		in[0] = (in[0] - viewport[voffset]) / viewport[voffset + 2];
		in[1] = (in[1] - viewport[voffset + 1]) / viewport[voffset + 3];
		
		// Map to range -1 to 1
		in[0] = in[0] * 2 - 1;
		in[1] = in[1] * 2 - 1;
		in[2] = in[2] * 2 - 1;
		
		Matrix.multiplyMV(out, 0, finalMatrix, 0, in, 0);
		if (out[3] == 0.0f) 
		return false;
		
		out[0] /= out[3];
		out[1] /= out[3];
		out[2] /= out[3];
		obj[ooffset] = out[0];
		obj[ooffset + 1] = out[1];
		obj[ooffset + 2] = out[2];
		
		return true;
		}
}
