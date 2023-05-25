#!/bin/sh

LD_LIBRARY_PATH=${JAVA_HOME}/jre/lib/i386/:${JAVA_HOME}/jre/lib/i386/client:${JAVA_HOME}/jre/lib/:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH
JEVRESP_CLASSPATH=../JEvalResp.jar
export JEVRESP_CLASSPATH
./jevtest
