package com.example.bluetoothmessenger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class ServiceRunInBackground extends Service{
    
    final static int myID = 1234;
    SharedPreferences sharedPref;
    
    private final IBinder mBinder = new LocalBinder();// Binder given to clients which is a interface for client to bind
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	ServiceRunInBackground getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServiceRunInBackground.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i("MainActivity", "creating ServiceRunInBackground");
        
    }
    
    PowerManager mgr;
    WakeLock wakeLock;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	//Log.i("MainActivity", "onStartCommand ServiceRunInBackground");
    	//sharedPref = getSharedPreferences("MainActivity", Context.MODE_PRIVATE);
    	
    	//Here I am starting the wake lock thingy
        mgr = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();
    	
    	// This part is for starting the service in foreground with a notification//
    	//The intent to launch when the user clicks the expanded notification
    	Intent intentExpandNotification = new Intent(this, MainActivity.class);
    	intentExpandNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);
    	
    	//THis Notification constructor has deprecated, but good to support old phones
        Notification notification = new Notification(R.drawable.icon_small, "Rifat", System.currentTimeMillis());
        
        //This method is deprecated. Use Notification.Builder instead.
        notification.setLatestEventInfo(this, "BodyAcoustics", "Privacy Sensitive Sensing of Body Sounds", pendIntent);
        
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(myID, notification);
        //Finished setting up notification and startForeground
    	
        //Now call the ServiceBluetooth
        Intent intentServiceBluetooth = new Intent(getApplicationContext(), ServiceBluetooth.class);//ServiceAudioCapture
        intentServiceBluetooth.putExtra(ServiceBluetooth.FILENAME, intent.getExtras().getString(ServiceBluetooth.FILENAME));
        //i.putExtra(ServiceBluetooth.FILENAME,filename.getText().toString());
        getApplicationContext().startService(intentServiceBluetooth);
        
    	return super.onStartCommand(intent, flags, startId);
    }
   
 
   @Override
   public IBinder onBind(Intent arg0) {
	   //Log.i("MainActivity", "onBind ServiceRunInBackground");
       return mBinder;
   }
    
    
    @Override
	public boolean onUnbind(Intent intent) {
    	//Log.i("MainActivity", "onUnBind ServiceRunInBackground");
		//return super.onUnbind(intent);
    	//returning true because of Rebinding
    	return true;
	}
    
    
    @Override
	public void onRebind(Intent intent) {
    	//Log.i("MainActivity", "onReBind ServiceRunInBackground");
		super.onRebind(intent);
	}



	@Override
   public void onDestroy() {
       //Log.i("MainActivity", "Destroying ServiceRunInBackground");
       stopService(new Intent(getBaseContext(),ServiceBluetooth.class));//ServiceAudioCapture
       
       //releasing Wakelock
       wakeLock.release();
   }

}