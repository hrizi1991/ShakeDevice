package tn.enis.com.shakedevice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by hrizi on 12/11/16.
 */

public class MyService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private DataBaseHandler db = new DataBaseHandler(this);
    private int number=0;




    @Override
    public IBinder onBind(Intent intent) {




        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long actualTime = event.timestamp;
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter

        if (mAccel >= 20) {
            showNotification();
            Log.d("Insert","Inserting ....");
            db.addParams(new Params(x,y,z,actualTime));
            Log.d("Insert","Insert is finish .");
            Toast.makeText(this, "Device detected a shock !!", Toast.LENGTH_SHORT).show();





            SmsManager.getDefault().sendTextMessage(""+number,null,"your device detected a shock",null,null);
            Log.d("Send"," the message was sent by success to the number!! "+number);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            String str = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
            number = Integer.parseInt(str);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        Log.d("service ","the service is active now");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI, new Handler());

        return START_STICKY;
    }

    /**
     * show notification when Accel is more then the given int.
     */
    private void showNotification() {
        Log.d("", "the device has now detected a movement");
        final NotificationManager mgr = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder note = new NotificationCompat.Builder(this);
        note.setContentTitle("Device Accelerometer Notification");
        note.setTicker("New Message Alert!");
        note.setAutoCancel(true);
        // to set default sound/light/vibrate or all
        note.setDefaults(Notification.DEFAULT_ALL);
        // Icon to be set on Notification
        note.setSmallIcon(R.mipmap.ic_launcher);
        // This pending intent will open after notification click
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), 0);
        // set pending intent to notification builder
        note.setContentIntent(pi);
        mgr.notify(101, note.build());
    }



}
