package com.adr.resources;

import android.location.Location;

import com.adr.structures.Vector3D;

import java.io.File;

public abstract class DataHandler
{
    public void handleLocation(long time, Location location)
    {
    }

    public void handleGpsEnabled(long time)
    {
    }

    public void handleGpsDisabled(long time)
    {
    }

    public void handleGpsStatusChanged(long time, int status)
    {
    }

    public void handleAzimuth(long time, float azimuth)
    {
    }

    public void handleAcceleration(long time, Vector3D a)
    {
    }

    public void handleAcceleration(long time, float x, float y, float z)
    {
    }
    
    abstract public void restart();

    abstract public void start();

    abstract public void stop();

    abstract public void update();
}