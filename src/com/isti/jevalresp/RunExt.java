//RunExt.java:  Extension of 'Run.java' that accepts all inputs as method
//              parameters (instead of from the command line).
//
//   5/14/2002 -- [ET]  Changed access on 'preprocessParameters()' and
//                      'doGenerateOutput()' from 'protected' to 'public';
//                      created 'checkGenerateFreqArray()' and
//                      'generateResponseOutputs()' methods.
//   7/10/2002 -- [ET]  Changed 'apOutputFlag' to 'respTypeIndex'; added
//                      'multiOutFlag' parameters.
//   7/15/2002 -- [ET]  Removed 'preprocessParameters()' and added
//                      'genOutoutFlag' parameter to 'processAndOutput()'.
//    8/6/2002 -- [ET]  Added 'headerFlag' parameter to 'processAndOutput()',
//                      added support for passing 'logSpacingFlag' parameter
//                      to the 'CallbackProcWrite' constructor.
//   2/25/2005 -- [ET]  Modified to use 'UtilFns.GMT_TIME_ZONE_OBJ'.
//    3/8/2005 -- [ET]  Added optional 'useDelayFlag' parameter to
//                      'processAndOutput()' methods.
//    4/1/2005 -- [ET]  Added optional 'showInputFlag' parameter to
//                      'processAndOutput()' methods.
//  10/31/2005 -- [ET]  Added optional List-blockette interpolation
//                      parameters to 'processAndOutput()' methods.
//   7/15/2009 -- [KF]  Added 'getConsoleOut()' method.
//   5/20/2010 -- [ET]  Added optional parameters 'unwrapPhaseFlag' and
//                      'totalSensitFlag' to 'processAndOutput()' method.
//   5/28/2010 -- [ET]  Modified to use 'enterStartStopStageNums()' method.
//   1/25/2012 -- [ET]  Added support for specification of web-services
//                      server.
//   3/30/2012 -- [ET]  Added optional 'multiSvrFlag' parameter to
//                      'processAndOutput()' methods.
//  10/29/2013 -- [ET]  Added optional 'b62XValueStr' and  'b62XValue'
//                      parameters to 'processAndOutput()' methods.
//

package com.isti.jevalresp;

import java.util.Calendar;
import java.util.Arrays;
import java.io.File;
import com.isti.util.UtilFns;


/**
 * Class RunExt is an extension of 'Run.java' that accepts all inputs as
 * method parameters (instead of from the command line).
 */
