// jevresp.c:  'C'-language native interface to the Java 'JEvalResp'
//             program.
//
// A Java Virtual Machine is started to run the Java classes, so the JVM
// library files must be available via the library search path for the
// operating system.  The environment variable "JEVRESP_CLASSPATH" may be
// set to the location of the JEvalResp jar or class files to have them
// added to the classpath for the JVM.  The environment variable "SEEDRESP"
// may used to specify an addition location to search for response ("RESP")
// files.
//
//   1/2/2002 -- [ET]  Initial version.
//  11/3/2005 -- [ET]  Added 'evresp_itp()' and 'print_resp_itp()'
//                     functions (although support for List-blockette
//                     interpolation parameters is not really implemented).
//
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#define LIBSOURCEFLG 1       //set indicator flag for headers 
#include "jevresp.h"

#define JEVRESP_CLSPATH_STR "JEVRESP_CLASSPATH"  //name of alt classpath
#define JEVRESP_SEEDRESP_STR "SEEDRESP"          //env var for JVM

                        //name of Java classes:
#define RUNBLKS_CLASS_NAME "Lcom/isti/jevalresp/RunBlks;"
#define RINFOBLK_CLASS_NAME "Lcom/isti/jevalresp/RespInfoBlk;"
#define COMPLEXBLK_CLASS_NAME "Lcom/isti/jevalresp/ComplexBlk;"
#define OBJECTARR_CLASS_NAME "[Ljava/lang/Object;"
#define JEVRESP_METHOD_NAME "rBlksEvresp"   //name of method for finding
#define WRITE_METHOD_NAME "rBlksWriteResponse"   //name of method for output
#define GETCODE_METHOD_NAME "getExitStatusValue" //method for get exit code

         //local functions:
int fetchstringfield(char *destbuff,int destlen,jobject jsrcobj,
                                                          jfieldID fieldid);
double *fetchdoublearrfield(jobject jsrcobj,jfieldID fieldid,int *pcount);
struct complex *fetchcomplexarrfield(jobject jsrcobj,jfieldID fieldid);
jdoubleArray createdoublearrobj(double *dblarr,int numelems);
jarray createcomplexarrobj(struct complex *cpxarr,int numelems);
void seterr(int code,char *errmsg);

static JNIEnv *g_jenv = NULL;     //pointer to Java environment
                                  //Java class variables:
static jclass g_stringclass,g_doublearrclass,g_complexblkclass,
                                                           g_objectarrclass;
static jmethodID g_cblkinitid;    //constructor method ID for 'ComplexBlk'
                                  //field IDs for Java 'ComplexBlk':
static jfieldID g_cblkrealid,g_cblkimagid;
static int g_exitcode = 0;        //exit status value returned by program

#ifdef EVALRESP_COMP         //if compiling with 'evalresp.c' then
int def_units_flag;          //include global variables usually
struct channel *GblChanPtr;
float unitScaleFact;
char *curr_file;
int curr_seq_no;             // residing in original 'evresp.c'
jmp_buf jump_buffer;
char FirstLine[MAXLINELEN];
int FirstField;
#endif


// Finds and processes responses, returning the output in a linked list
// of 'response' structures.  Error and warning messages are sent to
// 'stderr', and an exit status code value may be fetched via the
// "getexitcode()" function (non-zero indicating an error status).
// This function has the same input and output as the original 'evalresp'
// "evresp()" function and may be used in its place.
//   stalst       - a comma-separated list of station name patterns to
//                  search for, or a null or empty string to accept all
//                  station names.
//   chalst       - a comma-separated list of channel name patterns to
//                  search for, or a null or empty string to accept all
//                  channel names.
//   netlst       - a comma-separated list of network name patterns to
//                  search for, or a null or empty string to accept all
//                  network names.
//   locidlst     - comma-separated a list of location/site name patterns
//                  to search for, or a null or empty string to accept all
//                  site names.
//   datestr      - a string version of a date to search for, or null to
//                  accept all dates.
//   unitsconvstr - output units conversion string for the requested output
//                  units type; one of:  "def", "dis", "vel" or "acc".
//   filename     - a specific filename (or directory) to use, or a null or
//                  empty string for all matching files.
//   freqarr      - an array of frequency values to use.
//   numfreqs     - number of elements in the frequency array.
//   resptypestr  - a string indicating the type of response data to be
//                  generated (not used by this function but included to
//                  maintain compatibility with orignal 'evalresp' code).
//   verbosestr   - if "-v" then verbose output messages sent to 'stderr'.
//   startstage   - if greater than zero then the start of the range of
//                  stage sequence numbers to use, otherwise all stages
//                  are used.
//   stopstage    - if greater than zero then the end of the range of stage
//                  sequence numbers to use, otherwise only the single
//                  stage specified by 'startStageNum' is used.
//   stdioflag    - non-zero for input from 'stdin', zero for input from
//                  files.
// Returns an array of 'response' structures, or null if an error occurred
// (in which case a message will be sent to 'stderr' and an exit status
// code will be set that may fetched via the "getexitcode()" function).
struct response * DLLEXT evresp(char *stalst,char *chalst,char *netlst,
             char *locidlst,char *datestr,char *unitsconvstr,char *filename,
            double *freqarr,int numfreqs,char *resptypestr,char *verbosestr,
                                 int startstage,int stopstage,int stdioflag)
{
  return evresp2(stalst,chalst,netlst,locidlst,datestr,unitsconvstr,
                   filename,freqarr,numfreqs,((jboolean)((verbosestr!=NULL&&
                           strcmp(verbosestr,"-v")==0)?JNI_TRUE:JNI_FALSE)),
                                            startstage,stopstage,stdioflag);
}


