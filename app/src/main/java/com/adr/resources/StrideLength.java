package com.adr.resources;

import com.adr.Adr;
import com.adr.AdrInterface;
import com.adr.resources.DataHandler;
import com.adr.resources.Data;
import com.adr.resources.AccelerationMeasure;
import com.adr.math.IndexSigmoid;
import com.adr.math.IndexLine;

import android.location.Location;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class StrideLength extends DataHandler
{
    public StrideLength(AdrInterface adr)
    {
	this.data = adr.getData();
    }
    
    public void start()
    {
	for(int i = 0; i < StrideLength.NUMBER_OF_STRIDE_LENGTHS; ++i)
	    {
		this.strideLength[i] = 0;
		this.strideLengthCount[i] = 0;
	    }	

	this.load("sdcard/com.adr/files/stride.save");
    }

    public void restart()    
    {
    }

    public void stop()
    {
	this.save("sdcard/com.adr/files/stride.save");
    }

    public void update()
    {
	
    }

    public void save(String file)
    {
	try
	    {
		FileWriter fw = new FileWriter(new File(file));
		for(int i = 0; i < StrideLength.NUMBER_OF_STRIDE_LENGTHS; ++i)
		    {
			fw.write(this.strideLength[i] + "," + this.strideLengthCount[i] + ",");
		    }

		fw.close();
	    }	
	catch(Exception e)
	    {
	    }
    }
    
    public void load(String file) 
    {
	try
	    {
		Scanner read = new Scanner(new File(file));
		String[] saved = read.nextLine().split(",");
		for(int i = 0; i < StrideLength.NUMBER_OF_STRIDE_LENGTHS; ++i)
		    {
			this.strideLength[i] = Float.valueOf(saved[2*i]);
			this.strideLengthCount[i] = Integer.valueOf(saved[2*i + 1]);
		    }
	    }	
	catch(Exception e)
	    {
	    }	
    }

    public void calculate(long numberOfSteps, float distance)
    {
	float x = AccelerationMeasure.m(this.data.getAccelerationX());
	float y = AccelerationMeasure.m(this.data.getAccelerationY());
	//float z = AccelerationMeasure.m(this.data.getAccelerationZ());

	int index = IndexLine.getIndex(StrideLength.NUMBER_OF_STRIDE_LENGTHS - 1, 15, (float)Math.sqrt(x*x + y*y)); 

	Adr.addOutput("measure index,mag: " + index + "," + Math.sqrt(x*x + y*y));

	float sl = distance/(float)numberOfSteps;

	if(Float.compare(sl, 1.0f) < 0)
	    {
		++this.strideLengthCount[index];
		this.strideLength[index] = (this.strideLength[index] * (float)(this.strideLengthCount[index] - 1) + sl)/(this.strideLengthCount[index]);
	    }
    }

    public float getStrideLength()
    {
	float x = AccelerationMeasure.m(this.data.getAccelerationX());
	float y = AccelerationMeasure.m(this.data.getAccelerationY());
	//float z = AccelerationMeasure.m(this.data.getAccelerationZ());

	int index = IndexLine.getIndex(StrideLength.NUMBER_OF_STRIDE_LENGTHS - 1, 30, (float)Math.sqrt(x*x + y*y)); 
	
	return 0.73787f;
	
	//return this.strideLength[index];
    }

    private Data data = null;
    private final static int NUMBER_OF_STRIDE_LENGTHS = 3;
    private float[] strideLength = new float[StrideLength.NUMBER_OF_STRIDE_LENGTHS];
    private int[] strideLengthCount = new int[StrideLength.NUMBER_OF_STRIDE_LENGTHS];
}