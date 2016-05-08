package com.adr;

import android.app.Service;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationProvider;
import android.util.Log;
import android.widget.Toast;
import android.hardware.GeomagneticField;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.core.learning.SupervisedTrainingElement;

import com.adr.resources.Data;
import com.adr.resources.Pedometer;
import com.adr.resources.Direction;
import com.adr.resources.Position;
import com.adr.resources.Movement;
import com.adr.resources.StrideLength;
import com.adr.resources.AccelerationMeasure;
import com.adr.structures.Vector3D;
import com.adr.AdrInterface;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.ArrayList;
import java.io.*;

class Update extends TimerTask
{
    public Update(Adr adr)
    {
	this.adr = adr;
    }

    public void run()
    {
	adr.update();
    }

    private Adr adr;
}

/**
 * This class is a Service that will implement Adaptive Dead
 * Reckoning, calculating the phone's position and giving it to other
 * apps that request it.
 */
public class Adr extends Service implements AdrInterface
{
    public void onCreate()
    {
	this.nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	showNotifications();

	this.toast = Toast.makeText(this, "Service started", 0);
	this.toast.show();

	this.lastLocation = new Location("");

	// -- water fountain EBW
	this.lastLocation.setLatitude(38.946694);
	this.lastLocation.setLongitude(-92.3312272);

	// -- parking garage by Lafferre
	// this.lastLocation.setLatitude(38.94548069);
	// this.lastLocation.setLongitude(-92.331956774);

	// -- Lafferre/Quad
	//this.lastLocation.setLatitude(38.946002202);
	//this.lastLocation.setLongitude(-92.3291699588);

	this.data = new Data(this);
	this.pedometer = new Pedometer(this.data);
	this.direction = new Direction(this.data);
	this.position = new Position();
	this.movement = new Movement(this.data);
	this.strideLength = new StrideLength(this);

	this.data.addHandler(this.pedometer);
	this.data.addHandler(this.direction);
	this.data.addHandler(this.position);
	this.data.addHandler(this.movement);
	this.data.addHandler(this.strideLength);
	this.data.startRecording(System.currentTimeMillis());

	this.timer.scheduleAtFixedRate(new Update(this), Adr.UPDATE_INTERVAL, Adr.UPDATE_INTERVAL);
    }

    public void update()
    {
	this.toast.cancel();
	this.stepCountWindow = this.pedometer.countLeastSquares(.8f);
	this.stepCount += this.stepCountWindow;

	if(this.stepCountWindow == 0)
	    {
		this.movement.adjust(false);
		this.moving = false;
	    }

	this.xMeasure = AccelerationMeasure.m(this.data.getAccelerationX());
	this.yMeasure = AccelerationMeasure.m(this.data.getAccelerationY());
	Adr.addOutput("x: " + xMeasure);
	Adr.addOutput("y: " + yMeasure);
	Adr.addOutput("step count: " + this.stepCountWindow);

	this.strideLengthAverage = this.strideLength.getStrideLength();

	Location location = this.position.getLocation();
	try
	    {
		if(lastLocation != null && location != null)
		    {
			float dist = lastLocation.distanceTo(location);

			this.accuracy = location.getAccuracy();

			Adr.addOutput("accuracy: " + accuracy);
			Adr.addOutput("gps override: " + this.usingGpsOverride);

			if(accuracy <= 6  && this.position.getUpdated() && this.usingGpsOverride)
			    this.usingGps = true;
			else
			    this.usingGps = false;

			// -- GPS AVAILABLE AND ACCURATE
			if(this.stepCountWindow > 0 &&
			   Float.compare(dist, this.stepCountWindow*1.5f) < 0 &&
			   this.usingGps)
			    {
				this.distTotal += dist;
				this.distWindow = dist;
				this.strideLength.calculate(this.stepCountWindow, dist);

				if(Float.compare(dist, 0) == 0)
				    {
					this.movement.adjust(false);
					this.moving = false;
				    }
				else
				    {
					this.movement.adjust(true);
					this.moving = true;
				    }

				this.gpsAngle = lastLocation.bearingTo(location);

				Adr.addOutput("USING GPS");
				//this.direction.train((float)Math.toRadians(this.gpsAngle));
				Adr.addOutput("bearing: " + this.gpsAngle);
			    }
			// -- GPS UNAVAILABLE OR INACCURATE
			else if(this.stepCountWindow > 0)
			    {
				this.moving = this.movement.decide();
				if(moving == true)
				    {
					Adr.addOutput("USING CALCULATION");
					// location
					this.distWindow = this.strideLengthAverage * (float)this.stepCountWindow;
					double d = this.distWindow / 1000.0D;
					//double d = 0.75D * (double)this.stepCountWindow / 1000.0D;
					double lastLat = Math.toRadians(this.lastLocation.getLatitude());
					double lastLon = Math.toRadians(this.lastLocation.getLongitude());
					//float angle = prediction;
					float angle = this.direction.getAverage();
					double lat = Math.asin(Math.sin(lastLat)*Math.cos(d/Adr.RADIUS_OF_EARTH) +
							       Math.cos(lastLat)*Math.sin(d/Adr.RADIUS_OF_EARTH)*Math.cos(angle));
					double lon = lastLon + Math.atan2(Math.sin(angle)*Math.sin(d/Adr.RADIUS_OF_EARTH)*Math.cos(lastLat),
									  Math.cos(d/Adr.RADIUS_OF_EARTH) - Math.sin(lastLat)*Math.sin(lat));

					this.lastLocation.reset();
					this.lastLocation.setLatitude(Math.toDegrees(lat));
					this.lastLocation.setLongitude(Math.toDegrees(lon));
				    }
			    }
			else
			    {
			    }
		    }
	    }
	catch(Exception e)
	    {
	    }

	Adr.addOutput("stride length average: " + this.strideLengthAverage);
        Adr.addOutput("angle: " + Math.toDegrees(this.direction.getAverage()));
	if(moving)
	    {
		Adr.addOutput("moving");
	    }
	else
	    {
		Adr.addOutput("not moving");
	    }

	if(this.lastLocation == null)
	    this.lastLocation = location;

	if(location != null)
	    if(this.stepCountWindow > 0 && this.usingGps)
		{
		    this.lastLocation = location;
		}

	if(this.lastLocation != null)
	    Adr.addOutput("coord: " + this.lastLocation.getLongitude() + " " + this.lastLocation.getLatitude());

	this.sendImmediatesData();

	this.stepCountWindow = 0;
	this.distWindow = 0;
	this.data.restart(System.currentTimeMillis());
	this.toast.setText(this.output);
	this.toast.show();
	this.clearOutput();
    }

