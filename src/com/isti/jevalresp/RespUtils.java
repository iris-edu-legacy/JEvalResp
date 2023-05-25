//RespUtils.java:  A group of static utility functions for JEvalResp.
//
//  12/11/2001 -- [ET]  Initial release version.
//   2/27/2002 -- [ET]  Added 'containsGlobChars()', 'isBeforeTime()',
//                      'isAfterTime()', 'inTimeRange()' and
//                      'enterDefaultPropValue()' methods.
//   4/29/2002 -- [ET]  Added 'checkFreqArrayParams()' and
//                      'generateFreqArray()' methods.
//    6/7/2002 -- [ET]  Added version of 'findRespfiles()' that accepts
//                      Vector of initial 'File' objects.
//   7/11/2002 -- [ET]  Improved 'parseRespDate()' method to be able to
//                      handle any number of fractional-second digits after
//                      the decimal point; added 'addDateFlag' parameter
//                      option to 'channelIdToFName()' method.
//   7/15/2002 -- [ET]  Added 'compareTimes()' method.
//    8/6/2002 -- [ET]  Added 'fissTimeToDate()', 'fissTimeToString()',
//                      'fissDateToString()' and 'channelIdToHdrString()'
//                      methods; added 'respEndDateObj' parameter to
//                      'channelIdToEvString()' method; changed so
//                      'numberFormatObj' is constructed via
//                      "NumberFormat.getInstance()".
//   2/28/2005 -- [ET]  Modified to use 'UtilFns.createDateFormatObj()';
//                      modified 'fmtNumber()' to convert "infinity" or
//                      "NAN" to "*".
//   3/10/2005 -- [ET]  Added 'isNegOrZero()' methods; modified min-freq
//                      checks to use 'isNegOrZero()' method; added
//                      'fileObjPathToUrlStr()' method.
//    4/5/2005 -- [ET]  Added 'getTextFormatRespStr()' method; modified
//                      'respStrToUnit()' method to setup "name" field
//                      of returned unit object and to allow given unit
//                      string to be surrounded by '>' and '<' or
//                      parenthesis or to be in "(METER(SECOND^-1))" style.
//   4/21/2005 -- [ET]  Changed "@returns" to "@return".
//   5/25/2005 -- [ET]  Added 'isNegativeOne()', 'isGainObjValid()' and
//                      'isSensObjValid()' methods.
//   11/3/2005 -- [ET]  Modified 'processFileNameList()' method to prevent
//                      it from confusing the colon in URLs with UNIX-style
//                      filename separator characters.
//   8/24/2006 -- [ET]  Modified to support "Tesla" units; added static
//                      'UnitImpl' variables for "Pascal" and "Tesla"
//                      units.
//   1/19/2012 -- [ET]  Fixed bug in 'findRespfiles()' where site/location
//                      parameters were not properly processed.
//  10/21/2013 -- [ET]  Modified to support "Centigrade" units.
//   8/26/2014 -- [ET]  Added 'globStringSiteArrMatch()' method; modified
//                      'findRespfiles()' method to make "--" match
//                      "no location".
//

package com.isti.jevalresp;

import java.io.*;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.ParseException;
import gnu.regexp.RE;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.UnitBase;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Normalization;
import edu.iris.Fissures.IfNetwork.Gain;
import edu.iris.Fissures.IfNetwork.Decimation;
import edu.iris.Fissures.IfNetwork.Filter;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.iris.Fissures.IfNetwork.ComplexNumberErrored;
import edu.iris.Fissures.IfNetwork.CoefficientErrored;
import edu.iris.Fissures.IfNetwork.PoleZeroFilter;
import edu.iris.Fissures.IfNetwork.CoefficientFilter;
import edu.iris.Fissures.IfNetwork.ListFilter;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.network.ResponsePrint;
import com.isti.util.UtilFns;

/**
 * Class RespUtils is a group of static utility functions for JEvalResp.
 */
public class RespUtils
{
    /**
     * DateFormat object for parsing and formatting 'evalresp'-style
     * date/time strings to/from Date objects.  Uses a pattern string of
     * "yyyy,DDD,HH:mm:ss" and is configured to the GMT time zone.
     */
  public static final DateFormat respDateFormatter =
                              UtilFns.createDateFormatObj("yyyy,D,HH:mm:ss",
                                                 UtilFns.GMT_TIME_ZONE_OBJ);
    /**
     * DateFormat object for parsing and formatting 'evalresp'-style
     * date/time strings (that include fractional seconds) to/from Date
     * objects.  Uses a pattern string of "yyyy,DDD,HH:mm:ss.SSS" and is
     * configured to the GMT time zone.
     */
  public static final DateFormat respDateMsFormatter =
                          UtilFns.createDateFormatObj("yyyy,D,HH:mm:ss.SSS",
                                                 UtilFns.GMT_TIME_ZONE_OBJ);
    /**
     * DateFormat object for formatting FISSURES-style date/time
     * strings from Date objects.  Uses a pattern string of
     * "yyyyDDD'T'HH:mm:ss.SSS'z'" and is configured to the GMT time zone.
     */
  public static final DateFormat fissDateFormatter =
                    UtilFns.createDateFormatObj("yyyyDDD'T'HH:mm:ss.SSS'z'",
                                                 UtilFns.GMT_TIME_ZONE_OBJ);
    /**
     * DateFormat object for formatting date/time values for use
     * with file names.  Uses a pattern string of "yyyy.DDD.HH.mm.ss.SSS"
     * and is configured to the GMT time zone.
     */
  public static final DateFormat fNameDateFormatter =
                        UtilFns.createDateFormatObj("yyyy.DDD.HH.mm.ss.SSS",
                                                 UtilFns.GMT_TIME_ZONE_OBJ);

    /** Date value of "2599,365,23:59:59" for "no end date". */
  public static final Date NO_ENDDATE_OBJ = new Date(19880899199000L);

    /** UnitImpl object for Pascal pressure units. */
  public static final UnitImpl PASCAL_UNITIMPL_OBJ =
            UnitImpl.divide(UnitImpl.NEWTON,UnitImpl.SQUARE_METER,"PASCAL");

    /** UnitImpl object for Tesla magnetic flux density units. */
  public static final UnitImpl TESLA_UNITIMPL_OBJ = UnitImpl.divide(
                          UnitImpl.divide(UnitImpl.divide(UnitImpl.KILOGRAM,
                 UnitImpl.SECOND),UnitImpl.SECOND),UnitImpl.AMPERE,"TESLA");

    /** UnitImpl object for Centigrade temperature units. */
  public static final UnitImpl CENTIGRADE_UNITIMPL_OBJ =
               new UnitImpl(UnitBase.KELVIN,UnitImpl.NONE,"CENTIGRADE",1,1);

                   //characters to be quoted in 'globToRegExString()':
  private static final String regExQuoteChars = ".^$[]{}()-|+";
  private static final double SMALL_FLOAT_VAL = 1e-40;
  private static final NumberFormat numberFormatObj =
                                                 NumberFormat.getInstance();

  static      //static initialization block; executes only once
  {                     //set initial pattern for number formatter object:
    if(numberFormatObj instanceof DecimalFormat)
      ((DecimalFormat)numberFormatObj).applyPattern("0.000000E00");
  }

    //private constructor so that no object instances may be created
    // (static access only)
  private RespUtils()
  {
  }

    /**
     * @return true if the glob-style pattern is matched on the given
     * data string.  A regular expression match is performed after
     * '*' characters are changed to ".*", '?' characters are changed
     * to "." and "special" characters are "quoted".
     * @param dataStr data string
     * @param patternStr pattern string
     */
  public static boolean globStringMatch(String dataStr,String patternStr)
  {
    return regStringMatch(dataStr, globToRegExString(patternStr));
  }

    /**
     * Converts the given glob-style pattern string to an equivilant
     * regular expression pattern.  All '*' characters are changed to
     * ".*", all '?' characters are changed to ".", and all other
     * "special" characters are "quoted" with a preceding backslash.
     * @param patternStr pattern string
     * @return regular expression string
     */
  public static String globToRegExString(String patternStr)
  {
    final StringBuffer buff = new StringBuffer();
    final int len = patternStr.length();
    char ch;
    for(int p=0; p<len; ++p)
    {    //for each character in given pattern string
      if((ch=patternStr.charAt(p)) == '*')
        buff.append(".*");        //change "*" to ".*"
      else if(ch == '?')
        buff.append('.');         //change "?" to "."
      else if(regExQuoteChars.indexOf(ch) >= 0)
        buff.append("\\" + ch);   //quote any "special" characters
      else
        buff.append(ch);          //otherwise put in character
    }
    return buff.toString();       //return string version of buffer
  }

    /**
     * Tests if any pattern in the given array matches the given string.
     * @return true if any glob-style pattern in the pattern string
     * array is matched on the given data string.  A regular expression
     * match is performed after '*' characters are changed to ".*",
     * '?' characters are changed to "." and "special" characters are
     * "quoted".
     * @param dataStr data string
     * @param patternStrArr pattern string array
     */
  public static boolean globStringArrMatch(String dataStr,
                                                    String [] patternStrArr)
  {
    String patStr;
    for(int p=0; p<patternStrArr.length; ++p)
    {    //for each pattern string in array
      if((patStr=patternStrArr[p]) != null && patStr.length() > 0 &&
                                            globStringMatch(dataStr,patStr))
      {       //pattern array entry contains data and match was made
        return true;
      }
    }
    return false;
  }