// Version of 'evresp()' with improved set of parameters.
// Finds and processes responses, returning the output in a linked list
// of 'response' structures.  Error and warning messages are sent to
// 'stderr', and an exit status code value may be fetched via the
// "getexitcode()" function (non-zero indicating an error status).
// This function has the same input and output as the original 'evalresp'
// "evresp()" function and may be used in its place.
//   stalst       - a comma-separated list of station name patterns to
//                  search for, or a null or empty string to accept all
//                  station names.
//   chalst       - a comma-separated list of channel name patterns to
//                  search for, or a null or empty string to accept all
//                  channel names.
//   netlst       - a comma-separated list of network name patterns to
//                  search for, or a null or empty string to accept all
//                  network names.
//   locidlst     - comma-separated a list of location/site name patterns
//                  to search for, or a null or empty string to accept all
//                  site names.
//   datestr      - a string version of a date to search for, or null to
//                  accept all dates.
//   unitsconvstr - output units conversion string for the requested output
//                  units type; one of:  "def", "dis", "vel" or "acc".
//   filename     - a specific filename (or directory) to use, or a null or
//                  empty string for all matching files.
//   freqarr      - an array of frequency values to use.
//   numfreqs     - number of elements in the frequency array.
//   verboseflag  - if non-zero then verbose output messages are sent to
//                  'stderr'.
//   startstage   - if greater than zero then the start of the range of
//                  stage sequence numbers to use, otherwise all stages
//                  are used.
//   stopstage    - if greater than zero then the end of the range of stage
//                  sequence numbers to use, otherwise only the single
//                  stage specified by 'startStageNum' is used.
//   stdioflag    - non-zero for input from 'stdin', zero for input from
//                  files.
// Returns an array of 'response' structures, or null if an error occurred
// (in which case a message will be sent to 'stderr' and an exit status
// code will be set that may fetched via the "getexitcode()" function).
struct response * DLLEXT evresp2(char *stalst,char *chalst,char *netlst,
             char *locidlst,char *datestr,char *unitsconvstr,char *filename,
                               double *freqarr,int numfreqs,int verboseflag,
                                 int startstage,int stopstage,int stdioflag)
{
  jsize i,numresps;
  jclass runblksclass,rinfoblkclass;
  jmethodID methid,gexitcodeid;
  jfieldID stanameid,chanameid,netnameid,sitenameid,csarrayid,freqarrid;
  jobject runblksobj,rinfoblkobj,jobj;
  jstring jstalst,jchalst,jnetlst,jlocidlst,jdatestr,junitsconvstr,
          jfilename;
  jdoubleArray jfreqarrobj;
  jarray jrinfoblkarrobj;
  struct response *respptr,*resptop=NULL,*respbot=NULL;

         //check parameters:
  if(freqarr == NULL || numfreqs <= 0)
  {      //parameter error; set error code and message
    seterr(1,"Error in 'evresp' parameters");
    return NULL;
  }
         //start JVM (if not already started):
  if(!isJVMrunning())
  {      //JVM not running
              //start Java Virtual Machine (use alternate classpath
              // variable and send SEEDRESP env var to JVM):
    if((g_jenv=startJVM(JEVRESP_CLSPATH_STR,JEVRESP_SEEDRESP_STR)) == NULL)
    {    //error starting JVM; set error code and message
      seterr(2,"Error starting Java Virtual Machine");
      return NULL;
    }
  }
  else   //JVM already running
    g_jenv = getJNIEnv();    //get pointer to Java environment
         //see if 'Object' class can be found
  if((*g_jenv)->FindClass(g_jenv,"Ljava/lang/Object;") == NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to find any Java classes");
    return NULL;
  }
         //find 'String' and 'double[]' Java classes:
  if((g_stringclass=(*g_jenv)->FindClass(g_jenv,"Ljava/lang/String;")) ==
       NULL || (g_doublearrclass=(*g_jenv)->FindClass(g_jenv,"[D")) == NULL)
  {      //class(es) not found; set error code and message
    seterr(3,"Unable to locate 'String' and/or 'double[]' class");
    return NULL;
  }
         //find 'Object[]' Java class:
  if((g_objectarrclass=(*g_jenv)->FindClass(
                                     g_jenv,OBJECTARR_CLASS_NAME)) == NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to locate Java class \"" OBJECTARR_CLASS_NAME "\"");
    return NULL;
  }
         //find 'ComplexBlk' Java class:
  if((g_complexblkclass=(*g_jenv)->FindClass(
                                     g_jenv,COMPLEXBLK_CLASS_NAME)) == NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to locate Java class \"" COMPLEXBLK_CLASS_NAME "\"");
    return NULL;
  }
         //find 'RunBlks' Java class:
  if((runblksclass=(*g_jenv)->FindClass(g_jenv,RUNBLKS_CLASS_NAME)) == NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to locate Java class \"" RUNBLKS_CLASS_NAME "\"");
    return NULL;
  }
         //find 'RespInfoBlk' Java class:
  if((rinfoblkclass=(*g_jenv)->FindClass(g_jenv,RINFOBLK_CLASS_NAME)) ==
                                                                       NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to locate Java class \"" RINFOBLK_CLASS_NAME "\"");
    return NULL;
  }
         //find fields in 'RespInfoBlk' Java class:
  if((stanameid=(*g_jenv)->GetFieldID(g_jenv,rinfoblkclass,"stationName",
                                           "Ljava/lang/String;")) == NULL ||
        (chanameid=(*g_jenv)->GetFieldID(g_jenv,rinfoblkclass,"channelName",
                                           "Ljava/lang/String;")) == NULL ||
        (netnameid=(*g_jenv)->GetFieldID(g_jenv,rinfoblkclass,"networkName",
                                           "Ljava/lang/String;")) == NULL ||
          (sitenameid=(*g_jenv)->GetFieldID(g_jenv,rinfoblkclass,"siteName",
                                           "Ljava/lang/String;")) == NULL ||
      (csarrayid=(*g_jenv)->GetFieldID(g_jenv,rinfoblkclass,"cSpectraArray",
                             "[Lcom/isti/jevalresp/ComplexBlk;")) == NULL ||
            (freqarrid=(*g_jenv)->GetFieldID(g_jenv,rinfoblkclass,"freqArr",
                                                             "[D")) == NULL)
  {      //error finding fields; set error code and message
    seterr(3,"Unable to locate fields in Java class \""
                                                  RINFOBLK_CLASS_NAME "\"");
    return NULL;
  }
         //find fields in 'ComplexBlk' Java class:
  if((g_cblkrealid=(*g_jenv)->GetFieldID(g_jenv,g_complexblkclass,
                                                     "real","D")) == NULL ||
               (g_cblkimagid=(*g_jenv)->GetFieldID(g_jenv,g_complexblkclass,
                                                       "imag","D")) == NULL)
  {      //error finding fields; set error code and message
    seterr(3,"Unable to locate fields in Java class \""
                                                COMPLEXBLK_CLASS_NAME "\"");
    return NULL;
  }
         //create Java variables that need to be allocated:
  jstalst = (*g_jenv)->NewStringUTF(g_jenv,stalst);
  jchalst = (*g_jenv)->NewStringUTF(g_jenv,chalst);
  jnetlst = (*g_jenv)->NewStringUTF(g_jenv,netlst);
  jlocidlst = (*g_jenv)->NewStringUTF(g_jenv,locidlst);
  jdatestr = (*g_jenv)->NewStringUTF(g_jenv,datestr);
  junitsconvstr = (*g_jenv)->NewStringUTF(g_jenv,unitsconvstr);
  jfilename = (*g_jenv)->NewStringUTF(g_jenv,filename);
         //create Java version of frequency array:
  if((jfreqarrobj=createdoublearrobj(freqarr,numfreqs)) == NULL)
  {      //error creating freq array; set error code and message
    seterr(4,"Unable to create frequency array (out of memory)");
    return NULL;
  }
         //find constructor for 'RunBlks' class (no parameters):
  if((methid=(*g_jenv)->GetMethodID(g_jenv,runblksclass,"<init>","()V")) ==
                                                                       NULL)
  {      //constructor not found; set error code and message
    seterr(3,"Unable to locate constructor for Java class \""
                                                   RUNBLKS_CLASS_NAME "\"");
    return NULL;
  }
         //construct instance of class:
  if((runblksobj=(*g_jenv)->NewObject(g_jenv,runblksclass,methid)) == NULL)
  {      //error instantiating; set error code and message
    seterr(5,"Unable to construct instance of Java class \""
                                                   RUNBLKS_CLASS_NAME "\"");
    return NULL;
  }
         //find 'RunBlks' instance method for finding responses:
  if((methid=(*g_jenv)->GetMethodID(g_jenv,runblksclass,JEVRESP_METHOD_NAME,
                   "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
                    "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
     "Ljava/lang/String;[DZIIZ)[Lcom/isti/jevalresp/RespInfoBlk;")) == NULL)
  {      //method not found; set error code and message
    seterr(3,"Unable to locate method \"" JEVRESP_METHOD_NAME
                             "\" in Java class \"" RUNBLKS_CLASS_NAME "\"");
    return NULL;
  }
         //find instance method for fetching exit status code value:
  if((gexitcodeid=(*g_jenv)->GetMethodID(g_jenv,runblksclass,
                                        GETCODE_METHOD_NAME,"()I")) == NULL)
  {      //method not found; set error code and message
    seterr(3,"Unable to locate method \"" GETCODE_METHOD_NAME
                             "\" in Java class \"" RUNBLKS_CLASS_NAME "\"");
    return NULL;
  }
         //call method to find, process and return responses:
  jobj = (*g_jenv)->CallObjectMethod(g_jenv,runblksobj,methid,
         jstalst,jchalst,jnetlst,jlocidlst,jdatestr,junitsconvstr,jfilename,
                jfreqarrobj,(jboolean)((verboseflag!=0)?JNI_TRUE:JNI_FALSE),
                                           (jint)startstage,(jint)stopstage,
                             (jboolean)((stdioflag!=0)?JNI_TRUE:JNI_FALSE));
         //check if exception thrown:
  if((*g_jenv)->ExceptionOccurred(g_jenv) != NULL)
  {      //exception was thrown
    (*g_jenv)->ExceptionDescribe(g_jenv);        //send out description
    (*g_jenv)->ExceptionClear(g_jenv);           //clear exception
    g_exitcode = 9;                              //set exit status code
    return NULL;
  }
  if(jobj == NULL)
  {      //method returned null
         //fetch and save exit status value from Java 'RunBlks' object:
    g_exitcode = (*g_jenv)->CallIntMethod(g_jenv,runblksobj,gexitcodeid);
    return NULL;
  }
         //check if returned object is an array of Objects, set ptr to
         // array, and get & check number of response elements in array:
  if(!((*g_jenv)->IsInstanceOf(g_jenv,jobj,g_objectarrclass)) ||
                                        (numresps=(*g_jenv)->GetArrayLength(
                               g_jenv,(jrinfoblkarrobj=(jarray)jobj))) <= 0)
  {      //no elements; set error code and message
    seterr(6,"No response elements in returned array");
    return NULL;
  }
         //convert array of Java 'RespInfoBlk' objects
         // to array of 'response' structures:
  for(i=0; i<numresps; ++i)
  {      //for each 'RespInfoBlk' array element; get element
    if((rinfoblkobj=(*g_jenv)->GetObjectArrayElement(
                                       g_jenv,jrinfoblkarrobj,i)) == NULL ||
               !((*g_jenv)->IsInstanceOf(g_jenv,rinfoblkobj,rinfoblkclass)))
    {      //object not 'RespInfoBlk'; set error code and message
      seterr(7,"Unable to fetch 'RespInfoBlk' array element");
      return NULL;
    }
         //create 'response' object:
    if((respptr=(struct response *)malloc(sizeof(struct response))) == NULL)
    {      //error creating response object; set error code and message
      seterr(4,"Unable to create response object (out of memory)");
      return NULL;
    }
         //fetch and copy various fields from Java 'RespInfoBlk' object
         // to new 'response' structure:
    if(!fetchstringfield(respptr->station,STALEN,rinfoblkobj,stanameid) ||
         !fetchstringfield(respptr->channel,CHALEN,rinfoblkobj,chanameid) ||
         !fetchstringfield(respptr->network,NETLEN,rinfoblkobj,netnameid) ||
         !fetchstringfield(respptr->station,STALEN,rinfoblkobj,stanameid) ||
        !fetchstringfield(respptr->locid,LOCIDLEN,rinfoblkobj,sitenameid) ||
      (respptr->rvec=fetchcomplexarrfield(rinfoblkobj,csarrayid)) == NULL ||
                                        (respptr->freqs=fetchdoublearrfield(
                         rinfoblkobj,freqarrid,&(respptr->nfreqs))) == NULL)
    {
      seterr(8,
              "Error fetching field contents of Java 'RespInfoBlk' object");
      return NULL;
    }
    respptr->next = NULL;         //setup as last item on linked list
         //add response structure item to linked list:
    if(resptop == NULL)           //if no top of list then
      resptop = respptr;          //make this item the first one
    else if(respbot != NULL)      //if bottom pointer OK then
      respbot->next = respptr;    //attach this item to bottom
    respbot = respptr;            //make this item new bottom of list
  }
  return resptop;  //return pointer to linked list of response structures
}


