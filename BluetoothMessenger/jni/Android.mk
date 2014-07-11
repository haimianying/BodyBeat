LOCAL_PATH:=$(call my-dir)

include $(CLEAR_VARS)
LOCAL_CFLAGS:= -Werror
LOCAL_MODULE:= features
LOCAL_SRC_FILES:= features.c mfcc.c voice_features.c inference.c
#LOCAL_LDLIBS := -llog 
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)