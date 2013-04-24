package fi.masum.securegallery;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements  OnFragmentChangedListener, OnBackStackChangedListener {
	
	private ActionBar mActionBar = null;
	private TextView msgView = null;
	private Button loginButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("Secure Galleray");
		mActionBar.setSubtitle("main");

		onFragmentChanged(R.layout.register_user, null);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    { 
    
    }

	
	@Override
	public void onFragmentChanged(int layoutResId, Bundle bundle) {
		Fragment f = null;
		if(layoutResId == R.layout.register_user){
			f = new RegisterUserFrgment();
		}
		else if(layoutResId == R.layout.login){
			f = new LogInFrgment();
		}
		else if(layoutResId == R.layout.signin){
			f = new SignInFrgment();
		}
		
		if(f != null){
			if(bundle != null){
				f.setArguments(bundle);
			}
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.screen_container, f, f.getClass().getSimpleName());
			transaction.addToBackStack(f.getClass().getSimpleName());
			transaction.commit();			
			getSupportFragmentManager().addOnBackStackChangedListener(this);
		}
	}
	
	@Override
	public void onBackStackChanged() {
		final int entryCount = getSupportFragmentManager().getBackStackEntryCount();
		if(entryCount == 1){
			mActionBar.setTitle(R.string.app_name);
			mActionBar.setSubtitle("main");
			mActionBar.setDisplayHomeAsUpEnabled(false);
		}else if(entryCount == 0){
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		//onUserAdded();
		return true;
	}
	
	public void onUserAdded()
	{
		
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("Secure Galleray");
		mActionBar.setSubtitle("User alredy registered! Login");
		mActionBar.setDisplayHomeAsUpEnabled(false);
		loginButton = (Button) findViewById(R.id.logIn);
		loginButton.setVisibility(View.VISIBLE);
		
		loginButton.setOnClickListener(new View.OnClickListener() {
	           
	            @Override
	            public void onClick(View v) {
	            	loginButton.setVisibility(View.GONE);
	            	onFragmentChanged(R.layout.login, null);
	            }
	        });		 

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			setContentView(R.layout.main);
			onUserAdded();
			Log.i("tag", "got the call");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
