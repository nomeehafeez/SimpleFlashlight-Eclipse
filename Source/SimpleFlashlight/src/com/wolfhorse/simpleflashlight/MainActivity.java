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

/**
 * @author TimSexton
 * June 30, 2014
 * @version 1.3
 * 
 */

public class MainActivity extends ActionBarActivity implements Callback {

	private final Context mContext = this;
	private PackageManager mPackageManager = null;
	private Camera mCamera = null;
	private ToggleButton mButton;
	private SurfaceView mPreview = null;
	private SurfaceHolder mHolder = null;
	private boolean mHasRequiredHardware = false;

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
        mHasRequiredHardware = (isCameraSupported() == true && isFlashSupported() == true);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
        if(mHasRequiredHardware)
        {
			if (mCamera == null)
				mCamera = Camera.open();
			
			if (mPreview == null)
				mPreview = (SurfaceView) findViewById(R.id.preview1);
			
	    	if (mPreview != null)
	    		mHolder = mPreview.getHolder();
	
	    	if (mHolder != null)
	    		mHolder.addCallback(this);
	    	
			if(mButton == null)
				mButton = (ToggleButton) findViewById(R.id.toggleButton1);
	
	    	if (mButton != null)
	    		mButton.bringToFront();
	    	
	    	// Default the light to ON
	    	toggleFlashlight(true);
        }
    }
    
    @Override
    protected void onStop() {
    	super.onStop();

    	if (mCamera != null)
    	{
    		mCamera.release();
    		mCamera = null;
    	}
    	
		if (mHolder != null)
			mHolder.removeCallback(this);
		mHolder = null;
		mPreview = null;
		mButton = null;
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
    
    public void toggleButton1_onClick(View view)
    {
    	// Turn the flashlight on/off
    	boolean lightOn = ((ToggleButton) view).isChecked();
  		toggleFlashlight(lightOn);
    }
    
    private void toggleFlashlight(boolean turnLightOn)
    {
    	if (mHasRequiredHardware)
    	{
			if (mCamera != null)
			{
	    		if(mButton != null)
	    			mButton.setChecked(turnLightOn);
	    		
				final Parameters p = mCamera.getParameters();
		    	if(turnLightOn)
		    	{
			    	p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			    	mCamera.setParameters(p);
			    	mCamera.startPreview();
					Log.i("info", "flashlight turned on!");
		    	}
		    	else
		    	{
		    	    p.setFlashMode(Parameters.FLASH_MODE_OFF);
		    	    mCamera.setParameters(p);
		    		mCamera.stopPreview();
		    		Log.i("info", "flashlight turned off!");
		    	}
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
	    if (mCamera != null) {
	        mCamera.release();
	    }
	    
  		if (mHolder != null)
			mHolder.removeCallback(this);
  		
  		mHolder = null;
	}

}
