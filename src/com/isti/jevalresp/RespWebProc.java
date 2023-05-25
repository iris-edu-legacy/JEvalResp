//RespWebProc.java:  Extension of high-level processing functions for
//                   'JEvalResp' that adds methods for fetching and
//                   processing responses from a web-services server.
//
//   1/23/2012 -- [ET]  Initial version.
//   3/28/2012 -- [ET]  Added support for specification of multiple
//                      web-services-server URLs; added 'multiSvrflag'
//                      parameter to constructor.
//  10/22/2013 -- [ET]  Added optional 'b62XValue' parameter to method
//                      'findAndOutputWebResponses()'.
//

package com.isti.jevalresp;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.text.DateFormat;
import java.net.URL;
import com.isti.util.UtilFns;
import com.isti.util.FileUtils;

/**
 * Class RespWebProc is an extension of high-level processing functions
 * for 'JEvalResp' that adds methods for fetching and processing responses
 * from a web-services server.
 */
public class RespWebProc extends RespProcessor
{
  protected final String serverUrlString;
  protected final boolean multiServerflag;
                        //setup date formatter for server-query calls:
  private static final DateFormat qDateFormatter =
                    UtilFns.createDateFormatObj("yyyy-MM-dd'T'HH:mm:ss.SSS",
                                                 UtilFns.GMT_TIME_ZONE_OBJ);

  /**
   * Creates processor object for fetching and processing responses from
   * a web-services server.
   * @param serverUrlStr base URL string(s) for web-services server(s).
   * @param multiOutputFlag true to allow multiple response outputs with
   * the same "net.sta.loc.cha" code.
   * @param headerFlag true to enable header information in the output
   * file; false for no header information.
   * @param outputDirectory output directory.
   * @param multiSvrflag true to fetch responses from all specified
   * web-services servers; false fetch no more than one response.
   */
  public RespWebProc(String serverUrlStr, boolean multiOutputFlag,
             boolean headerFlag, File outputDirectory, boolean multiSvrflag)
  {
                                       //pass flags to parent constructor:
    super(multiOutputFlag,headerFlag,outputDirectory);
    serverUrlString = serverUrlStr;    //save server base URL string
    multiServerflag = multiSvrflag;    //save multi-server flag
  }

