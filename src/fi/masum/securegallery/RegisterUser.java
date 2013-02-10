package fi.masum.securegallery;

import android.app.Activity;
import android.content.Intent;
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
	private String credential = "credential";
	private String db_name = "user";
	private String tag = "masum";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_user);

		try {
			userDB = openOrCreateDatabase(db_name, MODE_PRIVATE, null);
			createTable();
		} catch (SQLiteException se) {

			Log.e(getClass().getSimpleName(), se.getMessage()
					+ "### error createTable ##");
		}

		final EditText txtUserName = (EditText) findViewById(R.id.txtUsername);
		final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);

		Button btnSubmit = (Button) findViewById(R.id.btnSubmit);

		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String _username = txtUserName.getText().toString();
				String _password = txtPassword.getText().toString();

				try {
					insertData(_username, _password);

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
		userDB.execSQL("CREATE TABLE IF NOT EXISTS " + credential
				+ " (USER_NAME VARCHAR, " + "  PASSWORD VARCHAR );");
	}

	private void insertData(String usernamae, String password) {
		userDB.execSQL("INSERT INTO " + credential + " Values ('" + usernamae
				+ "','" + password + "');");

		userDB.execSQL("INSERT INTO " + credential + " Values ('aaa','bbb');");
	}
}
