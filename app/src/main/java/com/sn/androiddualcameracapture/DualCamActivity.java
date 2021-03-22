package com.sn.androiddualcameracapture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.nobrain.android.permissions.AndroidPermissions;
import com.nobrain.android.permissions.Checker;

import java.io.FileNotFoundException;
import java.io.InputStream;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DualCamActivity<CallbackContext> extends Activity {

	//	private CameraPreviewViewModel cameraPreviewViewModel;
	private static final int REQUEST_CODE = 102;
	private static int RESULT_LOAD_IMAGE_MAIN = 1;
	private static int RESULT_LOAD_IMAGE_SECONDARY = 2;
	public static String TAG = "DualCamActivity";

	// Camera views and camera object variables
	private BackCameraPreview mMainCamPreview;
	private FrontCameraPreview mSecondaryCamPreview;
	private Camera mMainCamera;
	private Camera mSecondaryCamera;
	private FrameLayout mainCameraPreview;
	private FrameLayout secondaryCameraPreview;

	// switching camera between front and back on full screen
	private boolean isSecondaryCameraFull = false;

	// Handling camera hide/show
	private Button BtnSecondaryCamHideShow;
	private boolean isSecondaryCamHide = false;
	private Button BtnMainCamHideShow;
	private boolean isMainCamHide = false;

	private Button BtnSecondaryCameraMove;
	private boolean isSecondaryCamMoved = false;
	private Button BtnSwitchCamera;

	// images
	ImageView secondaryImagePreview;
	ImageView mainImagePreview;
	private boolean isSecondaryImage = false;
	private boolean isMainImage = false;
	Button BtnUploadSecondaryImage;
	Button BtnUploadMainImage;


	@RequiresApi(api = Build.VERSION_CODES.Q)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// default operations
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dual_cam);

		// Checking Cameras permission
		AndroidPermissions.check(this)
				.permissions(Manifest.permission.CAMERA)
				.hasPermissions(new Checker.Action0() {
					@Override
					public void call(String[] permissions) {
						String msg = "Permission has " + permissions[0];
						Log.d(TAG, msg);
						/*Toast.makeText(DualCamActivity.this,
								msg,
								Toast.LENGTH_SHORT).show();*/
					}
				})
				.noPermissions(new Checker.Action1() {
					@Override
					public void call(String[] permissions) {
						String msg = "Permission has no " + permissions[0];
						Log.d(TAG, msg);
						Toast.makeText(DualCamActivity.this,
								msg,
								Toast.LENGTH_SHORT).show();

						ActivityCompat.requestPermissions(DualCamActivity.this
								, new String[]{Manifest.permission.CAMERA}
								, REQUEST_CODE);
					}
				})
				.check();

		Log.i(TAG, "Number of cameras: " + Camera.getNumberOfCameras());


		// getting view reference of cameras
		mMainCamPreview = (BackCameraPreview)findViewById(R.id.mBackCamPreview);
		mSecondaryCamPreview = (FrontCameraPreview)findViewById(R.id.mFrontCamPreview);
		mainCameraPreview = (FrameLayout) findViewById(R.id.main_camera_preview);
		secondaryCameraPreview = (FrameLayout) findViewById(R.id.secondary_camera_preview);
		
		// getting views of hide/show button
		BtnSecondaryCamHideShow = (Button)findViewById(R.id.secondary_on_off);
		BtnMainCamHideShow = (Button)findViewById(R.id.main_on_off);
		BtnSecondaryCameraMove = (Button)findViewById(R.id.move_secondary);
		BtnSwitchCamera = (Button)findViewById(R.id.switch_camera);

		// getting image upload button id's
		BtnUploadSecondaryImage = (Button)findViewById(R.id.upload_secondary_image);
		BtnUploadMainImage = (Button)findViewById(R.id.upload_main_image);

		// getting image preview
		secondaryImagePreview = findViewById(R.id.secondary_image_view);
		mainImagePreview = findViewById(R.id.main_image_view);

		// Create an instance of Cameras
		mMainCamera = getCameraInstance(0);
		// Create back camera Preview view and set it as the content of our activity.
		mMainCamPreview = new BackCameraPreview(this, mMainCamera);
		mSecondaryCamera = getCameraInstance(1);
		mSecondaryCamPreview = new FrontCameraPreview(this, mSecondaryCamera);

		// adding cameras into view
		mainCameraPreview.addView(mMainCamPreview);
		secondaryCameraPreview.addView(mSecondaryCamPreview);

		// Setting z-index
		RelativeLayout buttons_container = (RelativeLayout) findViewById(R.id.buttons_container);
		buttons_container.setZ(50);
		mainCameraPreview.setZ(10);
		for(int index = 0; index < ((FrameLayout) mainCameraPreview).getChildCount(); index++) {
			SurfaceView nextChild = (SurfaceView) ((FrameLayout) mainCameraPreview).getChildAt(index);
			nextChild.setZ(10);
		}
		secondaryCameraPreview.setZ(20);
		for(int index = 0; index < ((FrameLayout) secondaryCameraPreview).getChildCount(); index++) {
			SurfaceView nextChild = (SurfaceView) ((FrameLayout) secondaryCameraPreview).getChildAt(index);
			nextChild.setZ(20);
		}


		// Handling full screen Camera switch view Swap Function
		secondaryCameraPreview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				swapFeeds();
			}
		});

		// Handling full screen Camera switch view Swap Function
		BtnSwitchCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				swapFeeds();
			}
		});

		// Handling Front Camera View Hide and show
		BtnSecondaryCamHideShow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( isSecondaryCamHide ){
					showPictureInPictureCamera();
				}
				else{
					hidePictureInPictureCamera();
				}
			}
		});

		// Handling Back Camera View Hide and show
		BtnMainCamHideShow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( isMainCamHide ){
					showMainCamera();
				}
				else{
					hideMainCamera();
				}
			}
		});

		BtnSecondaryCameraMove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(isSecondaryCamMoved){
					setPictureInPictureSettings(20,40, 10,10);
					isSecondaryCamMoved = false;
				}
				else{
					setPictureInPictureSettings(30,50, 10,10);
					isSecondaryCamMoved = true;
				}
			}
		});

		// image upload click listener
		BtnUploadSecondaryImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( isSecondaryImage ){
					removeUploadedImage(RESULT_LOAD_IMAGE_SECONDARY);
				}
				else{
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE_SECONDARY);
				}
			}
		});

		BtnUploadMainImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( isMainImage ){
					removeUploadedImage(RESULT_LOAD_IMAGE_MAIN);
				}
				else{
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE_MAIN);
				}
			}
		});

	}

	public void swapFeeds() {
		// if front camera is on small screen then it will go into "if condition" other wise it will go into "else"
		if(isSecondaryCameraFull){
			Toast.makeText(DualCamActivity.this, "Switching to back cam!", Toast.LENGTH_LONG).show();
			FrameLayout.LayoutParams secondaryParams = (FrameLayout.LayoutParams) secondaryCameraPreview.getLayoutParams();
			FrameLayout.LayoutParams mainParams = (FrameLayout.LayoutParams) mainCameraPreview.getLayoutParams();

			FrameLayout.LayoutParams secondaryImageParams = (FrameLayout.LayoutParams) secondaryImagePreview.getLayoutParams();
			FrameLayout.LayoutParams mainImageParams = (FrameLayout.LayoutParams) mainImagePreview.getLayoutParams();
			secondaryCameraPreview.setLayoutParams(mainParams);
			mainCameraPreview.setLayoutParams(secondaryParams);

			secondaryImagePreview.setLayoutParams(mainImageParams);
			mainImagePreview.setLayoutParams(secondaryImageParams);

			secondaryCameraPreview.requestLayout();
			mainCameraPreview.requestLayout();

			secondaryImagePreview.requestLayout();
			mainImagePreview.requestLayout();

			isSecondaryCameraFull = false;
			secondaryCameraPreview.removeAllViews();
			secondaryCameraPreview.addView(mSecondaryCamPreview);

			secondaryCameraPreview.setZ(20);
			mainCameraPreview.setZ(10);

			secondaryImagePreview.setZ(20);
			mainImagePreview.setZ(10);

		}
		else{
			Toast.makeText(DualCamActivity.this, "Switching to front cam!", Toast.LENGTH_LONG).show();
			FrameLayout.LayoutParams secondaryParams = (FrameLayout.LayoutParams) secondaryCameraPreview.getLayoutParams();
			FrameLayout.LayoutParams mainParams = (FrameLayout.LayoutParams) mainCameraPreview.getLayoutParams();

			FrameLayout.LayoutParams secondaryImageParams = (FrameLayout.LayoutParams) secondaryImagePreview.getLayoutParams();
			FrameLayout.LayoutParams mainImageParams = (FrameLayout.LayoutParams) mainImagePreview.getLayoutParams();

			mainCameraPreview.setLayoutParams(secondaryParams);
			secondaryCameraPreview.setLayoutParams(mainParams);

			mainImagePreview.setLayoutParams(secondaryImageParams);
			secondaryImagePreview.setLayoutParams(mainImageParams);

			mainCameraPreview.requestLayout();
			secondaryCameraPreview.requestLayout();

			mainImagePreview.requestLayout();
			secondaryImagePreview.requestLayout();

			isSecondaryCameraFull = true;
			mainCameraPreview.removeAllViews();
			mainCameraPreview.addView(mMainCamPreview);

			mainCameraPreview.setZ(20);
			secondaryCameraPreview.setZ(10);

			mainImagePreview.setZ(20);
			secondaryImagePreview.setZ(10);

		}
	}

	public  void showPictureInPictureCamera(){
		if( isSecondaryImage ){
			secondaryImagePreview.setVisibility(View.VISIBLE);
		}
		else{
			if( isSecondaryCameraFull ){
				mainCameraPreview.setVisibility(View.VISIBLE);
			}
			else{
				secondaryCameraPreview.setVisibility(View.VISIBLE);
			}

		}
		isSecondaryCamHide = false;
	}

	public  void hidePictureInPictureCamera(){
		if( isSecondaryImage ){
			secondaryImagePreview.setVisibility(View.INVISIBLE);
		}else{
			if( isSecondaryCameraFull ){
				mainCameraPreview.setVisibility(View.INVISIBLE);
			}else{
				secondaryCameraPreview.setVisibility(View.INVISIBLE);
			}
		}

		isSecondaryCamHide = true;
	}

	public  void showMainCamera(){
		if( isMainImage ){
			mainImagePreview.setVisibility(View.VISIBLE);
		}else{
			if( isSecondaryCameraFull ){
				secondaryCameraPreview.setVisibility(View.VISIBLE);
			}else{
				mainCameraPreview.setVisibility(View.VISIBLE);
			}
		}
		isMainCamHide = false;
	}

	public  void hideMainCamera(){
		if( isMainImage ){
			mainImagePreview.setVisibility(View.INVISIBLE);
		}else{
			if( isSecondaryCameraFull ){
				secondaryCameraPreview.setVisibility(View.GONE);
			}else{
				mainCameraPreview.setVisibility(View.GONE);
			}
		}
		isMainCamHide = true;
	}

	public void setPictureInPictureSettings(int width, int height,int posX, int posY) {
		Display screenSize = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		screenSize.getSize(size);
		int totalWidth = size.x;
		int totalHeight = size.y;
		double w = (double)totalWidth / (double)100;
		double h = (double)totalHeight / (double)100;
		posY = (int) (posY*h);
		posX = (int) (posX*w);
		width = (int) (width*w);
		height = (int) (height*h);

		if( isSecondaryCameraFull ){
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mainCameraPreview.getLayoutParams();
			FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams) mainImagePreview.getLayoutParams();

			params.topMargin = posY;
			params.width = width;
			params.height = height;

			imageParams.topMargin = posY;
			imageParams.width = width;
			imageParams.height = height;

			if(isSecondaryCamMoved){
				params.gravity = Gravity.RIGHT;
				params.rightMargin = posX;

				imageParams.gravity = Gravity.RIGHT;
				imageParams.rightMargin = posX;
			}else{
				params.gravity = Gravity.LEFT;
				params.leftMargin = posX;

				imageParams.gravity = Gravity.LEFT;
				imageParams.leftMargin = posX;
			}

			mainCameraPreview.setLayoutParams(params);
			mainCameraPreview.requestLayout();

			mainImagePreview.setLayoutParams(imageParams);
			mainImagePreview.requestLayout();
		}else{
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) secondaryCameraPreview.getLayoutParams();
			FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams) secondaryImagePreview.getLayoutParams();

			params.topMargin = posY;
			params.width = width;
			params.height = height;

			imageParams.topMargin = posY;
			imageParams.width = width;
			imageParams.height = height;

			if(isSecondaryCamMoved){
				params.gravity = Gravity.RIGHT;
				params.rightMargin = posX;

				imageParams.gravity = Gravity.RIGHT;
				imageParams.rightMargin = posX;
			}else{
				params.gravity = Gravity.LEFT;
				params.leftMargin = posX;

				imageParams.gravity = Gravity.LEFT;
				imageParams.leftMargin = posX;
			}

			secondaryCameraPreview.setLayoutParams(params);
			secondaryCameraPreview.requestLayout();

			secondaryImagePreview.setLayoutParams(imageParams);
			secondaryImagePreview.requestLayout();
		}

	}

	public static Camera getCameraInstance(int cameraId){
		Camera c = null;
		try {
			c = Camera.open(cameraId); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
			Log.e(TAG,"Camera " + cameraId + " not available! " + e.toString() );
		}
		return c; // returns null if camera is unavailable
	}


	// Image Upload functionality
	@Override
	protected void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			try {
				final Uri imageUri = data.getData();
				final InputStream imageStream = getContentResolver().openInputStream(imageUri);
				final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
				if( reqCode == RESULT_LOAD_IMAGE_MAIN ){
					mainCameraPreview.setVisibility(View.INVISIBLE);
					mainImagePreview.setVisibility(View.VISIBLE);
					isMainImage = true;
					BtnUploadMainImage.setText("Remove Main");
					if(isSecondaryCameraFull){
						mainImagePreview.setZ(20);
					}
					else{
						mainImagePreview.setZ(10);
					}
					mainImagePreview.setImageBitmap(selectedImage);
				}
				else if( reqCode == RESULT_LOAD_IMAGE_SECONDARY ){
					Log.i(TAG, "checking i am int RESULT_LOAD_IMAGE_SECONDARY");
					secondaryCameraPreview.setVisibility(View.INVISIBLE);
					Log.i(TAG, "checking secondaryCameraPreview.getVisibility: "+secondaryCameraPreview.getVisibility());
					secondaryImagePreview.setVisibility(View.VISIBLE);
					isSecondaryImage = true;
					BtnUploadSecondaryImage.setText("Remove Secondary");
					if(isSecondaryCameraFull){
						secondaryImagePreview.setZ(10);
					}
					else{
						secondaryImagePreview.setZ(30);
					}
					secondaryImagePreview.setImageBitmap(selectedImage);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(DualCamActivity.this, "Something went wrong while accessing image!", Toast.LENGTH_LONG).show();
			}

		}else {
			Toast.makeText(DualCamActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
		}
	}

	public void removeUploadedImage(int reqCode){
		if( reqCode == RESULT_LOAD_IMAGE_MAIN ){
			mainImagePreview.setVisibility(View.INVISIBLE);
			mainCameraPreview.setVisibility(View.VISIBLE);
			BtnUploadMainImage.setText("Upload Main");
			mainImagePreview.setImageBitmap(null);
			isMainImage = false;
		}
		else if( reqCode == RESULT_LOAD_IMAGE_SECONDARY ){
			secondaryImagePreview.setVisibility(View.INVISIBLE);
			secondaryCameraPreview.setVisibility(View.VISIBLE);
			BtnUploadSecondaryImage.setText("Upload Secondary");
			secondaryImagePreview.setImageBitmap(null);
			isSecondaryImage = false;
		}
	}
}
