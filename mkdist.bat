xcopy /y JEvalResp.exe dist\
xcopy /y JEvalResp dist\
xcopy /y JEvalResp0 dist\
xcopy /y testnetws.bat dist\
xcopy /y testnetws dist\
xcopy /y jc.bat dist\
xcopy /y jc dist\
xcopy /y irisws.prop dist\
xcopy /y ncedcws.prop dist\
xcopy /y irisncedcws.prop dist\
xcopy /y doc\JEvalResp.html dist\
xcopy /y JEvalResp.jar dist\
xcopy /y JEvalRespClasses.jar dist\
xcopy /y JEvalRespf.jar dist\
mkdir dist\srcJarTemp
cd dist\srcJarTemp
xcopy /y /s ..\..\src src\
xcopy /y /s ..\..\filesrc filesrc\
xcopy /y /s ..\..\c c\
xcopy /y /s ..\..\Launcher Launcher\
xcopy /y ..\..\JEvalResp.jpx
xcopy /y ..\..\mkjdoc.bat
xcopy /y ..\..\ID.txt
xcopy /y ..\..\jdparams.txt
xcopy /y ..\..\*.library
xcopy /y /s ..\..\..\FissuresLib\fissuresIDL FissuresLib\fissuresIDL\
xcopy /y /s ..\..\..\FissuresLib\fissuresImpl FissuresLib\fissuresImpl\
xcopy /y /s ..\..\..\FissuresLib\idl FissuresLib\idl\
xcopy /y /s ..\..\..\FissuresLib\junit3.4 FissuresLib\junit3.4\
xcopy /y /s ..\..\..\FissuresLib\orbacus FissuresLib\orbacus\
xcopy /y ..\..\..\FissuresLib\jars\FissuresIDL.jar FissuresLib\jars\
xcopy /y ..\..\..\FissuresLib\jars\FissuresImpl.jar FissuresLib\jars\
xcopy /y /s ..\..\..\FissuresLib\ID.txt FissuresLib\
jar -cf ../src.jar src filesrc c Launcher JEvalResp.jpx mkjdoc.bat ID.txt jdparams.txt *.library FissuresLib
cd ..\..
rmdir /s /q dist\srcJarTemp
rmdir /s /q dist\javadocs
xcopy /y res\isti_long.gif dist\javadocs\
xcopy /y res\small_isti_logo.gif dist\javadocs\
javadoc -sourcepath src;..\isti.util\src -classpath JEvalResp.jar -d dist/javadocs -windowtitle "JEvalResp Source Code Documentation" -doctitle "JEvalResp Source Code Documentation" -overview doc/overview_desc.html -package com.isti.jevalresp com.isti.jevalresp.tests com.isti.util
xcopy /y jars\RespCORBAStub.jar dist\jars\
xcopy /y ..\isti.util\jars\isti.util.jar dist\jars\
xcopy /y ..\gnu.regexp\lib\gnu-regexp-1.1.3.jar dist\jars\