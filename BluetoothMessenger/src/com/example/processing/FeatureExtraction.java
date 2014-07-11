package com.example.processing;

public class FeatureExtraction {
	
	static {
		System.loadLibrary("features");
	}
	
	public static native double[] features(double[] data);
	public static native double statisticalFunctions(double[] data, int type);
	public static native int estimatePrimitives(double[] data);
	public static native double[] inference(double[] data);
	public static native int classification(double[] data);
	public static native void initVariables();
	public static native void delVariables();
	
}
