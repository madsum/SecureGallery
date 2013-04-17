package fi.masum.securegallery;

import junit.framework.Assert;
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
import android.widget.Toast;

public class RecoverPass extends Activity {
	
	private String db_path = "/data/data/fi.masum.securegallery/databases/";
	private String tag = "masum";
	private String table_Name = "user";
	private String db_name = "login";
	private String email = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recover_pass);
		
		Button btnSend = (Button) findViewById(R.id.btnSend);
	
		btnSend.setOnClickListener(new OnClickListener() {
			
			final EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
			@Override
			public void onClick(View v) {
				String _email = txtEmail.getText().toString();
				
				lookupData();

				if (_email.compareTo(email) == 0) {
					Toast.makeText(RecoverPass.this,
							"Sending email SUCCESSFULL NOW GO and login!", Toast.LENGTH_LONG)
							.show();
					Log.i(tag, "#### Sending successful email at " + _email);
					
					try
					{
						Intent startNewActivityOpen = new Intent(RecoverPass.this, Login.class);
						startActivity(startNewActivityOpen);
					}
					catch(Exception ex)
					{
						Log.i(tag, "start activity errro "+ex.getMessage());
					}
					
				} else {

					Toast.makeText(RecoverPass.this,
							"Wrong Email entered!", Toast.LENGTH_LONG)
							.show();
				}
			}
		});	
		
	}
	
	private void lookupData() {
		SQLiteDatabase checkDB = null;
		Cursor cursor = null;
		
		try {
			checkDB = SQLiteDatabase.openDatabase(db_path + db_name, null,
					SQLiteDatabase.OPEN_READONLY);
			cursor = checkDB.rawQuery("SELECT USER_NAME, PASSWORD, EMAIL FROM "
					+ table_Name, null);

		} catch (SQLiteException e) {
			Log.i(tag, "path: " + db_path + db_name + "\n" + e.getMessage());
		}

		Log.e(getClass().getSimpleName(), "#### here i'm!!");

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String username = cursor.getString(cursor.getColumnIndex("USER_NAME"));
				String password = cursor.getString(cursor.getColumnIndex("PASSWORD"));
				email = cursor.getString(cursor.getColumnIndex("EMAIL"));
				Log.i(tag, "####Email is fund and it is : "+email+"name: "+username+"pass: "+password);
			}
			cursor.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recover_pass, menu);
		return true;
	}

}