    /**
     * Tests if any pattern in the given array matches the given string.
     * This "site array" version also handles making the pattern "--"
     * match an empty data string.
     * @return true if any glob-style pattern in the pattern string
     * array is matched on the given data string.  A regular expression
     * match is performed after '*' characters are changed to ".*",
     * '?' characters are changed to "." and "special" characters are
     * "quoted".
     * @param dataStr data string
     * @param patternStrArr pattern string array
     */
    public static boolean globStringSiteArrMatch(String dataStr,
                                                 String[] patternStrArr) {
      String patStr;

      for (int p = 0; p < patternStrArr.length; ++p) {  // for each pattern string in array
        if ((patStr = patternStrArr[p]) != null) {      // pattern array entry contains data
          if (patStr.trim().length() <= 0 ||
                  patStr.equals("--")) {                // entry is blank or "--"; check if data string empty
            if (dataStr == null || dataStr.trim().length() <= 0) {
              return true;                              // if data string empty then return true
            }
          } else if (patStr.trim().equals("??")) {      // this always matches (even if empty)
            return true;
          } else if (globStringMatch(dataStr, patStr)) {// entry is not "--" and pattern is not "??"
            return true;                                // if match then return true
          }
        }
      }
      return false;
    }

  /**
     * @return true if the regular-expression pattern is matched on
     * the given data string.
     * @param dataStr data string
     * @param patternStr pattern string
     */
  public static boolean regStringMatch(String dataStr,String patternStr)
  {
    try
    {         //parse pattern and perform expression match:
      return (new RE(patternStr)).isMatch(dataStr);
    }
    catch(Exception ex)
    {         //error parsing pattern
//      log.warning("regStringMatch:  Error parsing pattern string \"" +
//                                                  patternStr + "\": " + ex);
    }
    return false;
  }

    /**
     * @return true if the given string contains any glob-style wildcard
     * characters ('*' or '?').
     * @param str string
     */
  public static boolean containsGlobChars(String str)
  {
    return str != null && (str.indexOf('*') >= 0 || str.indexOf('?') >= 0);
  }

    /**
     * Converts the given RESP file units string to a 'Unit' object.
     * @param unitNameStr unit name string.
     * @return A 'Unit' object corresponding to the given unit name,
     * or null if no match could be made.
     */
  public static Unit respStrToUnit(String unitNameStr)
  {
    boolean firstTryFlag = true;
    while(true)
    {    //loop if retrying after truncating before first whitespace
      UnitImpl unitImplObj;
      try
      {                        //trim leading or trailing whitespace:
        String unitStr = unitNameStr.trim();
        int len;
        if((len=unitStr.length()) > 2 && unitStr.charAt(0) == '>' &&
                                               unitStr.charAt(len-1) == '<')
        {    //name is surrounded by '>' and '<'; remove them
          unitStr = unitStr.substring(1,len-1).trim();
        }
        while((len=unitStr.length()) > 2 && unitStr.charAt(0) == '(' &&
                                               unitStr.charAt(len-1) == ')')
        {    //name is surrounded by parenthesis; remove them
          unitStr = unitStr.substring(1,len-1).trim();
        }
        int ePos, sPos = 0;
        char ch;
        unitImplObj = null;
        String prefixStr = UtilFns.EMPTY_STRING;
        if(len > 0 && unitStr.charAt(sPos) == '/')
        {    //first character is '/'; setup prefix string
          prefixStr = "/";
          ++sPos;                //start after '/' character
        }
        outerLoop:
        while(sPos < len)
        {    //for each sub-unit string processed
          while((ch=unitStr.charAt(sPos)) == '(' || ch == ')' ||
                                                 Character.isWhitespace(ch))
          {  //skip any leading whitespace or parenthesis
            if(++sPos >= len)
              break outerLoop;
          }
          ePos = sPos;      //scan through until separator character found:
          while(++ePos < len && (ch=unitStr.charAt(ePos)) != '(' &&
                     ch != ')' && ch != '/' && !Character.isWhitespace(ch));
          if(sPos < ePos)
          {  //unit string characters found; process sub-unit string
            if((unitImplObj=processUnitStrToUnitObj(
               prefixStr+unitStr.substring(sPos,ePos),unitImplObj)) == null)
            {     //error processing sub-unit string
              break;
            }
          }
          sPos = ePos;      //setup start position for next iteration
          if(ch == '/')
          {  //next sub-unit starts with '/'
            prefixStr = "/";     //setup prefix string for next iteration
            ++sPos;              //skip past '/' in string
          }
          else    //next sub-unit does not start with '/'
            prefixStr = UtilFns.EMPTY_STRING;
        }
      }
      catch(Exception ex)
      {
        unitImplObj = null;
      }
      Unit unitObj;
      if(unitImplObj == null && firstTryFlag)
      {  //unit string not matched and this is the first attempt
              //try using "old" method:
        unitNameStr = unitNameStr.trim();        //trim whitespace
        final int len = unitNameStr.length();
        int p = 0;      //truncate string before first whitespace character:
        while(++p < len)
        {  //loop until first whitespace character
          if(Character.isWhitespace(unitNameStr.charAt(p)))
          {     //first whitespace character found; truncate string
            unitNameStr = unitNameStr.substring(0,p).trim();
            break;             //exit loop
          }
        }
        unitObj = oldRespStrToUnit(unitNameStr);
      }
      else
        unitObj = unitImplObj;

      if(unitObj != null)
      {  //unit string matched OK
              //if not yet named then setup 'name' field:
        if(unitObj.name == null || unitObj.name.trim().length() <= 0)
          unitObj.name = unitObj.toString();
      }
      if(!firstTryFlag)
        return unitObj;
      firstTryFlag = false;
    }         //loop and retry with truncated-before-whitespace string
  }

    /**
     * Processes a unit string into a unit object.  A leading '/' or trailing
     * "^#" or "**#" are taken into account.
     * @param unitStr unit string to parse and process.
     * @param unitObj unit object into which to multiply the the given unit,
     * or null to create a new unit object.
     * @return A new 'UnitImpl' object corresponding to the given string, or
     * null if it could not be matched.
     */
  public static UnitImpl processUnitStrToUnitObj(String unitStr,
                                                           UnitImpl unitObj)
  {
    unitStr = unitStr.trim();          //trim leading or trailing whitespace
    int len;
    while((len=unitStr.length()) > 2 && unitStr.charAt(0) == '(' &&
                                               unitStr.charAt(len-1) == ')')
    {    //name is surrounded by parenthesis; remove them
      unitStr = unitStr.substring(1,len-1).trim();
    }
    int sPos = 0, ePos = 0, xPos = len, expVal = 1;
    if(len > 2)
    {    //string has enough characters for exponents
      char ch;     //scan backwards through any close paraenthesis
      while(((ch=unitStr.charAt(xPos-1)) == ')' ||
                                 Character.isWhitespace(ch)) && --xPos > 0);
      if((sPos=unitStr.lastIndexOf('^')) > 0 &&
                                         (sPos == xPos-3 || sPos == xPos-2))
      {  //exponent character found
        ePos = sPos + 1;
      }
      else if((sPos=unitStr.lastIndexOf("**")) > 0 &&
                                         (sPos == xPos-4 || sPos == xPos-3))
      {  //exponent characters found
        ePos = sPos + 2;
      }
    }
    if(ePos > 0)
    {    //exponent symbol found
      try
      {       //parse number after exponent symbol
        expVal = Integer.parseInt(unitStr.substring(ePos));
                             //remove exponent indicator from string:
        unitStr = (unitStr.substring(0,sPos) +
                                        unitStr.substring(xPos,len)).trim();
        while((len=unitStr.length()) > 2 && unitStr.charAt(0) == '(' &&
                                               unitStr.charAt(len-1) == ')')
        {     //name is surrounded by parenthesis; remove them
          unitStr = unitStr.substring(1,len-1).trim();
        }
      }
      catch(NumberFormatException ex)
      {
      }
    }
    if(len > 1 && unitStr.charAt(0) == '/')
    {    //leading '/' found
      expVal = -expVal;           //indicate division exponent
      unitStr = unitStr.substring(1).trim();     //take string after '/'
      while((len=unitStr.length()) > 2 && unitStr.charAt(0) == '(' &&
                                               unitStr.charAt(len-1) == ')')
      {  //name is surrounded by parenthesis; remove them
        unitStr = unitStr.substring(1,len-1).trim();
      }
    }
         //if ends with 'S' then remove it
    if(len > 2 && (unitStr.charAt(len-1) == 'S' ||
                                              unitStr.charAt(len-1) == 's'))
    {
      unitStr = unitStr.substring(0,len-1);
    }
    UnitImpl parsedUnitObj;
    if(unitStr.equalsIgnoreCase("COUNT") ||
                                   unitStr.equalsIgnoreCase("COUNT_UNIT") ||
                                        unitStr.equalsIgnoreCase("DIGITAL"))
    {
      parsedUnitObj = UnitImpl.COUNT;
    }
    else if(unitStr.equalsIgnoreCase("V") ||
                                         unitStr.equalsIgnoreCase("VOLT") ||
                                      unitStr.equalsIgnoreCase("VOLT_UNIT"))
    {
      parsedUnitObj = UnitImpl.VOLT;
    }
    else if(unitStr.equalsIgnoreCase("PA") ||
                                         unitStr.equalsIgnoreCase("PASCAL"))
    {
      parsedUnitObj = PASCAL_UNITIMPL_OBJ;
    }
    else if(unitStr.equalsIgnoreCase("T") ||
                                          unitStr.equalsIgnoreCase("TESLA"))
    {
      parsedUnitObj = TESLA_UNITIMPL_OBJ;
    }
    else if(unitStr.equalsIgnoreCase("C") ||
                                     unitStr.equalsIgnoreCase("CENTIGRADE"))
    {
      parsedUnitObj = CENTIGRADE_UNITIMPL_OBJ;
    }
    else if(unitStr.equalsIgnoreCase("CM"))
      parsedUnitObj = UnitImpl.CENTIMETER;
    else if(unitStr.equalsIgnoreCase("MM"))
      parsedUnitObj = UnitImpl.MILLIMETER;
    else if(unitStr.equalsIgnoreCase("NM"))
      parsedUnitObj = UnitImpl.NANOMETER;
    else if(unitStr.equalsIgnoreCase("KM"))
      parsedUnitObj = UnitImpl.KILOMETER;
    else if(unitStr.equalsIgnoreCase("METER") ||
                                              unitStr.equalsIgnoreCase("M"))
    {
      parsedUnitObj = UnitImpl.METER;
    }
    else if(unitStr.equalsIgnoreCase("SECOND") ||
           unitStr.equalsIgnoreCase("SEC") || unitStr.equalsIgnoreCase("S"))
    {
      parsedUnitObj = UnitImpl.SECOND;
    }
    else if(unitStr.equalsIgnoreCase("MILLISECOND") ||
         unitStr.equalsIgnoreCase("MSEC") || unitStr.equalsIgnoreCase("MS"))
    {
      parsedUnitObj = UnitImpl.MILLISECOND;
    }
    else
      parsedUnitObj = null;
    if(parsedUnitObj != null)
    {    //unit string was parsed OK
      if(expVal < 0)
      {  //negative exponent value
        if(expVal >= -1)
        {     //exponent value == -1
              //if unit obj given then divide into it; else ret parsed obj
          return (unitObj != null) ? UnitImpl.divide(unitObj,parsedUnitObj) :
                                                    parsedUnitObj.inverse();
        }
        if(unitObj != null)
        {
          do       //process negative exponent value
            unitObj = UnitImpl.divide(unitObj,parsedUnitObj);
          while(++expVal < 0);
          return unitObj;
        }
              //exponent value < -1
        final UnitImpl multUnitObj = parsedUnitObj;
        do
        {     //process negative exponent value
          parsedUnitObj =
                     UnitImpl.multiply(parsedUnitObj,multUnitObj.inverse());
        }
        while(++expVal < -1);
        return parsedUnitObj;
      }
      if(expVal > 1)
      {  //exponent value > 1
        final UnitImpl multUnitObj = parsedUnitObj;
        do         //process positive exponent value
          parsedUnitObj = UnitImpl.multiply(parsedUnitObj,multUnitObj);
        while(--expVal > 1);
      }
              //if unit obj given then multiply into it; else ret parsed obj
      return (unitObj != null) ?
                   UnitImpl.multiply(unitObj,parsedUnitObj) : parsedUnitObj;
    }
    return null;
  }