  /**
   * Finds responses (via web services) with matching channel IDs,
   * then processes them and writes their output.  This is a convenience
   * method that may be used to add a different "front-end" to the
   * program.
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
   * @param stdioFlag true for output to 'stdout', false for output to
   * file(s).
   * @return true if successful; false if error (in which case
   * an error message will be sent to 'stderr').
   */
  public boolean findAndOutputWebResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
                                   boolean totalSensitFlag,double b62XValue,
                       int respTypeIndex,boolean stdioFlag)
  {
         //find responses (each one is processed and written via
         // callback through the 'CallbackProcWrite' object):
    if(!findWebResponses(staArr,chaArr,netArr,siteArr,beginDateObj,
                                   endDateObj,verboseFlag,
                  new CallbackProcWrite(this,outUnitsConvIdx,freqArr,
                                   logSpacingFlag,verboseFlag,startStageNum,
                                    stopStageNum,useDelayFlag,showInputFlag,
                                         listInterpOutFlag,listInterpInFlag,
                                          listInterpTension,unwrapPhaseFlag,
                                                  totalSensitFlag,b62XValue,
                                       respTypeIndex,stdioFlag,System.err)))
    {    //error finding or processing responses; display error message
      System.err.println(getErrorMessage());
      return false;
    }
    return true;
  }

  /**
   * Finds responses (via web services) with matching channel IDs,
   * then processes them and writes their output.  This is a convenience
   * method that may be used to add a different "front-end" to the
   * program.
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
   * @param stdioFlag true for output to 'stdout', false for output to
   * file(s).
   * @return true if successful; false if error (in which case
   * an error message will be sent to 'stderr').
   */
  public boolean findAndOutputWebResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
                boolean totalSensitFlag,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputWebResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                        verboseFlag,startStageNum,stopStageNum,useDelayFlag,
                           showInputFlag,listInterpOutFlag,listInterpInFlag,
                      listInterpTension,unwrapPhaseFlag,totalSensitFlag,0.0,
                                            respTypeIndex,stdioFlag);
  }

  /**
   * Finds responses (via web services) with matching channel IDs,
   * then processes them and writes their output.  This is a convenience
   * method that may be used to add a different "front-end" to the
   * program.
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
   * @param stdioFlag true for output to 'stdout', false for output to
   * file(s).
   * @return true if successful; false if error (in which case
   * an error message will be sent to 'stderr').
   */
  public boolean findAndOutputWebResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
               double listInterpTension,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputWebResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                        verboseFlag,startStageNum,stopStageNum,useDelayFlag,
                           showInputFlag,listInterpOutFlag,listInterpInFlag,
          listInterpTension,false,false,0.0,respTypeIndex,stdioFlag);
  }

  /**
   * Finds responses (via web services) with matching channel IDs,
   * then processes them and writes their output.  This is a convenience
   * method that may be used to add a different "front-end" to the
   * program.
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
   * @param stdioFlag true for output to 'stdout', false for output to
   * file(s).
   * @return true if successful; false if error (in which case
   * an error message will be sent to 'stderr').
   */
  public boolean findAndOutputWebResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                        int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputWebResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                                     verboseFlag,startStageNum,stopStageNum,
                                 useDelayFlag,showInputFlag,false,false,0.0,
                                            respTypeIndex,stdioFlag);
  }

  /**
   * Finds responses (via web services) with matching channel IDs,
   * then processes them and writes their output.  This is a convenience
   * method that may be used to add a different "front-end" to the
   * program.
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
   * @param stdioFlag true for output to 'stdout', false for output to
   * file(s).
   * @return true if successful; false if error (in which case
   * an error message will be sent to 'stderr').
   */
  public boolean findAndOutputWebResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
       int stopStageNum,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputWebResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                                     verboseFlag,startStageNum,stopStageNum,
                               false,false,respTypeIndex,stdioFlag);
  }

  /**
   * Finds responses (via web services) with matching channel IDs.
   * Each found channel ID and response is reported via the
   * "RespCallback.responseInfo()' method.
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
   * @param verboseFlag true for verbose output messages.
   * @param respCallbackObj a 'RespCallback' object whose 'responseInfo()'
   * method will be called to report on each response found.
   * @return true if successful; false if error (in which case
   * 'getErrorMessage()' may be used to see information about the error).
   */
  public boolean findWebResponses(String [] staArr, String [] chaArr,
                     String [] netArr, String [] siteArr, Date beginDateObj,
                      Date endDateObj, boolean verboseFlag, RespCallback respCallbackObj)
  {
    if(staArr == null && chaArr == null)
    {    //both station and channel not given; set error message
      setErrorMessage("No station or channel parameters given");
      return false;
    }
    numRespFound = 0;        //clear responses found count
    boolean noNetFlag = false, noStaFlag = false;
              //if no net/sta array data then put in null:
    if(netArr == null || netArr.length <= 0)
    {
      netArr = new String[] { null };
      noNetFlag = true;      //indicate network code not given
//      if(verboseFlag)
//        System.err.println("Warning:  Network code not specified");
    }
    if(staArr == null || staArr.length <= 0)
    {
      staArr = new String[] { null };
      noStaFlag = true;      //indicate station name not given
//      if(verboseFlag)
//        System.err.println("Warning:  Station name not specified");
    }
              //if no site/cha array data then put in "*":
    if(siteArr == null || siteArr.length <= 0)
      siteArr = new String[] { "*" };
    if(chaArr == null || chaArr.length <= 0)
      chaArr = new String[] { "*" };
         //check if string contains comma-separated URLs:
    List svrUrlsList = UtilFns.listStringToVector(serverUrlString,',',true);
         //if not multiple then check for semicolon-separated URLs:
    if(svrUrlsList.size() < 2)
    {
      svrUrlsList = UtilFns.listStringToVector(serverUrlString,';',true);
         //if not multiple then check for space-separated URLs:
      if(svrUrlsList.size() < 2)
        svrUrlsList = UtilFns.listStringToVector(serverUrlString,' ',true);
    }
    final Iterator iterObj = svrUrlsList.iterator();
    Object obj;
    while(iterObj.hasNext())
    {  //for each web-services server URL specified
      if((obj=iterObj.next()) instanceof String)
      {  //object is non-null string; fetch responses from server
        if(findOneServerWebResps(staArr,chaArr,netArr,siteArr,beginDateObj,
                                     endDateObj,verboseFlag,respCallbackObj,
                                    (String)obj,noNetFlag,noStaFlag))
        {  //fetch attempt returned success flag
                   //if not fetching from all specified servers and response
                   // was found then stop processing and return success:
          if(!multiServerflag && numRespFound > 0)
            return true;
        }
        else
        {  //fetch attempt returned error flag; show and clear error msg
          if(verboseFlag)
            System.err.println(getErrorMessage());
          clearErrorMessage();
        }
      }
    }
    if(numRespFound <= 0)
    {    //no matching responses found; set error message
      setErrorMessage("No matching responses found");
      return false;
    }
    return true;
  }

  /**
   * Finds responses (via web services) with matching channel IDs.
   * Each found channel ID and response is reported via the
   * "RespCallback.responseInfo()' method.
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
   * @param verboseFlag true for verbose output messages.
   * @param respCallbackObj a 'RespCallback' object whose 'responseInfo()'
   * method will be called to report on each response found.
   * @return true if successful; false if error (in which case
   * 'getErrorMessage()' may be used to see information about the error).
   */
  public boolean findWebResponses(String staListStr,String chaListStr,
                     String netListStr,String siteListStr,Date beginDateObj,
                        Date endDateObj,boolean verboseFlag,
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
                                         "in 'findWebResponses()':  " + ex);
      return false;
    }
    return findWebResponses(staArr,chaArr,netArr,siteArr,beginDateObj,
                       endDateObj,verboseFlag,respCallbackObj);
  }

  /**
   * Finds responses with matching channel IDs on the given web-services
   * server.
   * Each found channel ID and response is reported via the
   * "RespCallback.responseInfo()' method.
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
   * @param verboseFlag true for verbose output messages.
   * @param respCallbackObj a 'RespCallback' object whose 'responseInfo()'
   * method will be called to report on each response found.
   * @param lSvrUrlStr local copy of base URL string for single
   * web-services server.
   * @param noNetFlag true if no network name provided.
   * @param noStaFlag true if no station name provided.
   * @return true if successful; false if error (in which case
   * 'getErrorMessage()' may be used to see information about the error).
   */
  protected boolean findOneServerWebResps(String [] staArr, String [] chaArr,
                     String [] netArr, String [] siteArr, Date beginDateObj,
                                       Date endDateObj, boolean verboseFlag,
                            RespCallback respCallbackObj, String lSvrUrlStr,
                      boolean noNetFlag, boolean noStaFlag)
  {
    String queryStr,str;
    int p;
    for(int netIdx=0; netIdx<netArr.length; ++netIdx)
    {  //for each network name pattern element
      for(int staIdx=0; staIdx<staArr.length; ++staIdx)
      {  //for each station name pattern element
        for(int siteIdx=0; siteIdx<siteArr.length; ++siteIdx)
        {  //for each site/location name pattern element
          for(int chaIdx=0; chaIdx<chaArr.length; ++chaIdx)
          {  //for each channel name pattern element
            queryStr = UtilFns.EMPTY_STRING;     //clear query string
            if(netArr[netIdx] != null)
              queryStr += "&net=" + netArr[netIdx];
            if(staArr[staIdx] != null)
              queryStr += "&sta=" + staArr[staIdx];
            if(siteArr[siteIdx] != null)
            {                //convert site/location "  " to "--":
              str = (siteArr[siteIdx].trim().length() > 0) ?
                                                    siteArr[siteIdx] : "--";
              queryStr += "&loc=" + str;
            }
            if(chaArr[chaIdx] != null)
              queryStr += "&cha=" + chaArr[chaIdx];
            if(queryStr.length() > 0 && lSvrUrlStr != null &&
                                             lSvrUrlStr.trim().length() > 0)
            {  //at least one entry for query and URL string OK
                        //if begin time given then add to query:
              if(beginDateObj != null)
                queryStr += "&start=" + qDateFormatter.format(beginDateObj);
                        //if end time given then add to query:
              if(endDateObj != null)
                queryStr += "&end=" + qDateFormatter.format(endDateObj);

              // so here we have lSvrUrlStr with the URL passed in and queryStr
              // with with parameters, prefixed with &
              queryStr = queryStr.substring(1);  // drop leading &

              // but lSvrUrlStr might itself have extra parameters.
              // if so, chop them off and transfer to queryStr
              String prefix = lSvrUrlStr.trim();
              if (prefix.contains("&")) {
                  queryStr += prefix.substring(prefix.indexOf("&"));
                  prefix = prefix.substring(0, prefix.indexOf("&"));
              }

              if (!prefix.endsWith("/")) prefix += "/";
              prefix += "query?";

              queryStr = prefix + queryStr;

              if(verboseFlag)
              {  //verbose messages enabled; show status message
                System.err.println("Sending query to web-services server:  "
                                                                + queryStr);
              }
              final InputStream stmObj;
              try
              {  //attempt to open connection via query string:
                stmObj = (new URL(queryStr)).openStream();
              }
              catch(FileNotFoundException ex)
              {  //matching response not found; set message and abort
                setErrorMessage("Unable to find matching response data " +
                                                  "on web-services server");
                return false;
              }
              catch(Exception ex)
              {  //error returned from server; set message and abort
                String errStr = ex.toString();
                if(ex instanceof IOException &&
                                 errStr.indexOf("java.io.IOException") >= 0)
                {  //exception is vanilla IOException; remove "IOException"
                  errStr = ex.getMessage();
                  if(errStr.indexOf("400") >= 0)
                  {  //server returned "Bad Request"
                        //if known problem then customize error message:
                    if(noNetFlag && noStaFlag)
                      errStr = "Network and station codes not specified";
                    else if(noNetFlag)
                      errStr = "Network code not specified";
                    else if(noStaFlag)
                      errStr = "Station code not specified";
                    else if(netArr[netIdx] != null &&
                                        (netArr[netIdx].indexOf('*') >= 0 ||
                                          netArr[netIdx].indexOf('?') >= 0))
                    {
                      errStr = "Network code contains wildcard(s)";
                    }
                    else if(staArr[staIdx] != null &&
                                        (staArr[staIdx].indexOf('*') >= 0 ||
                                          staArr[staIdx].indexOf('?') >= 0))
                    {
                      errStr = "Station code contains wildcard(s)";
                    }
                  }
                }
                setErrorMessage("Error querying web-services server:  " +
                                                                    errStr);
                return false;
              }
              String fRespStr;
              try
              {  //attempt to fetch response data:
                fRespStr = FileUtils.readStreamToString(stmObj).trim();
              }
              catch(Exception ex)
              {  //error opening connection; set message and abort
                setErrorMessage("Error fetching response data from web-" +
                                                 "services server:  " + ex);
                return false;
              }
              finally
              {
                FileUtils.closeStream(stmObj);   //close connection
              }
              final int fRespLen;
              if((fRespLen=fRespStr.length()) <= 0)
              {  //no data received
                setErrorMessage("No data received from " +
                                                     "web-services server");
                return false;
              }
              if(verboseFlag)
              {  //verbose messages enabled; show status message
                System.err.println("Received data from web-services " +
                                           "server (len=" + fRespLen + ")");
              }
              String idNameAppendStr;
              if(multiServerflag)
              {  //fetching responses from all specified servers
                        //use server URL as string to be appended:
                idNameAppendStr = lSvrUrlStr;
                             //remove leading "http://":
                if(idNameAppendStr.startsWith("http://"))
                  idNameAppendStr = idNameAppendStr.substring(7);
                             //truncate before trailing "/ws/resp":
                if((p=idNameAppendStr.indexOf("/ws/resp")) > 0)
                  idNameAppendStr = idNameAppendStr.substring(0,p);
                             //prepend leading underscore separator:
                  idNameAppendStr = '_' + idNameAppendStr;
              }
              else  //only fetching first response
                idNameAppendStr = null;
                        //create parser for fetched response data:
              final RespFileParser parserObj = new RespFileParser(
                           new ByteArrayInputStream(fRespStr.getBytes()), UtilFns.EMPTY_STRING);
                        //parse and process fetched response data:
              doReadResponses(staArr,chaArr,netArr,siteArr,beginDateObj,
                      endDateObj,respCallbackObj,parserObj,idNameAppendStr);
              parserObj.close();            //close input
            }
          }
        }
      }
    }
    return true;
  }
}
