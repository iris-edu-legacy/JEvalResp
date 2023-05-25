//CallbackProcWrite.java:  Implements a callback method that processes and
//                         writes the output of a response.
//
//  12/12/2001 -- [ET]  Initial release version.
//    5/2/2002 -- [ET]  Added ability to set 'respProcObj' after
//                      construction via 'setRespProcObj()' method; broke
//                      output generation out into 'generateOutput()'
//                      methods so subclasses can do it differently.
//   5/15/2002 -- [ET]  Added verbose-mode message to show what output
//                      files were generated.
//   5/28/2002 -- [ET]  Changed so that any error reported via the
//                      'responseInfo()' method is always then cleared.
//    6/5/2002 -- [ET]  Took out erroneous "Internal error" message; added
//                      'showInfoMessage()' method.
//   7/10/2002 -- [ET]  Changed 'apOutputFlag' to 'respTypeIndex'; changed
//                      so any given 'fileName' sent to 'responseInfo()' is
//                      only shown once (in verbose mode).
//    8/6/2002 -- [ET]  Added 'respEndDateObj' parameter to 'responseInfo()'
//                      and modified to show it on verbose output; added
//                      'logSpacingFlag' parameter to constructors and
//                      implemented passing it on to the 'RespProcessor'
//                      object.
//    3/7/2005 -- [ET]  Modified 'responseInfo()' to only display error
//                      if 'respProcObj' contains error message and to
//                      always return 'false' if error; added optional
//                      'useDelayFlag' parameter to constructors.
//    4/1/2005 -- [ET]  Added optional 'showInputFlag' parameter to
//                      constructors.
//   11/1/2005 -- [ET]  Added optional List-blockette interpolation
//                      parameters to constructors; modified to use
//                      'outGenObj.printListStageMsgs()' method.
//   5/25/2007 -- [ET]  Modified to show informational message fetched
//                      via OutputGenerator 'getInfoMessage()' method.
//   5/24/2010 -- [ET]  Added optional parameters 'unwrapPhaseFlag' and
//                      'totalSensitFlag' to constructor; modified to
//                      set 'unwrapPhaseFlag'=true when response-output
//                      type is "fap".
//   1/24/2012 -- [ET]  Modified 'responseInfo()' method to not display
//                      given filename if empty.
//  10/22/2013 -- [ET]  Added optional 'b62XValue' parameter to constructor.
//

package com.isti.jevalresp;

import java.util.Date;
import java.io.PrintStream;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Response;

/**
 * Class CallbackProcWrite implements a callback method that processes and
 * writes the output of a response.
 */
public class CallbackProcWrite implements RespCallback
{
    //variables used on each response processed:
  protected RespProcessor respProcObj = null;
  protected final int outUnitsConvIdx;
  protected final double [] freqArr;
  protected final boolean logSpacingFlag;
  protected final boolean verboseFlag;
  protected final int startStageNum;
  protected final int stopStageNum;
  protected final boolean useDelayFlag;
  protected final boolean showInputFlag;
  protected final boolean listInterpOutFlag;
  protected final boolean listInterpInFlag;
  protected final double listInterpTension;
  protected boolean unwrapPhaseFlag;
  protected final boolean totalSensitFlag;
  protected final double b62XValue;
  protected final int respTypeIndex;
  protected final boolean stdioFlag;
  protected final PrintStream outStm;
  protected String prevFileName = null;     //input filename tracker

