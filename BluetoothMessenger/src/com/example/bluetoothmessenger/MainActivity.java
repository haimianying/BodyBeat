package com.example.bluetoothmessenger;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	private EditText filename;
	private Button cbutton;
	private Button dbutton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setup();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent i = new Intent(MainActivity.this, ServiceRunInBackground.class);
		stopService(i);
		Log.i("MainActivity","onDestroy MainActivity");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// Sets up the listeners for the interface
	private void setup() {
		cbutton = (Button) findViewById(R.id.connect_button);
		dbutton = (Button) findViewById(R.id.disconnect_button);
		filename = (EditText) findViewById(R.id.name);
		
		//The onClickListener of Connect Button
		cbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(MainActivity.this, ServiceBluetooth.class);
				i.putExtra(ServiceBluetooth.FILENAME,filename.getText().toString());
				startService(i); 
			}
		});
		
		//The onClickListener of Disconnect Button
		dbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(MainActivity.this, ServiceBluetooth.class);
				stopService(i);
			}
		});
	}
}
