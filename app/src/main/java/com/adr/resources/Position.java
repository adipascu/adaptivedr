package com.adr.resources;

import android.location.Location;

public class Position extends DataHandler
{
    public Position()
    {
    }

    public void handleLocation(long time, Location location)
    {
	this.location = location;
	this.updated = true;
    }

    public void handleGpsEnabled(long time)
    {
	this.enabled = true;
    }

    public void handleGpsDisabled(long time)
    {
	this.enabled = false;
    }

    public void handleGpsStatusChanged(long time, int status)
    {
	this.status = status;
    }

    public void restart()
    {
	this.updated = false;
    }

    public Location getLocation()
    {
	return this.location;
    }

    public int getStatus()
    {
	return this.status;
    }

    public boolean getUpdated()
    {
	return this.updated;
    }

    public void start()
    {
	
    }

    public void stop()
    {
	
    }

    public void update()
    {
	
    }

    private Location location = null;
    private boolean enabled = false;
    private int status = 0;
    private boolean updated = false;
}