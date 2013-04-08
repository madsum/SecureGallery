package fi.masum.securegallery;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import fi.masum.securegallery.BaseDialog.OnDismissListener;


public class SignInActivity extends Activity implements OnDismissListener {
    private SkyApplication mApp;
    private LiveAuthClient mAuthClient;
    private ProgressDialog mInitializeDialog;
    private Button mSignInButton;
    private Button mSignOutButton;
    private TextView mBeginTextView;
    
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
	private File mTempFile;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        mApp = (SkyApplication) getApplication();
        mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
        mApp.setAuthClient(mAuthClient);

        mBeginTextView = (TextView) findViewById(R.id.beginTextView);
        mSignInButton = (Button) findViewById(R.id.signInButton);
        mSignOutButton = (Button) findViewById(R.id.signOutButton);
        
  		context = this.getApplicationContext();
		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;
		mChoiceDlg = new ChoiceDialog(this, mOptions, this, "save phtoto", "");
		mYesNoDlg = new YesNoDialog(this, this, "Uplaod phtoto", "Do you want to uplaod to Sky Drive?");

        
        showSignIn();
        
            mSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuthClient.login(SignInActivity.this,
                                      Arrays.asList(Config.SCOPES),
                                      new LiveAuthListener() {
                        @Override
                        public void onAuthComplete(LiveStatus status,
                                                   LiveConnectSession session,
                                                   Object userState) {
                            
                        	if (status == LiveStatus.CONNECTED) {
                                launchMainActivity(session);
                            } else {
                                showToast("Login did not connect. Status is " + status + ".");
                            }
                        }
    
                        @Override
                        public void onAuthError(LiveAuthException exception, Object userState) {
                            showToast(exception.getMessage());
                        }
                    });
                }
            });
            
            mSignOutButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LiveAuthClient authClient = mApp.getAuthClient();
                    authClient.logout(new LiveAuthListener() {
                        @Override
                        public void onAuthError(LiveAuthException exception, Object userState) {
                            showToast(exception.getMessage());
                        }

                        @Override
                        public void onAuthComplete(LiveStatus status,
                                                   LiveConnectSession session,
                                                   Object userState) {
                        	mApp.setSession(null);
                        	mApp.setConnectClient(null);
                        	showToast("I'm done Logout. Now go end it");
                            //getParent().finish();
                        }
                    });
                }
            });
            
    		
    		
    		Button.OnClickListener mTakePicSOnClickListener = new Button.OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
    				}
    			};

    		Button picBtn = (Button) findViewById(R.id.btnCamera);
    		setBtnListenerOrDisable( 
    				picBtn, 
    				mTakePicSOnClickListener,
    				MediaStore.ACTION_IMAGE_CAPTURE
    		);    		    		          
    }

    @Override
    protected void onStart() 
    {
        super.onStart();
        showSignIn();
    }

    private void launchMainActivity(LiveConnectSession session) 
    {
        assert session != null;
        mApp.setSession(session);
        mApp.setConnectClient(new LiveConnectClient(session));
        mSignOutButton.setVisibility(View.VISIBLE);
        mSignInButton.setVisibility(View.GONE);
        showToast("Login Successful!");
        startActivity(new Intent(getApplicationContext(), SkyDriveActivity.class));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSignIn() {
        mSignInButton.setVisibility(View.VISIBLE);
        mBeginTextView.setVisibility(View.VISIBLE);
    }
    


	private void dispatchTakePictureIntent(int actionCode) 
	{

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePictureIntent, actionCode);
	}	
	
	private void setBtnListenerOrDisable(Button btn, Button.OnClickListener onClickListener, String intentName) 
	{
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setClickable(false);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		
		if( requestCode == ACTION_TAKE_PHOTO && resultCode == RESULT_OK)
		{
			handleCameraPhoto(data);		
		}
		
	}

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
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
	 * found, this method returns false.*/
	
	public static boolean isIntentAvailable(Context context, String action) 
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}	
	
	
	

	private void handleCameraPhoto(Intent intent) 
	{
		Bundle extras = intent.getExtras();
		mTempImg = (Bitmap) extras.get("data");
		mImageView.setVisibility(View.VISIBLE);		
		mChoiceDlg.show();		
	}

	public void savePrivatePic() {
	    String fileName = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date())+ "_img.jpg";
	    String data = "/data/data/fi.masum.securegallery/files/";
	    FileOutputStream fos;
		try 
		{
			if(mTempImg != null)
			{
				fos = openFileOutput(fileName, Context.MODE_PRIVATE);
				mTempImg.compress(CompressFormat.JPEG, 90, fos);
				fos.close();
			}
		    showMsg("Pic save correctly!");
		} 
		catch (IOException e) 
		{
			Log.i("tag", "erro savePrivatePic: "+e.getMessage());
		}
		mImageView.setImageBitmap(mTempImg);
	}
	
	public void savePublicPic() 
	{
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		File outputDir = context.getCacheDir(); // context being the Activity pointer
		try
		{
		File outputFile = File.createTempFile("prefix", "extension", outputDir);
		File mTempFile = File.createTempFile(imageFileName, ".jpg", outputDir);
		mCurrentPhotoPath = mTempFile.getAbsolutePath();
		}
		catch(Exception e)
		{
			Log.i("tag", "error: createImageFile "+e.getMessage());
		}
						
		galleryAddPic();
		mImageView.setImageBitmap(mTempImg);
	}	
		
	private void galleryAddPic() 
	{
	    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}	
	
	void showMsg(String msg)
	{
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_LONG;

    	Toast toast = Toast.makeText(context, msg, duration);
    	toast.show();
	}
	
	private File createImageFile() 
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		Log.i("tag", "timeStamp "+timeStamp+" imageFileName "+imageFileName);
		// create temp directory to save img
		File outputDir = context.getCacheDir(); // context being the Activity pointer
		try
		{
		File outputFile = File.createTempFile("prefix", "extension", outputDir);
		File mTempFile = File.createTempFile(imageFileName, ".jpg", outputDir);
		}
		catch(Exception e)
		{
			Log.i("tag", "error: createImageFile "+e.getMessage());
		}
		
		return mTempFile;
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
	    	//mYesNoDlg.show();
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
}