// Version of 'evresp()' with 'listinterp...' parameters (not currently
// implemented).
struct response *evresp_itp(char *stalst, char *chalst, char *net_code,
                            char *locidlst, char *date_time, char *units,
                            char *file, double *freqs, int nfreqs,
                            char *rtype, char *verbose, int start_stage,
                            int stop_stage, int stdio_flag,
                            int listinterp_out_flag, int listinterp_in_flag,
                            double listinterp_tension)
{
  return evresp(stalst,chalst,net_code,locidlst,date_time,units,file,
                    freqs,nfreqs,rtype,verbose,start_stage,stop_stage,
                    stdio_flag);
}


// Writes the output for the given linked list of 'response' structures.
// Error and warning messages are sent to 'stderr', and an exit status
// code value may be fetched via the "getexitcode()" function (non-zero
// indicating an error status).  This function has the same input and
// output as the original 'evalresp' "print_resp()" function and may be
// used in its place.
// The outputted response is either in the form of a complex spectra
// (freq, real_resp, imag_resp) to the file SPECTRA.NETID.STANAME.CHANAME
// (if rtype = "cs") or in the form of seperate amplitude and phase files
// (if rtype = "ap") with names like AMP.NETID.STANAME.CHANAME and
// PHASE.NETID.STANAME.CHANAME.  If the 'stdio_flag' is set to 1, then
// the response information will be output to stdout, prefixed by a header
// that includes the NETID, STANAME, and CHANAME, as well as whether the
// response given is in amplitude/phase or complex response (real/imaginary)
// values.  If either case, the output to stdout will be in the form of
// three columns of real numbers, in the former case they will be
// freq/amp/phase tuples, in the latter case freq/real/imaginary tuples.
//   freqarr     - parameter not used; included to maintain compatibility
//                 with orignal 'evalresp' code
//   numfreqs    - parameter not used; included to maintain compatibility
//                 with orignal 'evalresp' code
//   resplist    - the linked list of response structures to be outputted.
//   resptypestr - a string indicating the type of response data to be
//                 generated ("ap" or "cs").
//   stdioflag   - non-zero for output to 'stdout', zero for output to
//                 files.
void DLLEXT print_resp(double *freqarr,int numfreqs,struct response *resplist,
                                            char *resptypestr,int stdioflag)
{
  printresp2(resplist,resptypestr,stdioflag);
}


