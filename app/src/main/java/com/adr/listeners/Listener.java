package com.adr.listeners;

import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.LocationManager;

import com.adr.resources.Data;

public abstract class Listener
{
    public boolean register(SensorManager sensorManager)
    {
	return true;
    }

    public boolean register(LocationManager locationManager)
    {
	return true;
    }

    public void unregister(SensorManager sensorManager)
    {
    }

    public void unregister(LocationManager locationManager)
    {
    }

    abstract public void onStop();

    public void setData(Data data)
    {
	this.data = data;
    }

    protected Data data = null;
}