    /**
     * Converts the given RESP file units string to a 'Unit' object
     * (previous version).
     * @param nameStr unit name string.
     * @return A 'Unit' object corresponding to the given unit name,
     * or null if no match could be made.
     */
  public static Unit oldRespStrToUnit(String nameStr)
  {
    UnitImpl unitObj;

    if(nameStr == null)
      return null;
    nameStr = nameStr.toUpperCase();   //make uppercase string for comp
    if(nameStr.equals("COUNT") || nameStr.equals("COUNTS") ||
                  nameStr.equals("COUNT_UNIT") || nameStr.equals("DIGITAL"))
    {
      unitObj = UnitImpl.COUNT;
    }
    else if(nameStr.equals("V") || nameStr.equals("VOLT") ||
                     nameStr.equals("VOLTS") || nameStr.equals("VOLT_UNIT"))
    {
      unitObj = UnitImpl.VOLT;
    }
    else if(nameStr.equals("PA") || nameStr.equals("PASCAL"))
    {
      unitObj = PASCAL_UNITIMPL_OBJ;
    }
    else if(nameStr.equals("T") || nameStr.equals("TESLA"))
    {
      unitObj = TESLA_UNITIMPL_OBJ;
    }
    else
    {    //test if name starts with expected length abbreviation
      int pos = 2;      //position of end-of-string or '/'
      if(nameStr.startsWith("CM"))
        unitObj = UnitImpl.CENTIMETER;
      else if(nameStr.startsWith("MM"))
        unitObj = UnitImpl.MILLIMETER;
      else if(nameStr.startsWith("NM"))
        unitObj = UnitImpl.NANOMETER;
      else if(nameStr.startsWith("KM"))
        unitObj = UnitImpl.KILOMETER;
      else if(nameStr.startsWith("M"))
      {
        unitObj = UnitImpl.METER;
        pos = 1;
      }
      else    //unrecognized unit name
        return null;
      if(nameStr.length() > pos+1)
      {  //more data in string; should be velocity or acceleration unit
        if(nameStr.charAt(pos) == '/')
        {     //slash character found
          nameStr = nameStr.substring(pos+1);      //get string data after '/'
                   //if ends with "S" or "SEC" then create velocity unit:
          if(nameStr.equals("S") || nameStr.equals("SEC"))
            unitObj = UnitImpl.divide(unitObj,UnitImpl.SECOND);
          else if(nameStr.equals("S**2") || nameStr.equals("S/S") ||
                       nameStr.equals("(S**2)") || nameStr.equals("SEC**2") ||
                      nameStr.equals("SEC/SEC") || nameStr.equals("(SEC**2)"))
          {
            unitObj = UnitImpl.divide(      //create acceleration unit
                  UnitImpl.divide(unitObj,UnitImpl.SECOND),UnitImpl.SECOND);
          }
          else     //unrecognized unit name
            return null;
        }
        else       //unrecognized unit name
          return null;
      }  //no more data in string; stay with simple length unit
    }
    return (Unit)unitObj;
  }

    /**
     * @return an 'evalresp' output filename built from the given
     * 'ChannelId' object, with periods separating the names
     * ("net.sta.loc.cha").
     * @param chObj channel ID
     * @param addDateFlag if true then a date code in the format
     * ".yyyy.DDD.HH.mm.ss.SSS" built from the channel-ID will be
     * appended to the returned string.
     */
  public static String channelIdToFName(ChannelId chObj,boolean addDateFlag)
  {
    if(chObj == null)        //if null handle then
      return "(null)";       //return indicator string

    String dateStr;
    if(addDateFlag)
    {    //date/time code to be added to name
      Date dateObj;
      try
      {          //convert FISSURES time string to Date:
        dateObj = (new ISOTime(chObj.begin_time.date_time)).getDate();
      }
      catch(Exception ex)
      {    //error converting date string
        dateObj = null;
      }
      if(dateObj != null)
      {     //begin-date converted OK; format into filename string
        dateStr = "." + fNameDateFormatter.format(dateObj);
        int p;        //check if any trailing zeros can be trimmed:
        if((p=dateStr.lastIndexOf('.')) > 0 &&
                                      dateStr.substring(p).equals(".000"))
        {   //string ends zero milliseconds
          dateStr = dateStr.substring(0,p);    //trim trailing zeros
          while((p=dateStr.lastIndexOf('.')) > 0 &&
                                       dateStr.substring(p).equals(".00"))
          {      //for each trailing ".00"; trim it
            dateStr = dateStr.substring(0,p);
          }
        }
      }
      else    //error converting date string
        dateStr = UtilFns.EMPTY_STRING;
    }
    else      //no date/time code to be added
      dateStr = UtilFns.EMPTY_STRING;
    return channelIdToFName(chObj.station_code,chObj.channel_code,
          ((chObj.network_id != null) ? chObj.network_id.network_code :
                           UtilFns.EMPTY_STRING),chObj.site_code) + dateStr;
  }

    /**
     * @return an 'evalresp' output filename built from the given
     * station/channel/network names, with periods separating the names
     * ("net.sta.loc.cha").
     * @param staName station name
     * @param chaName channel name
     * @param netName net name
     * @param siteName site name
     */
  public static String channelIdToFName(String staName,String chaName,
                                             String netName,String siteName)
  {
    return fixIdStr(netName) + "." + fixIdStr(staName) + "." +
                               fixIdStr(siteName) + "." + fixIdStr(chaName);
  }

  /**
   * Fix ID string to be valid in a filename
   * @param str string
   * @return ID string
   */
  private static String fixIdStr(String str)
  {
    if(str == null || (str=str.trim()).length() <= 0 || str.startsWith("?"))
      return UtilFns.EMPTY_STRING;     //if no data then return empty string
                             //create buffer version of string:
    final StringBuffer buff = new StringBuffer(str);
    final int len = str.length();
    char ch;
    for(int i=0; i<len; ++i)
    {    //for each character in string; check if OK in filename
      if(!Character.isLetterOrDigit(ch=str.charAt(i)) && ch != '_')
        buff.setCharAt(i,'_');    //if not OK then replace with '_'
    }
    return buff.toString();       //return string version of buffer
  }

    /**
     * @return a string representation of the given 'ChannelId' object.
     * @param chObj channel object
     * @param shortFlag if true then a short version of the information
     * is returned.
     */
  public static String channelIdToString(ChannelId chObj,boolean shortFlag)
  {
    if(chObj == null)        //if null handle then
      return "(null)";       //return indicator string
    if(shortFlag)
    {    //short version requested
      return chObj.station_code + "," + chObj.channel_code + "," +
         ((chObj.network_id != null && chObj.network_id.network_code != null
                            && chObj.network_id.network_code.length() > 0) ?
                               chObj.network_id.network_code : "??") + "," +
                ((chObj.site_code != null && chObj.site_code.length() > 0) ?
                                                    chObj.site_code : "??");
    }
    ByteArrayOutputStream btOutStream = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(btOutStream);
    out.println("Station code:  " + chObj.station_code);
    out.println("Channel code:  " + chObj.channel_code);
    if(chObj.network_id != null)
    {
      out.println("Network code:  " + chObj.network_id.network_code);
      out.println("Network begin_time:  " +
                                   ((chObj.network_id.begin_time != null) ?
                        chObj.network_id.begin_time.date_time : "(null)"));
    }
    else
      out.println("'ChannelId.network_id' is null");
    out.println("Site code:  " + chObj.site_code);
    out.println("Begin time:  " + ((chObj.begin_time != null) ?
                                    chObj.begin_time.date_time : "(null)"));

    out.flush();             //flush data out to byte array
    final String retStr = btOutStream.toString();     //save string data
    out.close();             //close stream
    return retStr;           //return string data
  }

