/* DO NOT EDIT THIS FILE - it is machine generated */
/* Generated for class glguerin.io.imp.mac.macosx.TinAlias */
/* Generated from TinAlias.java*/

#ifndef _Included_glguerin_io_imp_mac_macosx_TinAlias
#define _Included_glguerin_io_imp_mac_macosx_TinAlias
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif

/**
	 TinAlias is the FileForker.Alias for MacOSXForker.
	 It maintains an AliasHandle internally, in addition to a Pathname
	 and a couple of FileInfo's.
	 
	 @author Gregory Guerin
*/

/*
 * Class:     glguerin_io_imp_mac_macosx_TinAlias
 * Method:    getHandleSize
 * Signature: (I)I
 */

/**
	 @return  size, always less than 2 GB; or negative error-code
	## int getHandleSize( int anyHand );
*/

JNIEXPORT jint JNICALL Java_glguerin_io_imp_mac_macosx_TinAlias_getHandleSize
	(JNIEnv *, jclass, jint);

/*
 * Class:     glguerin_io_imp_mac_macosx_TinAlias
 * Method:    getHandleData
 * Signature: (I[BII)I
 */

/**
	 @return  result-code
	## int getHandleData( int anyHand, byte[] bytes, int offset, int count );
*/

JNIEXPORT jint JNICALL Java_glguerin_io_imp_mac_macosx_TinAlias_getHandleData
	(JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * Class:     glguerin_io_imp_mac_macosx_TinAlias
 * Method:    freeHand
 * Signature: (I)I
 */

/**
	 @return  result-code
*/

JNIEXPORT jint JNICALL Java_glguerin_io_imp_mac_macosx_TinAlias_freeHand
	(JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
