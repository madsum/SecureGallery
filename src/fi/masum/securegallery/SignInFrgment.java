package fi.masum.securegallery;

import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;

public class SignInFrgment extends SherlockFragment {
	
    private SkyApplication mApp;
    private LiveAuthClient mAuthClient;
    private ProgressDialog mInitializeDialog;
    private Button mSignInButton;
    private Button mSignOutButton;
    private TextView mBeginTextView;
    private TextView mWelcomeTextView;
    private View mView = null;
    
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
	private OnFragmentChangedListener mListener = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setHasOptionsMenu(true);
	
		final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("Sign In");
		actionBar.setSubtitle("sky drive");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        mApp = (SkyApplication) getActivity().getApplication();
        mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
        mApp.setAuthClient(mAuthClient);
        
        mView = inflater.inflate(R.layout.signin, container, false);

        mWelcomeTextView = (TextView) mView.findViewById(R.id.TextView01);
        mSignInButton = (Button) mView.findViewById(R.id.signInButton);
        mSignOutButton = (Button) mView.findViewById(R.id.signOutButton);
        
        SetSignIn();
        SetSignOut();
        
        return mView;

	
	}
		
	private void SetSignIn()
	{
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthClient.login(getActivity(),
                                  Arrays.asList(Config.SCOPES),
                                  new LiveAuthListener() {
                    @Override
                    public void onAuthComplete(LiveStatus status,
                                               LiveConnectSession session,
                                               Object userState) {
                        
                    	if (status == LiveStatus.CONNECTED) {
                    		mSignInButton.setEnabled(false);
                    		mSignOutButton.setVisibility(View.VISIBLE);
                    		mSignInButton.setVisibility(View.GONE);
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
	}

	private void SetSignOut()
	{
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
                    	mSignInButton.setEnabled(true);
                    	mSignOutButton.setVisibility(View.GONE);
                		mSignInButton.setVisibility(View.VISIBLE);
                    	showToast("SignOut from sky drive!");
                        //getParent().finish();
                    }
                });
            }
        });			
	}

      
	private void launchSkyDriveActivity(LiveConnectSession session) 
	{
		assert session != null;
		mApp.setSession(session);
		mApp.setConnectClient(new LiveConnectClient(session));
		showToast("Login Successful!");
		startActivity(new Intent(getActivity().getApplication().getApplicationContext(), SkyDriveActivity.class));
	}

	private void showToast(String message) 
	{
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try{
			mListener = (OnFragmentChangedListener) activity;			
		}catch(Exception e){
			//Log.e(TAG, "error", e);
		}
//		final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
//		actionBar.setDisplayHomeAsUpEnabled(true);
//		actionBar.setTitle("signin");
//		actionBar.setSubtitle("sub");
	}
	
//	@Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
//    { 
//    
//    }
	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.option, menu);
//
//		super.onCreateOptionsMenu(menu, inflater);
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.camera:
//			Log.i("tag", "got camer click");
//			return (true);
//		}
//
//		return (super.onOptionsItemSelected(item));
//	}	

	      
}

