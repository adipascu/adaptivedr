package com.adr.resources;

import com.adr.math.Measure;

public class LevelnessMeasure extends Measure
{
    public static float m(float[] data)
    {
	if(data.length == 0)
	    return 0;
	float sum = 0;	
	float prev = data[0];

	for(int i = 0; i < data.length; ++i)
	    {
		sum += Math.pow(data[i] - prev, 2);
		prev = data[i];
	    }
	
	return sum;
    }
}