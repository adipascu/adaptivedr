package com.adr.math.tests;

import junit.framework.*;
import com.adr.structures.ComplexNumber;
import com.adr.math.Filter;

public class FilterTest extends TestCase
{
    public FilterTest(String name)
    {
	super(name);
    }

    public void testUnityFilter()
    {
	ComplexNumber[] data = new ComplexNumber[3];
	data[0] = new ComplexNumber(1, 0);
	data[1] = new ComplexNumber(2, 0);
	data[2] = new ComplexNumber(3, 0);
	
	Filter f = new Filter();
	ComplexNumber[] out = f.filter(data);

	System.out.println(out[0] + " " + out[1] + " " + out[2]);
    }
}