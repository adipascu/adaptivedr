package com.adr.structures;

public class Vector3D
{
    public Vector3D()
    {
    }
    
    public Vector3D(double x, double y, double z)
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    public Vector3D(Vector3D d)
    {
	this.x = d.getX();
	this.y = d.getY();
	this.z = d.getZ();
    }

    public double getX()
    {
	return this.x;
    }

    public double getY()
    {
	return this.y;
    }

    public double getZ()
    {
	return this.z;
    }

    public double getMagnitude()
    {
	return Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }

    public double getAngleX()
    {
	return Math.atan(this.y/this.x);
    }

    public double getAngleZ()
    {
	return Math.acos(this.z);
    }

    public Vector3D multiply(double s)
    {
	return new Vector3D(this.x*s, this.y*s, this.z*s);
    }

    public double dot(Vector3D v)
    {
	return this.x*v.getX() + this.y*v.getY() + this.z*v.getZ();
    }

    public Vector3D add(Vector3D v)
    {
	return new Vector3D(this.x + v.getX(),
			    this.y + v.getY(),
			    this.z + v.getZ());
    }

    public Vector3D subtract(Vector3D v)
    {
	return new Vector3D(this.x - v.getX(),
			    this.y - v.getY(),
			    this.z - v.getZ());
    }

    public Vector3D normalize()
    {
	return (new Vector3D(this)).multiply(this.getMagnitude());
    }

    private double x = 0;
    private double y = 0;
    private double z = 0;
}