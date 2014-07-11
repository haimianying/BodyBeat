#include <string.h>
#include <jni.h>
#include <math.h>
#include <android/log.h>
#include <voice_features.h>
#include <mfcc.h>
#include <parameter_features.h>
#include <parameter_inference.h>
#include <inference.h>

#define DEBUG 0


//MFCC variables
double * mfccBuffer=NULL;
double * frameLevelFeat=NULL;//the actual output double array in the native layer
double * logSubbandPwr=NULL;
double * featiPerWindow = NULL;
double * windowLevelFeat=NULL;
double * fitnessMeasure = NULL;
double temp;



//Parameters of Model 1
double mu1[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov1[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 2
double mu2[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov2[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 3
double mu3[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov3[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 4
double mu4[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov4[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 5
double mu5[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov5[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 6
double mu6[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov6[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 7
double mu7[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov7[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 8
double mu8[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov8[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};

//Parameters of Model 9
double mu9[10] = {46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543,  -0.0561,  -14.4209, 46.3543};
double cov9[10] = {26.3543,  -0.0561,  -4.4209, 4.3543,  -0.561,  -1.4209, 4.3543,  -0.0561,  -1.4209, 46.3543};



//-------------------------------------------------------------------------------------------
// Initializing all the global variables
//-------------------------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_example_processing_FeatureExtraction_initVariables(JNIEnv* env, jobject this){
	if(DEBUG) LOGD("JNI audio init mfcc");

	//initializing the features for functions in voice_features.c
	initVoicedFeaturesFunction();

	//initializing the features for functions in mfcc.c
	//initMfcc(sample_rate, mell, high_freq, low_freq, inputlen);
	initMfcc(8000, 12, 4000, 20, 513);

	frameLevelFeat = (double *) malloc(TOTAL_FEAT * sizeof(double));
	windowLevelFeat = (double *) malloc(TOTAL_WINDOW_LEVEL_FEAT * sizeof(double));
	featiPerWindow = (double *) malloc(FRAME_PER_WINDOW * sizeof(double));
	logSubbandPwr = (double *) malloc(8 * sizeof(double)); //size of logSubBandPwr is 8 now
	fitnessMeasure = (double *) malloc(NUM_MODEL * sizeof(double));
	return;
}

//-------------------------------------------------------------------------------------------
// Destroying all the global variables
//-------------------------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_com_example_processing_FeatureExtraction_delVariables(JNIEnv* env, jobject this){
	if(DEBUG) LOGD("JNI audio del mfcc");

	//deleting the features for functions in voice_features.c
	destroyVoicedFeaturesFunction();

	//deleting the features for functions in mfcc.c
	delMfcc();

	SAFE_DELETE(frameLevelFeat);
	SAFE_DELETE(windowLevelFeat);
	SAFE_DELETE(logSubbandPwr);
	SAFE_DELETE(featiPerWindow);
	SAFE_DELETE(fitnessMeasure);

	return;
}

//**********************************************************************************
//
// 	compute all the frame level features and pass the values to the primitive calculation
//
//**********************************************************************************

JNIEXPORT jdoubleArray JNICALL Java_com_example_processing_FeatureExtraction_features(JNIEnv* env, jobject this, jdoubleArray data){

	// All of the features
	// Get the information we needs jdouble * pdata =
	inputFrameData = (*env)->GetDoubleArrayElements(env, data, 0);// need to figure out if jdouble can be easily type casted to double
	jdoubleArray results = (*env) -> NewDoubleArray(env, TOTAL_FEAT);//initializing the output double array

	// Do the pre-processing
	normalizeSpec(FRAME_LENGTH);

	//Do the frame level feature extraction
	frameLevelFeat[0] = spectralRolloff(FRAME_LENGTH,.9);
	frameLevelFeat[1] = spectralCentroid(FRAME_LENGTH);
	frameLevelFeat[2] = spectralVariance(frameLevelFeat[1], FRAME_LENGTH);
	frameLevelFeat[3] = spectralSkew(frameLevelFeat[1], frameLevelFeat[2], FRAME_LENGTH);
	frameLevelFeat[4] = spectralEntropy(FRAME_LENGTH);

	//frameLevelFeat[5] = mean(normalizedSpec, FRAME_LENGTH);
	//frameLevelFeat[6] = variance(normalizedSpec, FRAME_LENGTH, frameLevelFeat[5]);
	//frameLevelFeat[7] = variance(normalizedSpec, FRAME_LENGTH, 0);//rms

	//frameLevelFeat[8] = min(normalizedSpec, FRAME_LENGTH);
	//frameLevelFeat[9] = max(normalizedSpec, FRAME_LENGTH);

	frameLevelFeat[5] = computeLogTotalPower(inputFrameData, FRAME_LENGTH);

	computeLogSubbandPower(inputFrameData, FRAME_LENGTH, logSubbandPwr);//5 values
	memcpy(frameLevelFeat+6, logSubbandPwr, (8)*sizeof(double) );

	// Single feature mfcc
	//mfccBuffer = calculateMfcc(inputFrameData);
	//memcpy(frameLevelFeat+16, mfccBuffer, (12)*sizeof(double) );

	//memcpy arrays :: void * memcpy ( void * destination, const void * source, size_t num );
	//memcpy(frameLevelFeat+2, mfccBuffer, (12)*sizeof(double) );

	//compute magnitude spectrum
	//computeMagnitudeSpec(inputFrameData, FRAME_LENGTH);//the output is saved in magnSpect, FRAME_LENGTH is defined in voice_features.h

	//memcpy arrays :: void * memcpy ( void * destination, const void * source, size_t num );
	//memcpy(frameLevelFeat+2+12, magnSpect, (512)*sizeof(double) );

	// Release the information we retrieved
	//(*env)->ReleaseDoubleArrayElements(env, data, inputFrameData, JNI_ABORT);//<<--------- check if JNI_ABORT is fine
	(*env)->SetDoubleArrayRegion(env,results,0,TOTAL_FEAT,(jdouble*)frameLevelFeat);
	return results;
}

JNIEXPORT jdouble JNICALL Java_com_example_processing_FeatureExtraction_statisticalFunctions(JNIEnv* env, jobject this, jdoubleArray data, jint type){

	// All of the features
	// Get the information we needs jdouble * pdata =
	featiPerWindow = (*env)->GetDoubleArrayElements(env, data, 0);// need to figure out if jdouble can be easily type casted to double

	int t = type;
	//Do the window level feature extraction
	if(t==0){
		return mean(featiPerWindow,FRAME_PER_WINDOW);
	}else if(t==1){
		temp = mean(featiPerWindow,FRAME_PER_WINDOW);
		return variance(featiPerWindow, FRAME_PER_WINDOW, temp);
	}else if(t==2){
		return variance(featiPerWindow, FRAME_PER_WINDOW, 0);
	}else if(t==3){
		return min(featiPerWindow, FRAME_PER_WINDOW);
	}else if(t==4){
		return max(featiPerWindow, FRAME_PER_WINDOW);
	}else{
		return 0;
	}

		/*else if(==5){
		return
	}else if(==6){

	}else if(==7){

	}else if(==8){

	}*/
}


//**********************************************************************************
//
// classify body sounds using window level features
//
//**********************************************************************************

JNIEXPORT jint JNICALL Java_com_example_processing_FeatureExtraction_classification(JNIEnv* env, jobject this, jdoubleArray data){

	// All of the features
	// Get the information we needs jdouble * pdata =
	windowLevelFeat = (*env)->GetDoubleArrayElements(env, data, 0);// need to figure out if jdouble can be easily type casted to double

	fitnessMeasure[0]=computeLLGaussian(windowLevelFeat, mu1, cov1, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[1]=computeLLGaussian(windowLevelFeat, mu2, cov2, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[2]=computeLLGaussian(windowLevelFeat, mu3, cov3, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[3]=computeLLGaussian(windowLevelFeat, mu4, cov4, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[4]=computeLLGaussian(windowLevelFeat, mu5, cov5, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[5]=computeLLGaussian(windowLevelFeat, mu6, cov6, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[6]=computeLLGaussian(windowLevelFeat, mu7, cov7, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[7]=computeLLGaussian(windowLevelFeat, mu8, cov8, TOTAL_WINDOW_LEVEL_FEAT);
	fitnessMeasure[8]=computeLLGaussian(windowLevelFeat, mu9, cov9, TOTAL_WINDOW_LEVEL_FEAT);

	double max = fitnessMeasure[0];
	int index = 0;
	int i;
	for (i = 1; i < 9; i++ ) {
		if (fitnessMeasure[i] > max) {
			max = fitnessMeasure[i];
			index = i;
		}
	}

	return index;
}





//**********************************************************************************
//
// 	compute all the frame level features and pass the values to the primitive calculation
//
//**********************************************************************************

/*

JNIEXPORT jint JNICALL Java_com_example_processing_FeatureExtraction_estimatePrimitives(JNIEnv* env, jobject this, jdoubleArray data){

	// All of the features
	// Get the information we needs jdouble * pdata =
	inputFrameData = (*env)->GetDoubleArrayElements(env, data, 0);// need to figure out if jdouble can be easily type casted to double
	jdoubleArray results = (*env) -> NewDoubleArray(env, TOTAL_FEAT);//initializing the output double array

	// Do the pre-processing
	normalizeSpec(FRAME_LENGTH);

	//Do the frame level feature extraction
	frameLevelFeat[0] = spectralCentroid(FRAME_LENGTH);
	frameLevelFeat[1] = spectralEntropy(FRAME_LENGTH);
	frameLevelFeat[2] = mean(normalizedSpec, FRAME_LENGTH);
	frameLevelFeat[3] = variance(normalizedSpec, FRAME_LENGTH, 0);//rms

	// Single feature mfcc
	//mfccBuffer = calculateMfcc(inputFrameData);

	//memcpy arrays :: void * memcpy ( void * destination, const void * source, size_t num );
	//memcpy(frameLevelFeat+2, mfccBuffer, (12)*sizeof(double) );

	//compute magnitude spectrum
	//computeMagnitudeSpec(inputFrameData, FRAME_LENGTH);//the output is saved in magnSpect, FRAME_LENGTH is defined in voice_features.h

	//memcpy arrays :: void * memcpy ( void * destination, const void * source, size_t num );
	//memcpy(frameLevelFeat+2+12, magnSpect, (512)*sizeof(double) );


	return computePremitives(frameLevelFeat);
}
*/
