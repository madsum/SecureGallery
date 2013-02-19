package fi.masum.securegallery;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import android.database.sqlite.SQLiteOpenHelper;  

public class SecureNote extends Activity {

	private SQLiteDatabase userDB = null;
	private String table_Name = "user";
	private String db_name = "login";
	//private String tag = "masum";
	//private SQLiteDatabase userDB = null;
	//private String credential = "credential";
	private Cursor cursor = null;
	private String username = null;
	private String password = null;
	//private String db_name = "user";
	private String db_path = "/data/data/fi.masum.securegallery/databases/";
	private String tag = "masum";
	private static boolean test = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// if (test) {
		// SecureNote.this.deleteDatabase(db_name);
		// File dir = getFilesDir();
		// File file = new File(db_path, "data");
		// boolean deleted = file.delete();
		// test = false;
		// }

		if (checkDataBase()) {
			Log.i(tag, "yes DB. Lets login then");
			setContentView(R.layout.secure_note);
			LoginUser();
		} else {

			Log.i(tag, "no db so register a user.");

			Intent startNewActivityOpen = new Intent(SecureNote.this,
					RegisterUser.class);
			startActivity(startNewActivityOpen);
		}
	}

	private void LoginUser() {

		try {
			userDB = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
		} catch (SQLiteException se) {
			Log.e(getClass().getSimpleName(), se.getMessage()
					+ "### exception ##");
		}

		final EditText txtUserName = (EditText) findViewById(R.id.txtUsername);
		final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);

		Button btnLogin = (Button) findViewById(R.id.btnLogin);

		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String _username = txtUserName.getText().toString();
				String _password = txtPassword.getText().toString();

				// insertData();
				lookupData();

				if (_username.compareTo(username) == 0
						&& _password.compareTo(password) == 0) {
					
					Toast.makeText(SecureNote.this,
							"Login SUCCESSFULL NOW GO ON!", Toast.LENGTH_LONG)
							.show();
					Log.i(tag, "#### user:" + username
							+ " INFO TAG. ##### password: " + password
							+ "##success!!");
					EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
					txtPassword.setText("");

//					Intent startNewActivityOpen = new Intent(SecureNote.this,
//							NotesList.class);

					try {
//						startActivity(startNewActivityOpen);
					} catch (Exception e) {
						Log.e(tag, e.getMessage() + "### Error #####");
					}
				} else {

					Toast.makeText(SecureNote.this,
							"Wrong username or password!", Toast.LENGTH_LONG)
							.show();
				}
			}
		});
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
}