// Version of 'print_resp()' with improved set of parameters.
// Writes the output for the given linked list of 'response' structures.
// Error and warning messages are sent to 'stderr', and an exit status
// code value may be fetched via the "getexitcode()" function (non-zero
// indicating an error status).  This function has the same input and
// output as the original 'evalresp' "print_resp()" function and may be
// used in its place.
// The outputted response is either in the form of a complex spectra
// (freq, real_resp, imag_resp) to the file SPECTRA.NETID.STANAME.CHANAME
// (if rtype = "cs") or in the form of seperate amplitude and phase files
// (if rtype = "ap") with names like AMP.NETID.STANAME.CHANAME and
// PHASE.NETID.STANAME.CHANAME.  If the 'stdio_flag' is set to 1, then
// the response information will be output to stdout, prefixed by a header
// that includes the NETID, STANAME, and CHANAME, as well as whether the
// response given is in amplitude/phase or complex response (real/imaginary)
// values.  If either case, the output to stdout will be in the form of
// three columns of real numbers, in the former case they will be
// freq/amp/phase tuples, in the latter case freq/real/imaginary tuples.
//   resplist    - the linked list of response structures to be outputted.
//   resptypestr - a string indicating the type of response data to be
//                 generated ("ap" or "cs").
//   stdioflag   - non-zero for output to 'stdout', zero for output to
//                 files.
void DLLEXT printresp2(struct response *resplist,char *resptypestr,
                                                              int stdioflag)
{
  jsize i,numresps;
  struct response *respptr;
  jclass runblksclass,rinfoblkclass;
  jobject runblksobj,rinfoblkobj;
  jmethodID methid,rinfoinitid,gexitcodeid;
  jarray jrinfoblkarrobj,jcpxblkarrobj;
  jstring jrtypestr,jstastr,jchastr,jnetstr,jsitestr;
  jdoubleArray jfreqarrobj;

         //check parameters:
  if(resplist == NULL)
  {      //parameter error; set error code and message
    seterr(1,"Empty list of responses send to 'print_resp'");
    return;
  }
         //count number of responses in list:
  numresps = (jsize)0;
  respptr = resplist;
  do
  {      //for each response in list
    ++numresps;         //increment count
    if(respptr->rvec == NULL || respptr->freqs == NULL)
    {    //null pointers; set error code and message
      seterr(1,"Null array pointer in response");
      return;
    }
  }
  while((respptr=respptr->next) != NULL);
         //start JVM (if not already started):
  if(!isJVMrunning())
  {      //JVM not running
              //start Java Virtual Machine (use alternate classpath
              // variable and send SEEDRESP env var to JVM):
    if((g_jenv=startJVM(JEVRESP_CLSPATH_STR,JEVRESP_SEEDRESP_STR)) == NULL)
    {    //error starting JVM; set error code and message
      seterr(2,"Error starting Java Virtual Machine");
      return;
    }
  }
  else   //JVM already running
    g_jenv = getJNIEnv();    //get pointer to Java environment
         //find 'ComplexBlk' Java class:
  if((g_complexblkclass=(*g_jenv)->FindClass(
                                     g_jenv,COMPLEXBLK_CLASS_NAME)) == NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to locate Java class \"" COMPLEXBLK_CLASS_NAME "\"");
    return;
  }
         //find 'RunBlks' Java class:
  if((runblksclass=(*g_jenv)->FindClass(g_jenv,RUNBLKS_CLASS_NAME)) == NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to locate Java class \"" RUNBLKS_CLASS_NAME "\"");
    return;
  }
         //find 'RespInfoBlk' Java class:
  if((rinfoblkclass=(*g_jenv)->FindClass(g_jenv,RINFOBLK_CLASS_NAME)) ==
                                                                       NULL)
  {      //class not found; set error code and message
    seterr(3,"Unable to locate Java class \"" RINFOBLK_CLASS_NAME "\"");
    return;
  }
         //find constructor for 'RespInfoBlk' class:
  if((rinfoinitid=(*g_jenv)->GetMethodID(g_jenv,rinfoblkclass,"<init>",
                   "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;"
         "Ljava/lang/String;[Lcom/isti/jevalresp/ComplexBlk;[D)V")) == NULL)
  {      //constructor not found; set error code and message
    seterr(3,"Unable to locate constructor for Java class \""
                                                  RINFOBLK_CLASS_NAME "\"");
    return;
  }
         //find constructor for 'Complex' class (two double parameters):
  if((g_cblkinitid=(*g_jenv)->GetMethodID(g_jenv,g_complexblkclass,
                                                 "<init>","(DD)V")) == NULL)
  {      //constructor not found; set error code and message
    seterr(3,"Unable to locate constructor for Java class \""
                                                COMPLEXBLK_CLASS_NAME "\"");
    return;
  }
         //create Java string version of 'resptypestr':
  jrtypestr = (*g_jenv)->NewStringUTF(g_jenv,resptypestr);
         //create Java array of 'RespInfoBlk' objects:
  if((jrinfoblkarrobj=(*g_jenv)->NewObjectArray(g_jenv,numresps,
                                               rinfoblkclass,NULL)) == NULL)
  {      //error creating; set error code and message
    seterr(4,"Unable to create array of Java class \""
                                                  RINFOBLK_CLASS_NAME "\"");
    return;
  }
         //copy data from 'resplist' to Java 'jrinfoblkarrobj':
  respptr = resplist;
  for(i=(jsize)0; i<numresps; ++i)
  {      //for each response in list
              //create Java variables that need to be allocated:
    if((jstastr=(*g_jenv)->NewStringUTF(g_jenv,respptr->station)) == NULL ||
       (jchastr=(*g_jenv)->NewStringUTF(g_jenv,respptr->channel)) == NULL ||
       (jnetstr=(*g_jenv)->NewStringUTF(g_jenv,respptr->network)) == NULL ||
        (jsitestr=(*g_jenv)->NewStringUTF(g_jenv,respptr->locid)) == NULL ||
                                         (jcpxblkarrobj=createcomplexarrobj(
                                  respptr->rvec,respptr->nfreqs)) == NULL ||
         (jfreqarrobj=createdoublearrobj(respptr->freqs,respptr->nfreqs)) ==
                                                                       NULL)
    {      //error creating Java variables; set error code and message
      seterr(4,"Unable to create Java variables (out of memory)");
      return;
    }
              //construct instance of Java 'RespInfoBlk' class
              // filled in with values from 'response' item:
    if((rinfoblkobj=(*g_jenv)->NewObject(g_jenv,rinfoblkclass,rinfoinitid,
       jstastr,jchastr,jnetstr,jsitestr,jcpxblkarrobj,jfreqarrobj)) == NULL)
    {      //error instantiating; set error code and message
      seterr(5,"Unable to construct instance of Java class \""
                                                    RINFOBLK_CLASS_NAME "\"");
      return;
    }
              //enter Java 'RespInfoBlk' object into array:
    (*g_jenv)->SetObjectArrayElement(g_jenv,jrinfoblkarrobj,i,rinfoblkobj);
    respptr = respptr->next;      //move to next item in list
  }
         //find constructor for 'RunBlks' class (no parameters):
  if((methid=(*g_jenv)->GetMethodID(g_jenv,runblksclass,"<init>","()V")) ==
                                                                       NULL)
  {      //constructor not found; set error code and message
    seterr(3,"Unable to locate constructor for Java class \""
                                                   RUNBLKS_CLASS_NAME "\"");
    return;
  }
         //construct instance of Java 'RunBlks' class:
  if((runblksobj=(*g_jenv)->NewObject(g_jenv,runblksclass,methid)) == NULL)
  {      //error instantiating; set error code and message
    seterr(5,"Unable to construct instance of Java class \""
                                                   RUNBLKS_CLASS_NAME "\"");
    return;
  }
         //find instance method for writing responses:
  if((methid=(*g_jenv)->GetMethodID(g_jenv,runblksclass,WRITE_METHOD_NAME,
        "([Lcom/isti/jevalresp/RespInfoBlk;Ljava/lang/String;Z)Z")) == NULL)
  {      //method not found; set error code and message
    seterr(3,"Unable to locate method \"" WRITE_METHOD_NAME
                             "\" in Java class \"" RUNBLKS_CLASS_NAME "\"");
    return;
  }
         //find instance method for fetching exit status code value:
  if((gexitcodeid=(*g_jenv)->GetMethodID(g_jenv,runblksclass,
                                        GETCODE_METHOD_NAME,"()I")) == NULL)
  {      //method not found; set error code and message
    seterr(3,"Unable to locate method \"" GETCODE_METHOD_NAME
                             "\" in Java class \"" RUNBLKS_CLASS_NAME "\"");
    return;
  }
         //call method to output responses:
  (*g_jenv)->CallObjectMethod(g_jenv,runblksobj,methid,jrinfoblkarrobj,
                   jrtypestr,(jboolean)((stdioflag!=0)?JNI_TRUE:JNI_FALSE));
         //check if exception thrown:
  if((*g_jenv)->ExceptionOccurred(g_jenv) != NULL)
  {      //exception was thrown
    (*g_jenv)->ExceptionDescribe(g_jenv);        //send out description
    (*g_jenv)->ExceptionClear(g_jenv);           //clear exception
    g_exitcode = 9;                              //set exit status code
  }
         //fetch and save exit status value from Java 'RunBlks' object:
  g_exitcode = (*g_jenv)->CallIntMethod(g_jenv,runblksobj,gexitcodeid);
}