    /**
     * @return a string representation of the given 'ChannelId' object.
     * @param chObj channel object
     */
  public static String channelIdToString(ChannelId chObj)
  {
    return channelIdToString(chObj,false);
  }

    /**
     * @return a string representation of the given 'ChannelId' object
     * in 'evalresp' verbose display format:
     * (netCode staCode siteCode chaCode yyyy,ddd,hh:mm:ss yyyy,ddd,hh:mm:ss).
     * @param chObj the 'ChannelId' object.
     * @param respEndDateObj the end-time for the channel, or null for no
     * end-time included.
     */
  public static String channelIdToEvString(ChannelId chObj,
                                                        Date respEndDateObj)
  {
    if(chObj == null)        //if null handle then
      return "(null)";       //return indicator string
    String dateStr;
    try
    {
      final Date dateObj =        //convert FISSURES time string to Date
                        (new ISOTime(chObj.begin_time.date_time)).getDate();
                                  //convert Date to 'evalresp' time string:
      dateStr = respDateFormatter.format(dateObj);
    }
    catch(Exception ex)
    {    //error converting dates
      dateStr = "????";      //indicate error
    }
    return ((chObj.network_id != null &&
                                    chObj.network_id.network_code != null &&
                               chObj.network_id.network_code.length() > 0) ?
                               chObj.network_id.network_code : "??") + " " +
                                                  chObj.station_code + " " +
                ((chObj.site_code != null && chObj.site_code.length() > 0) ?
                                             chObj.site_code : "??") + " " +
                                        chObj.channel_code + " " + dateStr +
                                                    ((respEndDateObj!=null)?
       (" "+respDateFormatter.format(respEndDateObj)):UtilFns.EMPTY_STRING);
  }

    /**
     * @return a string representation of the given 'ChannelId' object
     * in 'evalresp' verbose display format:
     * (netCode staCode siteCode chaCode yyyy,ddd,hh:mm:ss).
     * @param chObj channel object
     */
  public static String channelIdToEvString(ChannelId chObj)
  {
    return channelIdToEvString(chObj,null);
  }

    /**
     * @return a string representation of the given 'ChannelId' object
     * in "header" display format.
     * @param channelIdObj the 'ChannelId' object.
     * @param respEndDateObj the end-time for the channel, or null for no
     * end-time included.
     * @param sLineStr the string to begin each line with.
     * @param sepStr the item separator string to use.
     * @param newlineStr the new-line string to use.
     */
  public static String channelIdToHdrString(ChannelId channelIdObj,
        Date respEndDateObj,String sLineStr,String sepStr,String newlineStr)
  {
    if(channelIdObj == null)
      return UtilFns.EMPTY_STRING;
              //create String for location item such that it only
              // appears if location information is available:
    final String locStr = (channelIdObj.site_code != null &&
                               channelIdObj.site_code.trim().length() > 0) ?
                          (sepStr + "Location: " + channelIdObj.site_code) :
                                                       UtilFns.EMPTY_STRING;
              //create String for begin-time item such that it only
              // appears if begin-time information is available:
    final String bTimeStr = (channelIdObj.begin_time != null &&
                                channelIdObj.begin_time.date_time != null) ?
               ("BeginTime: " + fissTimeToString(channelIdObj.begin_time)) :
                                                       UtilFns.EMPTY_STRING;
              //create String for end-time item such that it only
              // appears if end-time information is available:
    final String eTimeStr = (respEndDateObj != null) ? (sepStr +
                           "EndTime: " + fissDateToString(respEndDateObj)) :
                                                       UtilFns.EMPTY_STRING;
    return sLineStr + "Network: " + ((channelIdObj.network_id != null) ?
                              channelIdObj.network_id.network_code : "??") +
                 sepStr + "Station: " + channelIdObj.station_code + locStr +
                          sepStr + "Channel: " + channelIdObj.channel_code +
                                newlineStr + sLineStr + bTimeStr + eTimeStr;
  }

    /**
     * @return a string representation of the given 'ChannelId' object
     * in "header" display format.
     * @param channelIdObj the 'ChannelId' object.
     * @param respEndDateObj the end-time for the channel, or null for no
     * end-time included.
     * @param sepStr the item separator string to use.
     * @param newlineStr the new-line string to use.
     */
  public static String channelIdToHdrString(ChannelId channelIdObj,
                        Date respEndDateObj,String sepStr,String newlineStr)
  {
    return channelIdToHdrString(channelIdObj,respEndDateObj,
                                    UtilFns.EMPTY_STRING,sepStr,newlineStr);
  }

    /**
     * @return a string representation of the given 'Response' object.
     * @param respObj response object
     */
  public static String responseToString(Response respObj)
  {
    if(respObj == null)      //if null handle then
      return "(null)";       //return indicator string
    ByteArrayOutputStream btOutStream = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(btOutStream);
    if(respObj.the_sensitivity != null)
    {
      out.println("Sensitivity factor:    " +
                                respObj.the_sensitivity.sensitivity_factor);
      out.println("Sensitivity frequency: " +
                                         respObj.the_sensitivity.frequency);
    }
    else
      out.println("'Response.the_sensitivity' is null");
    out.println("Stages:");
    for(int stageNum=0; stageNum< respObj.stages.length; ++stageNum)
    {    //for each stage
      Stage stageObj = respObj.stages[stageNum];
      try
      {
        out.println("  Stage #" + (stageNum+1) + ":   ");
        out.println("    Transfer type:  " + stageObj.type.value());
        out.println("    Input units:  " +
                                      unitToUnitImpl(stageObj.input_units));
        out.println("    Output units: " +
                                     unitToUnitImpl(stageObj.output_units));
        if(stageObj.the_normalization != null)
        {
          if(stageObj.the_normalization.length > 0)
          {
            out.println("    AO normalization factor: " +
                     stageObj.the_normalization[0].ao_normalization_factor);
            out.println("    Normalization freq:      " +
                          stageObj.the_normalization[0].normalization_freq);
          }
        }
        else
          out.println("    'Response.the_normalization' is null");
        if(stageObj.the_gain != null)
        {
          out.println("    Gain factor:    " +
                                             stageObj.the_gain.gain_factor);
          out.println("    Gain frequency: " + stageObj.the_gain.frequency);
        }
        else
          out.println("    'Response.the_gain' is null");
        if(stageObj.the_decimation != null)
        {
          if(stageObj.the_decimation.length > 0)
          {
//            final Double intTimeObj =
//                              deciToSampIntTime(stageObj.the_decimation[0]);

            out.println("    Decimation:");
            out.println("      Input rate:         " +
                                     stageObj.the_decimation[0].input_rate);
//            out.println("      Input rate:         " +
//                                     stageObj.the_decimation[0].input_rate +
//                                  ", intervalTime = " + ((intTimeObj!=null)?
//                                                (intTimeObj+" sec"):"???"));
            out.println("      Factor:             " +
                                         stageObj.the_decimation[0].factor);
            out.println("      Offset:             " +
                                         stageObj.the_decimation[0].offset);
            out.println("      Estimated delay:    " +
                                stageObj.the_decimation[0].estimated_delay);
            out.println("      Correction applied: " +
                             stageObj.the_decimation[0].correction_applied);
          }
        }
        else
          out.println("    'Response.the_decimation' is null");
      }
      catch(Exception ex)
      {
        out.println("  Error in stage #" + (stageNum+1) + ":  " + ex);
      }
      if(stageObj.filters != null)
      {
        out.println("    Number of filters: " + stageObj.filters.length);
        Filter filterObj;
        for(int filterNum=0; filterNum<stageObj.filters.length; ++filterNum)
        {     //for each filter in stage
          try
          {
            if((filterObj=stageObj.filters[filterNum]) != null)
            {
              if(filterObj.discriminator().equals(FilterType.POLEZERO))
              {
                final PoleZeroFilter pz = filterObj.pole_zero_filter();
                out.println("    Poles and Zeros Filter (" +
                                                pz.poles.length + " poles, " +
                                                pz.zeros.length + " zeros):");
                for(int k=0; k<pz.poles.length; ++k)
                {
                  out.println("      Pole:  " + pz.poles[k].real + " " +
                        pz.poles[k].real_error + " " + pz.poles[k].imaginary +
                                           " " + pz.poles[k].imaginary_error);
                }
                for(int k=0; k<pz.zeros.length; ++k)
                {
                  out.println("      Zero:  " + pz.zeros[k].real + " " +
                        pz.zeros[k].real_error + " " + pz.zeros[k].imaginary +
                                           " " + pz.zeros[k].imaginary_error);
                }
              }
              else if(filterObj.discriminator().equals(FilterType.COEFFICIENT))
              {
                final CoefficientFilter cf = filterObj.coeff_filter();
                out.println("    Coefficients Filter (" +
                                           cf.numerator.length + " numers, " +
                                         cf.denominator.length + " denoms):");
                for(int k=0; k<cf.numerator.length; ++k)
                {
                  out.println("      Numerator:   " + cf.numerator[k].value +
                                                 " " + cf.numerator[k].error);
                }
                for(int k=0; k< cf.denominator.length; ++k)
                {
                  out.println("      Denominator: " + cf.denominator[k].value
                                             + " " + cf.denominator[k].error);
                }
              }
              else if(filterObj.discriminator().equals(FilterType.LIST))
              {
                final ListFilter lf = filterObj.list_filter();
                out.println("    List Filter (" +
                                    lf.frequency.length + " frequencies):");
                out.println("      FreqUnits=\"" +
                    unitToUnitImpl(lf.frequency_unit) + "\" PhaseUnits=\"" +
                                      unitToUnitImpl(lf.phase_unit) + "\"");
                for(int i=0; i<lf.frequency.length; ++i)
                {
                  out.println("      Freq=" + lf.frequency[i] + "  Amp=" +
                             lf.amplitude[i] + " " + lf.amplitude_error[i] +
                                            "  Phase=" + lf.phase[i] + " " +
                                                         lf.phase_error[i]);
                }
              }
              else
              {
                out.println("    Unknown filter type (value=" +
                    filterObj.discriminator().value() + "); 'Stage.filters[" +
                                                            filterNum + "]'");
              }
            }
            else
              out.println("    'Stage.filters[" + filterNum + "]' is null");
          }
          catch(Exception ex)
          {
            out.println("    Error in 'filters[" + filterNum +
                               "]' of stage #" + (stageNum+1) + ":  " + ex);
          }
        }
      }
      else
        out.println("'Stage.filters' is null");
    }
    out.flush();             //flush data out to byte array
    final String retStr = btOutStream.toString();     //save string data
    out.close();             //close stream
    return retStr;           //return string data
  }

