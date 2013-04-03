package fi.masum.securegallery;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;


public class SignInActivity extends Activity {
    private SkyApplication mApp;
    private LiveAuthClient mAuthClient;
    private ProgressDialog mInitializeDialog;
    private Button mSignInButton;
    private Button mSignOutButton;
    private TextView mBeginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        mApp = (SkyApplication) getApplication();
        mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
        mApp.setAuthClient(mAuthClient);
        //showSignIn();

        //mInitializeDialog = ProgressDialog.show(this, "", "Initializing. Please wait...", false);

        mBeginTextView = (TextView) findViewById(R.id.beginTextView);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        showSignIn();
    }

    private void launchMainActivity(LiveConnectSession session) {
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
}
