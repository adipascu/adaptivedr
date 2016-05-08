package com.adr.resources;

import com.adr.math.Measure;

public class AccelerationMeasure extends Measure
{
    public static float m(float[] data)
    {
	float measure = 0;
	float sum = 0;
	for(int i = 0; i < data.length; ++i)
	    {
		float prev = i == 0 ? 0 : data[i - 1];
		measure += Math.signum(data[i]) * Math.abs(data[i] - prev) * Math.exp(Math.abs(data[i]));
		sum += data[i];
	    }

	// subtract the mean
	measure -= sum/(float)data.length;

	return measure;
    }
}