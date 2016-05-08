package com.adr.listeners;

import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;

import com.adr.listeners.Listener;
import com.adr.listeners.MagneticFieldListener;
import com.adr.resources.Data;
import com.adr.structures.Vector3D;

public class AccelerometerListener extends Listener implements SensorEventListener
{
    public AccelerometerListener()
    {
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    public void onSensorChanged(SensorEvent sensorEvent)
    {
	if(sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
	    return;
	

	if(AccelerometerListener.accel != null && MagneticFieldListener.magn != null)
	    {
		float[] R = new float[9];
		float[] I = new float[9];
		float[] values = new float[3];

		if(!SensorManager.getRotationMatrix(R, I, AccelerometerListener.accel, MagneticFieldListener.magn))
		    return;

		// get azimuth
		SensorManager.getOrientation(R, values);

		this.data.addAzimuth(values[0]);
		this.data.addAcceleration(new Vector3D(R[0]*sensorEvent.values[0] + R[1]*sensorEvent.values[1] + R[2]*sensorEvent.values[2],
						       R[3]*sensorEvent.values[0] + R[4]*sensorEvent.values[1] + R[5]*sensorEvent.values[2],
						       R[6]*sensorEvent.values[0] + R[7]*sensorEvent.values[1] + R[8]*sensorEvent.values[2]));
	    }

	AccelerometerListener.accel = sensorEvent.values.clone();
    }

    public boolean register(SensorManager sensorManager)
    {
	return sensorManager.registerListener(this,
					      sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					      SensorManager.SENSOR_DELAY_FASTEST,
					      new Handler());
    }

    public void unregister(SensorManager sensorManager)
    {
	sensorManager.unregisterListener(this);
    }

    public void onStop()
    {
    }

    public static float[] accel = null;
}