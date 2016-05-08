package com.adr.listeners;

import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.content.Context;

import com.adr.listeners.Listener;

public class MagneticFieldListener extends Listener implements SensorEventListener
{
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    public void onSensorChanged(SensorEvent sensorEvent)
    {
	if(sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
	    return;
	
	MagneticFieldListener.magn = sensorEvent.values.clone();
    }

    public boolean register(SensorManager sensorManager)
    {
	return sensorManager.registerListener(this,
					      sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
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

    public static float[] magn = null;
}