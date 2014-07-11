

int estimateMaxLikelihoodModel(double *feat);
double computeProbGMM(double *feat,double *w, double *mu, double *Cov, int featLen, int numComponent);
double computeProbGaussian(double *feat,double *mu, double *Cov, int featLen);
double computeLLGaussian(double *feat,double *mu, double *Cov, int featLen);
