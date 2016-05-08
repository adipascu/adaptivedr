package com.adr.resources;

import android.hardware.SensorManager;
import android.location.LocationManager;
import android.location.Location;
import android.content.Context;

import com.adr.structures.Vector3D;
import com.adr.resources.DataHandler;
import com.adr.listeners.AccelerometerListener;
import com.adr.listeners.MagneticFieldListener;
import com.adr.listeners.GpsListener;
import com.adr.Adr;

import java.util.Vector;
import java.io.*;

public class Data
{
    public Data(Adr adr)
    {
	File dir = new File("sdcard/com.adr/files");
	dir.mkdirs();
	
	this.sensorManager = (SensorManager)adr.getSystemService(Context.SENSOR_SERVICE);
	this.locationManager = (LocationManager)adr.getSystemService(Context.LOCATION_SERVICE);

	this.accelerometerListener.setData(this);
	this.magneticFieldListener.setData(this);
	this.gpsListener.setData(this);

	this.accelFile = new File("sdcard/com.adr/files/accel");
    }

    public void startRecording(long time)
    {
	this.restart(time);

	this.accelerometerListener.register(this.sensorManager);
	this.magneticFieldListener.register(this.sensorManager);
	this.gpsListener.register(this.locationManager);

	for(DataHandler dh : this.dataHandlers)
	    dh.start();
    }

    public void restart(long time)
    {
	this.startTime = time;
	this.accelFile.delete();
	this.accelX = new float[0];
	this.accelY = new float[0];
	this.accelZ = new float[0];
	this.times = new long[0];
	for(DataHandler dh : this.dataHandlers)
	    dh.restart();
    }

    public void stopRecording()
    {
	this.accelerometerListener.unregister(this.sensorManager);
	this.magneticFieldListener.unregister(this.sensorManager);
	this.gpsListener.unregister(this.locationManager);

	for(DataHandler dh : this.dataHandlers)
	    dh.stop();
    }

    public void addHandler(DataHandler dh)
    {
	this.dataHandlers.add(dh);
    }

    // -- location
    public void addLocation(Location location)
    {
	for(DataHandler dh : this.dataHandlers)
	    dh.handleLocation(System.currentTimeMillis() - this.startTime, location);
    }

    public void gpsEnabled()
    {
	for(DataHandler dh : this.dataHandlers)
	    dh.handleGpsEnabled(System.currentTimeMillis() - this.startTime);
    }

    public void gpsDisabled()
    {
	for(DataHandler dh : this.dataHandlers)
	    dh.handleGpsDisabled(System.currentTimeMillis() - this.startTime);
    }

    public void gpsStatusChanged(int status)
    {
	for(DataHandler dh : this.dataHandlers)
	    dh.handleGpsStatusChanged(System.currentTimeMillis() - this.startTime, status);
    }

    // -- orientation
    public void addAzimuth(float azimuth)
    {
	for(DataHandler dh : this.dataHandlers)
	    dh.handleAzimuth(System.currentTimeMillis() - this.startTime, azimuth);
    }

    // -- acceleration
    public void addAcceleration(Vector3D a)
    {
	long time = (System.currentTimeMillis() - this.startTime);
	this.addTime(time);

	float[] newX = new float[this.accelX.length + 1];
	float[] newY = new float[this.accelY.length + 1];
	float[] newZ = new float[this.accelZ.length + 1];	

	System.arraycopy(this.accelX, 0, 
			 newX, 0,
			 this.accelX.length);

	System.arraycopy(this.accelY, 0, 
			 newY, 0,
			 this.accelY.length);

	System.arraycopy(this.accelZ, 0, 
			 newZ, 0,
			 this.accelZ.length);

	this.accelX = newX;
	this.accelY = newY;
	this.accelZ = newZ;

	this.accelX[this.accelX.length - 1] = (float)a.getX();
	this.accelY[this.accelY.length - 1] = (float)a.getY();
	this.accelZ[this.accelZ.length - 1] = (float)a.getZ();

	try
	    {
		FileWriter f = new FileWriter(this.accelFile, true);
		f.write(time + " " + a.getX() + " " + a.getY() + " " + a.getZ() + "\n");
		f.close();
	    }
	catch(Exception e)
	    {
	    }

	for(DataHandler dh : this.dataHandlers)
	    {
		dh.handleAcceleration(time, a);
	    }
    }

    public void addAcceleration(float x, float y, float z)
    {
	long time = (System.currentTimeMillis() - this.startTime);
	this.addTime(time);

	float[] newX = new float[this.accelX.length + 1];
	float[] newY = new float[this.accelY.length + 1];
	float[] newZ = new float[this.accelZ.length + 1];	

	System.arraycopy(this.accelX, 0, 
			 newX, 0,
			 this.accelX.length);

	System.arraycopy(this.accelY, 0, 
			 newY, 0,
			 this.accelY.length);

	System.arraycopy(this.accelZ, 0, 
			 newZ, 0,
			 this.accelZ.length);

	this.accelX = newX;
	this.accelY = newY;
	this.accelZ = newZ;	
	
	this.accelX[this.accelX.length - 1] = x;
	this.accelY[this.accelY.length - 1] = y;
	this.accelZ[this.accelZ.length - 1] = z;

	for(DataHandler dh : this.dataHandlers)
	    {
		dh.handleAcceleration(time, x, y, z);
	    }
    }

    public File getAccelerationFile()
    {
	return this.accelFile;
    }

    public float[] getAccelerationX()
    {
	return this.accelX;
    }

    public float[] getAccelerationY()
    {
	return this.accelY;
    }

    public float[] getAccelerationZ()
    {
	return this.accelZ;
    }

    // time
    private void addTime(long time)
    {
	if(this.times.length != 0)
	    if(this.times[this.times.length - 1] == time)
		return;
	long[] newTimes = new long[this.times.length + 1];	

	System.arraycopy(this.times, 0, 
			 newTimes, 0,
			 this.times.length);	

	this.times = newTimes;

	this.times[this.times.length - 1] = time;
    }

    public long[] getTimes()
    {
	return this.times;
    }

    // -- time information
    private long startTime = -1;

    // -- data
    private Vector<DataHandler> dataHandlers = new Vector<DataHandler>();
    private File accelFile = null;
    private float[] accelX = new float[0];
    private float[] accelY = new float[0];
    private float[] accelZ = new float[0];
    private long[] times = new long[0];

    // -- listeners
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private AccelerometerListener accelerometerListener = new AccelerometerListener();
    private MagneticFieldListener magneticFieldListener = new MagneticFieldListener();
    private GpsListener gpsListener = new GpsListener();

    public static final long SAMPLING_RATE = 10; // samples per second
}