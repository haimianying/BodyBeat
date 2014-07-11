#include <stdlib.h> //for malloc and free functions
#include <math.h> // for log, exp
#include <voice_features.h>
#include <parameter_features.h>

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "JNI_DEBUGGING", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,   "JNI_DEBUGGING", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)

//**********************************************************************************
//
// 	GLOBAL VARIABLES
//
//**********************************************************************************

//increment values
int i, j;

double sumWeighted, sumUnweighted;
double ans;

//others
double mean4normData;
double *inputFrameData = NULL;
double * normalizedSpec = NULL;
double *magnSpect = NULL;
float *normalizedData = NULL; //[FRAME_LENGTH];

//auto correlation variables
float *acorr = NULL;
float *normalizedAcorr = NULL; //[FRAME_LENGTH/2];
int numAcorrPeaks, maxAcorrPeakLag;
float maxAcorrPeakVal;
double *acorrPeakLagValueArray = NULL;
double *acorrPeakValueArray = NULL;

//new noise levels
float noise_levels_squared[] = { 313323.3139, 3447421.4631, 849487.3317,
		4970466.8673, 113357.2991, 5555636.7655, 1855021.6219, 895606.1497,
		3065567.8316, 776335.2660, 854274.0327, 1090088.0994, 3019730.0877,
		3655844.2966, 236999.0142, 4304609.4843, 6405226.2696, 1843092.3695,
		9173316.6939, 21630828.8307, 16263388.4042, 11404754.1548,
		19941196.6663, 1457255.9371, 18168342.4730, 42052289.2467,
		44042325.6875, 32247722.5981, 2011676.3041, 6074299.4380, 5882211.8827,
		9995846.5714, 7108377.8073, 9648296.5913, 7957625.2395, 4869497.9966,
		640651.8908, 6916802.0686, 2757616.1853, 4164544.5107, 12342580.5526,
		13503568.5890, 13845908.3417, 9493927.5368, 3114302.4080, 2392750.0994,
		8492050.7365, 10400196.9471, 1305503.7668, 1086534.2132, 4764130.5073,
		16487630.4039, 8986034.1857, 13336078.2968, 31309463.3870,
		10731556.8386, 1014672.1116, 34366201.4782, 50240615.6186,
		21330372.7745, 24961380.5634, 10249259.9054, 30486819.3726,
		16894792.6252, 7039800.0114, 12147734.2424, 2267833.3929, 902650.3518,
		12137600.2109, 14307545.3700, 5672974.7581, 8532034.5470, 2681650.1898,
		2414907.9629, 18043696.6965, 6332893.9686, 4360285.3629, 25501331.0764,
		11391750.0371, 21282070.4616, 16918551.0222, 5188720.3195, 1865565.8037,
		5893415.0590, 33805262.5840, 30592159.4570, 7335110.9880, 14156640.2657,
		4487435.8089, 988106.6597, 728928.4012, 8288801.8810, 28150424.8026,
		32061833.2068, 11747452.8986, 575020.8570, 6235905.0577, 10763087.3341,
		8611935.2125, 2721796.2762, 11942092.8518, 10560377.4934, 5342085.1527,
		8831059.9619, 16781420.4441, 12025451.2722, 6820551.0588, 8460722.3187,
		3395084.4233, 1645975.5070, 9650400.4853, 21628000.1132, 12631230.1225,
		19762860.7356, 2021392.2510, 174910.5152, 2625306.3453, 7631385.1782,
		542882.9105, 131875.3727, 9141038.7323, 22794483.8868, 3463261.7127,
		2486781.1943, 22100238.5963, 32082266.0727, 3480147.2106, 5468995.4561 };

//**********************************************************************************
//
// 	initialize voiced features values
//
//**********************************************************************************
void initVoicedFeaturesFunction() {
	//initialization
	inputFrameData = (double *) malloc(FRAME_LENGTH * sizeof(double));
	normalizedSpec = (double *) malloc(FRAME_LENGTH * sizeof(double));

	acorr = (float *) malloc(FRAME_LENGTH * sizeof(float));
	normalizedAcorr = (float *) malloc((HALF_FRAME_LENGTH) * sizeof(float));

	normalizedData = (float *) malloc(FRAME_LENGTH * sizeof(float));
	magnSpect = (double *) malloc(FRAME_LENGTH * sizeof(double));
	acorrPeakValueArray = (double *) malloc(HALF_FRAME_LENGTH * sizeof(double));
	acorrPeakLagValueArray = (double *) malloc(
	HALF_FRAME_LENGTH * sizeof(double));

}

