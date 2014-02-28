package com.finder.android.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import com.finder.android.common.Constants;
import com.finder.android.view.MyGLSurfaceView;

public class MapActivity extends Activity {
	
	public static final String TAG = "MapActivity";
	private MyGLSurfaceView mGLView = null;	
	private Handler mHandler = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mGLView = new MyGLSurfaceView(this);
		setContentView(mGLView);
		mHandler = new MyHandler();
		init();
	}
	
	private ArrayList<Bitmap> smallTile = new ArrayList<Bitmap>();
	
	private void init() {
		if (isExternalStorageReadable()) {
			new InitThread().start();
		} else {
			Log.e(TAG, "SD card is not available");
		}
	}
	
	class InitThread extends Thread {
		@Override
		public void run() {
			Log.d(TAG, "begin load img");
			String smallTileName = "finder/tile128/128";
			String tail = ".png";
			for(int i = 0; i <= 400; i++) {
				String filename = null;
				if (i < 10 && i > 0) {
					filename = smallTileName + "_0" + i + tail;
				} else if (i == 0){
					filename = "finder/tiletest.png";
				}else {
					filename = smallTileName + "_" + i + tail;
				} 
				File file = new File(Environment.getExternalStorageDirectory(), filename );
				if (!file.exists()) {
					Log.e(TAG, file.getName() + " not exists");
					return;
				}
//				FileInputStream fin = null;
//				try {
//					fin =  new FileInputStream(file);
//					FileChannel fc = fin.getChannel();
//					ByteBuffer bb = ByteBuffer.allocateDirect((int)file.length());
//					try {
//						int size = fc.read(bb);
//						if (size != file.length()) {
//							Log.d(TAG, "fc only read " + size + " bytes while file has " + file.length() + " bytes");
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
					InputStream is = null;
					try {
						is = (InputStream)new FileInputStream(file);
						Bitmap bitmap = BitmapFactory.decodeStream(is);
						smallTile.add(bitmap);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} finally {
						try {
							is.close();
						} catch(Exception e){
							
						}
					}
			}
			sendMsg(Constants.INIT_SMALL_TILE_OK);
		}
	}
	
	
	private void sendMsg(Constants msg) {
		Message message = new Message();
		int a =10;
		Bundle data = new Bundle();
		data.putSerializable("msg", msg);
		message.setData(data);
		mHandler.sendMessage(message);
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			Constants what = (Constants) msg.getData().getSerializable("msg");
			switch(what) {
			case INIT_SMALL_TILE_OK:
				mGLView.setTileReady(MyGLSurfaceView.SMALL, smallTile);
				break;
				
			}
		}
		
	}
}
