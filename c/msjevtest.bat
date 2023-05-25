@echo off
rem Batch file for compiling 'jevtest' project with Microsoft VC compiler
echo on
cl -Ievalresp -I%JAVA_HOME%\include -I%JAVA_HOME%\include\win32 -DWIN32 jevtest.c jevresp.c startJVM.c -MT -link %JAVA_HOME%\lib\jvm.lib -out:jevtest.exe