//**********************************************************************************
//
// 	destroy voiced features values
//
//**********************************************************************************
void destroyVoicedFeaturesFunction() {
	SAFE_DELETE(normalizedSpec);
	SAFE_DELETE(acorr);
	SAFE_DELETE(normalizedAcorr);

	SAFE_DELETE(normalizedData);
	SAFE_DELETE(acorrPeakValueArray);
	SAFE_DELETE(acorrPeakLagValueArray);

}

//**********************************************************************************
//
// 	computed energy or loudness.
//
//**********************************************************************************
double computeEnergy(double *energySpec2, int len) {
	sumUnweighted = 0;

	for (i = 0; i < len; i++) {
		sumUnweighted += energySpec2[i];
	}
	return sumUnweighted;
}

double computeLogTotalPower(double *powerSpec2, int len) {
	sumUnweighted = 0;

	for (i = 0; i < len; i++) {
		sumUnweighted += powerSpec2[i];
	}
	return log(sumUnweighted);
}

void computeLogSubbandPower(double *powerSpec2, int len, double *logSubbandPwr) {
	for (i = 0; i < 8; i++) {
		logSubbandPwr[i] = 0;
	}
	for (i = 0; i < len; i = i + 1) {
		ans = i * FREQSHIFT_PER_FFTBIN;
		if (ans <= HALF_SAMPLING_FREQ / 128) {
			logSubbandPwr[0] += powerSpec2[i];
		} else if (ans > HALF_SAMPLING_FREQ / 128
				&& ans <= HALF_SAMPLING_FREQ / 64) {
			logSubbandPwr[1] += powerSpec2[i];
		} else if (ans > HALF_SAMPLING_FREQ / 64
				&& ans <= HALF_SAMPLING_FREQ / 32) {
			logSubbandPwr[2] += powerSpec2[i];
		} else if (ans > HALF_SAMPLING_FREQ / 32
				&& ans <= HALF_SAMPLING_FREQ / 16) {
			logSubbandPwr[3] += powerSpec2[i];
		} else if (ans > HALF_SAMPLING_FREQ / 16
				&& ans <= HALF_SAMPLING_FREQ / 8) {
			logSubbandPwr[4] += powerSpec2[i];
		} else if (ans > HALF_SAMPLING_FREQ / 8
				&& ans <= HALF_SAMPLING_FREQ / 4) {
			logSubbandPwr[5] += powerSpec2[i];
		} else if (ans > HALF_SAMPLING_FREQ / 4
				&& ans <= HALF_SAMPLING_FREQ / 2) {
			logSubbandPwr[6] += powerSpec2[i];
		} else if (ans > HALF_SAMPLING_FREQ / 2 && ans <= HALF_SAMPLING_FREQ) {
			logSubbandPwr[7] += powerSpec2[i];
		}
	}

	for (i = 0; i < 8; i++) {
		logSubbandPwr[i] = log(logSubbandPwr[i]);
	}

}

//**********************************************************************************
//
// 	computed margnitude of the fft values.
//	needed for computing spectral entropy and relative spectral entropy values
//
//**********************************************************************************
void computeMagnitudeSpec(double* src, int len) {
	for (j = 0; j < len; j++) {
		magnSpect[j] = sqrt(src[j]);
	}
}

//**********************************************************************************
//
// 	normalize autocorrelation values to stay between 1 or -1.
//
//**********************************************************************************
void normalizeAcorr(const float *in, float *out, int outLen) {
	int i;

	for (i = 0; i < outLen; i++) {
		out[i] = (float) ((float) in[i] / in[0]);
	}

}

//**********************************************************************************
//
// 	adds low power white noise to the signal to counter against low power peridic humming noise.
//
//**********************************************************************************
void whitenPowerSpectrumToCpx(const double *powerSpec, double *out, int energy,
		int len) {

	for (j = 0; j < len; j++) {
		out[j] = powerSpec[j] + energy * noise_levels_squared[j]; //energy;
	}
}

