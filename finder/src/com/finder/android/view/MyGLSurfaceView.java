package com.finder.android.view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.finder.android.R;
import com.finder.android.entity.Tile;
import com.finder.android.utils.CommonUtils;
import com.finder.android.utils.ShaderLoader;

public class MyGLSurfaceView extends GLSurfaceView{
	
	public static final String TAG = "MyGLSurfaceView";
	public static final int SMALL = 1;
	public static final int MID = 2;
	public static final int LARGE = 3;
	private ScaleGestureDetector mScaleGestureDetector = null;
	private Context cxt = null;
	private MyRenderer mRenderer = null;
	private float mRatio;
	private float WIDTH, HEIGHT;
	private float[] fingerPos = new float[4];
	/*
	 * for touch point
	 */
	private boolean isTouched = false;
	/*
	 * load small tile ok
	 */
	private boolean []isSmallReady = {false, false};
	
	private ArrayList<Bitmap> bufferList = null;
	
	public MyGLSurfaceView(Context cxt) {
		super(cxt);
		this.cxt = cxt;
		this.setEGLContextClientVersion(0x02);
		mRenderer = new MyRenderer();
		setRenderer(mRenderer);
		this.setRenderMode(RENDERMODE_WHEN_DIRTY);
		mScaleGestureDetector = new ScaleGestureDetector(cxt, new ScaleGestureListener());
		this.setOnTouchListener(new MyOnTouchListener());
	}
	
