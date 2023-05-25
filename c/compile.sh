#!/bin/sh

LD_LIBRARY_PATH=${JAVA_HOME}/jre/lib/i386/:${JAVA_HOME}/jre/lib/i386/client:${JAVA_HOME}/jre/lib/:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH
gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -L${JAVA_HOME}/jre/lib/i386 -ljava -o evalrespj -DEVALRESP_COMP evalresp/evalresp.c evalresp/alloc_fctns.c evalresp/string_fctns.c evalresp/error_fctns.c evalresp/parse_fctns.c evalresp/regexp.c evalresp/regsub.c evalresp/regerror.c evalresp/use_delay.c jevresp.c startJVM.c
