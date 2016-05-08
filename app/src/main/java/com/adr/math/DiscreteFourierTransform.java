package com.adr.math;

import com.adr.structures.ComplexNumber;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D; 

public class DiscreteFourierTransform
{
    public static ComplexNumber[] fft(ComplexNumber[] data)
    {
	double[] d = new double[2*data.length];
	for(int i = 0; i < data.length; ++i)
	    {
		d[i*2] = data[i].getReal();
		d[i*2 + 1] = data[i].getImaginary();
	    }

	DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
	fft.complexForward(d);

	ComplexNumber[] out = new ComplexNumber[data.length];
	for(int i = 0; i < data.length; ++i)
	    {
		out[i] = new ComplexNumber(d[i*2], d[i*2 + 1]);
	    } 

	return out;
    }

    public static ComplexNumber[] ifft(ComplexNumber[] data)
    {
	double[] d = new double[2*data.length];
	for(int i = 0; i < data.length; ++i)
	    {
		d[i*2] = data[i].getReal();
		d[i*2 + 1] = data[i].getImaginary();
	    }

	DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
	fft.complexInverse(d, true);

	ComplexNumber[] out = new ComplexNumber[data.length];
	for(int i = 0; i < data.length; ++i)
	    {
		out[i] = new ComplexNumber(d[i*2], d[i*2 + 1]);
	    }

	return out;
    }

    public static ComplexNumber[] forward(ComplexNumber[] data)
    {
	ComplexNumber[] out = new ComplexNumber[data.length];	
	long N = data.length;

	for(int n = 0; n < N; ++n)
	    {
		out[n] = new ComplexNumber();
		for(int k = 0; k < N; ++k)
		    {
			out[n] = out[n].add(data[k].multiply(ComplexNumber.create(1, -2.0*Math.PI * (double)k * (double)n / (double)N)));
		    }
	    }

	return out;
    }

    public static ComplexNumber[] inverse(ComplexNumber[] data)
    {
	ComplexNumber[] out = new ComplexNumber[data.length];	
	long N = data.length;

	for(int n = 0; n < N; ++n)
	    {
		out[n] = new ComplexNumber();
		for(int k = 0; k < N; ++k)
		    {
			out[n] = out[n].add(data[k].multiply(ComplexNumber.create(1, 2.0*Math.PI * (double)k * (double)n / (double)N)));
		    }
		out[n] = out[n].multiply(1.0/(double)N);
	    }

	return out;	
    }
}