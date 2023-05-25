//RunDirect.java:  Extension of 'Run.java' that supports input and output
//                 via method calls for the processing of a single response.
//
// 11/10/2009 -- [ET]  Initial version.
//  5/19/2010 -- [ET]  Added optional parameters 'unwrapPhaseFlag' and
//                     'totalSensitFlag' to 'processOneResponse()' method.
// 10/23/2013 -- [ET]  Added optional parameter 'b62XValue' to method
//                     'processOneResponse()'.
//

package com.isti.jevalresp;

import java.util.Date;
import java.util.Vector;
import java.util.Calendar;
import java.util.TimeZone;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Response;
import com.isti.util.UtilFns;

/**
 * Class RunDirect is an extension of 'Run' that supports input and output
 * via method calls for the processing of a single response.
 */
public class RunDirect extends Run
{
    /** Unit conversion type index value for "default" (0). */
  public static final int DEFAULT_UNIT_CONV =
                                          OutputGenerator.DEFAULT_UNIT_CONV;
    /** Unit conversion type index value for "displacement" (1). */
  public static final int DISPLACE_UNIT_CONV =
                                         OutputGenerator.DISPLACE_UNIT_CONV;
    /** Unit conversion type index value for "velocity" (2). */
  public static final int VELOCITY_UNIT_CONV =
                                         OutputGenerator.VELOCITY_UNIT_CONV;
    /** Unit conversion type index value for "acceleration" (3). */
  public static final int ACCEL_UNIT_CONV = OutputGenerator.ACCEL_UNIT_CONV;


