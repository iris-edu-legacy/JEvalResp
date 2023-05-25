// startJVM.h:  Header file for 'startJVM.c' -- 12/19/2001 -- [ET]

#ifndef STARTJVM_H
#define STARTJVM_H 1

#include <jni.h>

//if Windows compiler and 'DLLVERSION' defined to indicate DLL used:
#if (defined(_WIN32) || defined(WIN32)) && defined(DLLVERSION)
         //setup keywords for import and export of functions:
#if __BORLANDC__        //if Borland compiler
#define IMPORT_KEYWORD cdecl _import
#define EXPORT_KEYWORD cdecl _export
#else                   //if not Borland compiler (Microsoft)
#define IMPORT_KEYWORD
#define IMPORT_KEYWORD __declspec(dllimport)
#define EXPORT_KEYWORD __declspec(dllexport)
#endif
         //define import or export keyword:
#ifdef LIBSOURCEFLG               //if DLL library source then
#define DLLEXT EXPORT_KEYWORD     //use export keyword for DLL exports
#else                             //if not DLL library source then
#define DLLEXT IMPORT_KEYWORD     //use import keyword for DLL imports
#endif
         //if not Windows or not DLL version:
#else
#ifndef DLLEXT               //if 'DLLEXT' not defined then
#define DLLEXT               //define to null
#endif

#endif


#ifdef __cplusplus           //if C++ then
extern "C" {                 //turn off function name "mangling"
#endif

// Starts up a Java Virtual Machine.  Only one may be running at a time.
//   altclspathstr - the name of an environment variable to used as an
//                   alternate classpath, appended to the "CLASSPATH" value,
//                   or NULL for none.
//   namestr       - the name of an environment variable to be passed to
//                   the JVM, or NULL for none.
// Returns a pointer to the Java environment if successful, NULL if error.
JNIEnv * DLLEXT startJVM(char *altclspathstr,char *namestr);

// Returns pointer to Java Virtual Machine, or null if JVM not running.
JavaVM * DLLEXT getJVM(void);

// Returns pointer to Java environment, or null if JVM not running.
JNIEnv * DLLEXT getJNIEnv(void);

// Stops the Java Virtual Machine started by 'startJVM()'.
void DLLEXT stopJVM(void);

// Returns 1 if the JVM is running, 0 if not.
int DLLEXT isJVMrunning(void);

#ifdef __cplusplus           //if C++ then
}                            //close extern "C" bracket
#endif

#endif

