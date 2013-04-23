package fi.masum.securegallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

//import fi.metropolia.android.demo.R;

public class RegisterUserFrgment extends SherlockFragment implements OnClickListener {
	
	private SQLiteDatabase userDB = null;
	private String table_Name = "user";
	private String db_name = "login";
	private String db_path = "/data/data/fi.masum.securegallery/databases/";
	private String tag = "masum";
	private static boolean test = true;
	private Cursor cursor = null;
	private OnFragmentChangedListener mListener = null;
	private View mView = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try{
			mListener = (OnFragmentChangedListener) activity;			
		}catch(Exception e){
			//Log.e(TAG, "error", e);
		}
		final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("reguser");
		actionBar.setSubtitle("sub");
	} 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.register_user, container, false);
		
		if (test)
		{
			if(getActivity().getApplicationContext().deleteDatabase(db_name))
			{
				
				Log.i(tag, "delete db succeeed!");
			}
			else
			{
				Log.i(tag, "delete db faild!");
				
			}
			test = false;
		}

		if (checkDataBase()) {
			// I have database so just login user
			Log.i(tag, "yes I have DB. Lets login then");
			mListener.onFragmentChanged(R.layout.login, null);
			mView = inflater.inflate(R.layout.user_added, container, false);
			//getActivity().finish();
			return mView;
			}
		
	

		try{
			userDB = getActivity().openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
			createTable();

		} catch (SQLiteException se) {

			Log.e(getClass().getSimpleName(), se.getMessage()
					+ "### error createTable ##");
		}
		
		((Button)mView.findViewById(R.id.btnSubmit)).setOnClickListener(this);		
		return mView;
	}
	
	private void createTable() {
		try
		{
			//userDB.execSQL("DROP TABLE "+table_Name);	
			userDB.execSQL("CREATE TABLE IF NOT EXISTS " + table_Name + " (USER_NAME VARCHAR, " + "  PASSWORD VARCHAR, " + "  EMAIL VARCHAR );");
		}
		catch(Exception e)
		{	
			Log.i(tag, "#### createTable error ###" + e.getMessage());
		}
	}

	private void insertData(String usernamae, String password, String email) {
		
		userDB.execSQL("INSERT INTO " + table_Name + " Values ('" + usernamae + "','" + password + "','" + email + "');");
	}
	
	private void lookupData() {
		cursor = userDB.rawQuery("SELECT USER_NAME, PASSWORD FROM "
				+ table_Name, null);

		Log.e(getClass().getSimpleName(), "#### here i'm!!");

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String username = cursor.getString(cursor.getColumnIndex("USER_NAME"));
				String password = cursor.getString(cursor.getColumnIndex("PASSWORD"));
			}
			cursor.close();
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
	
	
	@Override
	public void onClick(View view) {
		
		final int viewId = view.getId();
		if(viewId == R.id.btnSubmit)
		{
			final EditText txtUserName = (EditText) mView.findViewById(R.id.txtUsername);
			final EditText txtPassword = (EditText) mView.findViewById(R.id.txtPassword);
			final EditText txtEmail = (EditText) mView.findViewById(R.id.txtEmail);
			
			String _username =  txtUserName.getText().toString();
			String _password = txtPassword.getText().toString();
			String _email = txtPassword.getText().toString();
			
			try {
				insertData(_username, _password, _email);

			} catch (SQLiteException ex) {
				Log.i(tag, "#### insert error ###" + ex.getMessage());

			} finally {
				userDB.close();
			}
			//getActivity().finish();
			mListener.onFragmentChanged(R.layout.login, null);	
		}
	}
	

}
