@echo off
rem Batch file for compiling 'evalrespj' project with Microsoft VC compiler
echo on
cl -Ievalresp -I%JAVA_HOME%\include -I%JAVA_HOME%\include\win32 -DWIN32 -DEVALRESP_COMP evalresp\evalresp.c evalresp\alloc_fctns.c evalresp\string_fctns.c evalresp\error_fctns.c evalresp\parse_fctns.c evalresp\regexp.c evalresp\regsub.c evalresp\regerror.c evalresp\use_delay.c jevresp.c startJVM.c -MT -link %JAVA_HOME%\lib\jvm.lib -out:evalrespj.exe