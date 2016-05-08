package com.adr.math;

import com.adr.structures.ComplexNumber;

import java.util.Vector;

public class Filter
{
    public Filter()
    {
	// The default is a unity gain filter
	this.xCoef.add(new Double(1));
	this.yCoef.add(new Double(1));
    }

    public ComplexNumber[] filter(ComplexNumber[] data)
    {
	ComplexNumber[] out = new ComplexNumber[data.length];
	for(int n = 0; n < data.length; ++n)
	    {
		out[n] = new ComplexNumber();
		Double a = yCoef.get(0); // this should always exist . . .
		for(int k = 1; k < this.yCoef.size(); ++k)
		    {
			ComplexNumber yn = n-k < 0 ? new ComplexNumber() : (n-k >= data.length ? new ComplexNumber() : out[n-k]);
			out[n] = out[n].add(yn.multiply(-yCoef.get(k)/a));
		    }

		for(int k = 0; k < this.xCoef.size(); ++k)
		    {
			ComplexNumber xn = n-k < 0 ? new ComplexNumber() : (n-k >= data.length ? new ComplexNumber() : data[n-k]);
			out[n] = out[n].add(xn.multiply(xCoef.get(k)/a));
		    }
	    }

	return out;
    }

    protected Vector<Double> xCoef = new Vector<Double>();
    protected Vector<Double> yCoef = new Vector<Double>();
}