/* evresp_.c:  Code for FORTRAN callable interface to 'evalresp' and
 *             'JEvalResp'.
 *
 *  8/23/2006 -- [ET]  Extracted contents from first part of 'evresp.c'
 *                     and added 'EVRESP__GLOBALS' flag; parameter list
 *                     modified to be all pointers.
 *
 */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

/*===================================================================
Name:      evresp_ Version 3.0
Purpose:
        FORTRAN callable interface to the evresp routine (below)
Reference:
        SEED. Standard for the Exchange of Earthquake Data
        Reference Manual
        SEED Format Version 2.3 or later
        ??? 1995
Author:    Thomas J. McSweeney

Usage (from FORTRAN):

        nmatch = evresp(sta,cha,net,datime,units,file,freq,nfreqs,resp,rtype,
     1                  verbose, start_stage, stop_stage)

Notes:
        C users should call 'evresp' directly, rather than using this interface.
        This interface includes extra arguments that are required by the FORTRAN
        compiler (the length of each string in the argument list, in the order
        that they appear in the argument list), which C programmers will probably
        not want to include in their call)

        whereas the C function returns a linked list of responses (one for each
        response that matched the user's request), this routine returns the
        response for one (1) station-channel-network for one (1) effective time.
        If more than one match is found for a given station-channel-network-time,
        an error condition is raised (and a value of -1 is returned to the calling
        routine to indicate failure).  Likewise, a value of 1 is returned if no
        match is found for the given station-channel-network-time.  If a unique
        match is found, a value of 0 is returned to the calling routine

 *=================================================================*/
/*
    8/28/2001 -- [ET]  Moved several variable definitions from 'evresp.h'
                       into this file.
 */

#include "./evresp.h"
#include <stdlib.h>
#include <string.h>

#ifdef EVRESP__GLOBALS       //enable if global variables below are needed
                             // and are not defined in 'jevresp.c'

/* define a global flag to use if using "default" units */
int def_units_flag;

/* define a pointer to a channel structure to use in determining the input and
   output units if using "default" units and for use in error output*/
struct channel *GblChanPtr;
float unitScaleFact;

/* define global variables for use in printing error messages */
char *curr_file;
int curr_seq_no;

/* and set a global variable to contain the environment for the setjmp/longjmp
   combination for error handling */
jmp_buf jump_buffer;

#endif

int evresp_(char *sta, char *cha, char *net, char *locid, char *datime, 
	    char *units, char *file, float *freqs, int *nfreqs_in, float *resp,
	    char *rtype, char *verbose, int *start_stage, int *stop_stage,
	    int *stdio_flag, int *lsta, int *lcha, int *lnet, int *llocid,
	    int *ldatime, int *lunits, int *lfile, int *lrtype, int *lverbose)
{
  struct response *first = (struct response *)NULL;
  double *dfreqs;
  int i,j, nfreqs, start, stop, flag;

  /* add null characters to end of input string arguments (remove trailing
     spaces first */

  add_null(sta, (*lsta), 'a');
  add_null(cha, (*lcha), 'a');
  add_null(net, (*lnet), 'a');
  add_null(locid, (*llocid), 'a');
  add_null(datime, (*ldatime), 'a');
  add_null(units, (*lunits), 'a');
  add_null(file, (*lfile), 'a');
  add_null(rtype, (*lrtype), 'a');
  add_null(verbose, (*lverbose), 'a');

  nfreqs = *nfreqs_in;
  start = *start_stage;
  stop = *stop_stage;
  flag = *stdio_flag;

  dfreqs = alloc_double(nfreqs);
  for(i = 0; i < nfreqs; i++)
    dfreqs[i] = freqs[i];

  /* then call evresp */

  first = evresp(sta, cha, net, locid, datime, units, file, dfreqs, nfreqs,
             rtype, verbose, start, stop, flag);

  /* free up the frequency vector */

  free(dfreqs);

  /* check the output.  If no response found, return 1, else if more than one response
     found, return -1 */

  if(first == (struct response *)NULL) {
    return(1);
  }
  else if(first->next != (struct response *)NULL) {
    free_response(first);
    return(-1);
  }

  /* if only one response found, convert from complex output vector into multiplexed
     real output for FORTRAN (real1, imag1, real2, imag2, ..., realN, imagN) */

  for(i = 0, j = 0; i < nfreqs; i++) {
    resp[j++] = (float) first->rvec[i].real;
    resp[j++] = (float) first->rvec[i].imag;
  }

  /* free up dynamically allocated space */

  free_response(first);

  /* and return to FORTRAN program */

  return(0);

}