//**********************************************************************************
//
// 	zero  mean the audio signal
//
//**********************************************************************************
void normalize_data() //zero mean data
{
	//normalize data
	mean4normData = 0;

	for (i = 0; i < FRAME_LENGTH; i++) {
		mean4normData = mean4normData + inputFrameData[i]; //inputFrameData will contain the power spectra sent from the Maple side
	}

	mean4normData = mean4normData / FRAME_LENGTH;

	for (i = 0; i < FRAME_LENGTH; i++) {
		normalizedData[i] = inputFrameData[i] - mean4normData; //zero mean the data
	}

}

//**********************************************************************************
//
// 	find the peak values and corresponding lags of autocorrelation values
//
//**********************************************************************************
void findPeaks(const float *in, int length, int *numPeaks, float *maxPeakVal,
		int *maxPeakLag) {

	int i;
	float maxPeak = 0;
	int maxPeakIdx = 0;

	float nonInitialMax = 0;
	int maxIdx = 0;
	float gMin = 0;

	float lastVal;

	int pastFirstZeroCrossing = 0;

	int tn = 0;

	// start with (and thus skip) 0 lag
	lastVal = in[0];

	float localMaxPeakValue = 0;
	int localMaxPeakIndex = 0;

	for (i = 1; i < length; i++) {

		if (pastFirstZeroCrossing) {
			// are we in a peak?
			if (lastVal >= 0 && in[i] >= 0) {
				// then check for new max

				if (in[i] > localMaxPeakValue) {

					localMaxPeakValue = in[i];
					localMaxPeakIndex = i;

					if (in[i] > maxPeak) {
						maxPeak = in[i];
						maxPeakIdx = i;
					}
				}

				// did we just leave a peak?
			} else if (lastVal >= 0 && in[i] < 0 && maxPeak > 0) {

				// count the last peak
				acorrPeakValueArray[tn] = (double) localMaxPeakValue;
				acorrPeakLagValueArray[tn] = (double) localMaxPeakIndex;
				tn++;

			} else if (lastVal < 0 && in[i] >= 0) {
				//set the local acorr max to zero
				localMaxPeakValue = in[i];
				//localMaxPeakIndex = 0;

				// then check for new max
				if (in[i] > maxPeak) {
					maxPeak = in[i]; //it does only need non-initial maxpeak, so it not resetting the peak value every time
					maxPeakIdx = i;
				}
			}
		} else {
			if (in[i] <= 0) {
				pastFirstZeroCrossing = 1; //zero crossing is for initial peak (value always one)
			}
		}

		lastVal = in[i];

	}

	// set the return values
	*numPeaks = tn;

	*maxPeakVal = maxPeak;
	*maxPeakLag = maxPeakIdx;

}

//**********************************************************************************
//
// 	computes the autorcorrelation values
//
//**********************************************************************************
void computeAutoCorrelationPeaks2(const double* powerSpec_l,
		double* powerSpecCpx_l, int NOISE_01_l, int len) {
	whitenPowerSpectrumToCpx(powerSpec_l, powerSpecCpx_l, NOISE_01_l, len);

	//kiss_fftri(cfgInv, powerSpecCpx_l, acorr);<----------we need this.

	normalizeAcorr(acorr, normalizedAcorr, HALF_FRAME_LENGTH);

	//find peaks using autocorrealation values
	findPeaks(normalizedAcorr, HALF_FRAME_LENGTH, &numAcorrPeaks,
			&maxAcorrPeakVal, &maxAcorrPeakLag);

}

//**********************************************************************************
//
// 	normalize the spectrum in unit sum
//
//**********************************************************************************

void normalizeSpec(int len) {
	sumUnweighted = 0;
	for (i = 0; i < len; i++) {
		sumUnweighted = sumUnweighted + inputFrameData[i];
	}

	for (i = 0; i < len; i++) {
		normalizedSpec[i] = inputFrameData[i] / sumUnweighted;
	}
}

//**********************************************************************************
//
// 	Computes various spectral features
//
//**********************************************************************************