// Version of 'print_resp()' with 'listinterp...' parameters (not currently
// implemented).
void print_resp_itp(double *freqs, int nfreqs, struct response *first,
                char *rtype, int stdio_flag, int listinterp_out_flag,
                double listinterp_tension)
{
  print_resp(freqs,nfreqs,first,rtype,stdio_flag);
}


// Fetches and copies the contents of a string field to the given buffer.
// The global Java class variable 'g_stringclass' must be setup before
// this function is used.
//   destbuff - destination buffer.
//   destlen - the maximum length of the destination buffer.
//   jsrcobj - Java object to fetch field from.
//   fieldid - ID of string field to be fetched.
// Returns 1 if successful, 0 if error.
int fetchstringfield(char *destbuff,int destlen,jobject jsrcobj,
                                                           jfieldID fieldid)
{
  const char *sptr;
  jobject jobj;

       //get string field from object:
  if((jobj=(*g_jenv)->GetObjectField(g_jenv,jsrcobj,fieldid)) == NULL ||
                    !((*g_jenv)->IsInstanceOf(g_jenv,jobj,g_stringclass)) ||
     (sptr=(*g_jenv)->GetStringUTFChars(g_jenv,(jstring)jobj,NULL)) == NULL)
  {    //error fetching field object
    return 0;           //return error value
  }
  strncpy(destbuff,sptr,destlen);      //copy over contents
                   //free memory allocated by 'GetStringUTFChars()':
  (*g_jenv)->ReleaseStringUTFChars(g_jenv,(jstring)jobj,sptr);
  return 1;             //return success value
}


