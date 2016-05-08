package com.adr.resources;

import com.adr.structures.Vector3D;
import com.adr.resources.DataHandler;
import com.adr.resources.Data;
import com.adr.resources.stats.LinearRegression;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;


public class Pedometer extends DataHandler
{
    public Pedometer(Data data)
    {
	this.data = data;
    }

    public void handleAcceleration(long time, Vector3D a)
    {
	this.lin.add(time, a.getZ());      
	++this.accelerationSize;
    }

    public void restart()
    {
	this.accelerationSize = 0;
    }

    public long countLeastSquares(double sensitivity)
    {
	return this.countLeastSquares(sensitivity, 0, Double.POSITIVE_INFINITY);
    }

    public long countLeastSquares(double sensitivity, double low, double high)
    {
	this.lin.finalize();
	long count = 0;

	try
	    {
		Scanner scanner = new Scanner(this.data.getAccelerationFile());	
		String line = "";
		String[] raw = null;
		double height = 0;
		long time = 0;
		double value = 0;
		double x = 0;
		double y = 0;
		long lastTime = -1;

		for(int t = 0; (line = this.readLine(scanner)) != null; ++t)
		    {
			raw = line.split(" ");
			time = Long.valueOf(raw[0]);

			if(time < low || time > high)
			    continue;

			x = Double.valueOf(raw[1]);
			y = Double.valueOf(raw[2]);
			value = Double.valueOf(raw[3]);

			height = lin.evaluate(t);

			if(value <= height)
			    {
				this.flag = true;
			    }
			else if((value > height) &&
				(this.flag == true) &&
				(Math.abs(value - height) > sensitivity))
			    {
				this.flag = false;

				// check periodicity
				//
				// note: if this is the first
				// iteration, lastTime = -1, so this
				// will be true and needed
				//if(Math.abs(time - lastTime) > 400D)
				//  {
					++count;
					lastTime = time;
					//  }
				
			    }
		    }
		scanner.close();
	    }
	catch(Exception e)
	    {
	    }

	return count;
    }

    public long countFT(long sensitivty)
    {
	return 0;
    }

    private String readLine(Scanner scanner)
    {
	try
	    {		   
		return scanner.nextLine();
	    }
	catch(Exception e)
	    {
		return null;
	    }
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

    private LinearRegression lin = new LinearRegression();
    private Data data = null;
    private boolean flag = true;
    private int accelerationSize = 0;
}