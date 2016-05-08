package com.adr.resources;

import com.adr.Adr;
import com.adr.resources.Data;
import com.adr.resources.DataHandler;
import com.adr.resources.AccelerationMeasure;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.SupervisedTrainingElement;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Direction extends DataHandler
{
    public Direction(Data data)
    {
	this.data = data;
    }

    public void restart()
    {
	this.sampleSize = 0;
	this.xAverage = 0;
	this.yAverage = 0;
    }

    public void handleAzimuth(long time, float azimuth)
    {
	++this.sampleSize;
	float x = (float)Math.cos(azimuth);
	float y = (float)Math.sin(azimuth);
	this.xAverage = (this.xAverage*(float)(this.sampleSize - 1) + x)/(float)this.sampleSize;
	this.yAverage = (this.yAverage*(float)(this.sampleSize - 1) + y)/(float)this.sampleSize;
    }

    public void handleAcceleration(long time, float x, float y, float z)
    {
    }

    public void train(float angle)
    {
	float averageAngle = (float)Math.atan2(angleAverageY, angleAverageX);
	if(Float.compare(0.261799388f, Math.abs(angle - averageAngle)) < 0 && this.numberOfSamples != 0)
	    {
		this.numberOfSamples = 0;
		this.xMAverage = 0;
		this.yMAverage = 0;
		this.angleAverageX = 0;
		this.angleAverageY = 0;
		return;
	    }

        float xMeasure = AccelerationMeasure.m(this.data.getAccelerationX());
	float yMeasure = AccelerationMeasure.m(this.data.getAccelerationY());

	++this.numberOfSamples;
	this.xMAverage += (this.xMAverage * (float)(this.numberOfSamples - 1) + xMeasure)/(float)this.numberOfSamples;
	this.yMAverage += (this.yMAverage * (float)(this.numberOfSamples - 1) + yMeasure)/(float)this.numberOfSamples;
	this.angleAverageX += (this.angleAverageX * (float)(this.numberOfSamples - 1) + Math.cos(angle))/(float)this.numberOfSamples;
	this.angleAverageY += (this.angleAverageY * (float)(this.numberOfSamples - 1) + Math.sin(angle))/(float)this.numberOfSamples;

	if(this.numberOfSamples >= 5)
	    {
		this.train(this.xMAverage, this.yMAverage, averageAngle);
		this.numberOfSamples = 0;
		this.xMAverage = 0;
		this.yMAverage = 0;
		this.angleAverageX = 0;
		this.angleAverageY = 0;
	    }
    }

    public void train(float xMeasure, float yMeasure, float angle)
    {
	Adr.addOutput("training direction NN");
	try
	    {
		this.directionNnFw.write(xMeasure + " " + yMeasure + " " + angle + "\n");
	    }
	catch(Exception e)
	    {
		
	    }

	double mag = Math.sqrt(xMeasure * xMeasure + yMeasure * yMeasure);
	double[] in = new double[]{(xMeasure/mag + 1.0D)/2.0D, (yMeasure/mag + 1.0D)/2.0D};
	double[] out = new double[]{(angle + Math.PI)/(2*Math.PI)};
	TrainingSet set = new TrainingSet();
	set.addElement(new SupervisedTrainingElement(in, out));

	nn.learnInNewThread(set);	
    }

    public float predict()
    {
	double xMeasure = AccelerationMeasure.m(this.data.getAccelerationX());
	double yMeasure = AccelerationMeasure.m(this.data.getAccelerationY());
	double mag = Math.sqrt(xMeasure * xMeasure + yMeasure * yMeasure);

	double[] in = new double[]{(xMeasure/mag + 1.0D)/2.0D, (yMeasure/mag + 1.0D)/2.0D};
	nn.setInput(new TrainingElement(in).getInput());
	nn.calculate();
	return (float)(nn.getOutput().get(0)*2.0f*Math.PI - Math.PI);
    }

    public float getAverage()
    {
	if(this.xAverage == 0 && this.yAverage == 0)
	    return 0.0f;
	return (float)Math.atan2(this.yAverage, this.xAverage);
    }

    public static float normalize(float angle)
    {
	if(angle < 0)
	    angle = Math.abs(angle);
	else if(angle > 0)
	    angle = 2.0f*(float)Math.PI - angle;
	return angle;
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
	try
	    {
		File file = new File("sdcard/com.adr/files/directionNN.net");
		this.directionNnFw = new FileWriter(file, true);
		this.directionNnFw.write("\n");
		Scanner scanner = new Scanner(file);
		String line = "";

		for(int i = 0; (line = this.readLine(scanner)) != null; ++i)
		    {
			String[] raw = line.split(" ");
			this.train(Float.valueOf(raw[0]), Float.valueOf(raw[1]), Float.valueOf(raw[2]));
		    }
	    }
	catch(Exception e)
	    {
	    }
    }
    
    public void stop()
    {
	try
	    {
		this.directionNnFw.close();
	    }
	catch(Exception e)
	    {
		
	    }
    }

    public void update()
    {
	
    }

    private Data data = null;
    private long sampleSize = 0;
    private float average = 0;
    private float xAverage = 0;
    private float yAverage = 0;
    private FileWriter directionNnFw = null;

    private long numberOfSamples = 0;
    private float xMAverage = 0;
    private float yMAverage = 0;
    private float angleAverageX = 0;
    private float angleAverageY = 0;

    private MultiLayerPerceptron nn = new MultiLayerPerceptron(TransferFunctionType.TANH, 2, 80, 5, 1);
 }