// Returns the contents of a 'double[]' field.  The global Java class
// variable 'g_doublearrclass' must be setup before this function is used.
//   jsrcobj - Java object to fetch field from.
//   fieldid - ID of 'double[]' field to be fetched.
//   pcount  - if not NULL then filled in with the number of values in the
//             returned array.
// Returns an allocated array of 'double' values, or NULL if an error
// occurred.
double *fetchdoublearrfield(jobject jsrcobj,jfieldID fieldid,int *pcount)
{
  jsize i,jdarrlen;
  jdouble *jdbuff;
  jobject jobj;
  double *retarr;

       //get double[] field from object:
  if((jobj=(*g_jenv)->GetObjectField(g_jenv,jsrcobj,fieldid)) == NULL ||
                 !((*g_jenv)->IsInstanceOf(g_jenv,jobj,g_doublearrclass)) ||
                                  (jdbuff=(*g_jenv)->GetDoubleArrayElements(
                                 g_jenv,(jdoubleArray)jobj,NULL)) == NULL ||
                                        (jdarrlen=(*g_jenv)->GetArrayLength(
                                  g_jenv,(jdoubleArray)jobj)) <= (jsize)0 ||
                 (retarr=(double *)malloc(jdarrlen*sizeof(double))) == NULL)
  {      //error fetching field object or allocating array
    return NULL;             //return error indicator
  }
         //copy each value from 'jdouble' to 'double' array:
  for(i=(jsize)0; i<jdarrlen; ++i)
    retarr[i] = (double)(jdbuff[i]);
         //free 'GetDoubleArrayElements()' array (don't update Java copy):
  (*g_jenv)->ReleaseDoubleArrayElements(g_jenv,(jdoubleArray)jobj,jdbuff,
                                                                 JNI_ABORT);
  if(pcount != NULL)                   //if not NULL then
    *pcount = (int)jdarrlen;           //enter length of array
  return retarr;             //return array of doubles
}


