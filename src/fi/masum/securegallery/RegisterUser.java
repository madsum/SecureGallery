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


public class RegisterUser extends Activity {

	private SQLiteDatabase userDB = null;
	private String table_Name = "user";
	private String db_name = "login";
	private String tag = "masum";
	private Cursor cursor = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_user);

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

		Button btnSubmit = (Button) findViewById(R.id.btnSubmit);

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
				
				Intent startNewActivityOpen = new Intent(RegisterUser.this,
						Login.class);
				startActivity(startNewActivityOpen);
				finish();

			}
		});

		Button btnCancel = (Button) findViewById(R.id.btnCancel);

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
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
<<<<<<< HEAD
	
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
=======
>>>>>>> 4a7b6e99c5ff8da8d06a68ea09f1427efcd7aa50
}