    /**
     * @return the sensitivity factor for the given Response object,
     * or 0.0 if it cannot be returned.
     * @param respObj response object
     */
  public static double getRespSensitivity(Response respObj)
  {
    return (respObj != null && respObj.the_sensitivity != null) ?
                           respObj.the_sensitivity.sensitivity_factor : 0.0;
  }

    /**
     * @return the sensitivity frequency for the given Response object,
     * or 0.0 if it cannot be returned.
     * @param respObj response object
     */
  public static double getRespSensFrequency(Response respObj)
  {
    return (respObj != null && respObj.the_sensitivity != null) ?
                                    respObj.the_sensitivity.frequency : 0.0;
  }

    /**
     * Return the length (in seconds) of the sampling interval of the
     * given 'Decimation' object.
     * @param deciObj decimation object
     * @return A 'Double' object containing the length value; or null if
     * the given 'Decimation' object contains null handles or if its units
     * type is not based on 'seconds'.
     */
  public static Double deciToSampIntTime(Decimation deciObj)
  {
    final Sampling sampObj;
    final Unit unitObj;
              //extract sampling and units objects and check units:
    if(deciObj == null || (sampObj=deciObj.input_rate) == null ||
                       sampObj.numPoints <= 0 || sampObj.interval == null ||
                             (unitObj=sampObj.interval.the_units) == null ||
                                            unitObj.the_unit_base == null ||
                             !unitObj.the_unit_base.equals(UnitBase.SECOND))
    {
      return null;
    }
    return new Double(sampObj.interval.value / sampObj.numPoints *
                                                      pow10(unitObj.power));
  }

    /**
     * Return the length (in seconds) specified by the given time-interval
     * Quantity object.
     * @param intervalObj interval object
     * @return A 'Double' object containing the length value; or null if
     * the given 'Quantity' object contains null handles or if its units
     * type is not based on 'seconds'.
     */
  public static Double quantityToIntTime(Quantity intervalObj)
  {
    final Unit unitObj;
              //extract and check units object:
    if(intervalObj == null || (unitObj=intervalObj.the_units) == null ||
                                            unitObj.the_unit_base == null ||
                             !unitObj.the_unit_base.equals(UnitBase.SECOND))
    {
      return null;
    }
    return new Double(intervalObj.value * pow10(unitObj.power));
  }

    /**
     * Parses the given 'evalresp' format date string into a 'Date'
     * object.  The date string must be in "yyyy,D,HH:mm:ss.SSS" format,
     * but may also be a truncated subset of the format (for example,
     * "yyyy,D,HH:mm:ss" or "yyyy,D").  Any number of fractional-second
     * digits are allowed after the decimal point.
     * @param dateStr date string
     * @return a Date object, or null if error.
     */
  public static Date parseRespDate(String dateStr)
  {
    if(dateStr != null && dateStr.length() > 0)
    {    //date string contains data
              //string must be in "yyyy,D,HH:mm:ss.SSS" format before
              // it is parsed; add to end of string if necessary:
                   //trim any trailing comma or colon:
      if(dateStr.endsWith(",") || dateStr.endsWith(":"))
        dateStr = dateStr.substring(0,dateStr.length()-1);
      int p;
      if((p=dateStr.indexOf(',')) < 0)
        dateStr += ",0,00:00:00.000";
      else if((p=dateStr.indexOf(',',p+1)) < 0)
        dateStr += ",00:00:00.000";
      else if((p=dateStr.indexOf(':',p+1)) < 0)
        dateStr += ":00:00.000";
      else if((p=dateStr.indexOf(':',p+1)) < 0)
        dateStr += ":00.000";
      else if((p=dateStr.indexOf('.',p+1)) < 0)
        dateStr += ".000";
      else
      {  //string contains fractional seconds
        ++p;                 //increment to character after '.'
        int c;               //determine number of digits after '.'
        if((c=dateStr.length()-p) != 3)
        {     //other than three digits after '.'; need to convert
          if(c > 0)
          {   //at least one digit after '.'
            double fSec;
            try
            {      //convert fractional seconds to floating-point value:
              fSec = Double.parseDouble(dateStr.substring(p-1));
            }
            catch(NumberFormatException ex)
            {      //error converting value; use zero value
              fSec = 0.0;
            }
              //substitute in 3 significant digits of fractional seconds:
            dateStr = dateStr.substring(0,p) + (int)(fSec*1000+0.5);
          }
          else     //no digits after '.'
            dateStr += "000";          //add zero value with 3 digits
        }
      }
      try
      {            //parse into Date object and return
        return respDateMsFormatter.parse(dateStr);
      }
      catch(ParseException ex) {}      //if error then return null
    }
    return null;
  }

    /**
     * @return the value of the given power of 10 (10**exp).
     * @param exp exponent value to use.
     */
  public static double pow10(int exp)
  {
    long pVal = 1;
    final int absExp = Math.abs(exp);
    for(int i=0; i<absExp; ++i)
      pVal *= 10;
    return (exp < 0) ? 1.0/pVal : (double)pVal;
  }

    /**
     * @return true if given value is near zero.
     * @param val value to compare.
     */
  public static boolean isZero(double val)
  {
    return (Math.abs(val) < SMALL_FLOAT_VAL);
  }

    /**
     * @return true if given value is near zero.
     * @param val value to compare.
     */
  public static boolean isZero(float val)
  {
    return (Math.abs(val) < (float)SMALL_FLOAT_VAL);
  }

    /**
     * @return true if both parts of the given complex value are near zero.
     * @param val value to compare.
     */
  public static boolean isZero(ComplexBlk val)
  {
    return (Math.abs(val.real) < SMALL_FLOAT_VAL) &&
                                     (Math.abs(val.imag) < SMALL_FLOAT_VAL);
  }

    /**
     * @return true if given value is negative or near zero.
     * @param val value to compare.
     */
  public static boolean isNegOrZero(double val)
  {
    return val < SMALL_FLOAT_VAL;
  }

    /**
     * @return true if given value is negative or near zero.
     * @param val value to compare.
     */
  public static boolean isNegOrZero(float val)
  {
    return val < (float)SMALL_FLOAT_VAL;
  }

    /**
     * Returns true if the given value is "nearly" equal to '-1'.
     * @param val value to compare.
     * @return true if the given value is "nearly" equal to '-1'.
     */
  public static boolean isNegativeOne(float val)
  {
    return (val > -1) ? (val + 1 < (float)SMALL_FLOAT_VAL) :
                                        (-1 - val < (float)SMALL_FLOAT_VAL);
  }

    /**
     * Determines if the given gain object is "valid".  The gain object
     * is "valid" if it is not null, its gain value is not zero, and its
     * frequency and gain values are not equal to '-1'.
     * @param gainObj gain object to test.
     * @return true if the given gain object is "valid", false if not.
     */
  public static boolean isGainObjValid(Gain gainObj)
  {
    return (gainObj != null && !isZero(gainObj.gain_factor) &&
                                       (!isNegativeOne(gainObj.frequency) ||
                                      !isNegativeOne(gainObj.gain_factor)));
  }

    /**
     * Determines if the given sensitivity object is "valid".  The
     * sensitivity object is "valid" if it is not null, its sensitivity
     * value is not zero, and its frequency and sensitivity values are
     * not equal to '-1'.
     * @param sensObj sensitivity object to test.
     * @return true if the given sensitivity object is "valid", false if
     * not.
     */
  public static boolean isSensObjValid(Sensitivity sensObj)
  {
    return (sensObj != null && !isZero(sensObj.sensitivity_factor) &&
                                       (!isNegativeOne(sensObj.frequency) ||
                               !isNegativeOne(sensObj.sensitivity_factor)));
  }

