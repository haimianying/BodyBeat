#define SAMPLING_FREQ 8000.0 //<--------------------sampling frequency of audio
#define HALF_SAMPLING_FREQ SAMPLING_FREQ/2
#define FFTSIZE 1024//<---------------------------The FFT size used in the maple side
#define FRAME_LENGTH 256//<-----------------------the number of total features/values received by the native layer
#define FREQSHIFT_PER_FFTBIN SAMPLING_FREQ/FFTSIZE //<-----------SAMPLING_FREQ/FFTSIZE(0:1:FFTSIZE/2) This is the freq corresponding to one side of the freq spectra
#define HALF_FRAME_LENGTH FRAME_LENGTH/2
#define PI 3.14159265

#define TOTAL_FEAT 7//<--------------------the number of total frame-level feature  extracted by the native layer
#define FRAME_PER_WINDOW 24 //<-----------------3 seconds, 3/.125=24
#define TOTAL_WINDOW_LEVEL_FEAT 10//<--------------------the number of total window-level feature
