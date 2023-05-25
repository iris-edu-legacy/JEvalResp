// jevtest.c:  Test program for 'C'-language native interface to the Java
//             'JEvalResp' program.
//
//             Note:  Under Windows, to have a program use 'jevresp.dll'
//                    you need to define the 'DLLVERSION' symbol.
//
//  12/19/2001 -- [ET]
//
#include <stdio.h>
#include "jevresp.h"

main()
{
  int count;
  static struct response *resplist,*respptr;
  static double freqarr[] = { 1.0, 2.0, 3.0, 4.0, 5.0,
                              6.0, 7.0, 8.0, 9.0, 10.0 };

  resplist = evresp2("*","*","*","*","","",".",freqarr,
                                sizeof(freqarr)/sizeof(freqarr[0]),1,0,0,0);
  printf("\n");
  count = 0;                 //count number of responses returned
  respptr = resplist;
  while(respptr != NULL)
  {
    printf("%3d: sta=%s, cha=%s, net=%s, loc=%s, nfreqs=%d\n",count+1,
                         respptr->station,respptr->channel,respptr->network,
                                            respptr->locid,respptr->nfreqs);
    ++count;
    respptr = respptr->next;
  }
  printf("\n%d response%s returned\n",count,((count!=1)?"s":""));

  printresp2(resplist,"ap",0);
  stopJVM();

  printf("Return code = %d\n",getexitcode());
  return getexitcode();
}

