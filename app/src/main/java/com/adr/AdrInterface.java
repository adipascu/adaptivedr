package com.adr;

import com.adr.resources.Data;

import android.location.Location;

/**
 * This class is an interface to retrieve the information in the Adr
 * Service.
 */
public interface AdrInterface
{
    public Location getLocation();
    public long getStepCount();
    public float getStrideLength();
    public Data getData();
}