  /**
   * Finds and processes one response, returning the output in an
   * 'OutputGenerator' object.
   * @param stationStr station name to search for, or a null or empty
   * string to accept all station names.
   * @param channelStr channel name to search for, or a null or empty
   * string to accept all channel names.
   * @param networkStr network name to search for, or a null or empty
   * string to accept all network names.
   * @param siteStr site name to search for, or a null or empty string
   * to accept all site names.
   * @param dateObj date to search for, or null to accept all dates.
   * @param outUnitsConvIdx output units conversion index for the
   * requested output units type; one of the '..._UNIT_CONV' values.
   * @param fileNameParam a specific filename (or directory) to use, or
   * a null or empty string for all matching files.
   * @param freqArr an array of frequency values to use.
   * @param startStageNum if greater than zero then the start of the
   * range of stage sequence numbers to use, otherwise all stages
   * are used.
   * @param stopStageNum if greater than zero then the end of the
   * range of stage sequence numbers to use, otherwise only the single
   * stage specified by 'startStageNum' is used.
   * @param useDelayFlag true to use estimated delay in phase calculation.
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
   * @param stdioFlag true for input from 'stdin', false for input from
   * file.
   * @return An 'OutputGenerator' object, or null if an error occurred (in
   * which case 'getErrorMessage()' may be used to fetch information
   * about the error).
   */
  public OutputGenerator processOneResponse(String stationStr,
                       String channelStr, String networkStr, String siteStr,
                                    Date dateObj, final int outUnitsConvIdx,
                              String fileNameParam, final double [] freqArr,
                            final int startStageNum, final int stopStageNum,
                                                 final boolean useDelayFlag,
            final boolean listInterpOutFlag, final boolean listInterpInFlag,
              final double listInterpTension, final boolean unwrapPhaseFlag,
                      final boolean totalSensitFlag, final double b62XValue,
                              final boolean stdioFlag)
  {
              //convert null ID strings to empty strings:
    if(stationStr == null)
      stationStr = UtilFns.EMPTY_STRING;
    if(channelStr == null)
      channelStr = UtilFns.EMPTY_STRING;
    if(networkStr == null)
      networkStr = UtilFns.EMPTY_STRING;
    if(siteStr == null)
      siteStr = UtilFns.EMPTY_STRING;
    final Vector respBlkVec = new Vector();      //RespInfoBlk objs
         //create response processor object:
    final RespProcessor respProcObj = new RespProcessor(false,false,
                                                           outputDirectory);
         //find responses (each one is reported via 'RespCallback'):
    if(!respProcObj.findResponses(stationStr,channelStr,networkStr,
              siteStr,dateObj,null,fileNameParam,stdioFlag,
          new RespCallback()      //anonymous class to handle callback
          {        //implement do-nothing set method
            public void setRespProcObj(RespProcessor respProcObj) {}
                   //for each response found; process it:
            public boolean responseInfo(String fileName,
                                 ChannelId channelIdObj,Date respEndDateObj,
                    String channelIdFName,Response respObj,String errMsgStr)
            {
              if(respObj != null)
              {    //response object contains data
                   //process response information:
                final OutputGenerator outGenObj;
                if((outGenObj=respProcObj.processResponse(fileName,respObj,
                                 freqArr,true,outUnitsConvIdx,startStageNum,
                          stopStageNum,useDelayFlag,false,listInterpOutFlag,
                                         listInterpInFlag,listInterpTension,
                        unwrapPhaseFlag,totalSensitFlag,b62XValue)) == null)
                {  //error processing response; enter error message
                  setErrorMessage(respProcObj.getErrorMessage());
                  if(respProcObj.getNumRespFound() > 1)    //if >1 response
                    respProcObj.clearErrorMessage();       // then clear err
                  return false;        //indicate not processed OK
                }
//                if(outGenObj.getInfoFlag())
//                {  //info message available; show it
//                  System.err.println(outGenObj.getInfoMessage());
//                  outGenObj.clearInfoMessage();            //clear message
//                }
                   //reponse output calculated OK
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
                setErrorMessage(errMsgStr);      //enter error message
              return false;            //indicate not processed OK
            }
            public void showInfoMessage(String msgStr)
            {
            }
          }))
    {    //error finding responses; set error code and message
      setErrorMessage(respProcObj.getErrorMessage());
      return null;
    }
    if(!respProcObj.getErrorFlag())
    {    //no errors flagged
      final Object obj;
      if(respBlkVec.size() <= 0 || !((obj=respBlkVec.get(0)) instanceof
                                                               RespInfoBlk))
      {  //no 'RespInfoBlk' objects found in vector
        setErrorMessage("No matching responses found");
        return null;
      }
              //return output generator via first 'RespInfoBlk' in vector:
      return new OutputGenerator(
          ((RespInfoBlk)obj).cSpectraArray,((RespInfoBlk)obj).freqArr,true);
    }
         //errors were flagged
    exitStatusValue = 16;         //set non-zero exit status code
    return null;
  }

  /**
   * Finds and processes one response, returning the output in an
   * 'OutputGenerator' object.
   * @param stationStr station name to search for, or a null or empty
   * string to accept all station names.
   * @param channelStr channel name to search for, or a null or empty
   * string to accept all channel names.
   * @param networkStr network name to search for, or a null or empty
   * string to accept all network names.
   * @param siteStr site name to search for, or a null or empty string
   * to accept all site names.
   * @param dateObj date to search for, or null to accept all dates.
   * @param outUnitsConvIdx output units conversion index for the
   * requested output units type; one of the '..._UNIT_CONV' values.
   * @param fileNameParam a specific filename (or directory) to use, or
   * a null or empty string for all matching files.
   * @param freqArr an array of frequency values to use.
   * @param startStageNum if greater than zero then the start of the
   * range of stage sequence numbers to use, otherwise all stages
   * are used.
   * @param stopStageNum if greater than zero then the end of the
   * range of stage sequence numbers to use, otherwise only the single
   * stage specified by 'startStageNum' is used.
   * @param useDelayFlag true to use estimated delay in phase calculation.
   * @param listInterpOutFlag true to interpolate amp/phase output
   * from responses containing List blockettes.
   * @param listInterpInFlag true to interpolate amp/phase input from
   * List blockettes in responses (before output is calculated).
   * @param listInterpTension tension value for List-blockette
   * interpolation algorithm.
   * @param unwrapPhaseFlag true to unwrap phase output values.
   * @param totalSensitFlag true to use stage 0 (total) sensitivity;
   * false to use computed sensitivity.
   * @param stdioFlag true for input from 'stdin', false for input from
   * file.
   * @return An 'OutputGenerator' object, or null if an error occurred (in
   * which case 'getErrorMessage()' may be used to fetch information
   * about the error).
   */
  public OutputGenerator processOneResponse(String stationStr,
                       String channelStr, String networkStr, String siteStr,
                                    Date dateObj, final int outUnitsConvIdx,
                              String fileNameParam, final double [] freqArr,
                            final int startStageNum, final int stopStageNum,
                                                 final boolean useDelayFlag,
            final boolean listInterpOutFlag, final boolean listInterpInFlag,
              final double listInterpTension, final boolean unwrapPhaseFlag,
                     final boolean totalSensitFlag, final boolean stdioFlag)
  {
    return processOneResponse(stationStr,channelStr,networkStr,siteStr,
                dateObj,outUnitsConvIdx,fileNameParam,freqArr,startStageNum,
               stopStageNum,useDelayFlag,listInterpOutFlag,listInterpInFlag,
    listInterpTension,unwrapPhaseFlag,totalSensitFlag,0.0,stdioFlag);
  }

  /**
   * Finds and processes one response, returning the output in an
   * 'OutputGenerator' object.
   * @param stationStr station name to search for, or a null or empty
   * string to accept all station names.
   * @param channelStr channel name to search for, or a null or empty
   * string to accept all channel names.
   * @param networkStr network name to search for, or a null or empty
   * string to accept all network names.
   * @param siteStr site name to search for, or a null or empty string
   * to accept all site names.
   * @param dateObj date to search for, or null to accept all dates.
   * @param outUnitsConvIdx output units conversion index for the
   * requested output units type; one of the '..._UNIT_CONV' values.
   * @param fileNameParam a specific filename (or directory) to use, or
   * a null or empty string for all matching files.
   * @param freqArr an array of frequency values to use.
   * @param startStageNum if greater than zero then the start of the
   * range of stage sequence numbers to use, otherwise all stages
   * are used.
   * @param stopStageNum if greater than zero then the end of the
   * range of stage sequence numbers to use, otherwise only the single
   * stage specified by 'startStageNum' is used.
   * @param useDelayFlag true to use estimated delay in phase calculation.
   * @param listInterpOutFlag true to interpolate amp/phase output
   * from responses containing List blockettes.
   * @param listInterpInFlag true to interpolate amp/phase input from
   * List blockettes in responses (before output is calculated).
   * @param listInterpTension tension value for List-blockette
   * interpolation algorithm.
   * @param stdioFlag true for input from 'stdin', false for input from
   * file.
   * @return An 'OutputGenerator' object, or null if an error occurred (in
   * which case 'getErrorMessage()' may be used to fetch information
   * about the error).
   */
  public OutputGenerator processOneResponse(String stationStr,
                       String channelStr, String networkStr, String siteStr,
                                    Date dateObj, final int outUnitsConvIdx,
                              String fileNameParam, final double [] freqArr,
                            final int startStageNum, final int stopStageNum,
                                                 final boolean useDelayFlag,
            final boolean listInterpOutFlag, final boolean listInterpInFlag,
                    final double listInterpTension, final boolean stdioFlag)
  {
    return processOneResponse(stationStr,channelStr,networkStr,siteStr,
                dateObj,outUnitsConvIdx,fileNameParam,freqArr,startStageNum,
               stopStageNum,useDelayFlag,listInterpOutFlag,listInterpInFlag,
                        listInterpTension,false,false,0.0,stdioFlag);
  }

  /**
   * Finds and processes one response, returning the output in an
   * 'OutputGenerator' object.
   * @param stationStr station name to search for, or a null or empty
   * string to accept all station names.
   * @param channelStr channel name to search for, or a null or empty
   * string to accept all channel names.
   * @param networkStr network name to search for, or a null or empty
   * string to accept all network names.
   * @param siteStr site name to search for, or a null or empty string
   * to accept all site names.
   * @param dateObj date to search for, or null to accept all dates.
   * @param outUnitsConvIdx output units conversion index for the
   * requested output units type; one of the '..._UNIT_CONV' values.
   * @param fileNameParam a specific filename (or directory) to use, or
   * a null or empty string for all matching files.
   * @param freqArr an array of frequency values to use.
   * @param startStageNum if greater than zero then the start of the
   * range of stage sequence numbers to use, otherwise all stages
   * are used.
   * @param stopStageNum if greater than zero then the end of the
   * range of stage sequence numbers to use, otherwise only the single
   * stage specified by 'startStageNum' is used.
   * @param stdioFlag true for input from 'stdin', false for input from
   * file.
   * @return An 'OutputGenerator' object, or null if an error occurred (in
   * which case 'getErrorMessage()' may be used to fetch information
   * about the error).
   */
  public OutputGenerator processOneResponse(String stationStr,
         String channelStr, String networkStr, String siteStr, Date dateObj,
               int outUnitsConvIdx, String fileNameParam, double [] freqArr,
    int startStageNum, int stopStageNum, boolean stdioFlag)
  {
    return processOneResponse(stationStr,channelStr,networkStr,siteStr,
                dateObj,outUnitsConvIdx,fileNameParam,freqArr,startStageNum,
       stopStageNum,false,false,false,0.0,false,false,0.0,stdioFlag);
  }

  /**
   * Finds and processes one response at a single frequency, returning
   * the resulting amplitude value.
   * @param stationStr station name to search for, or a null or empty
   * string to accept all station names.
   * @param channelStr channel name to search for, or a null or empty
   * string to accept all channel names.
   * @param networkStr network name to search for, or a null or empty
   * string to accept all network names.
   * @param siteStr site name to search for, or a null or empty string
   * to accept all site names.
   * @param dateObj date to search for, or null to accept all dates.
   * @param outUnitsConvIdx output units conversion index for the
   * requested output units type; one of the '..._UNIT_CONV' values.
   * @param fileNameParam a specific filename (or directory) to use, or
   * a null or empty string for all matching files.
   * @param freqVal frequency value to use.
   * @param startStageNum if greater than zero then the start of the
   * range of stage sequence numbers to use, otherwise all stages
   * are used.
   * @param stopStageNum if greater than zero then the end of the
   * range of stage sequence numbers to use, otherwise only the single
   * stage specified by 'startStageNum' is used.
   * @param stdioFlag true for input from 'stdin', false for input from
   * file.
   * @return A 'Double' object containing the resulting amplitude value,
   * or null if an error occurred (in which case 'getErrorMessage()' may
   * be used to fetch information about the error).
   */
  public Double getSingleResponseAmpVal(String stationStr,
         String channelStr, String networkStr, String siteStr, Date dateObj,
                  int outUnitsConvIdx, String fileNameParam, double freqVal,
    int startStageNum, int stopStageNum, boolean stdioFlag)
  {
    final OutputGenerator outGenObj;
    final AmpPhaseBlk [] ampPhaseArr;
    if((outGenObj=processOneResponse(stationStr,
                      channelStr,networkStr,siteStr,dateObj,outUnitsConvIdx,
                                     fileNameParam,(new double[] {freqVal}),
                           startStageNum,stopStageNum,stdioFlag)) != null &&
                         (ampPhaseArr=outGenObj.getAmpPhaseArray()) != null)
    {  //output generator and amp/phase array generated OK
      if(ampPhaseArr.length <= 0)
      {  //empty amp/phase array
        setErrorMessage("Unable to calculate amp/phase result");
        return null;
      }
      return new Double(ampPhaseArr[0].amp);     //return first amplitude
    }
    return null;
  }

  /**
   * Finds and processes one response at a single frequency, returning
   * the resulting amplitude value.
   * @param stationStr station name to search for, or a null or empty
   * string to accept all station names.
   * @param channelStr channel name to search for, or a null or empty
   * string to accept all channel names.
   * @param networkStr network name to search for, or a null or empty
   * string to accept all network names.
   * @param siteStr site name to search for, or a null or empty string
   * to accept all site names.
   * @param dateObj date to search for, or null to accept all dates.
   * @param outUnitsConvIdx output units conversion index for the
   * requested output units type; one of the '..._UNIT_CONV' values.
   * @param fileNameParam a specific filename (or directory) to use, or
   * a null or empty string for all matching files.
   * @param freqVal frequency value to use.
   * @return A 'Double' object containing the resulting amplitude value,
   * or null if an error occurred (in which case 'getErrorMessage()' may
   * be used to fetch information about the error).
   */
  public Double getSingleResponseAmpVal(String stationStr,
         String channelStr, String networkStr, String siteStr, Date dateObj,
                  int outUnitsConvIdx, String fileNameParam, double freqVal)
  {
    return getSingleResponseAmpVal(stationStr,channelStr,networkStr,siteStr,
            dateObj,outUnitsConvIdx,fileNameParam,freqVal,0,0,false);
  }

  /**
   * Finds and processes one response at a single frequency, returning
   * the resulting amplitude value.
   * @param stationStr station name to search for, or a null or empty
   * string to accept all station names.
   * @param channelStr channel name to search for, or a null or empty
   * string to accept all channel names.
   * @param networkStr network name to search for, or a null or empty
   * string to accept all network names.
   * @param siteStr site name to search for, or a null or empty string
   * to accept all site names.
   * @param yearVal year to search for, or 0 to accept all dates.
   * @param dayVal day-of-year to search for, or 0 for none.
   * @param outUnitsConvIdx output units conversion index for the
   * requested output units type; one of the '..._UNIT_CONV' values.
   * @param fileNameParam a specific filename (or directory) to use, or
   * a null or empty string for all matching files.
   * @param freqVal frequency value to use.
   * @return A 'Double' object containing the resulting amplitude value,
   * or null if an error occurred (in which case 'getErrorMessage()' may
   * be used to fetch information about the error).
   */
  public Double getSingleResponseAmpVal(String stationStr,
                       String channelStr, String networkStr, String siteStr,
                               int yearVal, int dayVal, int outUnitsConvIdx,
                      String fileNameParam, double freqVal)
  {
    final Date dateObj;
    if(yearVal > 0)
    {  //year value was given; convert year/day to Date object
      final Calendar calObj =
                          Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      calObj.clear();
      calObj.set(Calendar.YEAR,yearVal);
      calObj.set(Calendar.DAY_OF_YEAR, (dayVal > 0) ? dayVal : 1);
      dateObj = calObj.getTime();
    }
    else      //year value not given
      dateObj = null;
    return getSingleResponseAmpVal(stationStr,channelStr,networkStr,siteStr,
            dateObj,outUnitsConvIdx,fileNameParam,freqVal,0,0,false);
  }


  /**
   * Test program.
   * @param args program arguments.
   */
  public static void main(String [] args)
  {
    final String fileNameStr = (args.length > 0) ? args[0] :
                                                        "RESP.UU.ARGU..EHZ";
    final RunDirect runDirectObj = new RunDirect();
    final Double doubleObj;
    if((doubleObj=runDirectObj.getSingleResponseAmpVal("ARGU","EHZ",
                 null,null,2010,1,ACCEL_UNIT_CONV,fileNameStr,5.0)) != null)
    {
      System.out.println(doubleObj);
    }
    else
      System.out.println(runDirectObj.getErrorMessage());
  }
}