    /**
     * Builds an array of base Unit objects built from the given Unit
     * object.
     * @param unitObj unit object
     * @return An array of Unit objects, each of which is a base Unit object
     * (not composite).
     */
  public static Unit [] toUnitsArray(Unit unitObj)
  {
    if(unitObj != null && unitObj.the_unit_base != null)
    {    //unit object is valid
      if(!unitObj.the_unit_base.equals(UnitBase.COMPOSITE))
        return new Unit[] { unitObj }; //if base unit, return array with Unit
      final int len;         //unit is composite type
      if(unitObj.elements != null && (len=unitObj.elements.length) > 0)
      {  //elements array contains objects
        final Vector retVec = new Vector();
        Unit eUnit;
        for(int i=0; i<len; ++i)
        {     //for each Unit object in 'elements' array
          if((eUnit=unitObj.elements[i]) != null &&
                                                eUnit.the_unit_base != null)
          {   //Unit object is valid
            if(!eUnit.the_unit_base.equals(UnitBase.COMPOSITE))
              retVec.add(eUnit);  //if base unit then add to Vector
            else                  //if composite then make recursive call
              retVec.addAll(Arrays.asList(toUnitsArray(eUnit)));
          }
        }
        try
        {          //convert Vector to array and return it:
          return (Unit [])retVec.toArray(new Unit[retVec.size()]);
        }
        catch(Exception ex) {}
      }
    }                        //if error then
    return new Unit[0];      //return empty array
  }

    /**
     * @return the 'power' value of the first base-unit of the given unit
     * object, or 0 if no power value could be found.
     * @param unitObj unit object
     */
  public static int toFirstUnitPower(Unit unitObj)
  {
    while(unitObj != null && unitObj.the_unit_base != null)
    {    //find first base unit in object
      if(!unitObj.the_unit_base.equals(UnitBase.COMPOSITE))
        return unitObj.power;
      if(unitObj.elements == null || unitObj.elements.length < 1)
        break;
      unitObj = unitObj.elements[0];   //move to subunit
    }
    return 0;
  }

    /**
     * Converts a 'Unit' object to a 'UnitImpl' object.
     * @param unitObj unit object
     * @return 'UnitImpl' object
     */
  public static UnitImpl unitToUnitImpl(Unit unitObj)
  {
    if(unitObj == null)                //if null handle then
      return null;                     //return null
    if(unitObj instanceof UnitImpl)    //if already a 'UnitImpl' then
      return (UnitImpl)unitObj;        //cast and return object
         //build new 'UnitImpl' from fields of 'Unit' object:
    return (unitObj.the_unit_base != null &&
                         unitObj.the_unit_base.equals(UnitBase.COMPOSITE)) ?
                   new UnitImpl(unitObj.elements,unitObj.power,unitObj.name,
                                    unitObj.multi_factor,unitObj.exponent) :
              new UnitImpl(unitObj.the_unit_base,unitObj.power,unitObj.name,
                                     unitObj.multi_factor,unitObj.exponent);
  }

    /**
     * Formats and returns the given value as a String in the form
     * "#.######E+##".
     * @param val value to use.
     * @return A new value string.
     */
  public static String fmtNumber(double val)
  {
    if(Double.isInfinite(val) || Double.isNaN(val))   //if bad value then
      return "*           ";                          //convert to asterisk
    final String str = numberFormatObj.format(val);   //format to string
    int p;         //find exponent char and see if followed by '-':
    if((p=str.lastIndexOf('E')) < 0 || ++p >= str.length() ||
                                                        str.charAt(p) < '0')
    {    //exponent preceded by '-'
      return str;       //return formatted string as is
    }
                        //return with '+' inserted before exponent:
    return str.substring(0,p) + "+" + str.substring(p);
  }

    /**
     * Finds files that match the given sets of STA/CHA/NET criteria items.
     * The file names are expected to be in the format "NET.STA.CHA" and
     * prefixed with the value of the parameter 'prefixStr'.  All search
     * strings are interpreted as glob-style patterns ('*' and '?' only).
     * @param searchPathStr the path to the directory to be search; if
     * null or an empty string then the local directory is searched.
     * @param stationPatArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param channelPatArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param networkPatArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param sitePatArr site pattern array
     * @param prefixStr a prefix that all of the matching files must
     * contain.
     * @param initialFilesVec a Vector of File objects to be entered into
     * the returned array and to be appended with the found File objects,
     * or null for none.  File entries will not be duplicated in the
     * returned array (and Vector).
     * @return An array of 'File' objects including those from the
     * 'initialFilesVec' Vector and any new files that match the criteria
     * items.
     */
  public static File [] findRespfiles(String searchPathStr,
                            String [] stationPatArr,String [] channelPatArr,
                               String [] networkPatArr,String [] sitePatArr,
                                    String prefixStr,Vector initialFilesVec)
  {
    if(prefixStr == null)
      prefixStr = UtilFns.EMPTY_STRING;     //make sure prefix str not null
         //use initial Vector of File objects or create new if none given:
    final Vector retVec = (initialFilesVec != null) ? initialFilesVec :
                                                               new Vector();
                   //create list of files starting with 'prefixStr':
    File [] fileArr = resolveNameToFileObjs(searchPathStr,
                                                     (prefixStr + "*.*.*"));
    final int fileArrLen;
    if(fileArr != null && (fileArrLen=fileArr.length) > 0)
    {    //matching files were found
              //check given arrays of pattern strings and change any that
              // contain no data to have one global-match string:
      if(stationPatArr == null || stationPatArr.length <= 0)
        stationPatArr = new String[] {"*"};
      if(channelPatArr == null || channelPatArr.length <= 0)
        channelPatArr = new String[] {"*"};
      if(networkPatArr == null || networkPatArr.length <= 0)
        networkPatArr = new String[] {"*"};
      if(sitePatArr == null || sitePatArr.length <= 0)
        sitePatArr = new String[] {"*"};
      RE reObj;         //handle for regular expression object
      File fileObj;
      String siteStr;
      for(int staIdx=0; staIdx<stationPatArr.length; ++staIdx)
      {       //for each station name pattern element
        for(int chaIdx=0; chaIdx<channelPatArr.length; ++chaIdx)
        {     //for each channel name pattern element
          for(int netIdx=0; netIdx<networkPatArr.length; ++netIdx)
          {   //for each network name pattern element
            for(int siteIdx=0; siteIdx<sitePatArr.length; ++siteIdx)
            {   //for each site/location name pattern element
              siteStr = sitePatArr[chaIdx];
              //make "--" match "no location"
              if("--".equals(siteStr)) siteStr = UtilFns.EMPTY_STRING;
              //make "??" match "any location"
              if("??".equals(siteStr)) siteStr = "*";
              try       //create regular expression object
              {         // (including site/location names):
                reObj = new RE(globToRegExString(prefixStr +
                       networkPatArr[netIdx] + "." + stationPatArr[staIdx] +
                              "." + siteStr + "." + channelPatArr[chaIdx]));
              }
              catch(Exception ex)
              {         //if error then set to null
                reObj = null;
              }
              if(reObj != null)
              {    //regular expression object created OK
                for(int fIdx=0; fIdx<fileArrLen; ++fIdx)
                {      //for each filename in array; check for match
                  if(reObj.isMatch((fileObj=fileArr[fIdx]).getName()) &&
                                                retVec.indexOf(fileObj) < 0)
                  {    //match found and File not already in Vector
                    retVec.add(fileObj);    //add File object to Vector
                  }
                }
              }
            }
            try         //create regular expression object
            {           // (not including site/location names):
              reObj = new RE(globToRegExString(prefixStr +
                       networkPatArr[netIdx] + "." + stationPatArr[staIdx] +
                                              "." + channelPatArr[chaIdx]));
            }
            catch(Exception ex)
            {           //if error then set to null
              reObj = null;
            }
            if(reObj != null)
            {      //regular expression object created OK
              for(int fIdx=0; fIdx<fileArrLen; ++fIdx)
              {        //for each filename in array; check for match
                if(reObj.isMatch((fileObj=fileArr[fIdx]).getName()) &&
                                                retVec.indexOf(fileObj) < 0)
                {      //match found and File not already in Vector
                  retVec.add(fileObj);    //add File object to Vector
                }
              }
            }
          }
        }
      }
    }
    try
    {         //convert Vector of File objects to an array:
      fileArr = (File [])retVec.toArray(new File[retVec.size()]);
    }
    catch(Exception ex)
    {         //if error then setup to return empty array
      fileArr = new File[0];
    }
    return fileArr;          //return array of File objects
  }

    /**
     * Finds files that match the given sets of STA/CHA/NET criteria items.
     * The file names are expected to be in the format "NET.STA.CHA" and
     * prefixed with the value of the parameter 'prefixStr'.  All search
     * strings are interpreted as glob-style patterns ('*' and '?' only).
     * @param searchPathStr the path to the directory to be search; if
     * null or an empty string then the local directory is searched.
     * @param stationPatArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param channelPatArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param networkPatArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param sitePatArr site pattern array
     * @param prefixStr a prefix that all of the matching files must
     * contain.
     * @param initialFilesArr an array of File objects to be entered into
     * the returned array, or null for none.  File entries will not be
     * duplicated in the returned array.
     * @return An array of 'File' objects that match the criteria items,
     * or an empty array if no matching files were found.
     */
  public static File [] findRespfiles(String searchPathStr,
                            String [] stationPatArr,String [] channelPatArr,
                               String [] networkPatArr,String [] sitePatArr,
                                   String prefixStr,File [] initialFilesArr)
  {
         //if initial files array has elements convert to Vector and send,
         // otherwise send null:
    return findRespfiles(searchPathStr,stationPatArr,channelPatArr,
                                         networkPatArr,sitePatArr,prefixStr,
                  ((initialFilesArr != null && initialFilesArr.length > 0) ?
                        new Vector(Arrays.asList(initialFilesArr)) : null));
  }