double spectralRolloff(int len, double percentage) {
	sumUnweighted = 0;
	for (i = 0; i < len; i++) {
		sumUnweighted = sumUnweighted + normalizedSpec[i];
		if (sumUnweighted >= percentage) {
			break;
		}
	}
	return i * FREQSHIFT_PER_FFTBIN; //<---to convert it to hz we multiply with FREQSHIFT_PER_FFTBIN
}

double spectralEntropy(int len) {
	sumWeighted = 0;
	for (i = 0; i < len; i++) {
		if (normalizedSpec[i] != 0) {
			sumWeighted = sumWeighted
					- (normalizedSpec[i] * log(normalizedSpec[i]));
		}
	}
	return sumWeighted;
}

double spectralCentroid(int len) {
	sumWeighted = 0;
	for (i = 0; i < len; i++) {
		sumWeighted = sumWeighted + (normalizedSpec[i] * i);
	}
	return sumWeighted * FREQSHIFT_PER_FFTBIN; //<---to convert it to hz we multiply with FREQSHIFT_PER_FFTBIN
}

double spectralVariance(double SpecCentroid, int len) { // the unit of centroid is already hz
	sumWeighted = 0;
	for (i = 0; i < len; i++) {
		ans = i * FREQSHIFT_PER_FFTBIN - SpecCentroid; //<---to convert it to hz we multiply with FREQSHIFT_PER_FFTBIN
		sumWeighted = sumWeighted + (normalizedSpec[i] * ans * ans);
	}
	return sumWeighted;
}

double spectralSkew(double SpecCentroid, double SpecVariance, int len) { // the unit of centroid is already hz
	sumWeighted = 0;
	for (i = 0; i < len; i++) {
		ans = i * FREQSHIFT_PER_FFTBIN - SpecCentroid; //<---to convert it to hz we multiply with FREQSHIFT_PER_FFTBIN
		sumUnweighted = ans * ans; // using as a temp variable
		sumWeighted = sumWeighted + (normalizedSpec[i] * ans * ans * ans);
	}
	return sumWeighted; ///sqrt(pow(SpecVariance,3));
}

//**********************************************************************************
//
// 	Computes various statistical features: min, max, mean, variance,
//  we need median, skewness, kurtosis, slope,
//**********************************************************************************

double mean(double* inpArray, int len) {
	sumUnweighted = 0;
	for (i = 0; i < len; i++) {
		sumUnweighted = sumUnweighted + inpArray[i];
	}
	return sumUnweighted / len;
}

double variance(double* inpArray, int len, double mean) {
	sumWeighted = 0;
	for (i = 0; i < len; i++) {
		ans = inpArray[i] - mean;
		sumWeighted = sumWeighted + (ans * ans);
	}
	return sumWeighted / len;
}

double min(double* inpArray, int len) {
	ans = 0;
	for (i = 0; i < len; i++) {
		if (inpArray[i] > ans) {
			ans = inpArray[i];
		}
	}
	return ans;
}

double max(double* inpArray, int len) {
	ans = 0;
	for (i = 0; i < len; i++) {
		if (inpArray[i] < ans) {
			ans = inpArray[i];
		}
	}
	return ans;
}

//**********************************************************************************
//
// 	Computes primitives from cluster centers
//
//**********************************************************************************

#define NumPrimitives 2//<-------------change it accordingly
#define NumFeatures 4//<---------------change it accordingly
int minDistIndex;
double minDist, dist;
double clusterCenters[NumPrimitives][NumFeatures] = { { 0, 201, 402, 603 }, {
		1607, 1808, 2009, 2209 } };

int computePremitives(double* featureVector) {
	minDist = 0;
	minDistIndex = 0;
	for (i = 0; i < NumPrimitives; i++) {
		dist = 0;
		if (i == 0) {
			for (j = 0; j < NumFeatures; j++) {
				dist = dist
						+ (clusterCenters[i][j] - featureVector[j])
								* (clusterCenters[i][j] - featureVector[j]);
			}
			minDist = dist;
			minDistIndex = i;
		} else {
			for (j = 0; dist < minDist && j < NumFeatures; j++) {
				dist = dist
						+ (clusterCenters[i][j] - featureVector[j])
								* (clusterCenters[i][j] - featureVector[j]);
			}
			if (dist < minDist) {
				minDist = dist;
				minDistIndex = i;
			}
		}
	}

	return minDistIndex;
}

