#include "inference.h"
#include <android/log.h>
#include <math.h>
#include <parameter_inference.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)
int i,j;
double featMinusMu, sum, mul, max;
double likelihoodModel[NUM_MODEL];

//--------------------------------------------------------------------------------------
// Estimate the class that maximize log likelihood using GMM models specified in model.h
//--------------------------------------------------------------------------------------
/*int estimateMaxLikelihoodModel(double *feat){

	//assuming there are 2 models
	likelihoodModel[0]=computeProbGMM(feat,weights1, &mu1[0][0], &cov1[0][0], 3, 3);
	likelihoodModel[1]=computeProbGMM(feat,weights2, &mu2[0][0], &cov2[0][0], 3, 3);

	j=0;
	max=likelihoodModel[0];
	for(i=1;i<2;i++){//run untill checking all the models
		if(likelihoodModel[i]>max){
			max=likelihoodModel[i];
			j=i;
		}
	}
	return j;
}*/

//----------------------------------------------------------------------------
// Compute log likelihood using GMM
//----------------------------------------------------------------------------
/*double computeProbGMM(double *feat,double *w, double *mu, double *Cov, int featLen, int numComponent){

	sum = 0;
	for(i=0;i<numComponent;i++){
		sum = sum + w[i]*computeProbGaussian(feat,mu+(i*featLen), Cov+(i*featLen), featLen);
	}
	return sum;
}*/

//----------------------------------------------------------------------------
// Compute Prob using Gaussian distribution
//----------------------------------------------------------------------------
double computeLLGaussian(double *feat,double *mu, double *Cov, int featLen)
{

	sum = 0;
	mul = 1;
	for(i=0;i<featLen;i++){
		featMinusMu=(feat[i] - mu[i]);
		sum = sum - featMinusMu*featMinusMu/Cov[i];
		mul=mul/Cov[i];
	}

	return (0.5*sum)+log(pow((2*3.14),featLen/2)*sqrt(mul));

}
//----------------------------------------------------------------------------
// Compute log likelihood using Gaussian distribution
// mu = center of the gaussian distribution
// Cov = diagonal covariance matrix
//----------------------------------------------------------------------------
/*double computeProbGaussian(double *feat,double *mu, double *Cov, int featLen)
{

	sum = 0;
	mul = 1;
	for(i=0;i<featLen;i++){
		featMinusMu=(feat[i] - mu[i]);
		sum = sum - featMinusMu*featMinusMu/Cov[i];
		mul=mul/Cov[i];
	}

	return exp(0.5*sum)/(pow((2*3.14),featLen/2)*sqrt(mul));

}*/
