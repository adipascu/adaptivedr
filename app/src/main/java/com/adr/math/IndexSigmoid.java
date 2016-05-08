package com.adr.math;

public class IndexSigmoid
{
    public static int getIndex(int max, float upper, float val)
    {
	float scale = upper/((float)Math.log(max/(max - 0.5f) - 1.0f) - (float)Math.log(max/0.4f - 1.0f));
	float phase = upper - upper*(float)Math.log(max/(max - 0.5f) - 1.0f)/((float)Math.log(max/(max - 0.5f) - 1.0f) - (float)Math.log(max/0.4f - 1.0f));
	return (int)Math.round(max/(1 + Math.exp((val - phase)/scale)));
    }
}