public class RunExt extends Run
{

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staListStr string list of station names.
     * @param chaListStr string list of channel names.
     * @param netListStr string list of network names.
     * @param siteListStr string list of location names.
     * @param yearStr numeric string of year value for begin date..
     * @param julianDayStr numeric string of julian day value for begin
     * date.
     * @param timeStr time value to be added to begin date, in
     * HH:MM:SS[.SSS] format.
     * @param minFreqStr numeric string of minimum frequency value.
     * @param maxFreqStr numeric string of maximum frequency value.
     * @param numberFreqsStr numeric string of number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvStr units string (one of 'UNIT_CONV_LONGSTRS[]'
     * values).
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param headerFlag header flag
     * @param endYearStr numeric string of year value for end date.
     * @param endDayStr numeric string of julian day value for end date.
     * @param endTimeStr time value to be added to end date, in
     * HH:MM:SS[.SSS] format.
     * @param verboseFlag true for verbose output.
     * @param startStageStr numeric string of first stage to process.
     * @param stopStageStr numeric string of last stage to process.
     * @param logSpacingStr spacing string (one of 'TYPE_SPACE_LONGSTRS[]'
     * values.)
     * @param apOutputStr output string (one of 'RESP_TYPE_LONGSTRS[]'
     * values.)
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTensionStr numeric string of tension value for
     * List-blockette interpolation algorithm, or null for none.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @param b62XValueStr numeric string of sample value for polynomial
     * blockette (62), or null for none.
     * @param multiSvrFlag true to fetch responses from all specified
     * web-services servers; false fetch no more than one response.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String yearStr,
                       String julianDayStr,String timeStr,String minFreqStr,
                  String maxFreqStr,String numberFreqsStr,boolean stdioFlag,
             String outUnitsConvStr,boolean multiOutFlag,boolean headerFlag,
                       String endYearStr,String endDayStr,String endTimeStr,
               boolean verboseFlag,String startStageStr,String stopStageStr,
              String logSpacingStr,String apOutputStr,String fileNameString,
                                String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                                                String listInterpTensionStr,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag,
                   String b62XValueStr,boolean multiSvrFlag)
  {
              //preprocess (and load) parameters:
    exitStatusValue = 1;     //setup non-zero value in case of error exit
    try
    {         //convert list strings to arrays of strings:
      staNamesArray = (staListStr.trim().length() > 0) ?
            (String [])(UtilFns.listStringToVector(staListStr,',',false).
                                             toArray(new String[0])) : null;
      chaNamesArray = (chaListStr.trim().length() > 0) ?
            (String [])(UtilFns.listStringToVector(chaListStr,',',false).
                                             toArray(new String[0])) : null;
      netNamesArray = (netListStr.trim().length() > 0) ?
            (String [])(UtilFns.listStringToVector(netListStr,',',false).
                                             toArray(new String[0])) : null;
      siteNamesArray = (siteListStr.trim().length() > 0) ?
           (String [])(UtilFns.listStringToVector(siteListStr,',',false).
                                             toArray(new String[0])) : null;
    }
    catch(Exception ex)
    {         //exception occurred; set error message
      setErrorMessage("Internal error:  Unable to create string arrays " +
                                  "in 'RunExt.processAndOutput()':  " + ex);
      return false;
    }

         //process 'Year' parameter value:
    int val;
    beginCalObj = null;           //initialize to no date given
    if(yearStr != null && yearStr.length() > 0 && !yearStr.equals("*"))
    {    //string contains data and is not wildcard string
      try
      {       //convert string to integer:
        val = Integer.parseInt(yearStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error message
        setErrorMessage("Error in 'Begin Year' value (" + yearStr + ")");
        return false;
      }
              //create calender object with year value:
      beginCalObj = Calendar.getInstance(UtilFns.GMT_TIME_ZONE_OBJ);
      beginCalObj.clear();
      beginCalObj.set(Calendar.YEAR,val);
    }
         //process 'JulianDay' parameter value:
    if(julianDayStr != null && julianDayStr.length() > 0 &&
                                                  !julianDayStr.equals("*"))
    {    //string contains data and is not wildcard string
      try
      {       //convert string to integer:
        val = Integer.parseInt(julianDayStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error message
        setErrorMessage(
                 "Error in 'Begin JulianDay' value (" + julianDayStr + ")");
        return false;
      }
               //if calender object OK then enter j-day value:
      if(beginCalObj != null)
        beginCalObj.set(Calendar.DAY_OF_YEAR,val);
    }
         //process 'Time' parameter:
    if(timeStr != null && timeStr.length() > 0 && !timeStr.equals("*") &&
                                                        beginCalObj != null)
    {    //both 'time' and 'year' parameters were specified
              //process time of day ("HH:MM:SS") string:
      if(!addTimeToCalendar(beginCalObj,timeStr))
      {        //error processing string; set error message
        setErrorMessage("Unable to interpret 'Begin Time' value (\"" +
                                                           timeStr + "\")");
        return false;
      }
    }

         //process min/max/number frequencies:
    if(minFreqStr != null && minFreqStr.length() > 0)
    {    //min-freq string contains data
      try
      {       //convert string to double:
        minFreqValue = Double.parseDouble(minFreqStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error code and message
        setErrorMessage("Error in 'Min Freq' value (" + minFreqStr + ")");
        return false;
      }
      minimumFrequencyFlag = true;       //indicate minimum frequency entered
    }
    if(maxFreqStr != null && maxFreqStr.length() > 0 &&
       minimumFrequencyFlag)
    {    //max-freq string contains data and minimum frequency entered
      try
      {       //convert string to double:
        maxFreqValue = Double.parseDouble(maxFreqStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error code and message
        setErrorMessage("Error in 'Max Freq' value (" + maxFreqStr + ")");
        return false;
      }
    }
    if(numberFreqsStr != null && numberFreqsStr.length() > 0 &&
       minimumFrequencyFlag)
    {    //number-freqs string contains data and minimum frequency entered
      try
      {       //convert string to integer:
        if((numberFreqs=Integer.parseInt(numberFreqsStr)) < 1)
        {     //value out of range
          setErrorMessage("Value for 'numFreqs' (" + numberFreqs +
                                                          ") out of range");
          return false;
        }
      }
      catch(NumberFormatException ex)
      {       //error converting; set error code and message
        setErrorMessage(
                      "Error in 'numFreqs' value (" + numberFreqsStr + ")");
        return false;
      }
    }

         //process 'Units' parameter:
    if(outUnitsConvStr != null && outUnitsConvStr.length() > 0)
    {    //string contains data; find match
      if((outUnitsConvIdx=Arrays.asList(
                        UNIT_CONV_LONGSTRS).indexOf(outUnitsConvStr)) < 0 &&
                                             (outUnitsConvIdx=Arrays.asList(
                UNIT_CONV_STRS).indexOf(outUnitsConvStr.toLowerCase())) < 0)
      {  //cannot convert string to index value; set error message
        setErrorMessage("Error in 'Units' value (\"" + outUnitsConvStr +
                                                                     "\")");
        return false;
      }
    }
    else      //no data in string
      outUnitsConvIdx = UNIT_CONV_DEFIDX;   //use default index value

         //process 'Frequency Spacing' parameter:
    if(logSpacingStr != null && logSpacingStr.length() > 0)
    {    //string contains data; find match
      if(logSpacingStr == null || ((val=Arrays.asList(
                         TYPE_SPACE_LONGSTRS).indexOf(logSpacingStr)) < 0 &&
                                                         (val=Arrays.asList(
                TYPE_SPACE_STRS).indexOf(logSpacingStr.toLowerCase())) < 0))
      {  //cannot convert string to index value; set error message
        setErrorMessage("Error in 'Frequency Spacing' value (\"" +
                                                     logSpacingStr + "\")");
        return false;
      }
    }
    else      //no data in string
      val = 0;     //use default index value
                        //set flag true if 'log' spacing selected:
    logSpacingFlag = (val == 0);

         //process 'Response Type' parameter:
    if(apOutputStr != null && apOutputStr.length() > 0)
    {    //string contains data; find match
      if(apOutputStr == null || ((respTypeIndex=Arrays.asList(
                            RESP_TYPE_LONGSTRS).indexOf(apOutputStr)) < 0 &&
                                               (respTypeIndex=Arrays.asList(
                   RESP_TYPE_STRS).indexOf(apOutputStr.toLowerCase())) < 0))
      {  //cannot convert string to index value; set error message
        setErrorMessage("Error in 'Response Type' value (\"" +
                                                       apOutputStr + "\")");
        return false;
      }
    }

         //process "Start Stage" parameter:
    Integer startIntObj;
    if(startStageStr != null && startStageStr.length() > 0)
    {    //numeric string value entered
      try
      {       //convert String to Integer object:
        startIntObj = Integer.valueOf(startStageStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error code and message
        setErrorMessage(
                    "Error in 'Start Stage' value (" + startStageStr + ")");
        return false;
      }
    }
    else      //value not entered
      startIntObj = null;
         //process "Stop Stage" parameter:
    Integer stopIntObj;
    if(stopStageStr != null && stopStageStr.length() > 0)
    {    //numeric string value entered
      try
      {       //convert String to Integer object:
        stopIntObj = Integer.valueOf(stopStageStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error code and message
        setErrorMessage(
                      "Error in 'Stop Stage' value (" + stopStageStr + ")");
        return false;
      }
    }
    else      //value not entered
      stopIntObj = null;
              //enter given start/stop values:
    enterStartStopStageNums(startIntObj,stopIntObj);

         //process end-year parameter value:
    endCalObj = null;           //initialize to no date given
    if(endYearStr != null && endYearStr.length() > 0)
    {    //string contains data and is not wildcard string
      try
      {       //convert string to integer:
        val = Integer.parseInt(endYearStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error message
        setErrorMessage("Error in 'End Year' value (" + endYearStr + ")");
        return false;
      }
              //create calender object with year value:
      endCalObj = Calendar.getInstance(UtilFns.GMT_TIME_ZONE_OBJ);
      endCalObj.clear();
      endCalObj.set(Calendar.YEAR,val);
    }
         //process end-day parameter value:
    if(endDayStr != null && endDayStr.length() > 0)
    {    //string contains data and is not wildcard string
      try
      {       //convert string to integer:
        val = Integer.parseInt(endDayStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error message
        setErrorMessage(
                      "Error in 'End JulianDay' value (" + endDayStr + ")");
        return false;
      }
               //if calender object OK then enter j-day value:
      if(endCalObj != null)
        endCalObj.set(Calendar.DAY_OF_YEAR,val);
    }
         //process end-time parameter:
    if(endTimeStr != null && endTimeStr.length() > 0 && endCalObj != null)
    {    //both 'time' and 'year' parameters were specified
              //process time of day ("HH:MM:SS") string:
      if(!addTimeToCalendar(endCalObj,endTimeStr))
      {        //error processing string; set error message
        setErrorMessage("Unable to interpret 'End Time' value (\"" +
                                                        endTimeStr + "\")");
        return false;
      }
    }

    if(listInterpTensionStr != null && listInterpTensionStr.length() > 0)
    {    //List-blockette interpolation string contains data
      try
      {       //convert string to double:
        listInterpTension = Double.parseDouble(listInterpTensionStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error code and message
        setErrorMessage("Error in 'listInterpTension' value (" +
                                                listInterpTensionStr + ")");
        return false;
      }
    }

    if(b62XValueStr != null && b62XValueStr.length() > 0)
    {    //B62-value string contains data
      try
      {       //convert string to double:
        b62XValue = Double.parseDouble(b62XValueStr);
      }
      catch(NumberFormatException ex)
      {       //error converting; set error code and message
        setErrorMessage("Error in 'b62_x value' value (" +
                                                        b62XValueStr + ")");
        return false;
      }
    }
    else  //B62-value string empty
      b62XValue = 0.0;            //enter default value

    exitStatusValue = 0;     //clear exit status value

    this.stdioFlag = stdioFlag;             //save other parameters
    this.multiOutFlag = multiOutFlag;
    this.headerFlag = headerFlag;
    this.verboseFlag = verboseFlag;
    this.fileNameString = fileNameString;
    this.propsFileString = propsFileString;
    this.outputDirectory = outputDirectory;
    this.useDelayFlag = useDelayFlag;
    this.showInputFlag = showInputFlag;
    this.listInterpOutFlag = listInterpOutFlag;
    this.listInterpInFlag = listInterpInFlag;
    this.unwrapPhaseFlag = unwrapPhaseFlag;
    this.totalSensitFlag = totalSensitFlag;
    this.multiSvrFlag = multiSvrFlag;

    if(!genOutputFlag)            //if output flag not set then
      return true;                //return OK flag

              //generate output and return status flag:
    return doGenerateOutput();
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staListStr string list of station names.
     * @param chaListStr string list of channel names.
     * @param netListStr string list of network names.
     * @param siteListStr string list of location names.
     * @param yearStr numeric string of year value for begin date..
     * @param julianDayStr numeric string of julian day value for begin
     * date.
     * @param timeStr time value to be added to begin date, in
     * HH:MM:SS[.SSS] format.
     * @param minFreqStr numeric string of minimum frequency value.
     * @param maxFreqStr numeric string of maximum frequency value.
     * @param numberFreqsStr numeric string of number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvStr units string (one of 'UNIT_CONV_LONGSTRS[]'
     * values).
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param headerFlag header flag
     * @param endYearStr numeric string of year value for end date.
     * @param endDayStr numeric string of julian day value for end date.
     * @param endTimeStr time value to be added to end date, in
     * HH:MM:SS[.SSS] format.
     * @param verboseFlag true for verbose output.
     * @param startStageStr numeric string of first stage to process.
     * @param stopStageStr numeric string of last stage to process.
     * @param logSpacingStr spacing string (one of 'TYPE_SPACE_LONGSTRS[]'
     * values.)
     * @param apOutputStr output string (one of 'RESP_TYPE_LONGSTRS[]'
     * values.)
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTensionStr numeric string of tension value for
     * List-blockette interpolation algorithm.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @param multiSvrFlag true to fetch responses from all specified
     * web-services servers; false fetch no more than one response.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String yearStr,
                       String julianDayStr,String timeStr,String minFreqStr,
                  String maxFreqStr,String numberFreqsStr,boolean stdioFlag,
             String outUnitsConvStr,boolean multiOutFlag,boolean headerFlag,
                       String endYearStr,String endDayStr,String endTimeStr,
               boolean verboseFlag,String startStageStr,String stopStageStr,
              String logSpacingStr,String apOutputStr,String fileNameString,
                                String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                                                String listInterpTensionStr,
                            boolean unwrapPhaseFlag,boolean totalSensitFlag,
                                      boolean multiSvrFlag)
  {
    return processAndOutput(staListStr,chaListStr,netListStr,siteListStr,
          yearStr,julianDayStr,timeStr,minFreqStr,maxFreqStr,numberFreqsStr,
               stdioFlag,outUnitsConvStr,multiOutFlag,headerFlag,endYearStr,
                endDayStr,endTimeStr,verboseFlag,startStageStr,stopStageStr,
                   logSpacingStr,apOutputStr,fileNameString,propsFileString,
                   outputDirectory,genOutputFlag,useDelayFlag,showInputFlag,
                    listInterpOutFlag,listInterpInFlag,listInterpTensionStr,
                 unwrapPhaseFlag,totalSensitFlag,null,multiSvrFlag);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staListStr string list of station names.
     * @param chaListStr string list of channel names.
     * @param netListStr string list of network names.
     * @param siteListStr string list of location names.
     * @param yearStr numeric string of year value for begin date..
     * @param julianDayStr numeric string of julian day value for begin
     * date.
     * @param timeStr time value to be added to begin date, in
     * HH:MM:SS[.SSS] format.
     * @param minFreqStr numeric string of minimum frequency value.
     * @param maxFreqStr numeric string of maximum frequency value.
     * @param numberFreqsStr numeric string of number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvStr units string (one of 'UNIT_CONV_LONGSTRS[]'
     * values).
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param headerFlag header flag
     * @param endYearStr numeric string of year value for end date.
     * @param endDayStr numeric string of julian day value for end date.
     * @param endTimeStr time value to be added to end date, in
     * HH:MM:SS[.SSS] format.
     * @param verboseFlag true for verbose output.
     * @param startStageStr numeric string of first stage to process.
     * @param stopStageStr numeric string of last stage to process.
     * @param logSpacingStr spacing string (one of 'TYPE_SPACE_LONGSTRS[]'
     * values.)
     * @param apOutputStr output string (one of 'RESP_TYPE_LONGSTRS[]'
     * values.)
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTensionStr numeric string of tension value for
     * List-blockette interpolation algorithm.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @param b62XValueStr numeric string of sample value for polynomial
     * blockette (62), or null for none.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String yearStr,
                       String julianDayStr,String timeStr,String minFreqStr,
                  String maxFreqStr,String numberFreqsStr,boolean stdioFlag,
             String outUnitsConvStr,boolean multiOutFlag,boolean headerFlag,
                       String endYearStr,String endDayStr,String endTimeStr,
               boolean verboseFlag,String startStageStr,String stopStageStr,
              String logSpacingStr,String apOutputStr,String fileNameString,
                                String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                        String listInterpTensionStr,boolean unwrapPhaseFlag,
                boolean totalSensitFlag,String b62XValueStr)
  {
    return processAndOutput(staListStr,chaListStr,netListStr,siteListStr,
          yearStr,julianDayStr,timeStr,minFreqStr,maxFreqStr,numberFreqsStr,
               stdioFlag,outUnitsConvStr,multiOutFlag,headerFlag,endYearStr,
                endDayStr,endTimeStr,verboseFlag,startStageStr,stopStageStr,
                   logSpacingStr,apOutputStr,fileNameString,propsFileString,
                   outputDirectory,genOutputFlag,useDelayFlag,showInputFlag,
                    listInterpOutFlag,listInterpInFlag,listInterpTensionStr,
                 unwrapPhaseFlag,totalSensitFlag,b62XValueStr,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staListStr string list of station names.
     * @param chaListStr string list of channel names.
     * @param netListStr string list of network names.
     * @param siteListStr string list of location names.
     * @param yearStr numeric string of year value for begin date..
     * @param julianDayStr numeric string of julian day value for begin
     * date.
     * @param timeStr time value to be added to begin date, in
     * HH:MM:SS[.SSS] format.
     * @param minFreqStr numeric string of minimum frequency value.
     * @param maxFreqStr numeric string of maximum frequency value.
     * @param numberFreqsStr numeric string of number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvStr units string (one of 'UNIT_CONV_LONGSTRS[]'
     * values).
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param headerFlag header flag
     * @param endYearStr numeric string of year value for end date.
     * @param endDayStr numeric string of julian day value for end date.
     * @param endTimeStr time value to be added to end date, in
     * HH:MM:SS[.SSS] format.
     * @param verboseFlag true for verbose output.
     * @param startStageStr numeric string of first stage to process.
     * @param stopStageStr numeric string of last stage to process.
     * @param logSpacingStr spacing string (one of 'TYPE_SPACE_LONGSTRS[]'
     * values.)
     * @param apOutputStr output string (one of 'RESP_TYPE_LONGSTRS[]'
     * values.)
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTensionStr numeric string of tension value for
     * List-blockette interpolation algorithm.
     * @param unwrapPhaseFlag true to unwrap phase output values.
     * @param totalSensitFlag true to use stage 0 (total) sensitivity;
     * false to use computed sensitivity.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String yearStr,
                       String julianDayStr,String timeStr,String minFreqStr,
                  String maxFreqStr,String numberFreqsStr,boolean stdioFlag,
             String outUnitsConvStr,boolean multiOutFlag,boolean headerFlag,
                       String endYearStr,String endDayStr,String endTimeStr,
               boolean verboseFlag,String startStageStr,String stopStageStr,
              String logSpacingStr,String apOutputStr,String fileNameString,
                                String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                                                String listInterpTensionStr,
            boolean unwrapPhaseFlag,boolean totalSensitFlag)
  {
    return processAndOutput(staListStr,chaListStr,netListStr,siteListStr,
          yearStr,julianDayStr,timeStr,minFreqStr,maxFreqStr,numberFreqsStr,
               stdioFlag,outUnitsConvStr,multiOutFlag,headerFlag,endYearStr,
                endDayStr,endTimeStr,verboseFlag,startStageStr,stopStageStr,
                   logSpacingStr,apOutputStr,fileNameString,propsFileString,
                   outputDirectory,genOutputFlag,useDelayFlag,showInputFlag,
                    listInterpOutFlag,listInterpInFlag,listInterpTensionStr,
                         unwrapPhaseFlag,totalSensitFlag,null,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staListStr string list of station names.
     * @param chaListStr string list of channel names.
     * @param netListStr string list of network names.
     * @param siteListStr string list of location names.
     * @param yearStr numeric string of year value for begin date..
     * @param julianDayStr numeric string of julian day value for begin
     * date.
     * @param timeStr time value to be added to begin date, in
     * HH:MM:SS[.SSS] format.
     * @param minFreqStr numeric string of minimum frequency value.
     * @param maxFreqStr numeric string of maximum frequency value.
     * @param numberFreqsStr numeric string of number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvStr units string (one of 'UNIT_CONV_LONGSTRS[]'
     * values).
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param headerFlag header flag
     * @param endYearStr numeric string of year value for end date.
     * @param endDayStr numeric string of julian day value for end date.
     * @param endTimeStr time value to be added to end date, in
     * HH:MM:SS[.SSS] format.
     * @param verboseFlag true for verbose output.
     * @param startStageStr numeric string of first stage to process.
     * @param stopStageStr numeric string of last stage to process.
     * @param logSpacingStr spacing string (one of 'TYPE_SPACE_LONGSTRS[]'
     * values.)
     * @param apOutputStr output string (one of 'RESP_TYPE_LONGSTRS[]'
     * values.)
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTensionStr numeric string of tension value for
     * List-blockette interpolation algorithm.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String yearStr,
                       String julianDayStr,String timeStr,String minFreqStr,
                  String maxFreqStr,String numberFreqsStr,boolean stdioFlag,
             String outUnitsConvStr,boolean multiOutFlag,boolean headerFlag,
                       String endYearStr,String endDayStr,String endTimeStr,
               boolean verboseFlag,String startStageStr,String stopStageStr,
              String logSpacingStr,String apOutputStr,String fileNameString,
                                String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                                String listInterpTensionStr)
  {
    return processAndOutput(staListStr,chaListStr,netListStr,siteListStr,
          yearStr,julianDayStr,timeStr,minFreqStr,maxFreqStr,numberFreqsStr,
               stdioFlag,outUnitsConvStr,multiOutFlag,headerFlag,endYearStr,
                endDayStr,endTimeStr,verboseFlag,startStageStr,stopStageStr,
                   logSpacingStr,apOutputStr,fileNameString,propsFileString,
                   outputDirectory,genOutputFlag,useDelayFlag,showInputFlag,
                    listInterpOutFlag,listInterpInFlag,listInterpTensionStr,
                                             false,false,null,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staListStr string list of station names.
     * @param chaListStr string list of channel names.
     * @param netListStr string list of network names.
     * @param siteListStr string list of location names.
     * @param yearStr numeric string of year value for begin date..
     * @param julianDayStr numeric string of julian day value for begin
     * date.
     * @param timeStr time value to be added to begin date, in
     * HH:MM:SS[.SSS] format.
     * @param minFreqStr numeric string of minimum frequency value.
     * @param maxFreqStr numeric string of maximum frequency value.
     * @param numberFreqsStr numeric string of number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvStr units string (one of 'UNIT_CONV_LONGSTRS[]'
     * values).
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param headerFlag header flag
     * @param endYearStr numeric string of year value for end date.
     * @param endDayStr numeric string of julian day value for end date.
     * @param endTimeStr time value to be added to end date, in
     * HH:MM:SS[.SSS] format.
     * @param verboseFlag true for verbose output.
     * @param startStageStr numeric string of first stage to process.
     * @param stopStageStr numeric string of last stage to process.
     * @param logSpacingStr spacing string (one of 'TYPE_SPACE_LONGSTRS[]'
     * values.)
     * @param apOutputStr output string (one of 'RESP_TYPE_LONGSTRS[]'
     * values.)
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String yearStr,
                       String julianDayStr,String timeStr,String minFreqStr,
                  String maxFreqStr,String numberFreqsStr,boolean stdioFlag,
             String outUnitsConvStr,boolean multiOutFlag,boolean headerFlag,
                       String endYearStr,String endDayStr,String endTimeStr,
               boolean verboseFlag,String startStageStr,String stopStageStr,
              String logSpacingStr,String apOutputStr,String fileNameString,
                                String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag)
  {
    return processAndOutput(staListStr,chaListStr,netListStr,siteListStr,
          yearStr,julianDayStr,timeStr,minFreqStr,maxFreqStr,numberFreqsStr,
               stdioFlag,outUnitsConvStr,multiOutFlag,headerFlag,endYearStr,
                endDayStr,endTimeStr,verboseFlag,startStageStr,stopStageStr,
                   logSpacingStr,apOutputStr,fileNameString,propsFileString,
                   outputDirectory,genOutputFlag,useDelayFlag,showInputFlag,
                            false,false,null,false,false,null,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staListStr string list of station names.
     * @param chaListStr string list of channel names.
     * @param netListStr string list of network names.
     * @param siteListStr string list of location names.
     * @param yearStr numeric string of year value for begin date..
     * @param julianDayStr numeric string of julian day value for begin
     * date.
     * @param timeStr time value to be added to begin date, in
     * HH:MM:SS[.SSS] format.
     * @param minFreqStr numeric string of minimum frequency value.
     * @param maxFreqStr numeric string of maximum frequency value.
     * @param numberFreqsStr numeric string of number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvStr units string (one of 'UNIT_CONV_LONGSTRS[]'
     * values).
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param headerFlag header flag
     * @param endYearStr numeric string of year value for end date.
     * @param endDayStr numeric string of julian day value for end date.
     * @param endTimeStr time value to be added to end date, in
     * HH:MM:SS[.SSS] format.
     * @param verboseFlag true for verbose output.
     * @param startStageStr numeric string of first stage to process.
     * @param stopStageStr numeric string of last stage to process.
     * @param logSpacingStr spacing string (one of 'TYPE_SPACE_LONGSTRS[]'
     * values.)
     * @param apOutputStr output string (one of 'RESP_TYPE_LONGSTRS[]'
     * values.)
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String staListStr,String chaListStr,
                        String netListStr,String siteListStr,String yearStr,
                       String julianDayStr,String timeStr,String minFreqStr,
                  String maxFreqStr,String numberFreqsStr,boolean stdioFlag,
             String outUnitsConvStr,boolean multiOutFlag,boolean headerFlag,
                       String endYearStr,String endDayStr,String endTimeStr,
               boolean verboseFlag,String startStageStr,String stopStageStr,
              String logSpacingStr,String apOutputStr,String fileNameString,
          String propsFileString,File outputDirectory,boolean genOutputFlag)
  {
    return processAndOutput(staListStr,chaListStr,netListStr,siteListStr,
          yearStr,julianDayStr,timeStr,minFreqStr,maxFreqStr,numberFreqsStr,
               stdioFlag,outUnitsConvStr,multiOutFlag,headerFlag,endYearStr,
                endDayStr,endTimeStr,verboseFlag,startStageStr,stopStageStr,
                   logSpacingStr,apOutputStr,fileNameString,propsFileString,
                          outputDirectory,genOutputFlag,false,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staNamesArray array of station names.
     * @param chaNamesArray array of channel names.
     * @param netNamesArray array of network names.
     * @param siteNamesArray array of location names.
     * @param beginCalObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endCalObj the end of a date range to search for, or
     * null for no end date.
     * @param minFreqValue minimum frequency value.
     * @param maxFreqValue maximum frequency value.
     * @param numberFreqs number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvIdx units index value.
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param verboseFlag true for verbose output.
     * @param startStageNum first stage to process.
     * @param stopStageNum last stage to process.
     * @param logSpacingFlag true for log frequency spacing.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
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
     * @param multiSvrFlag true to fetch responses from all specified
     * web-services servers; false fetch no more than one response.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String [] staNamesArray,
                            String [] chaNamesArray,String [] netNamesArray,
                              String [] siteNamesArray,Calendar beginCalObj,
                 Calendar endCalObj,double minFreqValue,double maxFreqValue,
                      int numberFreqs,boolean stdioFlag,int outUnitsConvIdx,
                 boolean multiOutFlag,boolean verboseFlag,int startStageNum,
                  int stopStageNum,boolean logSpacingFlag,int respTypeIndex,
          String fileNameString,String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
              boolean totalSensitFlag,double b62XValue,boolean multiSvrFlag)
  {
    this.staNamesArray = staNamesArray;          //save parameter values
    this.chaNamesArray = chaNamesArray;
    this.netNamesArray = netNamesArray;
    this.siteNamesArray = siteNamesArray;
    this.beginCalObj = beginCalObj;
    this.endCalObj = endCalObj;
    this.minFreqValue = minFreqValue;
    this.maxFreqValue = maxFreqValue;
    this.numberFreqs = numberFreqs;
    this.stdioFlag = stdioFlag;
    this.outUnitsConvIdx = outUnitsConvIdx;
    this.multiOutFlag = multiOutFlag;
    this.verboseFlag = verboseFlag;
    this.startStageNum = startStageNum;
    this.stopStageNum = stopStageNum;
    this.logSpacingFlag = logSpacingFlag;
    this.respTypeIndex = respTypeIndex;
    this.fileNameString = fileNameString;
    this.propsFileString = propsFileString;
    this.outputDirectory = outputDirectory;
    this.useDelayFlag = useDelayFlag;
    this.showInputFlag = showInputFlag;
    this.listInterpOutFlag = listInterpOutFlag;
    this.listInterpInFlag = listInterpInFlag;
    this.listInterpTension = listInterpTension;
    this.unwrapPhaseFlag = unwrapPhaseFlag;
    this.totalSensitFlag = totalSensitFlag;
    this.b62XValue = b62XValue;
    this.multiSvrFlag = multiSvrFlag;

    if(!genOutputFlag)            //if output flag not set then
      return true;                //return OK flag

              //process, output and return status flag:
    return doGenerateOutput();
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staNamesArray array of station names.
     * @param chaNamesArray array of channel names.
     * @param netNamesArray array of network names.
     * @param siteNamesArray array of location names.
     * @param beginCalObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endCalObj the end of a date range to search for, or
     * null for no end date.
     * @param minFreqValue minimum frequency value.
     * @param maxFreqValue maximum frequency value.
     * @param numberFreqs number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvIdx units index value.
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param verboseFlag true for verbose output.
     * @param startStageNum first stage to process.
     * @param stopStageNum last stage to process.
     * @param logSpacingFlag true for log frequency spacing.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
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
     * @param multiSvrFlag true to fetch responses from all specified
     * web-services servers; false fetch no more than one response.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String [] staNamesArray,
                            String [] chaNamesArray,String [] netNamesArray,
                              String [] siteNamesArray,Calendar beginCalObj,
                 Calendar endCalObj,double minFreqValue,double maxFreqValue,
                      int numberFreqs,boolean stdioFlag,int outUnitsConvIdx,
                 boolean multiOutFlag,boolean verboseFlag,int startStageNum,
                  int stopStageNum,boolean logSpacingFlag,int respTypeIndex,
          String fileNameString,String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
               boolean totalSensitFlag,boolean multiSvrFlag)
  {
    return processAndOutput(staNamesArray,chaNamesArray,netNamesArray,
             siteNamesArray,beginCalObj,endCalObj,minFreqValue,maxFreqValue,
             numberFreqs,stdioFlag,outUnitsConvIdx,multiOutFlag,verboseFlag,
                    startStageNum,stopStageNum,logSpacingFlag,respTypeIndex,
               fileNameString,propsFileString,outputDirectory,genOutputFlag,
                               useDelayFlag,showInputFlag,listInterpOutFlag,
                         listInterpInFlag,listInterpTension,unwrapPhaseFlag,
                                   totalSensitFlag,0.0,multiSvrFlag);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staNamesArray array of station names.
     * @param chaNamesArray array of channel names.
     * @param netNamesArray array of network names.
     * @param siteNamesArray array of location names.
     * @param beginCalObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endCalObj the end of a date range to search for, or
     * null for no end date.
     * @param minFreqValue minimum frequency value.
     * @param maxFreqValue maximum frequency value.
     * @param numberFreqs number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvIdx units index value.
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param verboseFlag true for verbose output.
     * @param startStageNum first stage to process.
     * @param stopStageNum last stage to process.
     * @param logSpacingFlag true for log frequency spacing.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
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
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String [] staNamesArray,
                            String [] chaNamesArray,String [] netNamesArray,
                              String [] siteNamesArray,Calendar beginCalObj,
                 Calendar endCalObj,double minFreqValue,double maxFreqValue,
                      int numberFreqs,boolean stdioFlag,int outUnitsConvIdx,
                 boolean multiOutFlag,boolean verboseFlag,int startStageNum,
                  int stopStageNum,boolean logSpacingFlag,int respTypeIndex,
          String fileNameString,String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
                   boolean totalSensitFlag,double b62XValue)
  {
    return processAndOutput(staNamesArray,chaNamesArray,netNamesArray,
             siteNamesArray,beginCalObj,endCalObj,minFreqValue,maxFreqValue,
             numberFreqs,stdioFlag,outUnitsConvIdx,multiOutFlag,verboseFlag,
                    startStageNum,stopStageNum,logSpacingFlag,respTypeIndex,
               fileNameString,propsFileString,outputDirectory,genOutputFlag,
                               useDelayFlag,showInputFlag,listInterpOutFlag,
                         listInterpInFlag,listInterpTension,unwrapPhaseFlag,
                                    totalSensitFlag,b62XValue,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staNamesArray array of station names.
     * @param chaNamesArray array of channel names.
     * @param netNamesArray array of network names.
     * @param siteNamesArray array of location names.
     * @param beginCalObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endCalObj the end of a date range to search for, or
     * null for no end date.
     * @param minFreqValue minimum frequency value.
     * @param maxFreqValue maximum frequency value.
     * @param numberFreqs number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvIdx units index value.
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param verboseFlag true for verbose output.
     * @param startStageNum first stage to process.
     * @param stopStageNum last stage to process.
     * @param logSpacingFlag true for log frequency spacing.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
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
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String [] staNamesArray,
                            String [] chaNamesArray,String [] netNamesArray,
                              String [] siteNamesArray,Calendar beginCalObj,
                 Calendar endCalObj,double minFreqValue,double maxFreqValue,
                      int numberFreqs,boolean stdioFlag,int outUnitsConvIdx,
                 boolean multiOutFlag,boolean verboseFlag,int startStageNum,
                  int stopStageNum,boolean logSpacingFlag,int respTypeIndex,
          String fileNameString,String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
                                    boolean totalSensitFlag)
  {
    return processAndOutput(staNamesArray,chaNamesArray,netNamesArray,
             siteNamesArray,beginCalObj,endCalObj,minFreqValue,maxFreqValue,
             numberFreqs,stdioFlag,outUnitsConvIdx,multiOutFlag,verboseFlag,
                    startStageNum,stopStageNum,logSpacingFlag,respTypeIndex,
               fileNameString,propsFileString,outputDirectory,genOutputFlag,
                               useDelayFlag,showInputFlag,listInterpOutFlag,
                         listInterpInFlag,listInterpTension,unwrapPhaseFlag,
                                          totalSensitFlag,0.0,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staNamesArray array of station names.
     * @param chaNamesArray array of channel names.
     * @param netNamesArray array of network names.
     * @param siteNamesArray array of location names.
     * @param beginCalObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endCalObj the end of a date range to search for, or
     * null for no end date.
     * @param minFreqValue minimum frequency value.
     * @param maxFreqValue maximum frequency value.
     * @param numberFreqs number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvIdx units index value.
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param verboseFlag true for verbose output.
     * @param startStageNum first stage to process.
     * @param stopStageNum last stage to process.
     * @param logSpacingFlag true for log frequency spacing.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @param listInterpOutFlag true to interpolate amp/phase output
     * from responses containing List blockettes.
     * @param listInterpInFlag true to interpolate amp/phase input from
     * List blockettes in responses (before output is calculated).
     * @param listInterpTension tension value for List-blockette
     * interpolation algorithm.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String [] staNamesArray,
                            String [] chaNamesArray,String [] netNamesArray,
                              String [] siteNamesArray,Calendar beginCalObj,
                 Calendar endCalObj,double minFreqValue,double maxFreqValue,
                      int numberFreqs,boolean stdioFlag,int outUnitsConvIdx,
                 boolean multiOutFlag,boolean verboseFlag,int startStageNum,
                  int stopStageNum,boolean logSpacingFlag,int respTypeIndex,
          String fileNameString,String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                                   double listInterpTension)
  {
    return processAndOutput(staNamesArray,chaNamesArray,netNamesArray,
             siteNamesArray,beginCalObj,endCalObj,minFreqValue,maxFreqValue,
             numberFreqs,stdioFlag,outUnitsConvIdx,multiOutFlag,verboseFlag,
                    startStageNum,stopStageNum,logSpacingFlag,respTypeIndex,
               fileNameString,propsFileString,outputDirectory,genOutputFlag,
                               useDelayFlag,showInputFlag,listInterpOutFlag,
           listInterpInFlag,listInterpTension,false,false,0.0,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staNamesArray array of station names.
     * @param chaNamesArray array of channel names.
     * @param netNamesArray array of network names.
     * @param siteNamesArray array of location names.
     * @param beginCalObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endCalObj the end of a date range to search for, or
     * null for no end date.
     * @param minFreqValue minimum frequency value.
     * @param maxFreqValue maximum frequency value.
     * @param numberFreqs number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvIdx units index value.
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param verboseFlag true for verbose output.
     * @param startStageNum first stage to process.
     * @param stopStageNum last stage to process.
     * @param logSpacingFlag true for log frequency spacing.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @param useDelayFlag true to use estimated delay in phase calculation.
     * @param showInputFlag true to show RESP input text (sent to stdout).
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String [] staNamesArray,
                            String [] chaNamesArray,String [] netNamesArray,
                              String [] siteNamesArray,Calendar beginCalObj,
                 Calendar endCalObj,double minFreqValue,double maxFreqValue,
                      int numberFreqs,boolean stdioFlag,int outUnitsConvIdx,
                 boolean multiOutFlag,boolean verboseFlag,int startStageNum,
                  int stopStageNum,boolean logSpacingFlag,int respTypeIndex,
          String fileNameString,String propsFileString,File outputDirectory,
           boolean genOutputFlag,boolean useDelayFlag,boolean showInputFlag)
  {
    return processAndOutput(staNamesArray,chaNamesArray,netNamesArray,
             siteNamesArray,beginCalObj,endCalObj,minFreqValue,maxFreqValue,
             numberFreqs,stdioFlag,outUnitsConvIdx,multiOutFlag,verboseFlag,
                    startStageNum,stopStageNum,logSpacingFlag,respTypeIndex,
               fileNameString,propsFileString,outputDirectory,genOutputFlag,
   useDelayFlag,showInputFlag,false,false,0.0,false,false,0.0,false);
  }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param staNamesArray array of station names.
     * @param chaNamesArray array of channel names.
     * @param netNamesArray array of network names.
     * @param siteNamesArray array of location names.
     * @param beginCalObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endCalObj the end of a date range to search for, or
     * null for no end date.
     * @param minFreqValue minimum frequency value.
     * @param maxFreqValue maximum frequency value.
     * @param numberFreqs number of frequencies.
     * @param stdioFlag true for stdin/stdout I/O.
     * @param outUnitsConvIdx units index value.
     * @param multiOutFlag true to enable multiple response outputs with
     * the same 'net.sta.loc.cha' code.
     * @param verboseFlag true for verbose output.
     * @param startStageNum first stage to process.
     * @param stopStageNum last stage to process.
     * @param logSpacingFlag true for log frequency spacing.
     * @param respTypeIndex index value indicating type of output (separate
     * amp/phase file, complex-spectra file or single amp/phase file).
     * @param fileNameString file/path name entered.
     * @param propsFileString server-properties file name.
     * @param outputDirectory output directory
     * @param genOutputFlag if true then output will be generated.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean processAndOutput(String [] staNamesArray,
                            String [] chaNamesArray,String [] netNamesArray,
                              String [] siteNamesArray,Calendar beginCalObj,
                 Calendar endCalObj,double minFreqValue,double maxFreqValue,
                      int numberFreqs,boolean stdioFlag,int outUnitsConvIdx,
                 boolean multiOutFlag,boolean verboseFlag,int startStageNum,
                  int stopStageNum,boolean logSpacingFlag,int respTypeIndex,
                               String fileNameString,String propsFileString,
                 File outputDirectory,boolean genOutputFlag)
  {
    return processAndOutput(staNamesArray,chaNamesArray,netNamesArray,
             siteNamesArray,beginCalObj,endCalObj,minFreqValue,maxFreqValue,
             numberFreqs,stdioFlag,outUnitsConvIdx,multiOutFlag,verboseFlag,
                    startStageNum,stopStageNum,logSpacingFlag,respTypeIndex,
               fileNameString,propsFileString,outputDirectory,genOutputFlag,
                                                        false,false);
  }

    /**
     * Generates the frequency array and then the output.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean doGenerateOutput()
  {
    if(!checkGenerateFreqArray())      //check/generate freq array
      return false;          //if error then return false

         //create callback object that sends output to files
         // and send it to the 'generate' method:
    return generateResponseOutputs(new CallbackProcWrite(
                outUnitsConvIdx,frequenciesArray,logSpacingFlag,verboseFlag,
                      startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                       listInterpOutFlag,listInterpInFlag,listInterpTension,
                                  unwrapPhaseFlag,totalSensitFlag,b62XValue,
                                  respTypeIndex,stdioFlag,getConsoleOut()));
  }

    /**
     * Checks and generates the frequency array.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean checkGenerateFreqArray()
  {
         //check frequency array parameters:
    String str;
    if((str=RespUtils.checkFreqArrayParams(minFreqValue,maxFreqValue,
                                       numberFreqs,logSpacingFlag)) != null)
    {    //frequency array parameter invalid
      setErrorMessage(str);       //setup error message
      exitStatusValue = 15;       //setup exit status error code
      return false;
    }
         //generate array of frequency values:
    if((frequenciesArray=RespUtils.generateFreqArray(minFreqValue,
                          maxFreqValue,numberFreqs,logSpacingFlag)) == null)
    {    //error generating frequency array (shouldn't happen); set msg
      setErrorMessage("Error generating frequency array");
      exitStatusValue = 15;       //setup exit status error code
      return false;
    }
    return true;
  }

    /**
     * Generates response outputs.
     * @param respCallBackObj the response call-back object to use.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
  public boolean generateResponseOutputs(RespCallback respCallBackObj)
  {
         //find responses and output results to files:
    final boolean resultFlag;
    if(NetVersionFlag.value && propsFileString != null &&
                                               propsFileString.length() > 0)
    {    //"network" version of program and server props filename was given
              //check if web-services server specified:
      if(checkWebServicesServer())
        resultFlag = generateWebResponses(respCallBackObj);
      else    //if not web services then fetch from FISSURES network server:
        resultFlag = generateNetResponses(respCallBackObj);
    }
    else
      resultFlag = generateResponses(respCallBackObj);
    return resultFlag;
  }

    /**
     * Test program entry point; creates a 'RunExt' object, performs
     * operations and exits with a status code (non-zero means error).
     */
//  public static void main(String [] args)
//  {
//    final RunExt runExtObj = new RunExt();       //create 'RunExt' object
//                        //process parameters and generate output:
//    if(!runExtObj.processAndOutput("ALST","*","","","1999","100",
//                                    "11:12:13.456","0.01","100","100",false,
//                            UNIT_CONV_LONGSTRS[UNIT_CONV_DEFIDX],true,"","",
//                        TYPE_SPACE_LONGSTRS[0],RESP_TYPE_LONGSTRS[0],"",""))
//    {    //error flag returned; show error message
//      System.err.println(runExtObj.getErrorMessage());
//    }
//    System.exit(runExtObj.getExitStatusValue()); //exit with status value
//  }
}
