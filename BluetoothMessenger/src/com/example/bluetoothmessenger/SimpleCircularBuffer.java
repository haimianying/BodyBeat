package com.example.bluetoothmessenger;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.os.AsyncTask;
import android.util.Log;

import com.example.processing.FeatureExtraction;

/**
 * SimpleCircularBuffer stores all of the feature data from the frames that are
 * read in via Bluetooth.
 * 
 * Each (windowSize/2) frame, it will create an AsyncTask that creates a window
 * level feature that we can then run GMM on
 */
public class SimpleCircularBuffer {

	private double[][] frameLevelFeatures;
	private double[] windowLevelFeat;
	private double[] feature;
	private int counter;
	private boolean firstRun;
	PrintWriter pw;
	
	// Variables dictating the sizes of the buffer
	public int frameSize = 256;// <---- The total number of data transferred from Embedded Unit 
								// total bytes/ bytes in each val
	private int windowSize = 24;
	private int windowShift = 12;// <---- for 50% overlap
	private int len = 100 * windowSize;// <-----The total number of rows in the
										// full two dimensional circular buffer,
										// frameLevelFeatures, which will contain multiple (10) windows
	
	private int numFrameLevelFeatures = 7;// <---- number of frame level features. This must match with the native layer value
	private int numWindowLevelFeatures = 7;// <---- number of window level features.
	int[][] mapping = { { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 },
			{ 4, 1 }, { 5, 1 }, { 6, 1 }}; // (frame
											// level
											// feat
											// index,
											// type
											// of
											// statistical
											// function)
	
	public SimpleCircularBuffer() {
		frameLevelFeatures = new double[len][numFrameLevelFeatures];
		windowLevelFeat = new double[numWindowLevelFeatures];
		feature = new double[windowSize];
		counter = 0;// <---- index of frame/row of frameLevelFeatures matrix
		firstRun = true;

		FeatureExtraction.initVariables();
	}

	/**
	 * Puts an array of bytes into the buffer
	 * 
	 * @param ins
	 *            the array we are inserting
	 * @return the results of the calculations if adding this array completes a
	 *         window
	 */
	public synchronized void update(byte[] ins, PrintWriter pw1) {
		pw=pw1;
		
		double[] frame = new double[frameSize];
		// convert the byte inputs to int16
		for (int i = 0; i < frameSize; i++) {
			//byte[] val_int16 = { ins[i * 2 + 1], ins[i * 2] };// <------- It was in
															// the reverse
															// order, I just
															// changed it.
			//frame[i] = 1.0 * (ByteBuffer.wrap(val_int16).getShort());
			
			
			byte[] val_int32 = { ins[i * 4 + 3], ins[i * 4 + 2], ins[i * 4 + 1], ins[i * 4] };// <------- It was in
															// the reverse
															// order, I just
															// changed it.
			frame[i] = 1.0 * (ByteBuffer.wrap(val_int32).getInt());
		}

		// Log.v(TAG, "Began frame at: " + System.currentTimeMillis());
		
		// frame level feature extraction is implemented in native layer.
		double[] feats = FeatureExtraction.features(frame);
		
		//putting the frame level features in a two dimensional matrix
		for (int i = 0; i < numFrameLevelFeatures; i++) {
			frameLevelFeatures[counter][i] = feats[i];
		}
		
		//Write to file for debugging purpose 
		//pw.append(" RawData: " + Arrays.toString(frame)); // Print data from each frame
		//pw.append(" FrameLevFeat: " + Arrays.toString(feats)); // Print Frame Level features
		//pw.append("\n");
		//pw.flush();
		
		//Log.i("BodyBeatDebug", "Counter: " + counter);
		
		// Perform calculations if a window was completed
		if ((counter + 1) % windowShift == 0) {

			// On the first window we want all windowSize frames
			if (firstRun == true && counter == windowSize - 1) {
				new SignalProcessNInferenceThread().execute(counter);
				//funcWinLevFeat(counter);
				firstRun = false;
			}

			// Every window after that we take the next windowSize/2 frames and
			// the previous windowSize/2 frames
			else if (firstRun == false)
				new SignalProcessNInferenceThread().execute(counter);
				//funcWinLevFeat(counter);
				
		}
		

		// increment the counter
		counter = (counter + 1 == len) ? 0 : counter + 1;
	}
	
	int funcWinLevFeat (int counts){
		
		int count = counts;
		int row,col;
		
		for (int i = 0; i < numWindowLevelFeatures; i++) {
			// Format the data in the window frame by frame
			int featIndex = 0;
			for (int j = count; j > count - windowSize; j--) {
				row = j < 0 ? len + j : j;
				col = mapping[i][0];
				Log.i("BodyBeatDebug", "i: " + i + "j: " + j + "row: " + row + "col: " + col);
				feature[featIndex] = frameLevelFeatures[row][col];
				featIndex++;
			}
			windowLevelFeat[i] = FeatureExtraction.statisticalFunctions(feature, mapping[i][1]);
			
		}
		
		//Writing the window level features to a file for debugging purpose
		//pw.append(" WindowLevFeat: " + Arrays.toString(windowLevelFeat)); // Print Frame Level features
		//pw.append("\n");
		//pw.flush();
		
		// GMM operations here
		int bodySoundClass = 0;

		return bodySoundClass;
		
	}
	
	// AsyncTask for creating an additional working thread to handle Login
	private class SignalProcessNInferenceThread extends
			AsyncTask<Integer, Void, Integer> {

		protected Integer doInBackground(Integer... counts) {
			// initializing the 10 window level features
			int count = counts[0];
			
			int row,col;
			
			for (int i = 0; i < numWindowLevelFeatures; i++) {
				// Format the data in the window frame by frame
				int featIndex = 0;
				for (int j = count; j > count - windowSize; j--) {
					row = j < 0 ? len + j : j;
					col = mapping[i][0];
					//Log.i("BodyBeatDebug", "i: " + i + "j: " + j + "row: " + row + "col: " + col);
					feature[featIndex] = frameLevelFeatures[row][col];
					featIndex++;
				}
				windowLevelFeat[i] = FeatureExtraction.statisticalFunctions(feature, mapping[i][1]);
				
			}
			
			//Writing the window level features to a file for debugging purpose
			pw.append(" WindowLevFeat: " + Arrays.toString(windowLevelFeat)); // Print Frame Level features
			pw.append("\n");
			pw.flush();
			
			// GMM operations here
			int bodySoundClass = 0;

			return bodySoundClass;
		}

		@Override
		protected void onPostExecute(Integer success) {

		}

	}


}
