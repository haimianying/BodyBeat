package com.example.bluetoothmessenger;

import java.io.PrintWriter;

import android.util.Log;

//****************************************************************************************************
//
//   InputProcessor Class takes care of the header checking and striping the data off from the packet
//
//****************************************************************************************************
public class InputProcessor {

	SimpleCircularBuffer cirBuffer;
	PrintWriter pw;
	int len = 1026;// <------------Specify the number of bytes each frame
					// contains
	byte[] msgBuffer = new byte[len]; // <------buffer that keeps a frame data
	int count = 0;

	byte[] preemptChkBuf = new byte[4];
	int countChkBuf = 0;
	boolean ignoreCorrupted = true; // because at the very beginning we are yet
									// to find the header
	boolean isReadingFFs = true; // because at the very beginning we are still
									// looking for FFs
	private static final String TAG = "InputProcessor";

	public InputProcessor(PrintWriter p) {
		pw = p;
		cirBuffer = new SimpleCircularBuffer();
	}

	/**
	 * Reads in values given by the bluetooth stream into SimpleCircularBuffer
	 */
	public void read(byte[] readBuf, int numRead) {
		if (!ignoreCorrupted) { // This data had the FF header

			int numBeforeFull = len - count;

			if (count + numRead >= len) {
				// update the circular buffer
				System.arraycopy(readBuf, 0, msgBuffer, count, numBeforeFull);
				cirBuffer.update(msgBuffer, pw);
				count = 0;
				ignoreCorrupted = true;
				isReadingFFs = true;

				if (count + numRead > len) {
					// Check for FF header on the next frame
					byte[] remains = new byte[numRead - numBeforeFull]; // the
																		// spillover
					System.arraycopy(readBuf, numBeforeFull, remains, 0,
							numRead - numBeforeFull);
					checkForStart(remains, numRead - numBeforeFull);
				}
			} else {
				// read in values normally
				System.arraycopy(readBuf, 0, msgBuffer, count, numRead);
				count += numRead;
			}
		} else
			// Look for the FF header
			checkForStart(readBuf, numRead);
	}

	/**
	 * Checks the contents of the readBuf to check if there are FF headers
	 */
	private void checkForStart(byte[] readBuf, int numRead) {
		// here we are checking for the start of a frame byte series...
		// 1,2,3,4,5
		int i;
		boolean isHeaderFound = false;
		for (i = 0; i < numRead; i++) {
			if (isReadingFFs) { // We are looking for FFs
				if (readBuf[i] != -1) { // Reset the search on a non-FF value
					countChkBuf = 0;
					ignoreCorrupted = true;
					continue;
				}

				// progress one step if found an FF
				preemptChkBuf[countChkBuf] = readBuf[i];
				countChkBuf++;
				Log.i(TAG, "Looking for FFs, " + countChkBuf + " FFs found");

				if (countChkBuf == 4) { // Now we look for data
					countChkBuf = 0;
					isReadingFFs = false;
				}

			} else if (readBuf[i] != -1) { // Data should start on non-FF byte
				ignoreCorrupted = false;
				isHeaderFound = true;
				break;
			}
		}

		// Completed the header, now we can read values
		if (isHeaderFound) {
			int numLeft = numRead - i;
			if (numLeft > 0) {
				byte[] remains = new byte[numLeft];
				System.arraycopy(readBuf, i, remains, 0, numLeft);
				read(remains, numLeft);
			}
		}
	}
}
