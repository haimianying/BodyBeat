package com.example.bluetoothmessenger;

import java.io.PrintWriter;
import java.nio.ByteBuffer;

import android.os.AsyncTask;

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
	private int counter;
	private boolean firstRun;

	// Variables dictating the sizes of the buffer
	public int frameSize = 513;// <----
	private int windowSize = 24;
	private int windowShift = 12;// <---- for 50% overlap
	private int len = 10 * windowSize;// <-----The total number of rows in the
										// two dimensional circular buffer,
										// frameLevelFeatures
	private int numFeatures = 14;

	public SimpleCircularBuffer() {
		frameLevelFeatures = new double[len][numFeatures];
		counter = 0;
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
	public synchronized void update(byte[] ins, PrintWriter pw) {

		double[] frame = new double[frameSize];
		// convert the byte inputs to int16
		for (int i = 0; i < frameSize; i++) {
			byte[] int16 = { ins[i * 2 + 1], ins[i * 2] };// <------- It was in
															// the reverse
															// order, I just
															// changed it.
			frame[i] = 1.0 * (ByteBuffer.wrap(int16).getShort());
		}
		// Print data from each frame

		// Log.v(TAG, "Began frame at: " + System.currentTimeMillis());
		double[] feats = FeatureExtraction.features(frame);
		for (int i = 0; i < numFeatures; i++) {
			frameLevelFeatures[counter][i] = feats[i];
		}

		// Write to file
		// pw.append(Arrays.toString(data) + " FrameLevFeat: " +
		// Arrays.toString(feats));
		// pw.append("\n");
		// pw.flush();

		// Perform calculations if a window was completed
		if ((counter + 1) % windowShift == 0) {

			// On the first window we want all windowSize frames
			if (firstRun && counter == windowSize - 1) {
				new SignalProcessNInferenceThread().execute(counter);
				firstRun = false;
			}

			// Every window after that we take the next windowSize/2 frames and
			// the previous windowSize/2 frames
			else if (!firstRun)
				new SignalProcessNInferenceThread().execute(counter);
		}

		// increment the counter
		counter = (counter + 1 == len) ? 0 : counter + 1;
	}

	// AsyncTask for creating an additional working thread to handle Login
	private class SignalProcessNInferenceThread extends
			AsyncTask<Integer, Void, Integer> {

		protected Integer doInBackground(Integer... counts) {
			// initializing the 10 window level features
			int count = counts[0];
			int[][] indices = { { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 },
					{ 5, 1 }, { 6, 1 }, { 7, 1 }, { 8, 1 }, { 9, 1 }, { 10, 1 } }; // (frame
																					// level
																					// feat
																					// index,
																					// type
																					// of
																					// statistical
																					// function)
			double[] windowLevelFeat = new double[indices.length];
			for (int i = 0; i < windowLevelFeat.length; i++) {
				double[] feature = new double[windowSize];

				// Working with the statistical functions
				// windowLevelFeat[i]=FeatureExtraction.statisticalFunctions(data,1);//<------I
				// am not sure if indexing with i and data should be all the 24
				// values for feature i

				// Format the data in the window frame by frame
				int featIndex = 0;
				for (int j = count; j > count - windowSize; j--) {
					int index = j < 0 ? len + j : j;
					feature[featIndex] = frameLevelFeatures[index][indices[i][0]];
					featIndex++;
				}
				windowLevelFeat[i] = FeatureExtraction.statisticalFunctions(
						feature, indices[i][1]);
			}

			// GMM operations here
			int bodySoundClass = 0;

			return bodySoundClass;
		}

		@Override
		protected void onPostExecute(Integer success) {

		}

	}

}
