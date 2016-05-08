package com.adr.math;

public class IndexLine
{
    public static int getIndex(int max, float upper, float val)
    {
	int index = (int)Math.round((float)max/upper * val);
	if(index > max)
	    index = max;
	else if(index < 0)
	    index = 0;

	return index;
    }
}