    /**
     * Finds files that match the given sets of STA/CHA/NET criteria items.
     * The file names are expected to be in the format "NET.STA.CHA" and
     * prefixed with the value of the parameter 'prefixStr'.  All search
     * strings are interpreted as glob-style patterns ('*' and '?' only).
     * @param searchPathStr the path to the directory to be search; if
     * null or an empty string then the local directory is searched.
     * @param stationPatArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param channelPatArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param networkPatArr an array of network name patterns to search for,
     * or a null or empty array to accept all network names.
     * @param sitePatArr site pattern array
     * @param prefixStr a prefix that all of the matching files must
     * contain.
     * @return An array of 'File' objects that match the criteria items,
     * or an empty array if no matching files were found.
     */
  public static File [] findRespfiles(String searchPathStr,
                            String [] stationPatArr,String [] channelPatArr,
                               String [] networkPatArr,String [] sitePatArr,
                                                           String prefixStr)
  {
    return findRespfiles(searchPathStr,stationPatArr,channelPatArr,
                           networkPatArr,sitePatArr,prefixStr,(Vector)null);
  }

    /**
     * Processes the given list of file or directory names into an array
     * of 'File' objects.  Any file name may contain path information,
     * which will also be used for any later file names that do not
     * contain path information.  If no path information has been given
     * then the current-working directory will be used.  File names may
     * contain "glob-style" wildcard characters ('*' and '?').
     * Relative and absolute directory names may also be specified.
     * @param fileNameListStr a list of file or directory names, separated
     * by the 'File.pathSeparator' character and optional spaces.
     * @return An array 'File' objects.
     */
  public static File [] processFileNameList(String fileNameListStr)
  {
    final int fileNameListStrLen;
    if(fileNameListStr == null ||
                         (fileNameListStrLen=fileNameListStr.length()) <= 0)
    {    //no names data given
      return new File[0];         //return empty array
    }
                   //trim any leading or trailing spaces:
    fileNameListStr = fileNameListStr.trim();
    String parentDirStr = null;      //parent directory string
    String fStr,pStr;
    int ePos,sPos = 0;
    File fileObj;
    File [] fileArr;
    final Vector fileVec = new Vector();  //Vector of 'File' objects
    do
    {  //for each entry in param string; find next path separator char
      if((ePos=fileNameListStr.indexOf(File.pathSeparatorChar,sPos)) < 0)
      {
        ePos = fileNameListStrLen;     //if no separator then use end of str
      }       //check if ':' in URL was mistaken for filename separator:
      else if(File.pathSeparatorChar == ':' && ePos > sPos &&
                                            ePos + 2 < fileNameListStrLen &&
               UtilFns.isURLAddress(fileNameListStr.substring(sPos,ePos+3)))
      {  //':' was really part of URL; scan again for filename separator
        if((ePos=fileNameListStr.indexOf(File.pathSeparatorChar,ePos+2)) < 0)
          ePos = fileNameListStrLen;   //if no separator then use end of str
      }
            //create File object for current entry (save path name):
      fileObj = new File(fStr=fileNameListStr.substring(sPos,ePos).trim());
            //if current entry has a parent path then save it for use with
            // later entries without parent paths; if current entry does
            // not have a parent path and previous parent path was saved
            // then use it now:
      if((pStr=fileObj.getParent()) != null)
        parentDirStr = pStr;                   //save parent path string
      else if(parentDirStr != null)
        fileObj = new File(parentDirStr,fStr); //use prev parent path str
      if(containsGlobChars(fStr))
      {  //entry contains wildcard characters; resolve to array of 'File's
        if((fileArr=resolveNameToFileObjs(fileObj.getParent(),
                                                fileObj.getName())) != null)
        {     //resolved to array OK; add to Vector of 'File' objects
          fileVec.addAll(Arrays.asList(fileArr));
        }
      }
      else
        fileVec.add(fileObj);     //add current entry to Vector
    }       //move start pos past path separator; loop if more data
    while((sPos=ePos+1) < fileNameListStrLen);
    try
    {       //convert Vector of File object to an array:
      fileArr = (File [])fileVec.toArray(new File[fileVec.size()]);
    }
    catch(Exception ex)
    {       //exception error occurred (shouldn't happen)
      fileArr = new File[0];      //setup empty array
    }
    return fileArr;          //return array of 'File' objects
  }

    /**
     * Resolves the given name to an array of 'File' objects.
     * @param searchPathStr path to search for files, or null or empty
     * string to use the current-working directory (".").
     * @param fileNameStr the name string, which may contain "glob-style"
     * wildcard characters ('*' and '?').
     * @return An array of 'File' objects resolved for the given name, or
     * an empty array if none could be resolved.
     */
  public static File [] resolveNameToFileObjs(String searchPathStr,
                                                         String fileNameStr)
  {
    File [] fileArr = null;
    if(fileNameStr != null && fileNameStr.length() > 0)
    {    //parameter name contains data
      if(searchPathStr == null || searchPathStr.length() <= 0)
        searchPathStr = ".";      //if no search path then use local dir
      RE reObj;         //handle for regular expression object
      try     //create regular expression object for 'accept()' method
      {       // with pattern of filenames to search for:
        reObj = new RE(globToRegExString(fileNameStr));
      }
      catch(Exception ex)
      {       //if error then set to null
        reObj = null;
      }
      final RE reAcceptObj;
      if((reAcceptObj=reObj) != null)
      {  //regular expression object created OK
        try
        {          //create File object for directory to be searched:
          final File searchFileObj = new File(searchPathStr);
          fileArr = searchFileObj.listFiles(new FilenameFilter()
              {         //create anonymous FilenameFilter class:
                public boolean accept(File dirObj,String nameStr)
                {
                  return reAcceptObj.isMatch(nameStr);
                }
              });
        }
        catch(Exception ex) {}    //if exception then 'fileArr' will be null
      }
    }
    if(fileArr == null)           //if null handle then
      fileArr = new File[0];      //make it an empty array
    return fileArr;          //return array of 'File' objects
  }

    /**
     * Appends all elements in 'dArr2' to end of 'dArr1'.
     * @param fArr1 first value array
     * @param fArr2 second value array
     * @return A new array of float values.
     */
  public static float [] appendArrays(float [] fArr1,float [] fArr2)
  {
         //get length of arrays:
    final int fArr1Len = (fArr1 != null) ? fArr1.length : 0;
    final int fArr2Len = (fArr2 != null) ? fArr2.length : 0;
    final float [] retArr = new float[fArr1Len+fArr2Len];
    int i, rIdx = 0;
    for(i=0; i<fArr1Len; ++i)     //put in values from first array
      retArr[rIdx++] = fArr1[i];
    for(i=0; i<fArr2Len; ++i)     //put in values from second array
      retArr[rIdx++] = fArr2[i];
    return retArr;                //return new array of values
  }

    /**
     * Converts an array of 'float' values to an array of 'double' values.
     * @param fArr value array
     * @return A new array of 'double' values converted from the input
     * array.
     */
  public static double [] floatToDoubleArray(float [] fArr)
  {
    final int len = (fArr != null) ? fArr.length : 0; //number of values
    final double [] retArr = new double[len];         //create double array
    for(int i=0; i<len; ++i)                          //for each value
      retArr[i] = (double)(fArr[i]);                  //convert value
    return retArr;                                    //return new array
  }

    /**
     * @return true if the given date is before the given time object
     * (non-inclusive).
     * @param dateObj date object
     * @param timeObj time object
     */
  public static boolean isBeforeTime(Date dateObj,Time timeObj)
  {
    if(dateObj == null || timeObj == null)
      return false;
    final Date targetDateObj;
    try
    {              //convert FISSURES time string to Date:
      targetDateObj = (new ISOTime(timeObj.date_time)).getDate();
    }
    catch(Exception ex)
    {    //error converting date
      return false;
    }
              //return true if given date before time value:
    return dateObj.before(targetDateObj);
  }

    /**
     * @return true if the given date is after the given time object
     * (non-inclusive).
     * @param dateObj date object
     * @param timeObj time object
     */
  public static boolean isAfterTime(Date dateObj,Time timeObj)
  {
    if(dateObj == null || timeObj == null)
      return false;
    final Date targetDateObj;
    try
    {              //convert FISSURES time string to Date:
      targetDateObj = (new ISOTime(timeObj.date_time)).getDate();
    }
    catch(Exception ex)
    {    //error converting date
      return false;
    }
              //return true if given date after time value:
    return dateObj.after(targetDateObj);
  }

    /**
     * @return true if the given date is with the given time range
     * (inclusive).
     * @param dateObj date object
     * @param timeRangeObj time range object
     */
  public static boolean inTimeRange(Date dateObj,TimeRange timeRangeObj)
  {
    if(dateObj == null || timeRangeObj == null)
      return false;
    final Date rStartDateObj,rEndDateObj;
    try
    {
      rStartDateObj =         //convert FISSURES time string to Date:
                 (new ISOTime(timeRangeObj.start_time.date_time)).getDate();
      rEndDateObj =           //convert FISSURES time string to Date:
                   (new ISOTime(timeRangeObj.end_time.date_time)).getDate();
    }
    catch(Exception ex)
    {    //error converting dates
      return false;
    }
              //return true if given date not before and not after range:
    return !dateObj.before(rStartDateObj) && !dateObj.after(rEndDateObj);
  }

