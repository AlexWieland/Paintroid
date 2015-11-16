#include <jni.h>
#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <android/bitmap.h>
#include <queue>

#include <unistd.h>

#define  LOG_TAG    "jnibitmap"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define DEBUG 0

extern "C" {
  JNIEXPORT void JNICALL Java_com_threekkapps_library_bitmap_JniBitmap_floodFill(JNIEnv * env, jobject obj, jobject handle, uint32_t x, uint32_t y, uint32_t color, uint32_t tolerance);
  JNIEXPORT void JNICALL Java_com_threekkapps_library_bitmap_JniBitmap_floodFillAnywhere(JNIEnv * env, jobject obj, jobject handle, uint32_t x, uint32_t y, uint32_t color, uint32_t tolerance);
  
bool isPixelValid(int currentColor,int oldColor,int tolerance);
  void floodFill(uint32_t x, uint32_t y, uint32_t color, void* bitmapPixels, AndroidBitmapInfo* bitmapInfo, uint32_t tolerance);
  void floodFill_anywhere(uint32_t x, uint32_t y, uint32_t color, void* bitmapPixels, AndroidBitmapInfo* bitmapInfo, uint32_t tolerance);
}



JNIEXPORT void JNICALL Java_com_threekkapps_library_bitmap_JniBitmap_floodFill(JNIEnv * env, jobject obj, jobject bitmap, uint32_t x, uint32_t y, uint32_t color, uint32_t tolerance) {
	AndroidBitmapInfo bitmapInfo;
	uint32_t* storedBitmapPixels = NULL;

	int ret;
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return;
	}

	if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888!");
		return;
	}

	void* bitmapPixels;
	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
	    LOGE("AndroidBitmap_lockPixels() really failed ! error=%d", ret);
	    return;

	}

	floodFill(x,y,color, bitmapPixels, &bitmapInfo,tolerance);

	AndroidBitmap_unlockPixels(env, bitmap);

}

JNIEXPORT void JNICALL Java_com_threekkapps_library_bitmap_JniBitmap_floodFillAnywhere(JNIEnv * env, jobject obj, jobject bitmap, uint32_t x, uint32_t y, uint32_t color, uint32_t tolerance) {
	AndroidBitmapInfo bitmapInfo;
	uint32_t* storedBitmapPixels = NULL;

	int ret;
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return;
	}

	if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888!");
		return;
	}

	void* bitmapPixels;
	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
	    LOGE("AndroidBitmap_lockPixels() really failed ! error=%d", ret);
	    return;

	}

uint32_t alpha=color&0xff000000;
uint32_t red=(color>>16)&0xFF;
uint32_t green=(color>>8)&0xFF;
uint32_t blue = color&0xFF;

color=alpha+blue*(1<<16)+green*(1<<8)+red;

	floodFill_anywhere(x,y,color, bitmapPixels, &bitmapInfo,tolerance);

	AndroidBitmap_unlockPixels(env, bitmap);

}

    bool isPixelValid(int currentColor,int oldColor,int tolerance) {

    	if(tolerance != 0) {
    		int alpha = ((currentColor & 0xFF000000) >> 24);
    		int red = ((currentColor & 0xFF0000) >> 16) ;//* alpha / 255; // red
    		int green = ((currentColor & 0x00FF00) >> 8)  ;//* alpha / 255; // Green
    		int blue = (currentColor & 0x0000FF) ;// * alpha / 255; // Blue
		
		int alpha1 = ((oldColor & 0xFF000000) >> 24);
    		int red1 = ((oldColor & 0xFF0000) >> 16) ;//* alpha / 255; // red
    		int green1 = ((oldColor & 0x00FF00) >> 8)  ;//* alpha / 255; // Green
    		int blue1 = (oldColor & 0x0000FF) ;// * alpha / 255; // Blue

		int cube = (alpha-alpha1)*(alpha-alpha1)+(red-red1)*(red-red1)+(blue-blue1)*(blue-blue1)+(green-green1)*(green-green1);
		tolerance*=tolerance; //power ^2

		if(cube<tolerance)return true;
		else return false;
/*

    				return (red >= (red1 - tolerance)
    						&& red <= (red1 + tolerance)
    						&& green >= (green1 - tolerance)
    						&& green <= (green1 + tolerance)
						&& alpha >= (alpha1 - tolerance)
    						&& alpha <= (alpha1 + tolerance)
    						&& blue >= (blue1 - tolerance)
    						&& blue <= (blue1 + tolerance));*/
    	}else {
    		if(currentColor == oldColor){
				return true;
			}else {
				return false;
			}
    	}
	}


