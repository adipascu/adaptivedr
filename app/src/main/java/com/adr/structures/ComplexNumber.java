package com.adr.structures;

public class ComplexNumber
{
    public ComplexNumber()
    {
    }
	
    public ComplexNumber(double real, double imaginary)
    {
	this.real = real;
	this.imaginary = imaginary;
    }

    public static ComplexNumber create(double magnitude, double arg)
    {
	return new ComplexNumber(magnitude*Math.cos(arg),
				 magnitude*Math.sin(arg));
    }

    public void setReal(double real)
    {
	this.real = real;
    }

    public void setImaginary(double imaginary)
    {
	this.imaginary = imaginary;
    }
    
    public double getReal()
    {
	return this.real;
    }

    public double getImaginary()
    {
	return this.imaginary;
    }

    public double getMagnitude()
    {
	return Math.sqrt(this.real * this.real + this.imaginary * this.imaginary);
    }

    public double getArgument()
    {
	return Math.atan(this.imaginary/this.real);
    }

    // -- operations
    public ComplexNumber multiply(ComplexNumber b)
    {
	return new ComplexNumber(this.real * b.real - this.imaginary * b.imaginary,
				 this.real * b.imaginary + this.imaginary * b.real);
    }

    public ComplexNumber multiply(double b)
    {
	return new ComplexNumber(this.getReal() * b,
				 this.getImaginary() * b);
    }

    public ComplexNumber add(ComplexNumber b)
    {
	return new ComplexNumber(this.getReal() + b.getReal(),
				 this.getImaginary() + b.getImaginary());
    }

    public String toString()
    {
	return this.real + "+i" + this.imaginary;
    }

    private double real = 0;
    private double imaginary = 0;
}