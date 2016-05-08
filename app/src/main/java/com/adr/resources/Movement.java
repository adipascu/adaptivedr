package com.adr.resources;

import com.adr.resources.DataHandler;
import com.adr.resources.Data;
import com.adr.resources.LevelnessMeasure;
import com.adr.resources.AccelerationMeasure;
import com.adr.Adr;
import com.adr.math.IndexSigmoid;
import com.adr.math.IndexLine;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.util.TransferFunctionType;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;

public class Movement extends DataHandler
{
    public Movement(Data data)
    {
	this.data = data;
    }

    public void restart()
    {
	
    }

    private int getInputSize()
    {
    	return (Adr.UPDATE_INTERVAL/1000)*(Movement.MOVEMENT_MAX_FREQUENCY - Movement.MOVEMENT_MIN_FREQUENCY) + 1;
    }

    private float[] getMax()
    {
	float[] a = new float[Adr.UPDATE_INTERVAL];
	try
	    {
		Scanner scanner = new Scanner(this.data.getAccelerationFile());	
		String[] raw = null;
		String line = null;
		float value = 0;
		float lastValue = 0;
		long lastTime = 0;
		long time = 0;
		float m = 0;
		
		// fill missing data
		for(long t = 0; t < a.length; ++t)
		    {
			// if t is the last time read, read the next
			if(t == time)
			    {
				line = this.readLine(scanner);
				if(line != null)
			    {
					lastValue = value;
					lastTime = time;
					raw = line.split(" ");
					time = Long.valueOf(raw[0]);
					value = Float.valueOf(raw[3]); // z accel

					if(t == 0)
					    lastValue = value;

					// if this is the first one,
					// we want a horizontal trend
					// fill
					if(lastTime == -1)
					    {
						lastValue = value;
						lastTime = 0;
					    }
					
					if(lastTime != time)
					    m = (float)(value - lastValue)/(float)(time - lastTime);
				    }
			    }

			a[(int)t] = m*(t - lastTime) + lastValue;
		    }
	    }
	catch(Exception e)
	    {
	    }

	FloatFFT_1D fft = new FloatFFT_1D(a.length);
	fft.realForward(a);

	// remove the zero frequency, just in case, since we do not
	// ever need it anyway
	a[0] = 0;
	a[1] = 0;

	// find the maximum magnitude
	float mag = 0;
	float max = 0;
	int f = 0;

	for(int i = 1; i < Adr.UPDATE_INTERVAL/2 - 1; ++i)
	    {
		mag = (float)Math.sqrt(a[2*i]*a[2*i] + a[2*i + 1]*a[2*i + 1]); 

		if(mag > max)
		    {
			max = mag;
			f = i;
		    }
	    }
	

	float[] vals = new float[3];
	vals[0] = (float)f/(float)(Adr.UPDATE_INTERVAL/1000);
	vals[1] = max;
	
	float[] seg = new float[Movement.INPUT_SIZE];
	for(int i = 0; i < Movement.INPUT_SIZE; ++i)
	    {
		seg[i] = a[(i + Movement.MOVEMENT_MIN_FREQUENCY)*(Adr.UPDATE_INTERVAL/1000)];
	    }

	vals[2] = LevelnessMeasure.m(seg)/max;

	a = null;
       
	return vals;
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

    public boolean decide()
    {
	float[] vals = this.getMax();

	Adr.addOutput(vals[0] + " " + vals[1]);
	if(this.getHighest(this.getFreqIndex(vals[0]), this.getAmpIndex(vals[1]), this.getMeasureIndex(), this.getLevelnessIndex(vals[2])) ==
	   1)
	    return true;
	else
	    return false;
    }

    public void adjust(boolean moving)
    {
	float[] vals = this.getMax();

	this.smooth(this.getFreqIndex(vals[0]), this.getAmpIndex(vals[1]), this.getMeasureIndex(), this.getLevelnessIndex(vals[2]), moving ? 1 : 0);
    }

    private void smooth(int freq, int amp, int measure, int levelness, int moving)
    {
	for(int o = 0; o < Movement.OUTPUT_BIN_SIZE; ++o)
	    {
		bins[freq][amp][measure][levelness][o] += Math.pow(0.5, Math.abs(moving-o));
		bins[freq][amp][measure][levelness][o] *= 0.98;
		Adr.addOutput("smooth: " + o + " " + freq + " " + amp + " " + measure + " " + levelness);
	    }

	Adr.addOutput("smooth: " + moving + " " + bins[freq][amp][measure][levelness][0] + " " + bins[freq][amp][measure][levelness][1]);
    }

    private int getHighest(int freq, int amp, int measure, int levelness)
    {
	float max = -1;
	int index = 0;
	for(int o = 0; o < Movement.OUTPUT_BIN_SIZE; ++o)
	    {
		// NOTE: doing >= makes it default to moving
	 	if(Float.compare(this.bins[freq][amp][measure][levelness][o], max) >= 0)
		    {
			max = this.bins[freq][amp][measure][levelness][o];
			index = o;
		    }
	    }

	Adr.addOutput("highest: " + index + " " + bins[freq][amp][measure][levelness][0] + " " + bins[freq][amp][measure][levelness][1]);
	return index;
    }

    private int getFreqIndex(float freq)
    {
	return IndexLine.getIndex(Movement.FREQUENCY_BIN_SIZE, 10, freq * (float)(Adr.UPDATE_INTERVAL/1000));
    }

    private int getAmpIndex(float amp)
    {
	return IndexSigmoid.getIndex(Movement.AMPLITUDE_BIN_SIZE, Movement.MOVEMENT_AMPLITUDE_SENSITIVITY, amp);
    }

    private int getLevelnessIndex(float levelness)
    {
	return IndexSigmoid.getIndex(Movement.LEVELNESS_MEASURE_SIZE, 1500, levelness);
    }

    private int getMeasureIndex()
    {
	float x = AccelerationMeasure.m(this.data.getAccelerationX());
	float y = AccelerationMeasure.m(this.data.getAccelerationY());
	float mag = (float)Math.sqrt(x * x + y * y);
	Adr.addOutput("mag: " + mag);
	return IndexSigmoid.getIndex(Movement.ACCEL_MEASURE_SIZE, 50, mag);
    }

    public void start()
    {
	this.load("sdcard/com.adr/files/movement.save");
    }

    public void stop()
    {
	this.save("sdcard/com.adr/files/movement.save");
    }

    public void update()
    {
	
    }

    public void save(String file)
    {
	try
	    {
		FileWriter fw = new FileWriter(new File(file));

		for(int f = 0; f < Movement.FREQUENCY_BIN_SIZE+1; ++f)
		    for(int a = 0; a < Movement.AMPLITUDE_BIN_SIZE+1; ++a)
			for(int c = 0; c < Movement.ACCEL_MEASURE_SIZE+1; ++c)
			    for(int l = 0; l < Movement.LEVELNESS_MEASURE_SIZE+1; ++l)
				for(int o = 0; o < Movement.OUTPUT_BIN_SIZE; ++o)
				    {
					fw.write(this.bins[f][a][c][l][o]+",");
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
		Scanner fr = new Scanner(new File(file));
		String[] saved = fr.nextLine().split(",");
		int size = 0;
		for(int f = 0; f < Movement.FREQUENCY_BIN_SIZE+1; ++f)
		    for(int a = 0; a < Movement.AMPLITUDE_BIN_SIZE+1; ++a)
			for(int c = 0; c < Movement.ACCEL_MEASURE_SIZE+1; ++c)
			    for(int l = 0; l < Movement.LEVELNESS_MEASURE_SIZE+1; ++l)
				for(int o = 0; o < Movement.OUTPUT_BIN_SIZE; ++o)
				    {
					if(size < saved.length)
					    {
						this.bins[f][a][c][l][o] = Float.valueOf(saved[size]);
						++size;
					    }
				    }

	    }
	catch(Exception e)
	    {
		
	    }	
    }
    
    private static final int MOVEMENT_MIN_FREQUENCY = 1;
    private static final int MOVEMENT_MAX_FREQUENCY = 2;
    private static final float MOVEMENT_AMPLITUDE_SENSITIVITY = 2000;
    private static final int INPUT_SIZE = (Adr.UPDATE_INTERVAL/1000)*(Movement.MOVEMENT_MAX_FREQUENCY - Movement.MOVEMENT_MIN_FREQUENCY) + 1;
    private static final int FREQUENCY_BIN_SIZE = 9;
    private static final int AMPLITUDE_BIN_SIZE = 3;
    private static final int OUTPUT_BIN_SIZE = 2;
    private static final int ACCEL_MEASURE_SIZE = 3;
    private static final int LEVELNESS_MEASURE_SIZE = 3;
    private Data data = null;
    private float[][][][][] bins = new float[Movement.FREQUENCY_BIN_SIZE+1][Movement.AMPLITUDE_BIN_SIZE+1][Movement.ACCEL_MEASURE_SIZE+1][Movement.LEVELNESS_MEASURE_SIZE+1][Movement.OUTPUT_BIN_SIZE];
}