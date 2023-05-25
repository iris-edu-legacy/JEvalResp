// startJVM.c:  Functions for starting and stopping a Java Virtual Machine.
//              In order for these to work, the library search path must
//              include the location of the required Java library (see
//              the JNI documentation from Sun for more information.)
//
//              Note:  The JVM seems to react badly to being restarted
//                     after having been stopped.
//
//  12/19/2001 -- [ET]
//
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#define LIBSOURCEFLG 1       //set indicator flag for headers 
#include "startJVM.h"

#if defined(_WIN32) || defined(WIN32)       //if Windows then
#define PATH_SEP_STR ";"                    //use ';' for path separator
#else                                       //if not Windows then
#define PATH_SEP_STR ":"                    //use ':' for path separator
#endif

         //local function:
int addStrOption(JavaVMOption *optsptr,char *str,int numopts);
int addEnvOption(JavaVMOption *optsptr,char *namestr,int numopts);

static JavaVM *g_jvm = NULL;      //pointer to Java Virtual Machine
static JNIEnv *g_jenv = NULL;     //pointer to Java environment

// Starts up a Java Virtual Machine.  Only one may be running at a time.
//   altclspathstr - the name of an environment variable to used as an
//                   alternate classpath, appended to the "CLASSPATH" value,
//                   or NULL for none.
//   namestr       - the name of an environment variable to be passed to
//                   the JVM, or NULL for none.
// Returns a pointer to the Java environment if successful, NULL if error.
JNIEnv * DLLEXT startJVM(char *altclspathstr,char *namestr)
{
  int varlen,numopts=0;
  char *cpvalptr,*altvalptr,clspathbuff[1024]="-Djava.class.path=";
  JavaVMOption optsarr[2];
  JavaVMInitArgs vm_args;

  if(g_jvm != NULL)          //if one running then
    stopJVM();               //stop JVM
  vm_args.version = JNI_VERSION_1_2;             //use JNI v1.2
  vm_args.ignoreUnrecognized = JNI_TRUE;
         //add value of alternate classpath variable to buffer:
  if(altclspathstr != NULL && strlen(altclspathstr) > 0 &&
                                (altvalptr=getenv(altclspathstr)) != NULL &&
                                           (varlen=strlen(altvalptr)) > 0 &&
                         varlen+strlen(clspathbuff)+3 < sizeof(clspathbuff))
  {      //alt classpath variable exists and size not too small or large
    strcat(clspathbuff,altvalptr);          //add to class path buffer
  }
  else   //alternate classpath not added
    altvalptr = NULL;        //indicate not added
         //add value of environment variable "CLASSPATH" to buffer:
  if((cpvalptr=getenv("CLASSPATH")) != NULL &&
                                            (varlen=strlen(cpvalptr)) > 0 &&
                         varlen+strlen(clspathbuff)+2 < sizeof(clspathbuff))
  {      //"CLASSPATH" variable exists and size not too small or large
    if(altvalptr != NULL)                   //if previous path entered then
      strcat(clspathbuff,PATH_SEP_STR);     //add path separator
    strcat(clspathbuff,cpvalptr);           //add to class path buffer
  }
  else   //"CLASSPATH" variable not added
    cpvalptr = NULL;         //indicate not added
                             //add options to define JVM properties:
  if(altvalptr != NULL || cpvalptr != NULL)           //if either classpath
    numopts = addStrOption(optsarr,clspathbuff,0);    // added then enter
  numopts = addEnvOption(optsarr,namestr,numopts);    //enter user env var
                                  //enter options array (or null if none):
  vm_args.options = (numopts > 0) ? optsarr : NULL;
  vm_args.nOptions = numopts;     //enter number of options
         //create the Java Virtual Machine:
  if(JNI_CreateJavaVM(&g_jvm,(void **)&g_jenv,&vm_args) < 0)
    g_jenv = NULL;      //if error then indicate with null value
  return g_jenv;        //return pointer to Java environment (or NULL)
}

// Adds a string to the set of options passed to the JVM.
//   optsptr - array of 'JavaVMOption' structures (must have enough room
//             for options).
//   str     - the string to be added.
//   numopts - current number of option items in 'optsptr' array.
// Returns the new value for 'numopts'.
int addStrOption(JavaVMOption *optsptr,char *str,int numopts)
{
  int len;
  char *valbuff;

  if(optsptr != NULL && str != NULL && (len=strlen(str)) > 0)
  {      //string OK; allocate space for option item
    if((valbuff=(char *)malloc(len+2)) != NULL)
    {    //space for option string allocated OK
      strcpy(valbuff,str);
      optsptr[numopts++].optionString = valbuff; //put into options array
    }                                            // and inc # of options
  }
  return numopts;       //return new number of option items
}

// Adds an evironment variable (if it exists) to the set of options
// passed to the JVM.
//   optsptr - array of 'JavaVMOption' structures (must have enough room
//             for options).
//   namestr - name of environment variable to add definition for.
//   numopts - current number of option items in 'optsptr' array.
// Returns the new value for 'numopts'.
int addEnvOption(JavaVMOption *optsptr,char *namestr,int numopts)
{
  int namelen,varlen;
  char *sptr,*valbuff;

  if(optsptr != NULL && namestr != NULL && (namelen=strlen(namestr)) > 0 &&
                (sptr=getenv(namestr)) != NULL && (varlen=strlen(sptr)) > 0)
  {      //environment variable exists; allocate space for option item
    if((valbuff=(char *)malloc(namelen+varlen+8)) != NULL)
    {    //space for options string allocated OK
      sprintf(valbuff,"-D%s=%s",namestr,sptr);   //setup to define variable
      optsptr[numopts++].optionString = valbuff; //put into options array
    }                                            // and inc # of options
  }
  return numopts;       //return new number of option items
}

// Returns pointer to Java Virtual Machine, or null if JVM not running.
JavaVM * DLLEXT getJVM()
{
  return g_jvm;
}

// Returns pointer to Java environment, or null if JVM not running.
JNIEnv * DLLEXT getJNIEnv()
{
  return g_jenv;
}

// Stops the Java Virtual Machine started by 'startJVM()'.
void DLLEXT stopJVM()
{
  if(g_jvm != NULL)
  {
    (*g_jvm)->DestroyJavaVM(g_jvm);
    g_jvm = NULL;
    g_jenv = NULL;
  }
}

// Returns 1 if the JVM is running, 0 if not.
int DLLEXT isJVMrunning()
{
  return (g_jvm != NULL) ? 1 : 0;
}

