package fi.masum.securegallery;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

//import android.os.Bundle;

public class RegisterUser extends Activity {

	private SQLiteDatabase userDB = null;
	private String table_Name = "user";
	private String db_name = "login";
	private String db_path = "/data/data/fi.masum.securegallery/databases/";
	private String tag = "masum";
	private static boolean test = true;
	private Cursor cursor = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_user);
		
		if (test)
		{
			if(this.getApplicationContext().deleteDatabase(db_name))
			{
				
				Log.i(tag, "delete db succeeed!");
			}
			else
			{
				Log.i(tag, "delete db faild!");
				
			}
			test = false;
		}

		// if (test) {
		// SecureNote.this.deleteDatabase(db_name);
		// File dir = getFilesDir();
		// File file = new File(db_path, "data");
		// boolean deleted = file.delete();
		// test = false;
		// }

		
		if (checkDataBase()) {
			// I have database so just login user
			Log.i(tag, "yes DB. Lets login then");
			finish();
			Intent startNewActivityOpen = new Intent(RegisterUser.this,
					SecureNote.class);
			startActivity(startNewActivityOpen);
			//setContentView(R.layout.secure_note);
			//LoginUser();
		}
		
		try {
			userDB = this.openOrCreateDatabase(db_name, MODE_PRIVATE, null);
			createTable();

		} catch (SQLiteException se) {

			Log.e(getClass().getSimpleName(), se.getMessage()
					+ "### error createTable ##");
		}

		final EditText txtUserName = (EditText) findViewById(R.id.txtUsername);
		final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
		final EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		Button btnSubmit = (Button) findViewById(R.id.btnLogin);

		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String _username = txtUserName.getText().toString();
				String _password = txtPassword.getText().toString();
				String _email = txtPassword.getText().toString();

				try {
					insertData(_username, _password, _email);
					//lookupData();

				} catch (SQLiteException ex) {
					Log.i(tag, "#### insert error ###" + ex.getMessage());

				} finally {
					userDB.close();
				}
				finish();
				Intent startNewActivityOpen = new Intent(RegisterUser.this,
						SecureNote.class);
				startActivity(startNewActivityOpen);

			}
		});

		Button btnCancel = (Button) findViewById(R.id.btnCancel);

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent startNewActivityOpen = new Intent(RegisterUser.this,
						SecureNote.class);
				startActivity(startNewActivityOpen);

			}
		});
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

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.register_user, menu);
//		return true;
//	}

}