    private Message getDataMessage(int what)
    {
	Message msg = Message.obtain(null, what);
	Bundle bundle = new Bundle();
	if(lastLocation != null)
	    {
		bundle.putDouble("latitude", this.lastLocation.getLatitude());
		bundle.putDouble("longitude", this.lastLocation.getLongitude());
	    }
	bundle.putLong("stepcount", this.stepCount);
	bundle.putLong("stepcountwindow", this.stepCountWindow);
	bundle.putFloat("distance", this.distTotal);
	bundle.putFloat("distancewindow", this.distWindow);
	bundle.putFloat("averageangle", this.direction.getAverage());
	bundle.putBoolean("moving", this.moving);
	bundle.putFloat("stridelength", this.strideLengthAverage);
	bundle.putFloatArray("accelerationx", this.data.getAccelerationX());
	bundle.putFloatArray("accelerationy", this.data.getAccelerationY());
	bundle.putFloatArray("accelerationz", this.data.getAccelerationZ());
	bundle.putFloat("xmeasure", xMeasure);
	bundle.putFloat("ymeasure", yMeasure);
	bundle.putFloat("ymeasure", yMeasure);
	bundle.putLongArray("times", this.data.getTimes());

	msg.setData(bundle);

	return msg;
    }

    private boolean sendMessageTo(Messenger m, Message msg)
    {
	if(msg == null)
	    return false;
	try
	    {
		m.send(msg);
		return true;
	    }
	catch(Exception e)
	    {
		return false;
	    }
    }

    private void sendImmediatesData()
    {
	Message msg = this.getDataMessage(1);
	for(Messenger m : this.connections)
	    {
		if(!this.sendMessageTo(m, msg))
		    this.connections.remove(m);
	    }
    }

    class IncommingHandler extends Handler
    {
	public void handleMessage(Message msg)
	{
	    if(msg.what == -3)
		{
		    usingGpsOverride = !usingGpsOverride;
		}
	    if(msg.what == -2)
		{
		    stopSelf();
		}
	    if(msg.what == -1)
		{
		    stepCount = 0;
		    distTotal = 0;
		}
	    if(msg.what == 0)
		{
		    Message reply = getDataMessage(0);
		    sendMessageTo(msg.replyTo, reply);
		}
	    if(msg.what == 1)
		{
		    if(!connections.contains(msg.replyTo))
			connections.add(msg.replyTo);
		}
	    if(msg.what == 2)
		{
		    //connections.clear();
		    connections.remove(msg.replyTo);
		}
	    if(msg.what == 3)
		{
		    addOutput(msg.getData().getString("output"));
		}
	}
    }

    public IBinder onBind(Intent intent)
    {
	return this.messenger.getBinder();
    }

    public void onDestroy()
    {
	this.data.stopRecording();
	this.nm.cancelAll();
	this.timer.cancel();
    }

    // -- notifications
    private void showNotifications()
    {
	Notification not = new Notification(R.drawable.icon, "Service", System.currentTimeMillis());

	Intent notIntent = new Intent();
	PendingIntent penIntent = PendingIntent.getActivity(this, 0, notIntent, 0);
	not.setLatestEventInfo(this, "Service Started", "Adr", penIntent);
	this.nm.notify(0, not);
    }

    public static void addOutput(String msg)
    {
	if(Adr.output != "")
	    Adr.output += "\n";
	Adr.output += msg;
    }

    public static void clearOutput()
    {
	Adr.output = "";
    }

    // -- interface
    public Location getLocation()
    {
	return this.lastLocation;
    }

    public long getStepCount()
    {
	return this.stepCount;
    }

    public float getStrideLength()
    {
	return this.strideLengthAverage;
    }

    public Data getData()
    {
	return this.data;
    }

    // -- variables

    // -- messenger
    final Messenger messenger = new Messenger(new IncommingHandler());

    // -- notifications
    private NotificationManager nm;
    private Toast toast = null;
    private static String output = "";

    // -- data storage
    private Data data = null;
    private Pedometer pedometer;
    private Direction direction;
    private Position position;
    private Movement movement;
    private StrideLength strideLength;
    private long stepCount = 0;
    private long stepCountWindow = 0;
    private float distTotal = 0;
    private float distWindow = 0;
    private Location lastLocation = null;
    private float strideLengthAverage = 0;
    private float gpsAngle = 0;
    private float accuracy = 0;
    private float xMeasure = 0;
    private float yMeasure = 0;
    private boolean moving = false;

    // -- Gps status
    boolean usingGps = false;
    boolean usingGpsOverride = true;

    // -- connections
    Vector<Messenger> connections = new Vector<Messenger>();

    private Timer timer = new Timer();
    public static final int UPDATE_INTERVAL = 3000;
    public static final double RADIUS_OF_EARTH = 6378.1D;
}