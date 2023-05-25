//RunBlks.java:  Alternate execution manager for 'JEvalResp' that processes
//               responses into an array of 'RespInfoBlk' objects before
//               outputting them.
//
//  12/19/2001 -- [ET]  Initial release version.
//   4/29/2002 -- [ET]  Changed "RunBase" reference to "Run".
//    5/2/2002 -- [ET]  Added do-nothing 'setRespProcObj()' method to
//                      'RespCallback' implementation.
//    6/5/2002 -- [ET]  Added 'showInfoMessage()' method to
//                      'RespCallback' implementation.
//   7/11/2002 -- [ET]  Changed 'apOutputFlag' to 'respTypeIdx'.
//    8/5/2002 -- [ET]  Added 'respEndDateObj' parameter to 'responseInfo()'
//                      method in anonymous 'RespCallback' subclass and
//                      modified to show it on verbose output; put in
//                      default ('true') value for 'logSpacingFlag' param
//                      in call to 'RespProcessor.processResponse()' method
//                      and 'OutputGenerator' constructor.
//   3/26/2003 -- [KF]  Added 'outputDirectory' parameter.
//   9/12/2003 -- [ET]  Added new versions of methods without
//                      'outputDirectory' parameter.
//    3/7/2005 -- [ET]  Added optional 'useDelayFlag' parameter to method
//                      'rBlksEvresp()'.
//    4/1/2005 -- [ET]  Added optional 'showInputFlag' parameter to
//                      'processAndOutput()' methods.
//   4/21/2005 -- [ET]  Changed "@returns" to "@return".
//   11/1/2005 -- [ET]  Added optional List-blockette interpolation
//                      parameters to 'processAndOutput()' methods;
//                      modified to use 'outGenObj.printListStageMsgs()'
//                      method.
//   5/25/2007 -- [ET]  Modified to show informational message fetched
//                      via OutputGenerator 'getInfoMessage()' method.
//   5/19/2010 -- [ET]  Added optional parameters 'unwrapPhaseFlag' and
//                      'totalSensitFlag' to 'rBlksEvresp()' method.
//  10/23/2013 -- [ET]  Added optional parameter 'b62XValue' to method
//                      'rBlksEvresp()'.
//

package com.isti.jevalresp;

import java.util.*;
import java.io.File;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Response;

/**
 * Class RunBlks is an alternate execution manager for 'JEvalResp' that
 * processes responses into an array of 'RespInfoBlk' objects before
 * outputting them.
 */
