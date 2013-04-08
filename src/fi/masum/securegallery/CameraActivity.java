package fi.masum.securegallery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveOperation;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import fi.masum.securegallery.BaseDialog.OnDismissListener;


public class CameraActivity extends Activity implements OnDismissListener {

	private static final int ACTION_TAKE_PHOTO = 1;
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;
	private Bitmap mTempImg;
	private String mImagePath;
	private String mCurrentPhotoPath;
	public LiveConnectClient mClient;
	private SkyDriveActivity mParent;
	private Context context;
	private String[] mOptions = {"Save in private gallery", "Save in photo gallery"};
	private ChoiceDialog mChoiceDlg;
	private YesNoDialog mYesNoDlg;


	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		Log.i("tag", "timeStamp "+timeStamp+" imageFileName "+imageFileName);
		// create temp directory to save img
		File outputDir = context.getCacheDir(); // context being the Activity pointer
		File outputFile = File.createTempFile("prefix", "extension", outputDir);
		File imageF = File.createTempFile(imageFileName, ".jpg", outputDir);
		
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}

	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		
		//mImageView.setImageBitmap(bitmap);
		mImageView.setVisibility(View.VISIBLE);
	}

	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);
	}

	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePictureIntent, actionCode);
	}

	private void handleSmallCameraPhoto(Intent intent) {
		Bundle extras = intent.getExtras();
		mTempImg = (Bitmap) extras.get("data");
		mImageView.setVisibility(View.VISIBLE);		
		mChoiceDlg.show();
	}

	public void savePrivatePic() {
	    String fileName = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date())+ "_img.jpg";
	    String data = "/data/data/fi.masum.securegallery/files/";
	    mImagePath = data+fileName;
	    Bitmap bm = null;
	    FileOutputStream fos;
		try {
			if(mTempImg != null)
			{
				fos = openFileOutput(fileName, Context.MODE_PRIVATE);
				mTempImg.compress(CompressFormat.JPEG, 90, fos);
				fos.close();
			}
		    
		    showMsg("Pic save correctly!");
		
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		mImageView.setImageBitmap(mTempImg);
		
	}
	
	public void savePublicPic() 
	{
		
		galleryAddPic();
//		if(mTempImg != null)
//		{
//			MediaStore.Images.Media.insertImage(getContentResolver(), mTempImg, "MyTitle" , "Description");
//			mImageView.setImageBitmap(mTempImg);
//		}
	}
	
	void showMsg(String msg){
		
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_LONG;

    	Toast toast = Toast.makeText(context, msg, duration);
    	toast.show();
		
		
	}
	

	Button.OnClickListener mTakePicSOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		
		context = this.getApplicationContext();

		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;
		mChoiceDlg = new ChoiceDialog(this, mOptions, this, "save phtoto", "test");
		mYesNoDlg = new YesNoDialog(this, this, "Uplaod phtoto", "Do you want to uplaod to Sky Drive?");



		Button picSBtn = (Button) findViewById(R.id.btnIntendS);
		setBtnListenerOrDisable( 
				picSBtn, 
				mTakePicSOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if( requestCode == ACTION_TAKE_PHOTO && resultCode == RESULT_OK)
		{
			handleSmallCameraPhoto(data);		
		}
		
	}

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		//mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);

	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName
	) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setClickable(false);
		}
	}
	
    public void onDialogDismissed( BaseDialog dialog)
    {
    	if (dialog.choiceDialog)
    	{
	    	ChoiceDialog choiceDialog = (ChoiceDialog)dialog;
	    	int ret = choiceDialog.SelectedOption;    	
	    	if(choiceDialog.DidAccept)
	    	{
	    		if (choiceDialog.SelectedOption == 0 )
	    			savePrivatePic();
	    		else if (choiceDialog.SelectedOption == 1)
	    			savePublicPic();
	    			
	    	
	    		showMsg("seleteced option is accepted"+Integer.toString(ret));
	    		Log.i("tag", " choiceDialog ret = "+"seleteced option is accepted"+Integer.toString(ret));
	    	}
	    	else
	    	{
	    		showMsg("seleteced option is cancled "+Integer.toString(ret));
	    		Log.i("tag", " choiceDialog ret = "+"seleteced option is cancled "+Integer.toString(ret));
	    	}
	    	mYesNoDlg.show();
    	}
    	else if( dialog.yesNoDialog)
    	{
    		YesNoDialog yesDialog = (YesNoDialog)dialog;
    		
    		int tt = yesDialog.ChoosedButton;
    		
    		if( yesDialog.ChoosedButton == -1)
    		{
    			//File photo = new File(mImagePath);
    			// do uplaod here
    			//mParent.uploadPhoto(photo.getAbsolutePath());
    		}
    			
    		Log.i("tag", " YesNoDialog ret = "+Integer.toString(tt));
    		
    	}    	
    }
    
    
} // end of class 