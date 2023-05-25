#!/bin/sh

LD_LIBRARY_PATH=${JAVA_HOME}/jre/lib/i386/:${JAVA_HOME}/jre/lib/i386/client:${JAVA_HOME}/jre/lib/:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH
gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -L${JAVA_HOME}/jre/lib/i386 -ljava -o jevtest jevtest.c jevresp.c startJVM.c