    /**
     * Constructs a response processing callback object.
     * @param respProcObj the "RespProcessor' object being used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTension tension value for List-blockette
     * interpolation algorithm.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @param b62XValue sample value for polynomial blockette (62).
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(RespProcessor respProcObj,int outUnitsConvIdx,
               double [] freqArr,boolean logSpacingFlag,boolean verboseFlag,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag,
                       double b62XValue,int respTypeIndex,boolean stdioFlag,
                                                         PrintStream outStm)
  {
    this.respProcObj = respProcObj;
    this.outUnitsConvIdx = outUnitsConvIdx;
    this.freqArr = freqArr;
    this.logSpacingFlag = logSpacingFlag;
    this.verboseFlag = verboseFlag;
    this.startStageNum = startStageNum;
    this.stopStageNum = stopStageNum;
    this.useDelayFlag = useDelayFlag;
    this.showInputFlag = showInputFlag;
    this.listInterpOutFlag = listInterpOutFlag;
    this.listInterpInFlag = listInterpInFlag;
    this.listInterpTension = listInterpTension;
    this.unwrapPhaseFlag = unwrapPhaseFlag;
    this.totalSensitFlag = totalSensitFlag;
    this.b62XValue = b62XValue;
    this.respTypeIndex = respTypeIndex;
    this.stdioFlag = stdioFlag;
    this.outStm = outStm;
  }

    /**
     * Constructs a response processing callback object.
     * @param respProcObj the "RespProcessor' object being used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTension tension value for List-blockette
     * interpolation algorithm.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(RespProcessor respProcObj,int outUnitsConvIdx,
               double [] freqArr,boolean logSpacingFlag,boolean verboseFlag,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag,
                     int respTypeIndex,boolean stdioFlag,PrintStream outStm)
  {
    this(respProcObj,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                          unwrapPhaseFlag,totalSensitFlag,0.0,respTypeIndex,
                                                          stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.
     * @param respProcObj the "RespProcessor' object being used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTension tension value for List-blockette
     * interpolation algorithm.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(RespProcessor respProcObj,int outUnitsConvIdx,
               double [] freqArr,boolean logSpacingFlag,boolean verboseFlag,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                     int respTypeIndex,boolean stdioFlag,PrintStream outStm)
  {
    this(respProcObj,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                            false,false,0.0,respTypeIndex,stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.
     * @param respProcObj the "RespProcessor' object being used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(
            RespProcessor respProcObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                     int respTypeIndex,boolean stdioFlag,PrintStream outStm)
  {
    this(respProcObj,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                              false,false,0.0,false,false,0.0,respTypeIndex,
                                                          stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.  After using this
     * constructor, the 'setRespProcObj()' must be called to setup the
     * 'RespProcessor' object before this object is used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTension tension value for List-blockette
     * interpolation algorithm.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
               double listInterpTension,int respTypeIndex,boolean stdioFlag,
                                                         PrintStream outStm)
  {
    this(null,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                            false,false,0.0,respTypeIndex,stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.  After using this
     * constructor, the 'setRespProcObj()' must be called to setup the
     * 'RespProcessor' object before this object is used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTension tension value for List-blockette
     * interpolation algorithm.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @param b62XValue sample value for polynomial blockette (62).
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
                                   boolean totalSensitFlag,double b62XValue,
                     int respTypeIndex,boolean stdioFlag,PrintStream outStm)
  {
    this(null,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                    unwrapPhaseFlag,totalSensitFlag,b62XValue,respTypeIndex,
                                                          stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.  After using this
     * constructor, the 'setRespProcObj()' must be called to setup the
     * 'RespProcessor' object before this object is used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTension tension value for List-blockette
     * interpolation algorithm.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
                                  boolean totalSensitFlag,int respTypeIndex,
                                       boolean stdioFlag,PrintStream outStm)
  {
    this(null,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                          unwrapPhaseFlag,totalSensitFlag,0.0,respTypeIndex,
                                                          stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.  After using this
     * constructor, the 'setRespProcObj()' must be called to setup the
     * 'RespProcessor' object before this object is used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                     int respTypeIndex,boolean stdioFlag,PrintStream outStm)
  {
    this(null,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                              false,false,0.0,false,false,0.0,respTypeIndex,
                                                          stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.
     * @param respProcObj the "RespProcessor' object being used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(
            RespProcessor respProcObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                       int stopStageNum,int respTypeIndex,boolean stdioFlag,
                                                         PrintStream outStm)
  {
    this(respProcObj,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                       startStageNum,stopStageNum,false,false,respTypeIndex,
                                                          stdioFlag,outStm);
  }

    /**
     * Constructs a response processing callback object.  After using this
     * constructor, the 'setRespProcObj()' must be called to setup the
     * 'RespProcessor' object before this object is used.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @param outStm output stream for messages to be sent to (usually
     * 'System.err'), or null for no messages.
     */
  public CallbackProcWrite(
               int outUnitsConvIdx,double [] freqArr,boolean logSpacingFlag,
                     boolean verboseFlag,int startStageNum,int stopStageNum,
                     int respTypeIndex,boolean stdioFlag,PrintStream outStm)
  {
    this(null,outUnitsConvIdx,freqArr,logSpacingFlag,verboseFlag,
                       startStageNum,stopStageNum,false,false,respTypeIndex,
                                                          stdioFlag,outStm);
  }

