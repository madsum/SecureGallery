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
import fi.masum.securegallery.BaseDialog.OnBaseDismissListener;


public class SignInActivity extends Activity {
    private SkyApplication mApp;
    private LiveAuthClient mAuthClient;
    private ProgressDialog mInitializeDialog;
    private Button mSignInButton;
    private Button mSignOutButton;
    private TextView mBeginTextView;
    private TextView mWelcomeTextView;
    
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
	private File mTempFile;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        mApp = (SkyApplication) getApplication();
        mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
        mApp.setAuthClient(mAuthClient);

        mWelcomeTextView = (TextView) findViewById(R.id.TextView01);
        mSignInButton = (Button) findViewById(R.id.signInButton);
        mSignOutButton = (Button) findViewById(R.id.signOutButton);
		
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
                        		launchSkyDriveActivity(session);
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
                        	//showToast("I'm done Logout. Now go end it");
                            //getParent().finish();
                        }
                    });
                }
            });  		    		          
    }

    @Override
    protected void onStart() 
    {
        super.onStart();
        showSignIn();
    }

    private void launchSkyDriveActivity(LiveConnectSession session) 
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
    }   
 }
