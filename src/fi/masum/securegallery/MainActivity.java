package fi.masum.securegallery;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class MainActivity extends SherlockFragmentActivity implements  OnFragmentChangedListener, OnBackStackChangedListener {
	
	private ActionBar mActionBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mActionBar = getSupportActionBar();
		mActionBar.setSubtitle("main contain");
		onFragmentChanged(R.layout.register_user, null);
		//getSupportActionBar().hide();
//		
//		mActionBar = getSupportActionBar();
//		mActionBar.setSubtitle("main");
//		mActionBar.setHomeButtonEnabled(false);
//		getSupportFragmentManager().addOnBackStackChangedListener(this);
//		onFragmentChanged(R.layout.register_user, null);
		
//		Fragment fragment = null;
//		// do not call the method getFragmentManager()
//		getSupportFragmentManager().addOnBackStackChangedListener(this);
//		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//		fragment = new RegisterUser();
//		//fragment.setArguments(b);
//		transaction.replace(R.id.screen_container, fragment, "RegisterUser");
//		
//		// add to stack as root, so the stack count is 1
//		transaction.addToBackStack("RegisterUser");
//		transaction.commit();
		
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
			mActionBar.setSubtitle("main conat");
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}else if(entryCount == 0){
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
