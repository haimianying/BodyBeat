package com.example.bluetoothmessenger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

public class ServiceBluetooth extends Service {

	// Broadcasting values to the activity
	public static final String BLUETOOTH_IN = "com.example.bluetoothmanager.read.in";
	public static final int BLUETOOTH_NOTIFICATION_ID = 20130916;
	public static final String FILENAME = "filename";

	FileOutputStream f;
	PrintWriter pw;

	// audio feature options
	private final int FRAME_SIZE = 256;
	private int FRAME_STEP = FRAME_SIZE / 2;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	IntentFilter filter;
	BroadcastReceiver receiver;
	String tag = "BodyAcousticDebug";
	String write_to_file = "";
	String filename = "default";

	ConnectThread connect;
	ConnectedThread connectedThread;
	
	// Used for storing the data we get from Bluetooth
	InputProcessor ip;

	// Our handler handles responses to the Bluetooth
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			// Creates a connection with the Bluetooth Module
			case SUCCESS_CONNECT:
				connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
				connectedThread.start();
				break;
			// Reads the data into our buffer
			case MESSAGE_READ:
				int bytes = msg.arg1;
				byte[] readBuf = (byte[]) msg.obj;
				ip.read(readBuf, bytes);
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		init();// Initializing variables

		// Before running discovery, it is a good idea to get the information of
		// the already
		// paired devices, and it is done by the getPairedDevices()
		getPairedDevices();

		// Starting discovery after canceling any previous discovery
		// The startDiscovery will return a true upon successful start of the
		// discovery phase
		// Discovery phase needs a broadcast receiver to receive info on each
		// received device
		// this broadcast receiver is registered in the init()
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}

	PowerManager mgr;
	WakeLock wakeLock;
	final static int myID = 1234;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Starting the wake lock
		mgr = (PowerManager) getApplicationContext().getSystemService(
				Context.POWER_SERVICE);
		wakeLock = mgr
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();

		// This part is for starting the service in foreground with a
		// notification//
		// The intent to launch when the user clicks the expanded notification
		Intent intentExpandNotification = new Intent(this, MainActivity.class);
		intentExpandNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendIntent = PendingIntent
				.getActivity(this, 0, intent, 0);

		// THis Notification constructor has deprecated, but good to support old
		// phones
		Notification notification = new Notification(R.drawable.icon_small, "Rifat", System.currentTimeMillis());
		notification.setLatestEventInfo(this, "BodyAcoustics", "Privacy Sensitive Sensing of Body Sounds", pendIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(myID, notification);

		// Creating the file to write the raw data (for training our model)
		filename = intent.getStringExtra(FILENAME);
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File dir = new File(filepath, "BluetoothReports");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, filename + ".txt");
		try {
			f = new FileOutputStream(file);
			pw = new PrintWriter(f);
		} catch (FileNotFoundException e) {

		}
		
		// InputProcessor will handle all the data
		ip = new InputProcessor(pw);
		// Relay the message value to send back
		// message = intent.getStringExtra(WRITE_VALUE);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Do certain things
		super.onDestroy();
		
		// Writing the file
		try {
			pw.flush();
			pw.close();
			f.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}

		pairedDevices = null;
		unregisterReceiver(receiver);
		connectedThread.cancel();
		connect.cancel();
		btAdapter = null;

		wakeLock.release();
	}

	private void getPairedDevices() {
		// Get the paired devices via devicesArray (which is initialized in the
		// init()) and
		// put it into an arraylist of string, pairedDevices
		devicesArray = btAdapter.getBondedDevices();
		if (devicesArray.size() > 0) {
			for (BluetoothDevice device : devicesArray) {
				pairedDevices.add(device.getName());
				//Log.i(tag, "pd:" + device.getName());

			}
		} else {
			Toast.makeText(getApplicationContext(),
					"No paired device, please pair BodyAcoustics.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void init() {

		btAdapter = BluetoothAdapter.getDefaultAdapter();// initializing the
															// bluetooth adapter
		pairedDevices = new ArrayList<String>();// just an arraylist of string

		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);// intent filter
																// to receive
																// info of newly
																// discovered bt
																// devices
		devices = new ArrayList<BluetoothDevice>();

		receiver = new BluetoothFoundReceiver();

		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
	}

	private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			btAdapter.cancelDiscovery();
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}
	
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;

			// Get the input stream, using temp because
			// member stream is final
			try {
				tmpIn = socket.getInputStream();
			} catch (IOException e) {
			}
			mmInStream = tmpIn;
		}

		public void run() {
			byte[] buffer; // buffer store for the stream
			int bytes; // bytes returned from read()
			try {
				mmInStream.read();
			} catch (IOException e1) {
			}
			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					buffer = new byte[1024];
					bytes = mmInStream.read(buffer);

					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	class BluetoothFoundReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Upon receiving the ACTION_FOUND intent, we add the information of
			// newly discovered bt devices
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				devices.add(device);// add the BluetoothDevice in the arraylist
									// of BluetoothDevices, devices
				boolean isPaired = false;
				if (device.getName().equals("FireFly-E54B")) {// BoneMicBTSlave
					for (int a = 0; a < pairedDevices.size(); a++) {// compare
																	// if the
																	// BoneMicBTSlave
																	// is
																	// already
																	// paired
						if (device.getName().equals(pairedDevices.get(a))) {
							// append
							isPaired = true;
							break;
						}
					}

					if (isPaired == true) {
						if (btAdapter.isDiscovering()) {
							btAdapter.cancelDiscovery();
						}
						Toast.makeText(
								getApplicationContext(),
								"Found " + device.getName() + " paired="
										+ isPaired + " " + "\n"
										+ device.getAddress(),
								Toast.LENGTH_SHORT).show();

						// connecting with appropriate device
						connect = new ConnectThread(device);
						connect.start();
					} else {
						Toast.makeText(getApplicationContext(),
								"No paired device, please pair BodyAcoustics.",
								Toast.LENGTH_SHORT).show();
					}

				}

			}

			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// run some code
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// run some code

			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				if (btAdapter.getState() == btAdapter.STATE_OFF) {
					Toast.makeText(getApplicationContext(),
							"Please turn on bluetooth", Toast.LENGTH_SHORT)
							.show();
					// turnOnBT();
				}
			}

		}
	}

}