    /**
     * Sets the 'RespProcessor' object to be used.
     * @param respProcObj the "RespProcessor' object being used.
     */
  public void setRespProcObj(RespProcessor respProcObj)
  {
    this.respProcObj = respProcObj;
  }

    /**
     * Processes and writes the output of the given response.
     * @param fileName the name of source file for the response.
     * @param channelIdObj the channel ID associated with the response, or
     * null if a channel ID was not found.
     * @param respEndDateObj end date for channel ID, or null if a channel ID
     * was not found.
     * @param channelIdFName a string version of the channel ID associated
     * with the response, or null if a channel ID was not found.
     * @param respObj the response information, or null if a channel ID
     * was not found (error message in 'errMsgStr').
     * @param errMsgStr if 'channelIdObj' or 'respObj' is null then
     * 'errMsgStr' contains an error message string; otherwise null.
     * @return true if the response was processed and outputted
     * successfully, false if an error occurred.
     */
  public boolean responseInfo(String fileName,ChannelId channelIdObj,
                                  Date respEndDateObj,String channelIdFName,
                                          Response respObj,String errMsgStr)
  {
    if(verboseFlag)
    {    //verbose mode; show file name
      if(prevFileName == null || !prevFileName.equals(fileName))
      {  //no previous file name or not same as previous; show file name
        if(fileName != null && fileName.trim().length() > 0)
        {  //given filename not empty
          outStmPrintln(RespProcessor.LINE_SEP_STR);
          outStmPrintln("  " + fileName);
        }
        prevFileName = fileName;       //save file name for next time
      }
      outStmPrintln(RespProcessor.LINE_SEP_STR);
      if(channelIdObj != null)
      {       //channel ID object is OK; show channel information
        if(showInputFlag && respObj != null)
        {     //showing RESP input and response object is OK
          System.out.println(RespUtils.getTextFormatRespStr(
                                      channelIdObj,respEndDateObj,respObj));
          outStmPrintln(RespProcessor.LINE_SEP_STR);
        }
        outStmPrintln("  " +
                RespUtils.channelIdToEvString(channelIdObj,respEndDateObj));
      }
    }    //not verbose mode
    else if(showInputFlag && respObj != null)
    {    //showing RESP input and response object is OK
      System.out.println(RespUtils.getTextFormatRespStr(
                                      channelIdObj,respEndDateObj,respObj));
    }
    if(respProcObj == null)
    {    //response processor object null (shouldn't happen); show error msg
      outStmPrintln("Internal error:  'respProcObj' null" +
                                                    " in 'responseInfo()'");
      return false;          //indicate not processed OK
    }
    if(respObj != null)
    {    //response object contains data; process response information
      if(respTypeIndex == Run.RESP_FAP_TYPEIDX)  //if out type "fap" then
        unwrapPhaseFlag = true;                  //force 'unwrap'==true
      final OutputGenerator outGenObj;
      if((outGenObj=respProcObj.processResponse(fileName,respObj,
                       freqArr,logSpacingFlag,outUnitsConvIdx,startStageNum,
                  stopStageNum,useDelayFlag,showInputFlag,listInterpOutFlag,
                                         listInterpInFlag,listInterpTension,
                        unwrapPhaseFlag,totalSensitFlag,b62XValue)) == null)
      {  //error processing response; show error message
        outStmPrintln(respProcObj.getErrorMessage());
        respProcObj.clearErrorMessage();         //clear error message
        return false;        //indicate not processed OK
      }
      if(outGenObj.getInfoFlag())
      {  //info message available; show it
        outStmPrintln(outGenObj.getInfoMessage());
        outGenObj.clearInfoMessage();            //clear message
      }
              //if amp/phase output requested then generate (and
              // possibly interpolate) amp/phase data values now:
      final boolean ampPhaOutFlag;
      if((ampPhaOutFlag=(respTypeIndex!=Run.RESP_CS_TYPEIDX)) &&
                                       outGenObj.getAmpPhaseArray() == null)
      {  //error generating amp/phase data
        outStmPrintln(outGenObj.getErrorMessage());
        return false;        //indicate not processed OK
      }
         //reponse output calculated OK
      if(verboseFlag)
      {       //verbose mode; show extra information
        outStmPrintln(outGenObj.getRespInfoString());
              //list stage information:
        outStmPrintln(outGenObj.getStagesListStr());
              //if response contains List stage then output messages:
        if(outGenObj.getListStageFlag())
          outGenObj.printListStageMsgs(outStm,ampPhaOutFlag);
        if(outGenObj.getTotalSensitFlag())
        {  //stage 0 (total) sensitivity was used; show message
          outStmPrintln("Note:  Reported (stage 0) sensitivity was used " +
                                               "to compute response (-ts)");
        }
      }
         //check if calc sensitivity too much different from stage 0:
      final double fVal;
      if(respObj.stages != null &&
                    outGenObj.getNumCalcStages() >= respObj.stages.length &&
              !RespUtils.isZero(fVal=RespUtils.getRespSensitivity(respObj)))
      {  //response OK, all stages calc'd & resp sens not zero
              //calc percentage difference in sensitivity values:
        if(Math.abs((fVal-outGenObj.getCalcSensitivity())/fVal) >= 0.05)
        {     //greater than 5% difference; show message
          outStmPrintln("WARNING:  Computed and reported " +
                          "(stage 0) sensitivities differ by more than 5%");
        }
      }
      if(!generateOutput(outGenObj,channelIdObj,respEndDateObj,
                                                            channelIdFName))
      {  //error writing output
        if(respProcObj.getErrorFlag())
        {     //response processor has error message; show it
          outStmPrintln(respProcObj.getErrorMessage());
          respProcObj.clearErrorMessage();         //clear error message
        }
        return false;        //indicate not processed OK
      }
      return true;           //indicate processed OK
    }
         //no data in response object
    if(errMsgStr != null)              //if not null then
      outStmPrintln(errMsgStr);        //show error message
    return false;            //indicate not processed OK
  }

