package com.wolfhorse.simpleflashlight;

import java.io.IOException;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ToggleButton;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * @author TimSexton
 * July 15, 2014
 * @version 1.4
 * 
 */

public class MainActivity extends ActionBarActivity implements Callback {

	private final Context mContext = this;
	private PackageManager mPackageManager = null;
	private Camera mCamera = null;
	private ToggleButton mButton;
	private SurfaceView mPreview = null;
	private SurfaceHolder mHolder = null;
	private Animation mAnimScale = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPackageManager = mContext.getPackageManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        // Verify our required hardware exists on this device.
        isCameraSupported();
        isFlashSupported();

        // Setup button animation to zoom out and in when clicked
        // to give the user more visual button click feedback.
        mAnimScale = AnimationUtils.loadAnimation(this, R.animator.anim_scale);    
        	        
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mAnimScale = null;
    	mPackageManager = null;
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
        if (mPreview == null)
        	mPreview = (SurfaceView) findViewById(R.id.preview1);
    	
    	if (mPreview != null)
    		mHolder = mPreview.getHolder();
    	
    	if (mHolder != null)
    		mHolder.addCallback(this);
    	
    	if (mButton == null)
    		mButton = (ToggleButton) findViewById(R.id.toggleButton1);
    	
		if (mButton != null)
		{
			mButton.bringToFront();
			mButton.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View view) {
		        	// Turn the flashlight on/off
		        	view.startAnimation(mAnimScale);
		        	toggleFlash(mButton.isChecked());
		        }
		    });
		}
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	   	  	
		if (mHolder != null)
			mHolder.removeCallback(this);
		
		mHolder = null;
		mPreview = null;
    }
    
    @Override
    protected void onResume()
    {
    	OpenCamera();    	
    	
		// Default the light to the ToggleButton's check state. (ON by default).
    	// This will reset to the default if the user closes the app with the back button, but not when switching apps.
    	if (mButton != null)
    		toggleFlash(mButton.isChecked());
    	
    	super.onResume();
    }
    
    @Override
    protected void onPause()
    {
    	CloseCamera();
    	super.onPause();
    }
    
    private void CloseCamera(){
        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }
    
    private void OpenCamera(){
    	CloseCamera();
    	mCamera = Camera.open();
    }
    
    /**
     * @param packageManager
     * @return true <b>if the device supports camera flash</b><br/>
     * false <b>if the device doesn't support camera flash</b>
     */
    private boolean isFlashSupported(){ 
     // if device support camera flash?
     if (mPackageManager == null)
       	 return false;
        
     if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
      return true;
     }
     else
     {
			AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
			alertDialog.setTitle("No Camera Flash");
		    alertDialog.setMessage("This device's camera doesn't support flash.");
			alertDialog.setButton(RESULT_OK, "OK", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int which) { 
				Log.e("err", "This device's camera doesn't support flash.");
			}
			});
			alertDialog.show();
     }
     return false;
    }

    /**
     * @param packageManager
     * @return true <b>if the device supports camera</b><br/>
     * false <b>if the device doesn't support camera</b>
     */
    private boolean isCameraSupported(){
     // if device support camera?
     if (mPackageManager == null)
    	 return false;
    	
     if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      return true;
     } 
     else
     {
   	   AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
   	   alertDialog.setTitle("No Camera");
   	      alertDialog.setMessage("This device doesn't support a camera.");
   	      alertDialog.setButton(RESULT_OK, "OK", new DialogInterface.OnClickListener() {
   	          public void onClick(final DialogInterface dialog, final int which) { 
   	           Log.e("err", "This device doesn't support a camera.");
   	          }
   	       });
   	   alertDialog.show();
     }
     return false;
    }
     
    /**
     * Toggles the camera's flash.
     * @param turnLightOn
     */
    private void toggleFlash(boolean turnLightOn)
    {
		if (mCamera != null)
		{
			final Parameters paramaters = mCamera.getParameters();
	    	if(turnLightOn)
	    	{
				paramaters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		    	mCamera.setParameters(paramaters);
	    		mCamera.startPreview();
				Log.i("info", "Flash is ON...");
	    	}
	    	else
	    	{
				paramaters.setFlashMode(Parameters.FLASH_MODE_OFF);
		    	mCamera.setParameters(paramaters);
    	    	mCamera.stopPreview();
	    		Log.i("info", "Flash is OFF...");
	    	}
		}
    }
    
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	super.onCreateView(inflater, container, savedInstanceState);
        	
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
    	if (mCamera != null && mHolder != null)
    	{
			try {
				mCamera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

}
