//RespTest.java - Tests JEvalResp functionality.
//
//  11/28/2001 -- [ET]  Initial version.
//    8/5/2002 -- [ET]  Added dummy "logSpacingFlag" parameter in call to
//                      'OutputGenerator.calculateResponse()' method and
//                      'null' "headerStr" parameters in calls to 'write'
//                      methods in 'OutputGenerator'.
//   3/26/2003 -- [KF] Added 'outputDirectory' parameter.
//

package com.isti.jevalresp.tests;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Response;
import com.isti.jevalresp.*;
import com.isti.util.UtilFns;
import com.isti.util.LogFile;

/**
 * Class RespTest tests JEvalResp functionality.
 */
public class RespTest
{
  public static void main(String [] args)
  {
    File outputDirectory = null;
    int argsLen = args.length;
              //delete any trailing empty or blank arguments:
    while(argsLen > 0 && args[argsLen-1].trim().length() <= 0)
      --argsLen;
    if(argsLen <= 0)
    {
      System.out.println(
              "RespTest filename STA_LST CHA_LST NET_LST LOC_LST year day output");
      return;
    }
    String fnStr="",staStr="",chaStr="",netStr="",locStr="",yearStr="",
           dayStr="";
//    String staStr="",chaStr="",netStr="",locStr="",yearStr="1998",dayStr="1";
//    String staStr="ALST",chaStr="ELE",netStr="UW",locStr="";
//    String staStr="TEST",chaStr="*",netStr="UW",locStr="";

    for(int p=0; p<argsLen; ++p)
      System.out.println("args[" + p + "] = \"" + args[p] + "\"");

    switch(argsLen)
    {
      default:
      case 8:
        outputDirectory = new File(args[7]);
      case 7:
        dayStr = args[6];
      case 6:
        yearStr = args[5];
      case 5:
        locStr = args[4];
      case 4:
        netStr = args[3];
      case 3:
        chaStr = args[2];
      case 2:
        staStr = args[1];
      case 1:
        fnStr = args[0];
      case 0:
    }
    final String [] staArr = (staStr.trim().length() > 0) ?
                (String [])(UtilFns.quotedStringsToVector(staStr,',',false).
                                             toArray(new String[0])) : null;
    final String [] chaArr = (chaStr.trim().length() > 0) ?
                (String [])(UtilFns.quotedStringsToVector(chaStr,',',false).
                                             toArray(new String[0])) : null;
    final String [] netArr = (netStr.trim().length() > 0) ?
                (String [])(UtilFns.quotedStringsToVector(netStr,',',false).
                                             toArray(new String[0])) : null;
    final String [] locArr = (locStr.trim().length() > 0) ?
                (String [])(UtilFns.quotedStringsToVector(locStr,',',false).
                                             toArray(new String[0])) : null;
    Date dateObj = null;
    if(yearStr.length() > 0)
    {
      int yearVal,dayVal;
      try
      {
        yearVal = Integer.parseInt(yearStr);
      }
      catch(NumberFormatException ex)
      {
        System.err.println("Illegal 'year' value");
        return;
      }
      if(dayStr.length() > 0)
      {
        try
        {
          dayVal = Integer.parseInt(dayStr);
        }
        catch(NumberFormatException ex)
        {
          System.err.println("Illegal 'day' value");
          return;
        }
      }
      else
        dayVal = 1;
      final Calendar calObj =
                          Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      calObj.clear();
      calObj.set(Calendar.YEAR,yearVal);
      calObj.set(Calendar.DAY_OF_YEAR,dayVal);
      dateObj = calObj.getTime();

      final DateFormat dateFormatObj = DateFormat.getInstance();
      dateFormatObj.setTimeZone(TimeZone.getTimeZone("GMT"));
      System.out.println("Date = " + dateFormatObj.format(dateObj));
    }

//    System.out.println(argsLen + " \"" + staStr + "\", \"" + chaStr +
//                              "\", \"" + netStr + "\", \"" + locStr + "\"");

    final LogFile logFileObj = LogFile.initGlobalLogObj(
                           "RespTest.log",LogFile.ALL_MSGS,LogFile.WARNING);
              //construct with console log-level at WARNING to avoid "Log
              // file opened" console message; then change to ALL_MSGS:
    logFileObj.setConsoleLevel(LogFile.ALL_MSGS);

    final RespFileParser parserObj = new RespFileParser(fnStr);
    if(!parserObj.getErrorFlag())
    {
      final ChanIdHldr chanIdHldrObj;
      if((chanIdHldrObj=parserObj.findChannelId(staArr,chaArr,netArr,
                                              locArr,dateObj,null)) != null)
      {
        final ChannelId channelIdObj = chanIdHldrObj.channelIdObj;
//        logFileObj.info("Found channel:" + UtilFns.newline +
//                      RespFileParser.channelIdToString(channelIdObj,false));
        logFileObj.info("Found channel:  " +
                            RespUtils.channelIdToString(channelIdObj,true));
        final Response respObj;
        if(!parserObj.getErrorFlag() &&
                                 (respObj=parserObj.readResponse()) != null)
        {
//          logFileObj.info("Response info:" + UtilFns.newline +
//                                    RespFileParser.responseToString(respObj));
          final OutputGenerator outGenObj = new OutputGenerator(respObj);
          if(outGenObj.checkResponse())
          {
            logFileObj.info("'checkResponse()' OK");
            if(outGenObj.normalizeResponse(0,0))
            {
              logFileObj.info("Calculated sensitivity: " +
                       RespUtils.fmtNumber(outGenObj.getCalcSensitivity()));
              logFileObj.info("Response sensitivity  : " +
                                          ((respObj.the_sensitivity!=null) ?
                                                        RespUtils.fmtNumber(
                   respObj.the_sensitivity.sensitivity_factor) : "<null>"));

              final double minFreq = 0.001;
              final double maxFreq = 10.0;
              final int numFreqs = 100;
              final double [] freqArr = new double[numFreqs];
              for(int i=0; i<numFreqs; ++i)
                freqArr[i] = minFreq + i*(maxFreq-minFreq)/(numFreqs-1);
              if(outGenObj.calculateResponse(freqArr,true,
                                    OutputGenerator.VELOCITY_UNIT_CONV,0,0))
              {
                if(!outGenObj.writeCSpectraData(
                    outputDirectory,"cSpectra.txt",null))
                {
                  logFileObj.error("'writeCSpectraData()' error:  " +
                                               outGenObj.getErrorMessage());
                }
                if(!outGenObj.writeAmpPhaseData(
                    outputDirectory,"ampData.txt","phaseData.txt",null))
                {
                  logFileObj.error("'writeAmpPhaseData()' error:  " +
                                               outGenObj.getErrorMessage());
                }
              }
              else
              {
                logFileObj.error("'calculateResponse()' error:  " +
                                               outGenObj.getErrorMessage());
              }
            }
            else
            {
              logFileObj.error("'normalizeResponse()' error:  " +
                                                 outGenObj.getErrorMessage());
            }
          }
          else
          {
            logFileObj.error("'checkResponse()' error:  " +
                                               outGenObj.getErrorMessage());
          }
        }
        else
        {
          logFileObj.error("Error parsing file (\"" +
                                     parserObj.getInputFileName() + "\"):  " +
                                                 parserObj.getErrorMessage());
        }
      }
      else
        logFileObj.error("Unable to find matching response");
    }
    else
    {
      logFileObj.error("Input file (\"" + parserObj.getInputFileName() +
                              "\") error:  " + parserObj.getErrorMessage());
    }

    parserObj.close();
    logFileObj.close();
  }
}