    /**
     * @return true if the range of dates defined the by given dates
     * intersects with the given time-range object.  If 'endDateObj' is
     * null then 'beginDateObj' is treated as a single date that must be
     * within the time-range.  If both are null then true is returned.
     * @param beginDateObj begin date object
     * @param endDateObj end date object
     * @param timeRangeObj time range object
     */
  public static boolean datesInTimeRange(Date beginDateObj,Date endDateObj,
                                                     TimeRange timeRangeObj)
  {
    final Date rStartDateObj,rEndDateObj;
    try
    {
      rStartDateObj =         //convert FISSURES time string to Date:
                 (new ISOTime(timeRangeObj.start_time.date_time)).getDate();
      rEndDateObj =           //convert FISSURES time string to Date:
                   (new ISOTime(timeRangeObj.end_time.date_time)).getDate();
    }
    catch(Exception ex)
    {    //error converting dates
      return false;
    }
    if(endDateObj == null)
    {    //no end-date given
      if(beginDateObj == null)         //if also no begin-date given then
        return true;                   //just return true
              //return true if given date not before and not after range:
      return !beginDateObj.before(rStartDateObj) &&
                                           !beginDateObj.after(rEndDateObj);
    }
                             //return true if begin-date not after range
                             // and end-date not before range:
    return !beginDateObj.after(rEndDateObj) &&
                                          !endDateObj.before(rStartDateObj);
  }

    /**
     * Compares the given time objects.
     * @param time1Obj time object 1
     * @param time2Obj time object 2
     * @return a value less than 0 if time1Obj is before time2Obj, a value
     * greater than 0 if time1Obj is after time2Obj, and the value 0 if the
     * the given time objects are equal or if an error is detected.
     *
     */
  public static int compareTimes(Time time1Obj,Time time2Obj)
  {
    if(time1Obj == null)                    //handle null cases
      return (time2Obj == null) ? 0 : -1;
    if(time2Obj == null)
      return (time1Obj == null) ? 0 : 1;

    final Date date1Obj,date2Obj;
    try
    {              //convert FISSURES time strings to Dates:
      date1Obj = (new ISOTime(time1Obj.date_time)).getDate();
      date2Obj = (new ISOTime(time2Obj.date_time)).getDate();
    }
    catch(Exception ex)
    {    //error converting dates
      return 0;
    }
                        //compare Date objects and return result:
    return date1Obj.compareTo(date2Obj);
  }

    /**
     * Converts a FISSURES 'Time' object to a 'Date' object.
     * @param timeObj time object
     * @return A 'Date' object, or null if the given 'Time' object could
     * not be converted.
     */
  public static Date fissTimeToDate(Time timeObj)
  {
    try
    {              //convert FISSURES time string to Date and return:
      return (new ISOTime(timeObj.date_time)).getDate();
    }
    catch(Exception ex)
    {    //error converting date
      return null;
    }
  }

    /**
     * Converts a FISSURES 'Time' object to a String with the format
     * ""yyyy,D,HH:mm:ss.SSS".  If the trailing milliseconds value is
     * ".000" then it is trimmed.
     * @param timeObj time object
     * @return A String representing the date/time, or "???" if the given
     * 'Time' object could not be converted.
     */
  public static String fissTimeToString(Time timeObj)
  {
    final Date dateObj;      //convert 'Time' to 'Date' object:
    if((dateObj=fissTimeToDate(timeObj)) == null)
      return "???";          //if error then return "???"
    return fissDateToString(dateObj);
  }

    /**
     * Converts a 'Date' object to a String with the format
     * ""yyyy,D,HH:mm:ss.SSS".  If the trailing milliseconds value is
     * ".000" then it is trimmed.
     * @param dateObj date object
     * @return A String representing the date/time.
     */
  public static String fissDateToString(Date dateObj)
  {
                             //convert 'Date' object to a String:
    final String retStr = respDateMsFormatter.format(dateObj);
    if(!retStr.endsWith(".000"))       //if does not end with ".000" then
      return retStr;                   //return date/time String
                             //trim ".000" from end of String and return:
    return retStr.substring(0,retStr.length()-4);
  }

    /**
     * Enters the given property value if the current value is null.
     * @param propObj the 'Properties' object holding the properties.
     * @param nameStr the name of the given property.
     * @param defaultStr the default value to be entered.
     * @return true if the given default value was entered, false if not
     * (the property already had a value).
     */
  public static boolean enterDefaultPropValue(Properties propObj,
                                           String nameStr,String defaultStr)
  {
    if(propObj != null)
    {    //Properties object OK
      if(propObj.getProperty(nameStr) == null)
      {       //current value is empty; enter "default" value
        propObj.setProperty(nameStr,defaultStr);
        return true;         //indicate value entered
      }
    }
    return false;            //indicate value not entered
  }

    /**
     * Checks the given parameters (used to generate an array of
     * frequency values) for validity.
     * @param minFreq the minimum frequency to generate output for.
     * @param maxFreq the maximum frequency to generate output for.
     * @param numFreqs the number of frequencies to generate output for.
     * @param logSpacingFlag log spacing flag
     * @return an error message, or null if the parameters are valid.
     */
  public static String checkFreqArrayParams(double minFreq,double maxFreq,
                                        int numFreqs,boolean logSpacingFlag)
  {

         //check that at least 'minFreq' parameter was entered:
    if(numFreqs <= 0)
      return "Frequency values not specified";
    if(numFreqs > 1 && !isZero(maxFreq-minFreq) &&
                                     logSpacingFlag && isNegOrZero(minFreq))
    {    //more than one freq, log spacing and min is <= zero
      return
           "Frequency value of zero or less not allowed with 'log' spacing";
    }
    return null;        //return null for parameters OK
  }

    /**
     * Generates an array of frequency values.
     * @param minFreq the minimum frequency to generate output for.
     * @param maxFreq the maximum frequency to generate output for.
     * @param numFreqs the number of frequencies to generate output for.
     * @param logSpacingFlag log spacing flag
     * @return A new array of double values, or null if an error was
     * detected.
     */
  public static double [] generateFreqArray(double minFreq,double maxFreq,
                                        int numFreqs,boolean logSpacingFlag)
  {
    if(numFreqs <= 0)             //if no frequencies then
      return null;                //return null for error
         //create array for frequency values:
    final double [] freqArr = new double[numFreqs];
    if(numFreqs > 1 && !isZero(maxFreq-minFreq))
    {    //more than one frequency
      if(minFreq > maxFreq)
      {  //min greater than max; exchange values
        final double tempVal = maxFreq;
        maxFreq = minFreq;
        minFreq = tempVal;
      }
      if(logSpacingFlag)
      {  //logarithmic spacing selected
        if(isNegOrZero(minFreq))       //if min frequency <= zero then
          return null;                 //return null for error
                   //calculate multiplier value:
        final double multVal = Math.pow(maxFreq/minFreq,1.0/(numFreqs-1));
        double fVal = minFreq;    //start with minimum frequency
        int i = 0;                //initialize index
        while(true)
        {     //for each frequency
          freqArr[i] = fVal;      //enter value into array
          if(++i >= numFreqs)     //increment index
            break;                //if no more then exit loop
          fVal *= multVal;        //calculate next value
        }
      }
      else
      {  //linear spacing selected; calculate multiplier value
        final double multVal = (maxFreq-minFreq) / (numFreqs-1);
        for(int i=0; i<numFreqs; ++i)            //for each frequency
          freqArr[i] = minFreq + i*multVal;      //enter value into array
      }
    }
    else      //single frequency
      freqArr[0] = minFreq;       //enter frequency value
    return freqArr;
  }

  /**
   * Extracts and restores a URL string that has been saved into a 'File'
   * object.
   * @param pathStr the path string from the 'File' object.
   * @return The URL string that was entered into the 'File' object.
   */
  public static String fileObjPathToUrlStr(String pathStr)
  {
         //convert any backslashes back to slashes:
    final String retStr = pathStr.replace('\\','/');
         //fix "://" string that was converted to ":/":
    if(retStr.length() > 6)
    {
      if(retStr.startsWith("http:/") && retStr.charAt(6) != '/')
        return retStr.substring(0,6) + '/' + retStr.substring(6);
      if(retStr.startsWith("ftp:/") && retStr.charAt(5) != '/')
        return retStr.substring(0,5) + '/' + retStr.substring(5);
      if(retStr.startsWith("jar:/") && retStr.charAt(5) != '/')
        return retStr.substring(0,5) + '/' + retStr.substring(5);
    }
    return retStr;
  }

    /**
     * Returns a String describing the given response in text "RESP" format.
     * @param channelIdObj the channel ID associated with the response.
     * @param respEndDateObj end date for channel ID, or null for none.
     * @param respObj the response information.
     * @return A String describing the given response in text "RESP" format.
     */
  public static String getTextFormatRespStr(ChannelId channelIdObj,
                                       Date respEndDateObj,Response respObj)
  {
    try
    {              //create 'Instrumentation' object:
      final Instrumentation instObj = new Instrumentation() {};
      instObj.the_response = respObj;       //enter response object
                   //create and enter time range for response
                   // (if no end date then use "2599,365,23:59:59"):
      instObj.effective_time = new TimeRange(channelIdObj.begin_time,
                                new Time(RespUtils.fissDateFormatter.format(
         ((respEndDateObj != null) ? respEndDateObj : NO_ENDDATE_OBJ)),-1));
      final String retStr =
                          ResponsePrint.printResponse(channelIdObj,instObj);
      return (retStr != null) ? retStr.trim() : null;
    }
    catch(Exception ex)
    {    //some kind of exception error; show error message
      System.err.println("Error generating RESP-format text:");
      ex.printStackTrace();
    }
    return UtilFns.EMPTY_STRING;
  }


//  public static void main(String [] args)
//  {
//    String str;
//    while(true)
//    {
//      System.out.print("Enter unit string ('Q' to quit):  ");
//      if((str=UtilFns.getUserConsoleString()) == null ||
//                                                  str.equalsIgnoreCase("Q"))
//      {
//        return;
//      }
//      System.out.println(respStrToUnit(str));
//    }
//  }
}