    /**
     * Generates the output.
     * @param outGenObj the 'OutputGenerator' object to use.
     * @param channelIdObj the channel ID object to use.
     * @param respEndDateObj end date object to use.
     * @param channelIdFName the output filename to use.
     * @return true if successful, false if error.
     */
  protected boolean generateOutput(OutputGenerator outGenObj,
           ChannelId channelIdObj,Date respEndDateObj,String channelIdFName)
  {
    final boolean retFlag = respProcObj.outputData(
        outGenObj,channelIdObj,respEndDateObj,
        channelIdFName,respTypeIndex,stdioFlag);
    if(retFlag)
    {    //generation of output files successful
      if(verboseFlag)
      {  //verbose mode; show list of generated files
        final int num;
        if((num=respProcObj.getOutputFileNamesCount()) > 0)
        {     //at least one file generated; show name(s)
          outStmPrintln("  Generated output file" + ((num!=1)?"s":"") +
                               ":  " + respProcObj.getOutputFileNamesStr());
        }
        else
          outStmPrintln("  No output files generated");
      }
    }
    return retFlag;
  }

    /**
     * Shows the given informational message.
     * @param msgStr message string
     */
  public void showInfoMessage(String msgStr)
  {
    outStmPrintln(msgStr);
  }

    /**
     * Sends string to 'outStm'.
     * @param str string
     */
  protected void outStmPrintln(String str)
  {
    if(outStm != null)
      outStm.println(str);
  }
}