// Returns the contents of a Java 'ComplexBlk[]' field.  The global Java
// class variables 'g_complexblkclass' and 'g_objectarrclass', and the
// global Java field ID variables 'g_cblkrealid' and 'g_cblkimagid'
// must be setup before this function is used.
//   jsrcobj - Java object to fetch field from.
//   fieldid - ID of 'ComplexBlk[]' field to be fetched.
// Returns an allocated array of 'complex' structures, or NULL if an error
// occurred.
struct complex *fetchcomplexarrfield(jobject jsrcobj,jfieldID fieldid)
{
  jsize i,jcarrlen;
  jarray jcarr;
  jobject jobj;
  struct complex *retarr;

       //get ComplexBlk[] field from object:
  if((jobj=(*g_jenv)->GetObjectField(g_jenv,jsrcobj,fieldid)) == NULL ||
                 !((*g_jenv)->IsInstanceOf(g_jenv,jobj,g_objectarrclass)) ||
                                        (jcarrlen=(*g_jenv)->GetArrayLength(
                                        g_jenv,(jarray)jobj)) <= (jsize)0 ||
                                           (retarr=(struct complex *)malloc(
                                  jcarrlen*sizeof(struct complex))) == NULL)
  {      //error fetching field object or allocating array
    return NULL;             //return error indicator
  }
  jcarr = (jarray)jobj;      //setup ptr to array of Java ComplexBlk objects
         //copy values from 'jarray' to 'complex' array:
  for(i=(jsize)0; i<jcarrlen; ++i)
  {      //for each element in Java 'ComplexBlk' array
    if((jobj=(*g_jenv)->GetObjectArrayElement(g_jenv,jcarr,i)) == NULL ||
                  !((*g_jenv)->IsInstanceOf(g_jenv,jobj,g_complexblkclass)))

    {      //fetch element did not return 'ComplexBlk' object
      seterr(8,"Unable to fetch 'ComplexBlk' array element");
      return NULL;
    }
    retarr[i].real =         //copy 'real' field value
              (double)((*g_jenv)->GetDoubleField(g_jenv,jobj,g_cblkrealid));
    retarr[i].imag =         //copy 'imag' field value
              (double)((*g_jenv)->GetDoubleField(g_jenv,jobj,g_cblkimagid));
  }
  return retarr;             //return array of doubles
}


