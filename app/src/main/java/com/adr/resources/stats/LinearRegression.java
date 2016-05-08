package com.adr.resources.stats;

import java.util.Scanner;
import java.io.File;

public class LinearRegression
{
    public LinearRegression()
    {
    }
    
    public void add(long t, double val)
    {
	this.sumXY += t * val;
	this.sumX += t;
	this.sumY += val;
	this.sumXSquared += t*t;

	++this.len;
    }

    public void finalize()
    {
	if(this.len*this.sumXSquared - this.sumX*this.sumX != 0)
	    {
		this.m = (this.len*this.sumXY - this.sumX*this.sumY)/(this.len*this.sumXSquared - this.sumX*this.sumX);
		this.b = (this.sumY - this.m*this.sumX)/this.len;
	    }
    }

    public double evaluate(double t)
    {
	return this.m*t + this.b;
    }

    private double sumXY = 0;
    private double sumX = 0;
    private double sumY = 0;
    private double sumXSquared = 0;    
    private double len = 0;
    private double m = 0;
    private double b = 0;
}