void floodFill_anywhere(uint32_t x, uint32_t y, uint32_t color, void* bitmapPixels, AndroidBitmapInfo* bitmapInfo,uint32_t tolerance){
if (x >bitmapInfo->width-1)
	return;
if (y > bitmapInfo->height-1)
	return;
if (x < 0)
	return;
if (y < 0)
	return;
uint32_t* pixels = (uint32_t*) bitmapPixels;

uint32_t oldColor;
oldColor = pixels[y*bitmapInfo->width+x];


for(long i=0;i<bitmapInfo->width*bitmapInfo->height;i++){
	if( isPixelValid (pixels[i],oldColor,tolerance)){
		pixels[i]=color;
	}
		


}






}

void floodFill(uint32_t x, uint32_t y, uint32_t color, void* bitmapPixels, AndroidBitmapInfo* bitmapInfo,uint32_t tolerance) {

		// Used to hold the the start( touched ) color that we like to change/fill
		int values [3] = { };


    	if (x >bitmapInfo->width-1)
    		return;
    	if (y > bitmapInfo->height-1)
    		return;
    	if (x < 0)
    		return;
    	if (y < 0)
    		return;

    	uint32_t* pixels = (uint32_t*) bitmapPixels;

    	uint32_t oldColor;

    	int red = 0; int blue = 0; int green = 0; int alpha = 0;
    	oldColor = pixels[y*bitmapInfo->width+x];

    	// Get red,green and blue values of the old color we like to chnage
    	alpha = (int) ((color & 0xFF000000) >> 24);
    	values[0] = (int) ((oldColor & 0xFF0000) >> 16) * alpha / 255; // red
    	values[1] = (int)((oldColor & 0x00FF00) >> 8) * alpha / 255; // Green
    	values[2] = (int) (oldColor & 0x0000FF) * alpha / 255; // Blue


    	alpha = (int) ((color & 0xFF000000) >> 24);
    	blue = (int) ((color & 0xFF0000) >> 16);
		green = (int)((color & 0x00FF00) >> 8);
		red = (int) (color & 0x0000FF);
		blue = blue * alpha / 255;
		green = green * alpha / 255;
		red = red * alpha / 255;

		int tmp = 0;
		tmp = red;
		red = blue;
		blue = tmp;

		color =  ((alpha<< 24) & 0xFF000000) | ((blue<< 16) & 0xFF0000) |
		              ((green << 8) & 0x00FF00) |
		              (red & 0x0000FF);


		//LOGD("AndroidBitmap_lockPixels() totally failed ! error=%d");
		std::queue < uint32_t > pixelsX;
		std::queue < uint32_t > pixelsY;

    	int nx = 0; int ny = 0;
    	int wx = 0; int wy = 0; int ex = 0; int ey = 0;

    	pixelsX.push(x);
    	pixelsY.push(y);

    	while (!pixelsX.empty()) {

    		nx = pixelsX.front(); ny = pixelsY.front();
    		pixelsX.pop();
    		pixelsY.pop();

    		if (pixels[ny*bitmapInfo->width+nx] == color)
    			continue;

    		wx = nx; wy = ny;
    		ex = wx+1; ey = wy;

    		while (wx>=0 && isPixelValid(pixels[wy*bitmapInfo->width+wx],oldColor,tolerance)) {
    			pixels[wy*bitmapInfo->width+wx] = color;

    			if (wy >= 0 && pixels[(wy-1)*bitmapInfo->width+wx] == oldColor) {
    				pixelsX.push(wx);
    				pixelsY.push(wy-1);

    			}
    			if (wy < bitmapInfo->height - 1  && isPixelValid(pixels[(wy+1)*bitmapInfo->width+wx],oldColor,tolerance)) {
    				pixelsX.push(wx);
    				pixelsY.push(wy+1);

				}
    			wx--;
    		}


    		while (ex <= bitmapInfo->width -1 && isPixelValid(pixels[ey*bitmapInfo->width+ex],oldColor,tolerance)) {
				pixels[ey*bitmapInfo->width+ex] = color;
				if (ey > 0 && isPixelValid(pixels[(ey-1)*bitmapInfo->width+ex],oldColor,tolerance)) {
					pixelsX.push(ex);
					pixelsY.push(ey-1);

				}
				if (ey < bitmapInfo->height - 1  && isPixelValid(pixels[(ey+1)*bitmapInfo->width+ex],oldColor,tolerance)) {
					pixelsX.push(ex);
					pixelsY.push(ey+1);

				}
				ex++;
			}

    	}
    }



