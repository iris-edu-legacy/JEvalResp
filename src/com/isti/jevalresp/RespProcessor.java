//RespProcessor.java:  High-level processing functions for 'JEvalResp'.
//
//  12/12/2001 -- [ET]  Initial release version.
//   2/28/2002 -- [ET]  Slight modifications for 'RespNetProc'
//                      compatibility.
//    5/2/2002 -- [ET]  Added version of 'outputFiles()' that accepts
//                      'PrintStream' parameter.
//   5/15/2002 -- [ET]  Added 'getOutputFileNamesStr()' method and support.
//   5/28/2002 -- [ET]  Added 'getNumberErrors()' method and support.
//    6/6/2002 -- [ET]  Added support for detecting and forwarding 'info'
//                      messages from the 'RespFileParser' object; added
//                      support for multiple file/pathnames and wildcards
//                      in the findResponses 'fileNameParam' parameter.
//   7/10/2002 -- [ET]  Changed 'apOutputFlag' to 'respTypeIndex' and
//                      implemented single amp/phase file option; renamed
//                      'outputFiles()' to 'outputData()'; added parameter
//                      'multiOutputFlag' and support to allow multiple
//                      outputs with same "net.sta.loc.cha" code.
//   7/11/2002 -- [ET]  Changed find-response methods to have 'beginDateObj'
//                      and 'endDateObj' parameters.
//    8/6/2002 -- [ET]  Changed so that the response's end-date is reported
//                      via the 'responseInfo()' call; added
//                      'logSpacingFlag' param to 'findAndOutputResponses()'
//                      method and implemented passing it on to the
//                      'OutputGenerator.calculateResponse()' method; added
//                      'headerFlag' parameter to constructor and implemented
//                      using it to enable header information in output
//                      files.
//   3/26/2003 -- [KF]  Added 'outputDirectory' parameter.
//    5/6/2003 -- [ET]  Modified javadoc (modified 'outputDirectory' param
//                      description).
//   2/25/2005 -- [ET]  Modified 'findResponses()' method to suppress
//                      "No matching response files found" error message
//                      when all matching response files contain errors.
//   3/10/2005 -- [ET]  Added optional 'useDelayFlag' parameter to methods
//                      'processResponse()' and 'findAndOutputResponses()';
//                      added support in 'findResponses()' method for URLs
//                      as input file names.
//    4/1/2005 -- [ET]  Added optional 'showInputFlag' parameter to methods
//                      'processResponse()' and 'findAndOutputResponses()'.
//  10/25/2005 -- [ET]  Added optional List-blockette interpolation
//                      parameters to methods 'processResponse()' and
//                      'findAndOutputResponses()'.
//   5/24/2010 -- [ET]  Added optional parameters 'unwrapPhaseFlag' and
//                      'totalSensitFlag' to 'processResponse()' and
//                      'findAndOutputResponses()' methods; modified to
//                      handle response-output type "fap".
//   1/27/2012 -- [ET]  Added 'doReadResponses()' method and modified
//                      'findResponses()' method to use it.
//   3/29/2012 -- [ET]  Added 'idNameAppendStr' parameter to method
//                      'doReadResponses()'.
//  10/22/2013 -- [ET]  Added optional 'b62XValue' parameter to methods
//                      'processResponse()' and 'findAndOutputResponses()'.
//

package com.isti.jevalresp;

import java.io.*;
import java.util.Date;
import java.util.Vector;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Response;
import com.isti.util.UtilFns;

/**
 * Class RespProcessor contains high-level processing functions for
 * 'JEvalResp'.
 */
public class RespProcessor
{
                             //prefix used when searching for RESP files:
  public static final String RESP_FILE_PREFIX = "RESP.";
  public static final String LINE_SEP_STR =   //"line" separator string
                       "--------------------------------------------------";
    //flag set true to allow multiple outputs with same net.sta.loc.cha:
  protected final boolean multiOutputFlag;
  protected final boolean headerFlag;     //true for header info in output
  protected int numRespFound = 0;         //# of responses found
  protected String errorMessage = null;   //error message from parsing
  protected int numberErrors = 0;         //# of errors that occurred
                             //holds names of output files generated:
  protected String outputFileNamesStr = "";
                             //number of names in 'outputFileNamesStr':
  protected int outputFileNamesCount = 0;
  protected final File outputDirectory;


    /**
     * Creates a response-processor object.
     * @param multiOutputFlag true to allow multiple response outputs with
     * the same "net.sta.loc.cha" code.
     * @param headerFlag true to enable header information in the output
     * file; false for no header information.
     * @param outputDirectory output directory, or null for current
     * directory.
     */
  public RespProcessor(boolean multiOutputFlag,boolean headerFlag,
                                                       File outputDirectory)
  {
    this.multiOutputFlag = multiOutputFlag;
    this.headerFlag = headerFlag;
    this.outputDirectory = outputDirectory;
  }

