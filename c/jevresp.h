// jevresp.h:  Header file for 'jevresp.c' -- 12/19/2001 -- [ET]

#ifndef JEVRESP_H
#define JEVRESP_H

#include <jni.h>
#include "startJVM.h"

//if 'DLLEXT' is defined (meaning 'jevresp.dll' is used) then the
// declarations for 'evresp()' and 'print_resp()' in the original
// "evresp.h" must be modified to match the declarations in this
// header file by inserting 'DLLEXT' into the declarations:
#ifdef DLLEXT
#define evresp DLLEXT evresp
#define print_resp DLLEXT print_resp
#endif
//if 'DLLEXT' is defined (meaning 'jevresp.dll' is used) then
// undo what was just done above:
#include "evalresp/evresp.h"
#ifdef DLLEXT
#undef evresp
#undef print_resp
#endif

#ifdef __cplusplus           //if C++ then
extern "C" {                 //turn off function name "mangling"
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
struct response * DLLEXT evresp (char *stalst,char *chalst,char *netlst,
             char *locidlst,char *datestr,char *unitsconvstr,char *filename,
              double *freqarr,int nfreqs,char *resptypestr,char *verbosestr,
                              int start_stage,int stop_stage,int stdioflag);


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
                                int startstage,int stopstage,int stdioflag);


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
void DLLEXT print_resp(double *freqsarr,int numfreqs,
                                                  struct response *resplist,
                                           char *resptypestr,int stdioflag);


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
                                                             int stdioflag);


// Returns the exit status code for the program (non-zero for error exit).
int DLLEXT getexitcode(void);

#ifdef __cplusplus           //if C++ then
}                            //close extern "C" bracket
#endif

#endif

