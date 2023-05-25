xcopy JEvalResp.exe JEvalResp_prerel\
xcopy JEvalResp JEvalResp_prerel\
xcopy JEvalResp0 JEvalResp_prerel\
xcopy testnet.bat JEvalResp_prerel\
xcopy testnet JEvalResp_prerel\
xcopy testnetws.bat JEvalResp_prerel\
xcopy testnetws JEvalResp_prerel\
xcopy jc.bat JEvalResp_prerel\
xcopy jc JEvalResp_prerel\
xcopy dmc.prop JEvalResp_prerel\
xcopy irisws.prop JEvalResp_prerel\
xcopy doc\JEvalResp.html JEvalResp_prerel\
xcopy JEvalResp.jar JEvalResp_prerel\
jar -cf JEvalResp_prerel\src.jar src filesrc c Launcher JEvalResp.jpx mkjdoc.bat jdparams.txt