	public void setTileReady(int index, ArrayList<Bitmap> list) {
		Log.d(TAG, "load bitmap ok!");
		switch(index) {
		case SMALL:
			Log.d(TAG, "small tile is ready");
			isSmallReady[0] = true;
			bufferList = list;
			Log.d(TAG, "isSmallReady[0] is " + isSmallReady[0]);
			break;
		}
	}
	
	
	private float mRotation = 0.0f; 
	private float mScale = 1.0f;
	private float mTransX = 0.0f;
	private float mTransY = 0.0f;
	private boolean isScaling = false;
	class MyOnTouchListener implements OnTouchListener {
		private float mPX, mPY;
		private float sPX, sPY;
		private float dPX, dPY;
		private float dist = 0.0f;
		@Override
		public boolean onTouch(View v, MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			switch(e.getAction()){
				case MotionEvent.ACTION_MOVE:
					float dx = x - mPX;
					float dy = y - mPY;
					mRotation = mRotation + ((dx + dy) * 180.0f / 320);
					if(mRotation > 30){
						mRotation = 30;
					}else if(mRotation < -0){
						mRotation = 0;
					}
					
					if (!isScaling) {
						mTransX -= dx / 500.0f / mScale;
						mTransY -= dy / 500.0f * mRatio / mScale;
					}
					requestRender();
					break;
				case MotionEvent.ACTION_DOWN:
					dPX = x;
					dPY = y;
					ByteBuffer bb = ByteBuffer.allocateDirect( 4 * 4 );
					bb.order(ByteOrder.nativeOrder());
					IntBuffer ib  = bb.asIntBuffer();
					GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, ib);
					int[] view = new int[4];
					for(int i = 0 ; i < 4; i++){
						view[i] = ib.get(i);
						//Log.d(TAG, "view : "+i+" " + view[i] );
					}
					
					IntBuffer ibz = IntBuffer.allocate(1);
					GLES20.glReadPixels( (int)x,(int)((float)view[3] - y), 1, 1, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_FLOAT, ibz);
					int z = ibz.get(0);
					Log.e(TAG, "view : " + view[0] + " " + view[1] + " " + view[2] + " " + view[3] + "%n" + "z = " + z);
					int result = GLU.gluUnProject(x, (float)view[3] - y, 0.0f, mRenderer.mModelViewMatrix, 0,
							mRenderer.mProjectionMatrix, 0, view, 0, fingerPos,0 );
					if (result != GLES20.GL_TRUE){
						Log.e(TAG , "unproject failed, so sad!");
					}
//					CommonUtils.unProject(x, (float)view[3] - y, 0.0f, mRenderer.mModelViewMatrix, 0,
//							mRenderer.mProjectionMatrix, 0, view, 0, fingerPos,0 );
					isTouched = true;
					
					break;
			}
			mPX = x;
			mPY = y;
			return mScaleGestureDetector.onTouchEvent(e);
		}
		
	}
	
	class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener{

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			isScaling = true;
			mScale *= detector.getScaleFactor();
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			isScaling = false;
		}
	}
	
	/**
	 * For check error
	 */
	private void checkGLError(String op){
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
			Log.e(TAG, op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}
	
	class MyRenderer implements GLSurfaceView.Renderer{
		boolean flag = false;
		@Override
		public void onDrawFrame(GL10 arg0) {
			// Redraw background color
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			float[] scratch = new float[16];
			float[] tScratch = new float[16];
			/**
			 *  camera
			 */
			Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0, 0, 0, 0, 1, 0);
			
			/**
			 *	rotation scale and translate in camera  
			 */
			Matrix.setRotateM(mRotationMatrix, 0, 0, 0, 1.0f, 0.0f);
			Matrix.scaleM(mViewMatrix, 0, mScale, mScale, 1);
			//Matrix.rotateM(mViewMatrix, 0, mRotation, 1.0f, 0, 0);
			Matrix.translateM(mViewMatrix, 0, mTransX, mTransY, 0);
			Matrix.multiplyMM(scratch, 0, mProjectionMatrix, 0, mViewMatrix, 0);
			float []indentityMatrix = new float[16];
			Matrix.setIdentityM(indentityMatrix, 0);
			Matrix.multiplyMM(mModelViewMatrix, 0, mViewMatrix, 0, indentityMatrix, 0);
			/**
			 * draw background tiles
			 */
			for(int i = 0; i < 1.0f / Tile.l; ++i){
				for(int j = 0; j < 1.0 / Tile.l; ++j){
					/**
					 * model matrix 
					 */
					Matrix.setIdentityM(mTranslateMatrix, 0);
					Matrix.translateM(mTranslateMatrix, 0,
							-(Tile.l / 2.0f + Tile.l*j*2.0f-1.0f), -(Tile.l / 2.0f + Tile.l*i*2.0f-1.0f), 0);
					Matrix.multiplyMM(tScratch, 0, scratch, 0, mTranslateMatrix, 0);
					Matrix.multiplyMM(mMVPMatrix, 0,tScratch, 0, mRotationMatrix, 0);
					// begin draw
					if (!isSmallReady[1]) {
						drawTile(mTextureID[0]);
					} else {
						drawTile(mTextureID[(i * 20 + j) % 400]);
					}
				}
			}
			if (isTouched){
				Log.e(TAG, "OBJ: X=" + fingerPos[0] + " Y=" + fingerPos[1] + " Z=" + fingerPos[2] + " w=" + fingerPos[3] );
				Matrix.setIdentityM(mTranslateMatrix, 0);
				Matrix.translateM(mTranslateMatrix, 0,
						fingerPos[0] / fingerPos[3],fingerPos[1] / fingerPos[3], fingerPos[2] / fingerPos[3]);		
				Matrix.multiplyMM(tScratch, 0, scratch, 0, mTranslateMatrix, 0);
				Matrix.multiplyMM(mMVPMatrix, 0, tScratch, 0, mRotationMatrix, 0);
				bindTexture(mTextureID[0], R.drawable.bg,null);
				drawTile(mTextureID[0]);
			}
			
			if (isSmallReady[0]) {
				Log.d(TAG, "begin bind small tile");
				isSmallReady[0] = false;
				//bindTexture(mTextureID[0], 0, bufferList.get(0));
				Log.d(TAG, "buffer list size is " + bufferList.size());
				for (int  i = 1; i < bufferList.size(); i++) {
					bindTexture(mTextureID[i-1], 0, bufferList.get(i));
				}
				isSmallReady[1] = true;
				Log.d(TAG, "bind small ok! " + isSmallReady[1]);
			}
		}

		@Override
		public void onSurfaceChanged(GL10 unused, int width, int height) {
			GLES20.glViewport(0, 0, width, height);
			WIDTH = width;
			HEIGHT = height;
			float ratio = (float) width / height;
			mRatio = ratio;
			mRatio = ratio;
			Matrix.frustumM(mProjectionMatrix, 0,-ratio , ratio, -1, 1, 2, 10);
		}

		@Override
		public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
			Matrix.setIdentityM(mModelViewMatrix, 0);
			// Set background color
			GLES20.glClearColor(255.0f, 255.0f, 255.0f, 1.0f);
			
			mTile = new Tile();
			int vertexShader = ShaderLoader.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			int fragShader = ShaderLoader.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
			
			mProgram = GLES20.glCreateProgram();
			if(mProgram == 0) {
				return;
			}
			GLES20.glAttachShader(mProgram, vertexShader);
			GLES20.glAttachShader(mProgram, fragShader);
			GLES20.glLinkProgram(mProgram);	// Create OpenGL ES program executables
			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus,0);
			if (linkStatus[0] != GLES20.GL_TRUE){
				Log.e(TAG, "Could not link program");
				Log.e(TAG, GLES20.glGetProgramInfoLog(mProgram));
				GLES20.glDeleteProgram(mProgram);
				return;
			}
			// get handler to vertex shader's aPosition member
			mPositionHandle 	= GLES20.glGetAttribLocation(mProgram, "aPosition");
			if (mPositionHandle == -1){
				throw new RuntimeException("can not get position handle");
			}
			mColorHandle	 	 	= GLES20.glGetUniformLocation(mProgram, "uColor");
			mMVPHandle 				= GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			if (mMVPHandle == -1){
				throw new RuntimeException("can not get mvp handle");
			}
			mTextureHandle 		= GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
			if (mTextureHandle == -1){
				throw new RuntimeException("can not get texture handle");
			}
			
			createTexture();
			
		}
		
		/**
		 * create texture 
		 */
		private void createTexture(){
			GLES20.glGenTextures(400, mTextureID, 0);
			for (int i = 0; i < 400; ++i) {
				bindTexture(mTextureID[i], R.drawable.robot, null);
			}
		}
		
		private void bindTexture(int texture, int drawable, Bitmap b){
			
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture);
			checkGLError("BIND TEXTURE 0");
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			checkGLError("BIND TEXTURE 1");
			
			if (b == null) {
				Bitmap bitmap;
				InputStream is = cxt.getResources().openRawResource(drawable);
				try{
					bitmap = BitmapFactory.decodeStream(is);
				} finally{
					try{
						is.close();
					} catch(IOException e){
						
					}
				}
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
				checkGLError("BIND TEXTURE 2");
				bitmap.recycle();
			}else {
				//GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 512, 512, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, b);
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0,  b, 0);
				checkGLError("BIND TEXTURE 3");
			}
			
		}
		
		/**
		 * 
		 * draw entity tile
		 */
		
		private void drawTile(int texture){
			GLES20.glUseProgram(mProgram);
			
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
			FloatBuffer vb = mTile.getVertexBuffer();
			vb.position(0);
			// prepare tile coordinate data
			GLES20.glVertexAttribPointer(
					mPositionHandle, Tile.COORD_PER_VERTEX, GLES20.GL_FLOAT, false,
					Tile.TILE_VERTICES_DATA_STRIDE_BYTES, mTile.getVertexBuffer());
			// enable a handle to the tile vertex
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			
			vb.position(3);
			GLES20.glVertexAttribPointer(
					mTextureHandle, 2, GLES20.GL_FLOAT, false,
					Tile.TILE_VERTICES_DATA_STRIDE_BYTES, mTile.getVertexBuffer());
			GLES20.glEnableVertexAttribArray(mTextureHandle);
			
			GLES20.glUniform4fv(mColorHandle, 1, mTile.getColor(), 0);
			GLES20.glUniformMatrix4fv(mMVPHandle, 1,false,mMVPMatrix, 0);
			
			GLES20.glDrawElements(
					GLES20.GL_TRIANGLES, Tile.ELEMENTS_NUM , GLES20.GL_UNSIGNED_SHORT, mTile.getDrawListBuffer());
			
			GLES20.glDisableVertexAttribArray(mPositionHandle);
			GLES20.glDisableVertexAttribArray(mTextureHandle);
		}
		
		
		private final String vertexShaderCode = 
				"attribute vec4 aPosition;\n" +
				"uniform mat4 uMVPMatrix;\n" +
				"attribute vec2 aTextureCoord;\n" +
				"varying vec2 vTextureCoord;\n" +
				"void main() {\n" +
				"	gl_Position = uMVPMatrix * aPosition;\n" +
				"	vTextureCoord = aTextureCoord;\n" +
				"}\n";
		private final String fragmentShaderCode = 
				"precision mediump float;\n" +
				"uniform vec4 uColor;\n" +
				"varying vec2 vTextureCoord;\n" +
				"uniform sampler2D sTexture;\n" +
				"void main(){\n" +
				"	gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
				"}";
		private Tile mTile = null;
		private int mProgram;
		private int mPositionHandle;
		private int mColorHandle;
		private int mMVPHandle;
		private int mTextureHandle;
		private int[] mTextureID = new int[400];
		
		private float[] mProjectionMatrix = new float[16];
		private float[] mViewMatrix       = new float[16];
		private float[] mMVPMatrix        = new float[16];
		private float[] mRotationMatrix   = new float[16];
		private float[] mTranslateMatrix  = new float[16];
		private float[] mModelViewMatrix 				= new float[16];
		
	}
	
	
}
