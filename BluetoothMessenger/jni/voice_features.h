#include <jni.h>

#define SAFE_DELETE(a)  if (a)  {     \
									free(a); \
									a = NULL; \
									}

//shared variable
extern int numAcorrPeaks, maxAcorrPeakLag;
extern float maxAcorrPeakVal;
extern double * inputFrameData;//This will contain the power spectra sent from the Maple side
extern double * normalizedSpec;//This will contain the normalized power spectra sent from the Maple side
extern float *normalizedData;
extern double *magnSpect;
extern double *acorrPeakValueArray;
extern double *acorrPeakLagValueArray;

//------------------------------------------------
//Initialize and Destroy global variables
//------------------------------------------------
void destroyVoicedFeaturesFunction();
void initVoicedFeaturesFunction();

//------------------------------------------------
//normalization
//------------------------------------------------
void normalize_data();
void normalizeSpec(int);

void computeMagnitudeSpec(double*,int);

//------------------------------------------------
//Energy Features
//------------------------------------------------
double computeEnergy(double *,int);
double computeLogTotalPower(double *,int);
void computeLogSubbandPower(double *powerSpec2, int len, double *logSubbandPwr);

void whitenPowerSpectrumToCpx(const double *powerSpec, double *out, int energy, int len);
void computeAutoCorrelationPeaks2(const double* powerSpec_l, double* powerSpecCpx_l, int NOISE_01_l, int len);
void findPeaks(const float *in, int length, int *numPeaks, float *maxPeakVal, int *maxPeakLag);
void normalizeAcorr(const float *in, float *out, int outLen);

//------------------------------------------------
//Spectral features
//------------------------------------------------
double spectralRolloff(int len,double percentage);
double spectralEntropy(int len);
double spectralCentroid(int len);
double spectralVariance(double SpecCentroid, int len);
double spectralSkew(double SpecCentroid, double SpecVariance, int len);

//------------------------------------------------
// This will run frame level statistical analysis
//------------------------------------------------
double mean(double*, int len);
double variance(double*, int len, double mean);
double min(double*, int len);
double max(double*, int len);

//------------------------------------------------
//Compute Primitives
//------------------------------------------------
int computePremitives(double* );