public class RunBlks
{
                        //strings for 'responseType' (-r) parameter:
  protected static final String RESP_TYPE_STRS[] = { "ap", "cs" };
  private int exitStatusValue = 0;   //exit status value returned by prog

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
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
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @param outputDirectory output directory, or null for current
     * directory.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String dateStr,
                                String outUnitsConvStr,String fileNameParam,
                          final double [] freqArr,final boolean verboseFlag,
                             final int startStageNum,final int stopStageNum,
                     final boolean useDelayFlag,final boolean showInputFlag,
             final boolean listInterpOutFlag,final boolean listInterpInFlag,
               final double listInterpTension,final boolean unwrapPhaseFlag,
                       final boolean totalSensitFlag,final double b62XValue,
               final boolean stdioFlag,File outputDirectory)
  {
/*
    System.out.println("Inside 'rBlksEvresp()'");
    System.out.print("staListStr=\"" + staListStr + "\", ");
    System.out.print("chaListStr=\"" + chaListStr + "\", ");
    System.out.print("netListStr=\"" + netListStr + "\", ");
    System.out.print("siteListStr=\"" + siteListStr + "\", ");
    System.out.print("dateStr=\"" + dateStr + "\", ");
    System.out.print("outUnitsConvStr=\"" + outUnitsConvStr + "\", ");
    System.out.print("fileNameParam=\"" + fileNameParam + "\", ");
    System.out.print("verboseFlag=" + verboseFlag + ", ");
    System.out.print("startStageNum=" + startStageNum + ", ");
    System.out.print("stopStageNum=" + stopStageNum + ", ");
    System.out.print("useDelayFlag=" + useDelayFlag + ", ");
    System.out.print("showInputFlag=" + showInputFlag + ", ");
    System.out.print("stdioFlag=" + stdioFlag + ", ");
    System.out.print("freqArr.length=" + freqArr.length + ", ");
    if(freqArr.length > 0)
    {
      System.out.print("freqArr[] = {");
      for(int i=0; i<freqArr.length; ++i)
        System.out.print(" " + freqArr[i]);
      System.out.print(" }");
    }
    System.out.println();
*/

    final Date dateObj;
    if(dateStr != null && dateStr.length() > 0)
    {    //date string was specified; convert to Date object:
      if((dateObj=RespUtils.parseRespDate(dateStr)) == null)
      {    //error parsing date string; set error code and message
        setErr(11,"Unable to process date string \"" + dateStr + "\"");
        return null;
      }
    }
    else      //date string not specified
      dateObj = null;        //set to null to indicate no date
    if(outUnitsConvStr == null || outUnitsConvStr.length() <= 0)
    {    //output units conversion string not given
      outUnitsConvStr =      //set to default value ("vel")
           OutputGenerator.UNIT_CONV_STRS[OutputGenerator.UNIT_CONV_DEFIDX];
    }
    final int outUnitsConvIdx;    //convert string to index value:
    if((outUnitsConvIdx=Arrays.asList(OutputGenerator.UNIT_CONV_STRS).
                                indexOf(outUnitsConvStr.toLowerCase())) < 0)
    {    //error matching string; set error code and message
      setErr(12,"Unable to process output units conversion string \"" +
                                                    outUnitsConvStr + "\"");
      return null;
    }
    final Vector respBlkVec = new Vector();      //RespInfoBlk objs
    if(verboseFlag)
    {    //verbose mode; show message
      System.err.println("<< " + Run.REVISION_STR + " Response Output >>");
    }
         //create response processor object:
    final RespProcessor respProcObj = new RespProcessor(false,false,
                                                           outputDirectory);
         //find responses (each one is reported via 'RespCallback'):
    if(!respProcObj.findResponses(staListStr,chaListStr,netListStr,
          siteListStr,dateObj,null,fileNameParam,stdioFlag,
          new RespCallback()      //anonymous class to handle callback
          {        //implement do-nothing set method
            public void setRespProcObj(RespProcessor respProcObj) {}
                   //for each response found; process it:
            public boolean responseInfo(String fileName,
                                 ChannelId channelIdObj,Date respEndDateObj,
                    String channelIdFName,Response respObj,String errMsgStr)
            {
              if(verboseFlag)
              {    //verbose mode; show file name
                System.err.println(RespProcessor.LINE_SEP_STR);
                System.err.println("  " + fileName);
                System.err.println(RespProcessor.LINE_SEP_STR);
                if(channelIdObj != null)
                {       //channel ID object is OK; show channel information
                  System.err.println("  " + RespUtils.channelIdToEvString(
                                              channelIdObj,respEndDateObj));
                }
              }
              if(respObj != null)
              {    //response object contains data
                   //process response information:
                final OutputGenerator outGenObj;
                if((outGenObj=respProcObj.processResponse(fileName,respObj,
                                 freqArr,true,outUnitsConvIdx,startStageNum,
                                    stopStageNum,useDelayFlag,showInputFlag,
                                         listInterpOutFlag,listInterpInFlag,
                                          listInterpTension,unwrapPhaseFlag,
                                        totalSensitFlag,b62XValue)) == null)
                {  //error processing response; show error message
                  System.err.println(respProcObj.getErrorMessage());
                  if(respProcObj.getNumRespFound() > 1)    //if >1 response
                    respProcObj.clearErrorMessage();       // then clear err
                  return false;        //indicate not processed OK
                }
                if(outGenObj.getInfoFlag())
                {  //info message available; show it
                  System.err.println(outGenObj.getInfoMessage());
                  outGenObj.clearInfoMessage();            //clear message
                }
                   //reponse output calculated OK
                if(verboseFlag)
                {       //verbose mode; show extra information
                  System.err.println(outGenObj.getRespInfoString());
                        //list stage information:
                  System.err.println(outGenObj.getStagesListStr());
                        //if response contains List stage then output msgs:
                  if(outGenObj.getListStageFlag())
                    outGenObj.printListStageMsgs(System.err,true);
                }
                   //check if calc sens too much different from stage 0:
                final double fVal;
                if(respObj.stages != null &&
                    outGenObj.getNumCalcStages() >= respObj.stages.length &&
                                                          !RespUtils.isZero(
                                 fVal=RespUtils.getRespSensitivity(respObj)))
                {  //response OK, all stages calc'd & resp sens not zero
                        //calc percentage difference in sensitivity values:
                  if(Math.abs((fVal-outGenObj.getCalcSensitivity())/fVal) >=
                                                                       0.05)
                  {     //greater than 5% difference; show message
                    System.err.println("WARNING:  Computed and reported " +
                          "(stage 0) sensitivities differ by more than 5%");
                  }
                }
                if(channelIdObj != null)
                {  //channel ID OK; create new response blk & add to Vector:
                  respBlkVec.add(new RespInfoBlk(
                        channelIdObj.station_code,channelIdObj.channel_code,
                                           ((channelIdObj.network_id!=null)?
                                   channelIdObj.network_id.network_code:""),
                        channelIdObj.site_code,outGenObj.getCSpectraArray(),
                                    outGenObj.getCalcFreqArray(),fileName));
                }
                return true;           //indicate processed OK
              }
                   //no data in response object
              if(errMsgStr != null)              //if not null then
                System.err.println(errMsgStr);   //show error message
              return false;            //indicate not processed OK
            }
                   //show the given informational message:
            public void showInfoMessage(String msgStr)
            {
              System.err.println(msgStr);
            }
          }))
    {    //error finding responses; set error code and message
      setErr(14,respProcObj.getErrorMessage());
      return null;
    }
    if(!respProcObj.getErrorFlag())
    {    //no errors flagged; build Vector to return
      final RespInfoBlk [] respBlkArr;
      try
      {         //convert Vector to array of 'RespInfoBlk':
        respBlkArr = (RespInfoBlk [])(respBlkVec.toArray(
                                       new RespInfoBlk[respBlkVec.size()]));
      }
      catch(Exception ex)
      {         //exception error occurred; set error message
        setErr(15,"Internal error:  Unable to create response block array " +
                                    "from Vector in 'rBlksEvresp()':  " + ex);
        return null;
      }
      return respBlkArr;       //return array of response blocks
    }
         //errors were flagged
    exitStatusValue = 16;         //set non-zero exit status code
    return null;
  }

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
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
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @param outputDirectory output directory, or null for current
     * directory.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String dateStr,
                                String outUnitsConvStr,String fileNameParam,
                          final double [] freqArr,final boolean verboseFlag,
                             final int startStageNum,final int stopStageNum,
                     final boolean useDelayFlag,final boolean showInputFlag,
             final boolean listInterpOutFlag,final boolean listInterpInFlag,
               final double listInterpTension,final boolean unwrapPhaseFlag,
                      final boolean totalSensitFlag,final boolean stdioFlag,
                                final File outputDirectory)
  {
    return rBlksEvresp(staListStr,chaListStr,netListStr,siteListStr,
                  dateStr,outUnitsConvStr,fileNameParam,freqArr,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                                        unwrapPhaseFlag,totalSensitFlag,0.0,
                                          stdioFlag,outputDirectory);
  }

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
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
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @param outputDirectory output directory, or null for current
     * directory.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String dateStr,
                                String outUnitsConvStr,String fileNameParam,
                          final double [] freqArr,final boolean verboseFlag,
                             final int startStageNum,final int stopStageNum,
                     final boolean useDelayFlag,final boolean showInputFlag,
             final boolean listInterpOutFlag,final boolean listInterpInFlag,
                     final double listInterpTension,final boolean stdioFlag,
                                final File outputDirectory)
  {
    return rBlksEvresp(staListStr,chaListStr,netListStr,siteListStr,
                  dateStr,outUnitsConvStr,fileNameParam,freqArr,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                          false,false,0.0,stdioFlag,outputDirectory);
  }

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @param outputDirectory output directory, or null for current
     * directory.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String dateStr,
                                String outUnitsConvStr,String fileNameParam,
                          final double [] freqArr,final boolean verboseFlag,
                             final int startStageNum,final int stopStageNum,
                     final boolean useDelayFlag,final boolean showInputFlag,
         final boolean stdioFlag,final File outputDirectory)
  {
    return rBlksEvresp(staListStr,chaListStr,netListStr,siteListStr,
                  dateStr,outUnitsConvStr,fileNameParam,freqArr,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
          false,false,0.0,false,false,0.0,stdioFlag,outputDirectory);
  }

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @param outputDirectory output directory, or null for current
     * directory.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,
      String chaListStr,String netListStr,String siteListStr,String dateStr,
        String outUnitsConvStr,String fileNameParam,final double [] freqArr,
                          final boolean verboseFlag,final int startStageNum,
       final int stopStageNum,final boolean stdioFlag,final File outputDirectory)
  {
    return rBlksEvresp(staListStr,chaListStr,netListStr,siteListStr,
                  dateStr,outUnitsConvStr,fileNameParam,freqArr,verboseFlag,
   startStageNum,stopStageNum,false,false,stdioFlag,outputDirectory);
  }

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects, using the current directory.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
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
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,
                     String chaListStr,String netListStr,String siteListStr,
                 String dateStr,String outUnitsConvStr,String fileNameParam,
                          final double [] freqArr,final boolean verboseFlag,
                             final int startStageNum,final int stopStageNum,
                     final boolean useDelayFlag,final boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
    double listInterpTension,final boolean stdioFlag)
  {
    return rBlksEvresp(staListStr,chaListStr,netListStr,siteListStr,dateStr,
            outUnitsConvStr,fileNameParam,freqArr,verboseFlag,startStageNum,
                  stopStageNum,useDelayFlag,showInputFlag,listInterpOutFlag,
  listInterpInFlag,listInterpTension,false,false,0.0,stdioFlag,null);
  }

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects, using the current directory.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,
                     String chaListStr,String netListStr,String siteListStr,
                 String dateStr,String outUnitsConvStr,String fileNameParam,
                          final double [] freqArr,final boolean verboseFlag,
                             final int startStageNum,final int stopStageNum,
                     final boolean useDelayFlag,final boolean showInputFlag,
                              final boolean stdioFlag)
  {
    return rBlksEvresp(staListStr,chaListStr,netListStr,siteListStr,dateStr,
            outUnitsConvStr,fileNameParam,freqArr,verboseFlag,startStageNum,
             stopStageNum,useDelayFlag,showInputFlag,stdioFlag,null);
  }

    /**
     * Finds and processes responses, returning the output in an array
     * of 'RespInfoBlk' objects, using the current directory.
     * @param staListStr a list of station name patterns to search for,
     * or a null or empty string to accept all station names.
     * @param chaListStr a list of channel name patterns to search for,
     * or a null or empty string to accept all channel names.
     * @param netListStr a list of network name patterns to search for,
     * or a null or empty string to accept all network names.
     * @param siteListStr a list of site name patterns to search for,
     * or a null or empty string to accept all site names.
     * @param dateStr a string version of a date to search for, or null
     * to accept all dates.
     * @param outUnitsConvStr output units conversion string for the
     * requested output units type; one of the 'UNIT_CONV_STRS' strings.
     * @param fileNameParam a specific filename (or directory) to use, or
     * a null or empty string for all matching files.
     * @param freqArr an array of frequency values to use.
     * @param verboseFlag true for verbose output messages.
     * @param startStageNum if greater than zero then the start of the
     * range of stage sequence numbers to use, otherwise all stages
     * are used.
     * @param stopStageNum if greater than zero then the end of the
     * range of stage sequence numbers to use, otherwise only the single
     * stage specified by 'startStageNum' is used.
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @return An array of 'RespInfoBlk' objects, or null if an
     * error occurred (in which case a message will be sent to 'stderr'
     * and an exit status code will be set that may fetched via the
     * 'getExitStatusValue()' method).
     */
  public RespInfoBlk [] rBlksEvresp(String staListStr,
      String chaListStr,String netListStr,String siteListStr,String dateStr,
        String outUnitsConvStr,String fileNameParam,final double [] freqArr,
                          final boolean verboseFlag,final int startStageNum,
       final int stopStageNum,final boolean stdioFlag)
  {
    return rBlksEvresp(staListStr,chaListStr,netListStr,siteListStr,
                  dateStr,outUnitsConvStr,fileNameParam,freqArr,verboseFlag,
              startStageNum,stopStageNum,false,false,stdioFlag,null);
  }

    /**
     * Writes the output for the given array of 'RespInfoBlk' objects.
     * @param respBlkArr an array of response blocks to be outputted.
     * @param respTypeStr a string indicating the type of response data
     * to be generated ("ap" or "cs").
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @param outputDirectory output directory, or null for current
     * directory.
     * @return true if successful, false if an error occurred (in which
     * case a message will be sent to 'stderr' and an exit status code
     * will be set that may fetched via the 'getExitStatusValue()' method).
     */
  public boolean rBlksWriteResponse(RespInfoBlk [] respBlkArr,
                                       String respTypeStr,boolean stdioFlag,
                                                       File outputDirectory)
  {
    final int respBlkArrLen;
    if(respBlkArr == null || (respBlkArrLen=respBlkArr.length) <= 0)
    {
      setErr(16,"No response data to write");
      return false;
    }
    final int respTypeIdx;        //convert string to index value:
    if((respTypeIdx=Arrays.asList(RESP_TYPE_STRS).
                                    indexOf(respTypeStr.toLowerCase())) < 0)
    {    //error matching string; set error code and message
      setErr(17,"Unable to process response type string \"" +
                                                        respTypeStr + "\"");
      return false;
    }
         //create response processor object:
    final RespProcessor respProcObj = new RespProcessor(false,false,
                                                           outputDirectory);
    RespInfoBlk respBlkObj;
    for(int i=0; i<respBlkArrLen; ++i)
    {    //for each response block array element
      if((respBlkObj=respBlkArr[i]) != null)
      {  //response block array element OK
              //create and pass an 'OutputGenerator' object and a channel
              // ID string; generate output file(s):
        if(!respProcObj.outputData(new OutputGenerator(
            respBlkObj.cSpectraArray,respBlkObj.freqArr,true),
                null,null,RespUtils.channelIdToFName(respBlkObj.stationName,
                              respBlkObj.channelName,respBlkObj.networkName,
                                respBlkObj.siteName),respTypeIdx,stdioFlag))
        {     //error writing output; show error msg
          System.err.println(respProcObj.getErrorMessage());
          if(respProcObj.getNumRespFound() > 1)  //if more than one response
            respProcObj.clearErrorMessage();     // then clear error
        }
      }
      else
      {  //response block array element is null; display error message
        System.err.println("Null 'RespInfoBlk' array element (" +
                                         i + ") in 'rBlksWriteResponse()'");
      }
    }
    if(!respProcObj.getErrorFlag())    //if no error flagged then
      return true;                     //return OK flag
    exitStatusValue = 18;         //set non-zero exit status code
    return false;
  }

    /**
     * Writes the output for the given array of 'RespInfoBlk' objects,
     * using the current directory.
     * @param respBlkArr an array of response blocks to be outputted.
     * @param respTypeStr a string indicating the type of response data
     * to be generated ("ap" or "cs").
     * @param stdioFlag true for output to 'stdout', false for output
     * to files.
     * @return true if successful, false if an error occurred (in which
     * case a message will be sent to 'stderr' and an exit status code
     * will be set that may fetched via the 'getExitStatusValue()' method).
     */
  public boolean rBlksWriteResponse(RespInfoBlk [] respBlkArr,
                                       String respTypeStr,boolean stdioFlag)
  {
    return rBlksWriteResponse(respBlkArr,respTypeStr,stdioFlag,null);
  }

    /**
     * Sets the exit status code and message for an error exit from the
     * program.
     * @param statusVal exit status value to be returned by program.
     * @param errMsgStr if specified then the error message to be sent
     * to 'stderr'.
     */
  protected void setErr(int statusVal,String errMsgStr)
  {
    if(exitStatusValue == 0)           //if no previous value then
      exitStatusValue = statusVal;     //enter status value
    if(errMsgStr != null && errMsgStr.length() > 0)
      System.err.println(errMsgStr);
  }

    /**
     * Returns the exit status value for the program.
     * @return the exit status value for the program.
     */
  public int getExitStatusValue()
  {
    return exitStatusValue;
  }
}