// Creates a Java array object of 'jdoubles' and fills it with the
// contents of the given 'double' array.
//   dblarr   - array of 'double' values.
//   numelems - size of array of 'double' values.
// Returns a new Java array object of 'jdoubles', or NULL if error.
jdoubleArray createdoublearrobj(double *dblarr,int numelems)
{
  jsize i;
  jdoubleArray retarrobj;
  jdouble *jdbuff;
         //allocate array of 'jdouble' and create Java 'jdoubleArray' obj:
  if((jdbuff=(jdouble *)malloc(numelems*sizeof(jdouble))) == NULL ||
      (retarrobj=(*g_jenv)->NewDoubleArray(g_jenv,(jsize)numelems)) == NULL)
  {
    return NULL;
  }
         //copy 'dblarr[]' values into 'jdbuff' array of 'jdouble':
  for(i=(jsize)0; i<(jsize)numelems; ++i)
    jdbuff[i] = (jdouble)(dblarr[i]);
         //load buffer of values into Java 'jarrobj':
  (*g_jenv)->SetDoubleArrayRegion(g_jenv,retarrobj,(jsize)0,(jsize)numelems,
                                                                    jdbuff);
  return retarrobj;
}


// Creates a Java array object of 'ComplexBlk' and fills it with the
// contents of the given 'complex' array.  The global Java class variable
// 'g_complexblkclass' and the global Java method ID variable 'g_cblkinitid'
// must be setup before this function is used.
//   cpxarr   - array of 'complex' structures.
//   numelems - size of array of 'complex' structures.
// Returns 1 if successful, 0 if error.
jarray createcomplexarrobj(struct complex *cpxarr,int numelems)
{
  jsize i;
  jarray retarrobj;
  jobject jobj;
                                  //create Java array of objects:
  if((retarrobj=(*g_jenv)->NewObjectArray(g_jenv,(jsize)numelems,
                                           g_complexblkclass,NULL)) == NULL)
  {
    return NULL;
  }
         //copy 'cpxarr[]' vals into 'retarrobj' array of 'ComplexBlk' objs:
  for(i=(jsize)0; i<(jsize)numelems; ++i)
  {      //for each element in 'cpxarr[]'
              //construct Java 'ComplexBlk' object with 'cpxarr[]' values:
    if((jobj=(*g_jenv)->NewObject(g_jenv,g_complexblkclass,g_cblkinitid,
              (jdouble)(cpxarr[i].real),(jdouble)(cpxarr[i].imag))) == NULL)
    {
      return NULL;
    }
              //enter Java 'ComplexBlk' object into array:
    (*g_jenv)->SetObjectArrayElement(g_jenv,retarrobj,i,jobj);
  }
  return retarrobj;
}


// Outputs the given error message to 'stderr' and sets up the given
// exit status code for the program.
void seterr(int code,char *errmsg)
{
  fprintf(stderr,"%s\n",errmsg);
  g_exitcode = code;
}


// Returns the exit status code for the program (non-zero for error exit).
int getexitcode()
{
  return g_exitcode;
}

