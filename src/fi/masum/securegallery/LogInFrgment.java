package fi.masum.securegallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

public class LogInFrgment extends SherlockFragment implements OnClickListener {
	
	private SQLiteDatabase userDB = null;
	private String table_Name = "user";
	private String db_name = "login";
	private Cursor cursor = null;
	private String username = null;
	private String password = null;
	private String db_path = "/data/data/fi.masum.securegallery/databases/";
	private String tag = "masum";
	private static boolean test = true;	
	private OnFragmentChangedListener mListener = null;
	private View mView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("login");
		actionBar.setSubtitle("local DB");
	}
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	
		if (checkDataBase()) {
			Log.i(tag, "yes DB. Lets login then");
			mView = inflater.inflate(R.layout.login, container, false);
		} else {

			Log.i(tag, "no db so register a user.");
			
			mListener.onFragmentChanged(R.layout.register_user, null);
		}
		((Button)mView.findViewById(R.id.btnLogin)).setOnClickListener(this);	
		return mView;
	}
	
	private void LoginUser() {
		try {
			userDB = getActivity().openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
		} catch (SQLiteException se) {
			Log.e(getClass().getSimpleName(), se.getMessage()
					+ "### exception ##");
		}

		final EditText txtUserName = (EditText) mView.findViewById(R.id.txtUsername);
		final EditText txtPassword = (EditText) mView.findViewById(R.id.txtPassword);
		final Button btnLogin = (Button) mView.findViewById(R.id.btnLogin);

		String _username = txtUserName.getText().toString();
		String _password = txtPassword.getText().toString();
		lookupData();

		if (_username.compareTo(username) == 0
				&& _password.compareTo(password) == 0) {
			
			Toast.makeText(getActivity(),
					"Login SUCCESSFULL NOW GO ON!", Toast.LENGTH_LONG)
					.show();
			Log.i(tag, "#### user:" + username
					+ " INFO TAG. ##### password: " + password
					+ "##success!!");
			txtPassword.setText("");
			
			//getActivity().startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
			
			
			mListener.onFragmentChanged(R.layout.signin, null);

		} 
		else {
			Toast.makeText(getActivity(),
					"Wrong username or password!", Toast.LENGTH_LONG)
					.show();
		}
	}
	
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(db_path + db_name, null,
					SQLiteDatabase.OPEN_READONLY);
			checkDB.close();
		} catch (SQLiteException e) {
			Log.i(tag, "path: " + db_path + db_name + "\n" + e.getMessage());
		}
		return checkDB != null ? true : false;
	}
	
	/**
	 * Run a query to get some data, then add it to a List and format as you
	 * require
	 */
	private void lookupData() {
		cursor = userDB.rawQuery("SELECT USER_NAME, PASSWORD FROM "
				+ table_Name, null);

		Log.e(getClass().getSimpleName(), "#### here i'm!!");

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				username = cursor.getString(cursor.getColumnIndex("USER_NAME"));
				password = cursor.getString(cursor.getColumnIndex("PASSWORD"));
			}
			cursor.close();
		}
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
//		actionBar.setTitle("login");
//		actionBar.setSubtitle("sub");
	} 
	
	@Override
	public void onClick(View view) {
		
		final int viewId = view.getId();
		if(viewId == R.id.btnLogin)
		{
			LoginUser();
		}
	}
	
}