    /**
     * Finds responses with matching channel IDs, then processes them
     * and writes their output.  This is a convenience method that may
     * be used to add a different "front-end" to the program.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param netArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
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
     * @param stdioFlag true for input from 'stdin' and output to 'stdout',
     * false for input and output via file(s).
     * @return true if successful; false if error (in which case
     * an error message will be sent to 'stderr').
     */
  public boolean findAndOutputResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                   Date endDateObj,String fileNameParam,int outUnitsConvIdx,
               double [] freqArr,boolean logSpacingFlag,boolean verboseFlag,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag,
                       double b62XValue,int respTypeIndex,boolean stdioFlag)
  {
         //find responses (each one is processed and written via
         // callback through the 'CallbackProcWrite' object):
    if(!findResponses(staArr,chaArr,netArr,siteArr,beginDateObj,endDateObj,
                                           fileNameParam,stdioFlag,
                        new CallbackProcWrite(this,outUnitsConvIdx,freqArr,
                      logSpacingFlag,verboseFlag,startStageNum,stopStageNum,
              useDelayFlag,showInputFlag,listInterpOutFlag,listInterpInFlag,
                          listInterpTension,unwrapPhaseFlag,totalSensitFlag,
                             b62XValue,respTypeIndex,stdioFlag,System.err)))
    {    //error finding or processing responses; display error message
      System.err.println(getErrorMessage());
      return false;
    }
    return true;
  }

    /**
     * Finds responses with matching channel IDs, then processes them
     * and writes their output.  This is a convenience method that may
     * be used to add a different "front-end" to the program.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param netArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
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
     * @param stdioFlag true for input from 'stdin' and output to 'stdout',
     * false for input and output via file(s).
     * @return true if successful; false if error (in which case
     * an error message will be sent to 'stderr').
     */
  public boolean findAndOutputResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                   Date endDateObj,String fileNameParam,int outUnitsConvIdx,
               double [] freqArr,boolean logSpacingFlag,boolean verboseFlag,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag,
                        int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputResponses(staArr,chaArr,netArr,siteArr,
                      beginDateObj,endDateObj,fileNameParam,outUnitsConvIdx,
                           freqArr,logSpacingFlag,verboseFlag,startStageNum,
                                    stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                unwrapPhaseFlag,totalSensitFlag,0.0,respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses with matching channel IDs, then processes them
     * and writes their output.  This is a convenience method that may
     * be used to add a different "front-end" to the program.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param netArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
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
     * @param stdioFlag true for input from 'stdin' and output to 'stdout',
     * false for input and output via file(s).
     * @return true if successful; false if error (in which case
     * an error message will be sent to 'stderr').
     */
  public boolean findAndOutputResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                   Date endDateObj,String fileNameParam,int outUnitsConvIdx,
               double [] freqArr,boolean logSpacingFlag,boolean verboseFlag,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                        int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputResponses(staArr,chaArr,netArr,siteArr,
                      beginDateObj,endDateObj,fileNameParam,outUnitsConvIdx,
                           freqArr,logSpacingFlag,verboseFlag,startStageNum,
                                    stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                                false,false,respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses with matching channel IDs, then processes them
     * and writes their output.  This is a convenience method that may
     * be used to add a different "front-end" to the program.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param netArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
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
     * @param stdioFlag true for input from 'stdin' and output to 'stdout',
     * false for input and output via file(s).
     * @return true if successful; false if error (in which case
     * an error message will be sent to 'stderr').
     */
  public boolean findAndOutputResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                   Date endDateObj,String fileNameParam,int outUnitsConvIdx,
               double [] freqArr,boolean logSpacingFlag,boolean verboseFlag,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                  boolean showInputFlag,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputResponses(staArr,chaArr,netArr,siteArr,
                      beginDateObj,endDateObj,fileNameParam,outUnitsConvIdx,
                           freqArr,logSpacingFlag,verboseFlag,startStageNum,
                                    stopStageNum,useDelayFlag,showInputFlag,
                        false,false,0.0,false,false,respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses with matching channel IDs, then processes them
     * and writes their output.  This is a convenience method that may
     * be used to add a different "front-end" to the program.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param netArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
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
     * @param stdioFlag true for input from 'stdin' and output to 'stdout',
     * false for input and output via file(s).
     * @return true if successful; false if error (in which case
     * an error message will be sent to 'stderr').
     */
  public boolean findAndOutputResponses(String [] staArr,String [] chaArr,
       String [] netArr,String [] siteArr,Date beginDateObj,Date endDateObj,
                 String fileNameParam,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
       int stopStageNum,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputResponses(staArr,chaArr,netArr,siteArr,
                      beginDateObj,endDateObj,fileNameParam,outUnitsConvIdx,
                           freqArr,logSpacingFlag,verboseFlag,startStageNum,
                           stopStageNum,false,false,respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses with matching channel IDs.  Each found channel ID
     * and response is reported via the "RespCallback.responseInfo()'
     * method.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param netArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param stdioFlag true for input from 'stdin', false for input
     * from files.
     * @param respCallbackObj a 'RespCallback' object whose 'responseInfo()'
     * method will be called to report on each response found.
     * @return true if successful; false if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public boolean findResponses(String [] staArr, String [] chaArr,
                     String [] netArr, String [] siteArr, Date beginDateObj,
                   Date endDateObj, String fileNameParam, boolean stdioFlag,
                              RespCallback respCallbackObj)
  {
    String channelIdFName;
    numRespFound = 0;        //clear responses found count
    if(stdioFlag)
    {    //"stdio" mode selected; input from 'stdin'; output to 'stdout'
                   //create parse object to read from 'stdin':
      final RespFileParser parserObj =
             new RespFileParser(System.in, "(stdin)");
      if(parserObj.getErrorFlag())
      {  //error creating parser object; set error message
              //call callback method in case verbose data needs to be shown:
        respCallbackObj.responseInfo("(stdin)",null,null,null,null,null);
        setErrorMessage("Error in 'stdin' data:  " +
                                               parserObj.getErrorMessage());
        return false;
      }
      final ChanIdHldr chanIdHldrObj;
      if((chanIdHldrObj=parserObj.findChannelId(staArr,chaArr,
                           netArr,siteArr,beginDateObj,endDateObj)) == null)
      {  //no matching channel ID found
              //call callback method in case verbose data needs to be shown:
        respCallbackObj.responseInfo("(stdin)",null,null,null,null,null);
        if(parserObj.getErrorFlag())
        {     //parser detected error; set message
          setErrorMessage("Error parsing channel ID from 'stdin' data:  " +
                                               parserObj.getErrorMessage());
        }
        else
        {     //no parser error; set message
          setErrorMessage("Unable to find matching channel ID in 'stdin' " +
                                                                    "data");
        }
        return false;
      }
              //set handle to channel-ID object for found channel:
      final ChannelId channelIdObj = chanIdHldrObj.channelIdObj;
         //generate filename from channel ID info (if flag set for multiple
         // outputs with same net.sta.loc.cha then include date code):
      channelIdFName = RespUtils.channelIdToFName(channelIdObj,
                                                           multiOutputFlag);
         //read and parse response data from input:
      final Response respObj = parserObj.readResponse();
         //send response information to callback (even if error):
      respCallbackObj.responseInfo((channelIdFName+" (from stdin)"),
                                  channelIdObj,chanIdHldrObj.respEndDateObj,
                                               channelIdFName,respObj,null);
      if(respObj == null)
      {      //'readResponse()' returned error; set error message
        setErrorMessage("Error parsing response from 'stdin' data:  " +
                                               parserObj.getErrorMessage());
        return false;
      }
      ++numRespFound;        //increment responses found count
      if(parserObj.getInfoFlag())
      {  //info message is available; forward it along
        respCallbackObj.showInfoMessage(parserObj.getInfoMessage());
        parserObj.clearInfoMessage();       //clear info message
      }
      return true;
    }
         //not "stdio" mode; process file(s):
    File [] fileArr;    //array of File objects used if multiple files
    final int fileNameParamLen;
    if(fileNameParam != null &&
                              (fileNameParamLen=fileNameParam.length()) > 0)
    {    //"-f" file name parameter was given
              //process list of file names into array of 'File' objects:
      if((fileArr=RespUtils.processFileNameList(fileNameParam)).length <= 0)
      {  //no 'File' objects returned; set error message
        setErrorMessage("No matching files found for \"" +
                                                      fileNameParam + "\"");
        return false;
      }
      File fileObj;
      if(fileArr.length == 1 && !multiOutputFlag)
      {  //only one 'File' object returned and multiple outputs with
         // same "net.sta.loc.cha" code not allowed
        fileObj = fileArr[0];          //setup handle to 'File' object
        boolean isFileFlag;
        try
        {     //test if 'File' references a normal file (not directory):
          isFileFlag = fileObj.isFile();
        }
        catch(Exception ex)
        {     //exception error occurred
          isFileFlag = false;     //indicate not a normal file
        }
        if(isFileFlag)
        {  //'File' references a normal file; process it here & exit
                                       //create parser object for file:
          final RespFileParser parserObj = new RespFileParser(
                                    fileObj.getAbsolutePath());
          ChannelId channelIdObj = null;      //handle for channel ID
          Date respEndDateObj = null;         //end-date for channel
          Response respObj = null;            //handle for response object
          if(!parserObj.getErrorFlag())
          {     //no errors detected so far
            final ChanIdHldr chanIdHldrObj;
            if((chanIdHldrObj=parserObj.findChannelId(staArr,chaArr,
                           netArr,siteArr,beginDateObj,endDateObj)) != null)
            {   //matching channel ID found
                     //set handle to channel-ID object for found channel:
              channelIdObj = chanIdHldrObj.channelIdObj;
                     //set handle to end-date for found channel:
              respEndDateObj = chanIdHldrObj.respEndDateObj;
                //read and parse response data from file:
              if((respObj=parserObj.readResponse()) == null)
              {      //'readResponse()' returned error; set error msg
                setErrorMessage("Error parsing response from \"" +
                                    parserObj.getInputFileName() + "\":  " +
                                               parserObj.getErrorMessage());
              }
            }
            else
            {   //no matching channel ID found
              if(parserObj.getErrorFlag())
              {      //parser detected error; set message
                setErrorMessage("Error parsing channel ID from \"" +
                                    parserObj.getInputFileName() + "\":  " +
                                               parserObj.getErrorMessage());
              }
              else
              {      //no parser error; set message
                setErrorMessage("Unable to find matching channel ID in \"" +
                                       parserObj.getInputFileName() + "\"");
              }
            }
          }
          else
          {     //error creating parser object for file; set error message
            setErrorMessage("Input file (\"" + parserObj.getInputFileName() +
                              "\") error:  " + parserObj.getErrorMessage());
          }
          parserObj.close();        //close input file
                //send response information to callback (even if error),
                     //generate filename from channel ID info (if allowing
                     // multiple outputs with same net.sta.loc.cha then
                     // include date code):
          respCallbackObj.responseInfo(parserObj.getInputFileName(),
                                                channelIdObj,respEndDateObj,
                   RespUtils.channelIdToFName(channelIdObj,multiOutputFlag),
                                                              respObj,null);
          if(respObj == null)       //if error then
            return false;           //return flag
          ++numRespFound;           //increment responses found count
          if(parserObj.getInfoFlag())
          {     //info message is available; forward it along
            respCallbackObj.showInfoMessage(parserObj.getInfoMessage());
            parserObj.clearInfoMessage();     //clear info message
          }
          return true;
        }
      }
      final Vector fileVec = new Vector();  //Vector to hold 'File' objects
      boolean anyDirsFlag = false;          //set true if any directory names
      boolean isFileFlag,isDirFlag;
      String nameStr;
      for(int i=0; i<fileArr.length; ++i)
      {  //for each 'File' object in array
        fileObj = fileArr[i];          //setup handle to 'File' object
        try
        {     //test if 'File' references a normal file (not directory):
          isFileFlag = fileObj.isFile();
        }
        catch(Exception ex)
        {     //exception error occurred
          isFileFlag = false;     //indicate not a normal file
        }
        if(isFileFlag || UtilFns.isURLAddress(
                          RespUtils.fileObjPathToUrlStr(fileObj.getPath())))
        {     //accessible as local file or is a URL address
          fileVec.add(fileObj);        //add to Vector
        }
        else
        {     //'File' does not reference a file
          nameStr = fileObj.getAbsolutePath();   //get name in 'File'
          try
          {       //test if name references a directory:
            isDirFlag = fileObj.isDirectory();
          }
          catch(Exception ex)
          {       //exception error occurred
            isDirFlag = false;       //indicate not a directory
          }
          if(!isDirFlag)
          {       //not a directory; set error message
            setErrorMessage("Unable to access \"" + nameStr +
                                               "\" as a file or directory");
            return false;
          }
          anyDirsFlag = true;     //indicate directory name found
                  //search for RESP files that match sta/cha/net names
                  // (use current entry as search path):
          RespUtils.findRespfiles(nameStr,staArr,chaArr,
                                   netArr,siteArr,RESP_FILE_PREFIX,fileVec);
        }
      }
      if(anyDirsFlag)
      {  //at least one directory name in list
        try
        {     //convert Vector of File object to array:
          fileArr = (File [])fileVec.toArray(new File[fileVec.size()]);
        }
        catch(Exception ex)
        {     //exception error occurred (shouldn't happen); set message
          setErrorMessage("Internal error:  Unable to convert Vector of " +
                                         "'File' objects to array:  " + ex);
          return false;
        }
      }
    }
    else
    {    //no "-f" file name parameter given
              //search for RESP files that match sta/cha/net names
              // (use local directory as search path):
      fileArr = RespUtils.findRespfiles(".",staArr,chaArr,netArr,siteArr,
                                                          RESP_FILE_PREFIX);
         //check if "SEEDRESP" property exists:
      final String seedRespStr;
      if((seedRespStr=System.getProperty("SEEDRESP")) != null &&
                                                   seedRespStr.length() > 0)
      {  //"SEEDRESP" property exists; attempt to access as directory
        final File seedRespFileObj = new File(seedRespStr);
        if(seedRespFileObj.isDirectory())
        {     //access to "SEEDRESP" directory OK
              //append matching filenames from "SEEDRESP" directory to list:
          fileArr = RespUtils.findRespfiles(
                     seedRespFileObj.getAbsolutePath(),staArr,chaArr,netArr,
                                          siteArr,RESP_FILE_PREFIX,fileArr);
        }
      }
    }
    String inFName;
    final int fileArrLen = (fileArr != null) ? fileArr.length : 0;
    int chanIdMatchCount = 0;
         //process array of files returned:
    for(int fIdx=0; fIdx<fileArrLen; ++fIdx)
    {    //for each filename in array
      inFName = fileArr[fIdx].getPath();    //get filename String
                        //remove leading "./" from filename:
      if(inFName.startsWith("./") || inFName.startsWith(".\\"))
        inFName = inFName.substring(2);
                                  //create parser obj for file:
      final RespFileParser parserObj = new RespFileParser(inFName);
        System.out.printf("processing '%s'\n", inFName);  // XXX
         //find and read responses; add to number-matched count:
      chanIdMatchCount += doReadResponses(staArr,chaArr,netArr,siteArr,
                    beginDateObj,endDateObj,respCallbackObj,parserObj,null);
      parserObj.close();             //close input
    }
    if(chanIdMatchCount <= 0)
    {    //no matching channel-IDs were found; set error message
      setErrorMessage("No matching response files found");
      return false;
    }
    return true;
  }

    /**
     * Finds and reads responses with matching channel IDs, using the
     * given parser object.  Each found channel ID and response is
     * reported via the "RespCallback.responseInfo()' method.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param netArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param respCallbackObj a 'RespCallback' object whose 'responseInfo()'
     * method will be called to report on each response found, or 'null'
     * for none.
     * @param parserObj parser object to use.
     * @param idNameAppendStr string to be appended to generated channel-ID
     * filename, or null for none.
     * @return The number of matching responses found.
     */
  protected int doReadResponses(String [] staArr, String [] chaArr,
                     String [] netArr, String [] siteArr, Date beginDateObj,
                              Date endDateObj, RespCallback respCallbackObj,
                           RespFileParser parserObj, String idNameAppendStr)
  {
    ChanIdHldr chanIdHldrObj;
    ChannelId channelIdObj;
    Response respObj;
    String channelIdFName,str;
    int numIdMatch = 0;
    final Vector chanIdFNameVec = new Vector();  //Vector of chan ID fnames
    if(!parserObj.getErrorFlag())
    {       //no errors detected so far
      if((chanIdHldrObj=parserObj.findChannelId(staArr,chaArr,
                         netArr,siteArr,beginDateObj,endDateObj)) != null)
      {     //first matching channel ID found
        ++numIdMatch;        //increment #-of-chan-ID-matches count
        do       //for each matching channel ID found
        {        //set handle to channel-ID object for found channel:
          channelIdObj = chanIdHldrObj.channelIdObj;
                      //generate filename from channel ID info
                      // (if allowing multiple outputs with same
                      // net.sta.loc.cha then include date code):
          channelIdFName = RespUtils.channelIdToFName(channelIdObj,
                                                         multiOutputFlag);
          if(idNameAppendStr != null)            //if append string given
            channelIdFName += idNameAppendStr;   // then append to name
                 //read and parse response data from file:
          if((respObj=parserObj.readResponse()) == null &&
                                                    respCallbackObj != null)
          { //'readResponse()' returned error; send back error message
            if((str=parserObj.getInputFileName()) != null &&
                                                    str.trim().length() > 0)
            {  //file name not empty; include in error message
              str = " from \"" + str + '\"';
            }
            else   //file name empty
              str = UtilFns.EMPTY_STRING;
            str = "Error parsing response" + str + ":  ";  //build err msg
            respCallbackObj.responseInfo(parserObj.getInputFileName(),
                                  channelIdObj,chanIdHldrObj.respEndDateObj,
                                                        channelIdFName,null,
                                       (str + parserObj.getErrorMessage()));
          }
          if(respObj != null && respCallbackObj != null)
          {  //response parsed OK and callback object was given
                   //process response and generate output:
            if(chanIdFNameVec.indexOf(channelIdFName) < 0)
            {  //channel ID not previously processed successfully
                      //send back response data for processing:
              if(respCallbackObj.responseInfo(parserObj.getInputFileName(),
                                channelIdObj,chanIdHldrObj.respEndDateObj,
                                             channelIdFName,respObj,null))
              {  //response processed OK; add ID to list of processed
                chanIdFNameVec.add(channelIdFName);
                ++numRespFound;      //increment responses found count
              }
              if(parserObj.getInfoFlag())
              {  //info message is available; forward it along
                respCallbackObj.showInfoMessage(parserObj.getInfoMessage());
                parserObj.clearInfoMessage();       //clear info message
              }
            }
            else //channel ID was already processed
            {         //send back warning message:
              respCallbackObj.responseInfo(parserObj.getInputFileName(),
                                channelIdObj,chanIdHldrObj.respEndDateObj,
                         channelIdFName,null,("WARNING:  Response with " +
                                    "duplicate channel ID ignored in \"" +
                                    parserObj.getInputFileName() + "\""));
            }
          }
        }        //loop if more than 1 output per net.sta.loc.cha allowed
        while(multiOutputFlag &&    // and next matching channel-ID found
                     (chanIdHldrObj=parserObj.findChannelId(staArr,chaArr,
                        netArr,siteArr,beginDateObj,endDateObj)) != null);
      }
      else if(parserObj.getErrorFlag() && respCallbackObj != null)
      {     //parser returned error; send back message
        respCallbackObj.responseInfo(parserObj.getInputFileName(),null,
                      null,null,null,("Error parsing channel ID from \"" +
                                  parserObj.getInputFileName() + "\":  " +
                                            parserObj.getErrorMessage()));
      }
    }
    else if(respCallbackObj != null)
    {  //error creating parser object for file; send back error message
      respCallbackObj.responseInfo(parserObj.getInputFileName(),null,null,
               null,null,"Input file (\"" + parserObj.getInputFileName() +
                            "\") error:  " + parserObj.getErrorMessage());
    }
    return numIdMatch;
  }

    /**
     * Finds responses with matching channel IDs.  Each found channel ID
     * and response is reported via the "RespCallback.responseInfo()'
     * method.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param stdioFlag true for input from 'stdin', false for input
     * from files.
     * @param respCallbackObj a 'RespCallback' object whose 'responseInfo()'
     * method will be called to report on each response found.
     * @return true if successful; false if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public boolean findResponses(String staListStr,String chaListStr,
                     String netListStr,String siteListStr,Date beginDateObj,
                     Date endDateObj,String fileNameParam,boolean stdioFlag,
                               RespCallback respCallbackObj)
  {
    final String [] staArr,chaArr,netArr,siteArr;
    try
    {         //convert list strings to arrays of strings:
      staArr = (staListStr.trim().length() > 0) ?
            (String [])(UtilFns.listStringToVector(staListStr,',',false).
                                             toArray(new String[0])) : null;
      chaArr = (chaListStr.trim().length() > 0) ?
            (String [])(UtilFns.listStringToVector(chaListStr,',',false).
                                             toArray(new String[0])) : null;
      netArr = (netListStr.trim().length() > 0) ?
            (String [])(UtilFns.listStringToVector(netListStr,',',false).
                                             toArray(new String[0])) : null;
      siteArr = (siteListStr.trim().length() > 0) ?
           (String [])(UtilFns.listStringToVector(siteListStr,',',false).
                                             toArray(new String[0])) : null;
    }
    catch(Exception ex)
    {         //exception occurred; set error message
      setErrorMessage("Internal error:  Unable to create string arrays " +
                                            "in 'findResponses()':  " + ex);
      return false;
    }
    return findResponses(staArr,chaArr,netArr,siteArr,beginDateObj,
                        endDateObj,fileNameParam,stdioFlag,respCallbackObj);
  }

    /**
     * Processes the given response object, calculating the complex
     * spectra output values.
     * @param inFName the file name associated with the response object.
     * @param respObj the response object to be processed.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
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
     * @return An 'OutputGenerator' object loaded with complex spectra
     * response output data; or null if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public OutputGenerator processResponse(String inFName,Response respObj,
               double [] freqArr,boolean logSpacingFlag,int outUnitsConvIdx,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag,
                                                           double b62XValue)
  {
         //create output generator:
    final OutputGenerator outGenObj = new OutputGenerator(respObj);
         //check validity of response:
    if(!outGenObj.checkResponse(       //if 'def', don't check units
                        outUnitsConvIdx==OutputGenerator.DEFAULT_UNIT_CONV))
    {    //error in response; set error code & msg
      setErrorMessage("Error in response from \"" + inFName + "\":  " +
                                               outGenObj.getErrorMessage());
      return null;
    }
         //response checked OK; do normalization:
    if(!outGenObj.normalizeResponse(startStageNum,stopStageNum))
    {    //normalization error; set error message
      setErrorMessage("Error normalizing response from \"" + inFName +
                                     "\":  " + outGenObj.getErrorMessage());
      return null;
    }
         //response normalized OK; calculate output:
    if(!outGenObj.calculateResponse(freqArr,logSpacingFlag,outUnitsConvIdx,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                                 unwrapPhaseFlag,totalSensitFlag,b62XValue))
    {    //calculation error; set error message
      setErrorMessage("Error calculating response from \"" + inFName +
                                     "\":  " + outGenObj.getErrorMessage());
      return null;
    }
    return outGenObj;
  }

    /**
     * Processes the given response object, calculating the complex
     * spectra output values.
     * @param inFName the file name associated with the response object.
     * @param respObj the response object to be processed.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
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
     * @return An 'OutputGenerator' object loaded with complex spectra
     * response output data; or null if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public OutputGenerator processResponse(String inFName,Response respObj,
               double [] freqArr,boolean logSpacingFlag,int outUnitsConvIdx,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag)
  {
    return processResponse(inFName,respObj,freqArr,logSpacingFlag,
                                 outUnitsConvIdx,startStageNum,stopStageNum,
                               useDelayFlag,showInputFlag,listInterpOutFlag,
                                         listInterpInFlag,listInterpTension,
                                       unwrapPhaseFlag,totalSensitFlag,0.0);
  }

    /**
     * Processes the given response object, calculating the complex
     * spectra output values.
     * @param inFName the file name associated with the response object.
     * @param respObj the response object to be processed.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
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
     * @return An 'OutputGenerator' object loaded with complex spectra
     * response output data; or null if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public OutputGenerator processResponse(String inFName,Response respObj,
               double [] freqArr,boolean logSpacingFlag,int outUnitsConvIdx,
                    int startStageNum,int stopStageNum,boolean useDelayFlag,
                            boolean showInputFlag,boolean listInterpOutFlag,
                          boolean listInterpInFlag,double listInterpTension)
  {
    return processResponse(inFName,respObj,freqArr,logSpacingFlag,
                                 outUnitsConvIdx,startStageNum,stopStageNum,
                               useDelayFlag,showInputFlag,listInterpOutFlag,
                        listInterpInFlag,listInterpTension,false,false,0.0);
  }

    /**
     * Processes the given response object, calculating the complex
     * spectra output values.
     * @param inFName the file name associated with the response object.
     * @param respObj the response object to be processed.
     * @param freqArr an array of frequency values to use.
     * @param logSpacingFlag true to indicate that the frequency spacing
     * is logarithmic; false to indicate linear spacing.
     * @param outUnitsConvIdx output units conversion index for the
     * requested output units type; one of the '..._UNIT_CONV' values.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @return An 'OutputGenerator' object loaded with complex spectra
     * response output data; or null if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public OutputGenerator processResponse(String inFName,Response respObj,
               double [] freqArr,boolean logSpacingFlag,int outUnitsConvIdx,
                                         int startStageNum,int stopStageNum)
  {
    return processResponse(inFName,respObj,freqArr,logSpacingFlag,
                                 outUnitsConvIdx,startStageNum,stopStageNum,
                               false,false,false,false,0.0,false,false,0.0);
  }

    /**
     * Writes the 'evalresp'-style output file or files for the given
     * 'OutputGenerator' object.
     * @param outGenObj an 'OutputGenerator' object that has had its
     * 'calculateResponse()' method executed successfully.
     * @param channelIdObj the channel ID associated with the
     * 'OutputGenerator' object.
     * @param respEndDateObj end date for channel ID.
     * @param channelIdFName a string of channel ID information associated
     * with the 'OutputGenerator' object.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stmObj stream to write output data into, or null for output
     * to files.
     * @return true if successful; false if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public boolean outputData(OutputGenerator outGenObj,
           ChannelId channelIdObj,Date respEndDateObj,String channelIdFName,
                                       int respTypeIndex,PrintStream stmObj)
  {
    outputFileNamesStr = "";           //initialize output files string
    outputFileNamesCount = 0;          //initialize name count

         //if flag set then build header information String:
    final String headerStr = headerFlag ?
                 RespUtils.channelIdToHdrString(channelIdObj,respEndDateObj,
                               Run.HDR_CMT_STR,", ",UtilFns.newline) : null;

         //generate output file(s):
    if(respTypeIndex != Run.RESP_CS_TYPEIDX)
    {    //amp/phase output selected
      if(stmObj == null)
      {  //not 'stdio' flag
        if(respTypeIndex != Run.RESP_AP2_TYPEIDX &&
                                      respTypeIndex != Run.RESP_FAP_TYPEIDX)
        {     //not single amp/phase file; write data to 2 output files
          final String ampFileNameStr = "AMP." + channelIdFName;
          final String phaseFileNameStr = "PHASE." + channelIdFName;
          if(!outGenObj.writeAmpPhaseData(
              outputDirectory,ampFileNameStr,phaseFileNameStr,headerStr))
          {   //error writing to files
            setErrorMessage("Error writing output files for \"" +
                    channelIdFName + "\":  " + outGenObj.getErrorMessage());
            return false;
          }
                                  //enter names into holder string:
          outputFileNamesStr = "\"" + ampFileNameStr + "\", \"" +
                                                    phaseFileNameStr + "\"";
          outputFileNamesCount = 2;    //set number of names in string
        }
        else
        {     //single amp/phase file
                   //setup filename, with prefix based on output type:
          final String outNameStr =
                ((respTypeIndex == Run.RESP_FAP_TYPEIDX) ? "FAP." : "AP.") +
                                                             channelIdFName;
          if(!outGenObj.writeAmpPhaseData(
                                      outputDirectory,outNameStr,headerStr))
          {   //error writing to file
            setErrorMessage("Error writing output file for \"" +
                    channelIdFName + "\":  " + outGenObj.getErrorMessage());
            return false;
          }
          outputFileNamesStr = "\"" + outNameStr + "\"";   //save name
          outputFileNamesCount = 1;    //set number of names in string
        }
      }
      else
      {  //'stdio' mode; write data to 'stdout'
        stmObj.println(LINE_SEP_STR);   //show separator
        stmObj.println("AMP/PHS." + channelIdFName);   //show name info
        stmObj.println(LINE_SEP_STR);   //show separator
        final String outNameStr = "(stdout)";
        if(!outGenObj.writeAmpPhaseData(new OutputStreamWriter(stmObj),
                                                      outNameStr,headerStr))
        {     //error writing to 'stdout'
          setErrorMessage("Error writing output for \"" +
                    channelIdFName + "\":  " + outGenObj.getErrorMessage());
          return false;
        }
        outputFileNamesStr = "\"" + outNameStr + "\"";     //save name
        outputFileNamesCount = 1;      //set number of names in string
      }
    }
    else
    {    //complex spectra output selected
      if(stmObj == null)
      {  //not 'stdio' flag; write data to output files
        final String outNameStr = "SPECTRA." + channelIdFName;
        if(!outGenObj.writeCSpectraData(outputDirectory,outNameStr,headerStr))
        {       //error writing to file
          setErrorMessage("Error writing output file for \"" +
                    channelIdFName + "\":  " + outGenObj.getErrorMessage());
          return false;
        }
        outputFileNamesStr = "\"" + outNameStr + "\"";     //save name
        outputFileNamesCount = 1;      //set number of names in string
      }
      else
      {  //'stdio' mode; write data to 'stdout'
        stmObj.println(LINE_SEP_STR);   //show separator
        stmObj.println("SPECTRA." + channelIdFName);   //show name info
        stmObj.println(LINE_SEP_STR);   //show separator
        final String outNameStr = "(stdout)";
        if(!outGenObj.writeCSpectraData(new OutputStreamWriter(stmObj),
                                                      outNameStr,headerStr))
        {     //error writing to 'stdout'
          setErrorMessage("Error writing output for \"" +
                    channelIdFName + "\":  " + outGenObj.getErrorMessage());
          return false;
        }
        outputFileNamesStr = "\"" + outNameStr + "\"";     //save name
        outputFileNamesCount = 1;      //set number of names in string
      }
    }
    return true;
  }

    /**
     * Writes the 'evalresp'-style output file or files for the given
     * 'OutputGenerator' object.
     * @param outGenObj an 'OutputGenerator' object that has had its
     * 'calculateResponse()' method executed successfully.
     * @param channelIdObj the channel ID associated with the
     * 'OutputGenerator' object.
     * @param respEndDateObj end date for channel ID.
     * @param channelIdFName a string of channel ID information associated
     * with the 'OutputGenerator' object.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param stdioFlag true for output to 'stdout', false for output
     * to file(s).
     * @return true if successful; false if error (in which case
     * 'getErorMessage()' may be used to see information about the error).
     */
  public boolean outputData(OutputGenerator outGenObj,
           ChannelId channelIdObj,Date respEndDateObj,String channelIdFName,
                                        int respTypeIndex,boolean stdioFlag)
  {
    if(stdioFlag)
    {    //stdio flag set; output to 'stdout'
      return outputData(outGenObj,channelIdObj,respEndDateObj,
                                   channelIdFName,respTypeIndex,System.out);
    }
    else
    {    //not stdio; output to files
      return outputData(outGenObj,channelIdObj,respEndDateObj,
                                         channelIdFName,respTypeIndex,null);
    }
  }

    /**
     * @return number of responses found by last call to 'findResponses()'.
     */
  public int getNumRespFound()
  {
    return numRespFound;
  }

    /**
     * @return a String containing the names of the most recently generated
     * output files (by the last call to 'outputData()'), or an empty
     * string if none have been generated.
     */
  public String getOutputFileNamesStr()
  {
    return outputFileNamesStr;
  }

    /**
     * @return the number of names returned by 'getOutputFileNamesStr()'
     * (will be 0, 1 or 2).
     */
  public int getOutputFileNamesCount()
  {
    return outputFileNamesCount;
  }

    /**
     * Enters error message (if none previously entered).
     * @param str error message string
     */
  protected void setErrorMessage(String str)
  {
    if(errorMessage == null)      //if no previous error then
      errorMessage = str;         //set error message
    ++numberErrors;               //increment error count
  }

    /**
     * @return true if an error was detected.  The error message may be
     * fetched via the 'getErrorMessage()' method.
     */
  public boolean getErrorFlag()
  {
    return (errorMessage != null);
  }

    /** @return message string for last error (or 'No error' if none). */
  public String getErrorMessage()
  {
    return (errorMessage != null) ? errorMessage : "No error";
  }

    /** Clears the error message string. */
  public void clearErrorMessage()
  {
    errorMessage = null;
  }

    /**
     * @return the number of errors that have occurred.  This total is
     * not cleared by 'clearErrorMessage()'.
     */
  public int getNumberErrors()
  {
    return numberErrors;
  }
}
