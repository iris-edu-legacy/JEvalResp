//Run.java:  Entry point and managing module for 'JEvalResp'.
//
//  12/18/2001 -- [ET]  Version 0.92Beta:  Initial release version.
//    3/1/2002 -- [ET]  Version 0.93Beta:  Added "net" support (including
//                      'propsFile' parameter) for fetching responses from
//                      FISSURES servers.
//   4/30/2002 -- [ET]  Version 0.94Beta:  Changed source code directories
//                      from using "netsrc" to "filesrc" ("net" support
//                      becomes default; file-only version uses "filesrc"
//                      classes); merged "RunBase.java" into this file and
//                      improved modularization; added 'RunExt' class.
//    5/2/2002 -- [ET]  Version 0.95Beta:  Changed 'generateResponses()'
//                      methods to have their 'RespCallback' object be
//                      passed in as a parameter; other internal changes.
//   5/14/2002 -- [ET]  Version 0.96Beta:  Changes to the 'RunExt' class.
//   5/15/2002 -- [ET]  Version 0.961Beta:  Added verbose-mode message to
//                      show what output files were generated.
//   5/22/2002 -- [ET]  Version 0.962Beta:  Added new verbose messages to
//                      indicate number of items retrieved from FISSURES
//                      server and to indicate when instrumentation for
//                      channel object not found on server.
//   5/28/2002 -- [ET]  Version 0.963Beta:  Fixed bug where errors were
//                      sometimes reported twice.
//   6/10/2002 -- [ET]  Version 0.97Beta:  Added support for gain-only
//                      stages and Generic Response Blockettes (#56);
//                      modified how response delay, correction and sample-
//                      interval values are calculated; filename ('-f')
//                      parameter now accepts multiple file and directory
//                      names and filenames with wildcards.
//   6/11/2002 -- [ET]  Version 0.971Beta:  Modified help screen item for
//                      filename ('-f') parameter.
//   7/10/2002 -- [ET]  Version 0.972Beta:  Added "ap2" option for response-
//                      type that outputs a single amplitude/phase ("AP.")
//                      file; improved ability to handle date codes with
//                      with more than 3 fractional-second digits (such as
//                      "2001,360,14:50:15.2426"); added "multiOutputFlag"
//                      ('-m') parameter and support to allow multiple
//                      outputs with same "net.sta.loc.cha" code.
//   7/11/2002 -- [ET]  Version 0.973Beta:  Added optional "endYear" ('-ey'),
//                      "endDay" ('-ed') and "endTime" ('-et') parameters
//                      and time-range implementation.
//   7/15/2002 -- [ET]  Version 0.974Beta:  Implemented time-range for
//                      network server access; changes to 'RunExt' module.
//   7/16/2002 -- [ET]  Version 0.975Beta:  Minor change to network-access
//                      debug output (available via '-debug' parameter).
//   7/17/2002 -- [ET]  Version 0.976Beta:  Changed occurrences of
//                      "net.sta.cha.loc" to "net.sta.loc.cha".
//    8/6/2002 -- [ET]  Version 0.977Beta:  Added support for fetching and
//                      displaying end-times for responses; added support
//                      for optional header information in generated output
//                      files ('-h' parameter).
//    8/7/2002 -- [ET]  Version 1.0:  Release version.
//   3/26/2003 -- [KF]  Version 1.01:
//                      Display version on header,
//                      Added 'outputDirectory' parameter.
//    5/6/2003 -- [ET]  Version 1.1:  Added support for using a
//                      Network DataCenter object via a path (like
//                      "edu/iris/dmc/IRIS_NetworkDC"); implemented
//                      using an iterator when fetching all channel-IDs
//                      for a network.
//  10/22/2003 -- [ET]  Version 1.2:  Implemented fix to IIR PZ
//                      transformation (to synchronize with 'evalresp'
//                      version 3.2.22).
//   1/28/2004 -- [ET]  Version 1.21:  Modified to allow FIR blockettes (61)
//                      to have a "Response Name" (F04) field.
//   3/15/2004 -- [SH]  Version 1.22:  Modified RESP file parsing to allow
//                      unneeded fields to be skipped over (allowing SHAPE-
//                      compatible RESP files to be parsed).
//   4/22/2005 -- [ET]  Version 1.27:  Modified to suppress "No matching
//                      response files found" error message when all
//                      matching response files contain errors; modified
//                      to display infinite-number value as "*" in verbose
//                      output; changed how 'calculatedDelay' is computed
//                      (by allowing FIR stages with no coefficients to
//                      affect the 'calculatedDelay' sum) to synchronize
//                      with 'evalresp'; modified to interpret gain object
//                      with '-1' values as equivalent to "no gain object"
//                      (sometimes comes from network server); added support
//                      for "use-delay" and "showInputFlag" parameters;
//                      added support for URLs as input file names;
//                      improved parsing of units in RESP file.
//   5/23/2005 -- [ET]  Version 1.5:  Public release version.
//    6/1/2005 -- [ET]  Version 1.51:  Modified to allow negative values
//                      in gain stages.
//   6/29/2005 -- [ET]  Version 1.52:  Fixed bug where response sensitivity
//                      frequency was not properly selected when no "stage 0
//                      gain" blockette was provided.
//   7/11/2005 -- [ET]  Version 1.53:  Modified to handle List Blockettes
//                      (55) that do not contain an "index" ("i") column
//                      in their data.
//  10/13/2005 -- [ET]  Version 1.54:  Modified to allow decimation and gain
//                      blockettes in stages containing list blockettes;
//                      modified to support JPlotResp ability to use
//                      AMP/PHASE files as input.
//   11/3/2005 -- [ET]  Version 1.55:  Implemented interpolation of
//                      amplitude/phase values from responses containing
//                      List blockettes (55); modified to be able to
//                      successfully parse response files that contain
//                      no "B052F03 Location:" entry; fixed support for
//                      URLs as input file names under UNIX.
//   8/22/2006 -- [ET]  Version 1.56:  Modified to support 'Tesla' units;
//                      modified to make sure that if input units are
//                      'Pascal' or 'Tesla' then output units conversion
//                      must be 'Velocity' or 'Default'.
//   5/25/2007 -- [ET]  Version 1.57:  Modified to check if any FIR
//                      coefficients filter should be normalized to 1.0
//                      at frequency zero and adjust the filter values
//                      if so.
//   7/15/2009 -- [KF]  Version 1.58:  Added 'getConsoleOut()' method.
//  11/10/2009 -- [ET]  Version 1.59:  Added 'RunDirect' class for support
//                      of input and output via method calls for processing
//                      a single response.
//   5/27/2010 -- [ET]  Version 1.594:  Added "use-estimated-delay" alias
//                      for "use-delay" parameter; modified to apply delay
//                      correction to asymmetrical FIR filters (using
//                      estimated delay if 'use-estimated-delay' is given,
//                      otherwise using correction-applied value);
//                      modified text-listing output to show "FIR_ASYM",
//                      "FIR_SYM1" and "FIR_SYM2" (instead of just "FIR");
//                      modified computation of calculated delay so it
//                      ignores stages with no coefficients; added
//                      parameters for unwrapping phase values ('-unwrap')
//                      and for using stage 0 (total) sensitivity in
//                      response calculations ('-ts'); added response-output
//                      ('-r') type "fap"; added verbose-output note for
//                      '-ts' parameter in use and warnings for stage
//                      correction-applied and estimated-delay values
//                      being negative.
//    6/1/2010 -- [ET]  Version 1.595:  Fixed processing of '-stage'
//                      parameter when single stage value is given;
//                      modified verbose output to display only start/stop
//                      stages selected via '-stage' parameter; modified
//                      output-file headers to show response-sensitivity
//                      frequency and A0 normalization factor from first
//                      stage.
//    6/3/2010 -- [ET]  Version 1.596:  Changed "A0" to "NormA0" in
//                      output-file headers.
//    8/6/2010 -- [ET]  Version 1.6:  Updated version number for release.
//   1/27/2012 -- [ET]  Version 1.65:  Added support for network access to
//                      web-services servers; fixed bug where site/location
//                      parameters were not properly processed when finding
//                      local RESP input files.
//   1/30/2012 -- [ET]  Version 1.7:  Updated version number for release.
//    4/3/2012 -- [ET]  Version 1.75:  Added support for specification of
//                      multiple web-services-server URLs; added support
//                      for '--multiSvrFlag' ('-ms') parameter.
//  10/29/2013 -- [ET]  Version 1.76:  Added command-line parameter 'b62_x'
//                      and support for Polynomial Blockette (62); added
//                      support for 'Centigrade' temperature units; modified
//                      processing of phase values with FIR_ASYM filters
//                      (per Gabi Laske); redefined processing of decimation
//                      as correction applied minus calculated delay;
//                      modified to use calculated delay with FIR_ASYM
//                      filters when 'use-estimated-delay' is false;
//                      modified 'calculateResponse()' method to use
//                      calculated delay with FIR_ASYM filters when
//                      'use-estimated-delay' is false.
//   4/11/2014 -- [ET]  Version 1.77:  Updated web-service URLs in '.prop'
//                      files.
//   8/26/2014 -- [ET]  Version 1.78:  Modified to properly handle
//                      location/site value of "--" (meaning location
//                      code empty).
//

package com.isti.jevalresp;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.ParseException;

import com.isti.jevalresp.x2r.ConvertingReader;
import edu.iris.Fissures.Unit;
import com.isti.util.UtilFns;
import com.isti.util.FileUtils;
import com.isti.util.CfgProperties;
import com.isti.util.CfgPropItem;

/**
 * Class Run is the entry point and managing module for 'JEvalResp'.
 */
public class Run
{
    /** Displayed name of program. */
    public static final String PROGRAM_NAME = "JEvalResp";
    /** Displayed program version # (+"(file)" if files-only version). */
    public static final String VERSION_NUM_STR = "1.80" +
            (NetVersionFlag.value ? UtilFns.EMPTY_STRING : "(file)");
    /** Displayed program revision string (name + version #). */
    public static final String REVISION_STR =
            PROGRAM_NAME + ", Version " + VERSION_NUM_STR;
    /** Strings for the 'outUnitsConv' (-u) parameter. */
    public static final String [] UNIT_CONV_STRS =      //def,dis,vel,acc
            OutputGenerator.UNIT_CONV_STRS;
    /** Default index value for 'outUnitsConv' (-u) parameter. */
    public static final int UNIT_CONV_DEFIDX =
            OutputGenerator.UNIT_CONV_DEFIDX;
    /** Longer versions of strings for the 'outUnitsConv' (-u) parameter. */
    public static final String [] UNIT_CONV_LONGSTRS =
            OutputGenerator.UNIT_CONV_LONGSTRS;
    /** Strings for 'typeOfSpacing' (-s) parameter. */
    public static final String TYPE_SPACE_STRS[] = { "log", "lin" };
    /** Longer versions of strings for 'typeOfSpacing' (-s) parameter. */
    public static final String TYPE_SPACE_LONGSTRS[] =
            { "Logarithmic", "Linear" };
    /** Strings for 'responseType' (-r) parameter. */
    public static final String RESP_TYPE_STRS[] = { "ap", "cs", "ap2", "fap" };
    /** Longer versions of strings for 'responseType' (-r) parameter. */
    public static final String RESP_TYPE_LONGSTRS[] =
            { "Amplitude/Phase", "Complex-Spectra",
                    "Amplitude/Phase2", "fAmplitude/Phase" };
    /** Index value (0) for "ap" response type (separate amp/phase files). */
    public static final int RESP_AP_TYPEIDX = 0;
    /** Index value (1) for "cs" response type (complex-spectra file). */
    public static final int RESP_CS_TYPEIDX = 1;
    /** Index value (2) for "ap2" response type (single amp/phase file). */
    public static final int RESP_AP2_TYPEIDX = 2;
    /** Index value (3) for "fap" response type (single, unwrapped file). */
    public static final int RESP_FAP_TYPEIDX = 3;
    /** Default value for List-blockette interpolation. */
    public static final double INTERP_TENSION_DEFVAL = 1000.0;
    /** String containing leading comment chars for output file headers. */
    public static final String HDR_CMT_STR = "# ";

    protected static final String VER1_STR = "version";  //parameter names for
    protected static final String VER2_STR = "ver";      // version info disp
    //format object for parsing time strings:
    protected static final DateFormat timeFmtObj =
            UtilFns.createDateFormatObj("HH:mm:ss.SSS",
                    UtilFns.GMT_TIME_ZONE_OBJ);

    protected String [] staNamesArray = null;      //array of station names
    protected String [] chaNamesArray = null;      //array of channel names
    protected String [] netNamesArray = null;      //array of network names
    protected String [] siteNamesArray = null;     //array of location names
    protected Calendar beginCalObj = null;         //begin date to match
    protected Calendar endCalObj = null;           //end date to match
    protected boolean minimumFrequencyFlag = false;  //true if minimum frequency entered

    protected double minFreqValue = 1.0;           //minimum frequency value
    protected double maxFreqValue = 1.0;           //maximum frequency value
    protected int numberFreqs = 1;                 //number of frequencies
    protected boolean stdioFlag = false;      //true for stdin/stdout I/O
    protected int outUnitsConvIdx = UNIT_CONV_DEFIDX;   //units index value
    protected double [] frequenciesArray = null;        //array of frequencies
    protected boolean multiOutFlag = false;   //true for multi-output
    protected boolean multiSvrFlag = false;   //true for multiple web servers
    protected boolean headerFlag = false;     //true for header in output file
    protected boolean verboseFlag = false;    //true for verbose output
    protected boolean debugFlag = false;      //true for debug messages
    protected int startStageNum = 0;          //first stage to process
    protected int stopStageNum = 0;           //last stage to process
    protected boolean logSpacingFlag = true;  //true for log frequency spacing
    protected boolean useDelayFlag = false;   //use est delay for phase calc
    protected boolean showInputFlag = false;  //show RESP input
    protected boolean listInterpOutFlag = false;   //interpolate List output
    protected boolean listInterpInFlag = false;    //interpolate List input
    protected double listInterpTension = 0.0;      //tension for List interp
    protected boolean unwrapPhaseFlag = false;     //unwrap phase values
    protected boolean totalSensitFlag = false;     //use stage 0 sensitivity
    protected double b62XValue = 0.0;         //sample value for poly blockette
    protected int respTypeIndex = RESP_AP_TYPEIDX; //idx for amp/phase output
    protected String fileNameString = UtilFns.EMPTY_STRING;  //fname entered
    protected String propsFileString = UtilFns.EMPTY_STRING; //svr-props file
    protected File outputDirectory = null;    //output directory

    protected int exitStatusValue = 0;        //exit status # returned by prog
    protected String errorMessage = null;     //generated error message

    /** Properties object for parameter processing. */
    protected final CfgProperties paramProps = new CfgProperties();

    /** RESP file name to be used. */
    protected final CfgPropItem fileNameProp = paramProps.add("fileName",
            UtilFns.EMPTY_STRING,"f","File and directory names to use");

    /** Properties file for FISSURES or web-services server. */
    protected final CfgPropItem propsFileProp = (NetVersionFlag.value) ?
            paramProps.add("propsFile",UtilFns.EMPTY_STRING,"p",
                    "Properties file for network server") : null;

    /** Output units conversion, one of:  vel, acc, dis, def. */
    protected final CfgPropItem outUnitsConvProp =
            paramProps.add("outUnitsConv",UNIT_CONV_STRS[UNIT_CONV_DEFIDX],"u",
                    "Output units (" + optionsArrToString(UNIT_CONV_STRS) + ")");

    /** Requested time of day of response to search for (HH:MM:SS). */
    protected final CfgPropItem timeOfDayProp = paramProps.add("timeOfDay",
            UtilFns.EMPTY_STRING,"t","Requested time of day (HH:MM:SS)");

    /** Type of frequency spacing, LOG or LIN. */
    protected final CfgPropItem typeOfSpacingProp =
            paramProps.add("typeOfSpacing",TYPE_SPACE_STRS[0],"s",
                    "Type of freq. spacing (" + optionsArrToString(TYPE_SPACE_STRS) + ")");

    /** Requested network ID string. */
    protected final CfgPropItem networkIdProp = paramProps.add("networkId",
            UtilFns.EMPTY_STRING,"n","Requested network ID(s)");

    /** Requested location ID string. */
    protected final CfgPropItem locationIdProp =
            paramProps.add("locationId","*","l","Requested location ID(s)");

    /** Type of response output, AP (amp/phase) or CS (complex-spectra). */
    protected final CfgPropItem responseTypeProp =
            paramProps.add("responseType",RESP_TYPE_STRS[0],"r",
                    "Type of output (" + optionsArrToString(RESP_TYPE_STRS) + ")");

    /** Start and stop stage numbers to be processed
     * (this parameter contains two integer values). */
    protected final CfgPropItem stageNumbersProp = paramProps.add(
            "stageNumbers",(new Integer[] {new Integer(-1),new Integer(-1)}),
            "stage","Start/stop stage #'s (1 or 2 values)");

    /** Flag set true to use 'stdio' for input and output (pipe). */
    protected final CfgPropItem stdioFlagProp =
            paramProps.add("stdioFlag",Boolean.FALSE,
                    "stdio","Use 'stdio' for I/O");

    /** Flag set true to enable multiple response outputs with
     *  the same 'net.sta.loc.cha' code. */
    protected final CfgPropItem multiOutFlagProp =
            paramProps.add("multiOutFlag",Boolean.FALSE,
                    "m","Enable outs with same net.sta.loc.cha");

    /** True to fetch from all specified web-services servers. */
    protected final CfgPropItem multiSvrFlagProp =
            paramProps.add("multiSvrFlag",Boolean.FALSE,
                    "ms","Fetch from multiple web servers");

    /** Year value for end of time range to be matched. */
    protected final CfgPropItem endYearProp =
            paramProps.add("endYear",new Integer(0),
                    "ey","Year value for end of time range");

    /** Julian day value for end of time range to be matched. */
    protected final CfgPropItem endDayProp =
            paramProps.add("endDay",new Integer(0),
                    "ed","Julian day val for end of time range");

    /** Time-of-day value for end of time range to be matched. */
    protected final CfgPropItem endTimeProp = paramProps.add("endTime",
            UtilFns.EMPTY_STRING,"et","Time-of-day val for end of time range");

    /** Flag set true to enable header info in output file(s). */
    protected final CfgPropItem headerFlagProp =
            paramProps.add("headerFlag",Boolean.FALSE,
                    "h","Enable header info in output file(s)");

    /** Flag set true to send verbose messages to 'stderr'. */
    protected final CfgPropItem verboseFlagProp =
            paramProps.add("verboseFlag",Boolean.FALSE,
                    "v","Send verbose messages to 'stderr'");

    /** Output directory value. */
    protected final CfgPropItem outputDirectoryProp =
            paramProps.add("outputDirectory",UtilFns.EMPTY_STRING,
                    "o","Output directory for generated files");

    /** Flag set true to use estimated delay in phase calculation. */
    protected final CfgPropItem useDelayFlagProp =
            paramProps.add("use-delay",Boolean.FALSE,
                    "use-estimated-delay","Use estimated delay in FIR_ASYM calc");

    /** Flag set true to show RESP input text (sent to stdout). */
    protected final CfgPropItem showInputFlagProp =
            paramProps.add("showInputFlag",Boolean.FALSE,
                    "i","Show RESP input text");

    /** Flag set true to interpolate List blockette output. */
    protected final CfgPropItem interpListFlagProp =
            paramProps.add("interpListFlag",Boolean.FALSE,
                    "il","Interpolate List blockette output");

    /** Flag set true to interpolate List blockette input. */
    protected final CfgPropItem interpInputFlagProp =
            paramProps.add("interpInputFlag",Boolean.FALSE,
                    "ii","Interpolate List blockette input");

    /** Tension value for List-blockette interpolation. */
    protected final CfgPropItem interpTensionProp =
            paramProps.add("interpTension",new Double(INTERP_TENSION_DEFVAL),
                    "it","Tension for List blockette interp");

    /** Flag set true to unwrap phase output values. */
    protected final CfgPropItem unwrapPhaseFlagProp =
            paramProps.add("unwrapPhaseFlag",Boolean.FALSE,
                    "unwrap","Unwrap phase output values");

    /** Flag set true to use stage 0 sensitivity instead of computed. */
    protected final CfgPropItem totalSensitFlagProp =
            paramProps.add("totalSensitFlag",Boolean.FALSE,
                    "ts","Use stage 0 (total) sensitivity");

    /** Sample value for polynomial blockette (62). */
    protected final CfgPropItem b62XValueProp =
            paramProps.add("b62XValue",new Double(0.0),
                    "b62_x","Sample value for polynomial blockette");

    /** Flag set true to send debug messages to 'stderr'. */
    protected final CfgPropItem debugFlagProp =    // (don't show on help info)
            paramProps.add("debugFlag",Boolean.FALSE,"debug",
                    UtilFns.EMPTY_STRING,true,false);

    /** Flag set to true if we should just dump the XML input
     * (hidden debug utility).
     */
    protected final CfgPropItem convertXmlFileProp = paramProps.add("convertXmlFile",
            UtilFns.EMPTY_STRING, "convert",
            "File to convert and dump (debug)");

    /**
     * Constructs a 'Run' object that processes parameters, performs
     * requested operations and generates output.
     * @param args an array of command-line parameters.
     */
    public Run(String [] args)
    {
        //process parameters and generate output:
        if(!processAndOutput(args))                  //if error then
            System.err.println(getErrorMessage());     //show message
    }

    /**
     * Constructs a 'Run' object that may be used to process parameters
     * and generate output.
     */
    public Run()
    {
    }

    /**
     * Get the console output.
     * @return the console output printstream.
     */
    public PrintStream getConsoleOut()
    {
        return System.err;
    }

    /**
     * Processes parameters, performs requested operations and generates
     * the output.
     * @param args an array of command-line parameters.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
    public boolean processAndOutput(String [] args)
    {

        //load and preprocess command-line parameters:
        final Vector paramsVec=preprocessParameters(args);

        //process switch parameters:
        if(!processSwitchParams()) {
            return false;
        }

        // if a dump was requested, do it
        if (!convertXmlFileProp.stringValue().equals(UtilFns.EMPTY_STRING)) {
            try {
                ConvertingReader.dump(convertXmlFileProp.stringValue());
                return true;
            } catch (Exception e) {
                // we have a constant for empty string, but response codes are
                // undocumented literals.  so let's use -1 here.
                setErr(-1, e.getMessage());
                return false;
            }
        }

        if (paramsVec == null) {
            return true;      //if no args then exit the program
        }

        //process non-switch parameters:
        if(!processNonSwitchParams(paramsVec)) {
            return false;
        }

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
        //create callback object that sends output to files:
        final CallbackProcWrite procWriteObj = new CallbackProcWrite(
                outUnitsConvIdx,frequenciesArray,logSpacingFlag,verboseFlag,
                startStageNum,stopStageNum,useDelayFlag,showInputFlag,
                listInterpOutFlag,listInterpInFlag,listInterpTension,
                unwrapPhaseFlag,totalSensitFlag,b62XValue,
                respTypeIndex,stdioFlag,getConsoleOut());

        //find responses and output results to files:
        final boolean resultFlag;
        if(NetVersionFlag.value && propsFileString != null &&
                propsFileString.length() > 0)
        {    //"network" version of program and server props filename was given
            //check if web-services server specified:
            if(checkWebServicesServer())
                resultFlag = generateWebResponses(procWriteObj);
            else    //if not web services then fetch from FISSURES network server:
                resultFlag = generateNetResponses(procWriteObj);
        }
        else
        {    //generate responses from input files; send to output files:
            resultFlag = generateResponses(procWriteObj);
        }
        return resultFlag;
    }

    /**
     * Process the propsFileString variable:
     * - If it's a URL, leave it as it is and return true
     * - Otherwise, assume it's a file and open it then:
     *   - If the first non-empty line is NOT a URL, it's CORBA so
     *     return false
     *   - Otherwise, read URLs and additional query parameters
     *     then construct a comma-separated set of URLs from that
     *     and set propsFileString to that value before returning
     *     true.
     *
     * So, in other words, return false for CORBA, and otherwise make sure
     * propsFileString is a comma-separated set of URLs, possible with
     * additional query parameters already set.
     *
     * @return false for CORBA, true otherwise.
     */
    protected boolean checkWebServicesServer()
    {

        // if it's a URL, just use it.
        if(UtilFns.isURLAddress(propsFileString)) return true;

        boolean foundUrl = false;
        List<String> urls = new Vector<String>();
        List<String> extraParams = new Vector<String>();

        try {
            for (String line : FileUtils.readFileToString(propsFileString).split("\n")) {
                line = line.trim();

                // skip empty and comment lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (UtilFns.isURLAddress(line)) {
                    foundUrl = true;
                    urls.add(line);
                } else {
                    // first line is not a URL, so CORBA
                    if (!foundUrl) {
                        return false;
                    } else {
                        extraParams.add(line);
                    }
                }
            }
        } catch (Exception e) {
            // this would be for no file
            // previous code punted to CORBA (see below)
            foundUrl = false;
        }

        // if we're here with no data then treat as CORBA (logic from
        // previous code)
        if (!foundUrl) return false;

        // otherwise, add the extra parameters to each URL and then
        // munge those together and save back in propsFileString
        propsFileString = "";
        for (String url: urls) {
            if (!propsFileString.isEmpty()) propsFileString += ",";
            propsFileString += url;
            for (String param: extraParams) {
                if (!propsFileString.endsWith("&")) propsFileString += "&";
                propsFileString += param;
            }
        }
        return true;
    }

    /**
     * Loads and preprocesses the command-line parameters.
     * @param args an array of command-line parameters.
     * @return a Vector of Strings, 1 for each non-switch parameter
     * found; or null if the program should be exited.
     */
    protected Vector preprocessParameters(String [] args)
    {
        Vector paramsVec;
        String str;
        Object obj;

        //load and pre-process command-line parameters:
        final boolean cmdLnProcFlag = paramProps.processCmdLnParams(
                args,true,VER1_STR,VER2_STR);

        //get count of parameters given:
        int argsLen = (args != null) ? args.length : 0;

        //decrement for each trailing empty or blank parameter:
        while(argsLen > 0 && (args[argsLen-1] == null ||
                args[argsLen-1].trim().length() <= 0))
        {
            --argsLen;
        }
        if(argsLen > 0)
        {    //at least 1 command-line param given; check for non-switch params
            if((paramsVec=paramProps.getExtraCmdLnParamsVec()) != null &&
                    !paramsVec.isEmpty() &&
                    (obj=paramsVec.firstElement()) instanceof String)
            {
                str = (String)obj;        //set to first non-switch parameter
            }
            else
                str = UtilFns.EMPTY_STRING;
        }
        else
        {    //no command-line parameters given
            str = "help";          //setup to show help screen
            paramsVec = null;      //initialize to no non-switch params
        }
        if(str != null && str.length() > 0)
        {    //extra cmd-ln params were gathered and first one is valid String
            //remove any leading switch characters:
            str = CfgProperties.removeSwitchChars(str);
            if(str.equalsIgnoreCase("help") || str.equalsIgnoreCase("h") ||
                    str.equals("?"))
            {  //parameter matches one for help screen request; show data
                System.out.println(REVISION_STR + " parameters:");
                System.out.println(" stationList channelList year julianDay " +
                        "minFreq maxFreq numFreqs [options]");
                System.out.println(PROGRAM_NAME + " options:");
                System.out.println(paramProps.getHelpScreenData());
                return null;              //return false to exit program
            }
            else if(str.equalsIgnoreCase(VER1_STR) ||
                    str.equalsIgnoreCase(VER2_STR))
            {  //parameter matches one for version information request; show data
                System.out.println(REVISION_STR);
                return null;              //return false to exit program
            }
        }
        if(!cmdLnProcFlag)
        {    //error processing cmd-line params or config file; show error
            getConsoleOut().println(PROGRAM_NAME + ":  " +
                    paramProps.getErrorMessage());
            exitStatusValue = 1;        //setup exit status error code
            return null;                //return false to exit program
        }
/*
    System.out.println(PROGRAM_NAME + ":");
    if(paramsVec != null && !paramsVec.isEmpty())
    {
      System.out.println("Non-switch params:  " +
                        UtilFns.enumToQuotedStrings(paramsVec.elements()));
    }
    System.out.println("Switch params:");
    System.out.println(paramProps.getDisplayString());
*/
        return paramsVec;
    }

    /**
     * Processes the non-switch parameters that were given on the command
     * line.
     * @param paramsVec a Vector of Strings, 1 for each non-switch
     * parameter.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
    protected boolean processNonSwitchParams(Vector paramsVec)
    {
        if(paramsVec != null)
        {    //Vector of non-switch parameter exists
            Object obj;
            String str;
            int val, paramCount = 0;
            final Enumeration e = paramsVec.elements();     //get enum of params
            while(e.hasMoreElements())
            {  //for each non-switch parameter specified
                if((obj=e.nextElement()) instanceof String)
                {     //parameter String object fetched OK
                    str = (String)obj;           //set handle to String
                    switch(paramCount)
                    {   //process parameter based on position in command line
                        case 0:          //station list
                            if(str.length() > 0)
                            {    //string contains data; convert list to array
                                if((staNamesArray=listToStringArray(str)) == null)
                                {  //error occurred; set error code and message
                                    setErr(paramCount+1,
                                            "Error in 'stationList' parameter (\"" + str + "\")");
                                    return false;
                                }
                            }
                            break;
                        case 1:          //channel list
                            if(str.length() > 0)
                            {    //string contains data; convert list to array
                                if((chaNamesArray=listToStringArray(str)) == null)
                                {  //error occurred; set error code and message
                                    setErr(paramCount+1,
                                            "Error in 'channelList' parameter (\"" + str + "\")");
                                    return false;
                                }
                            }
                            break;
                        case 2:          //year
                            if(str.length() > 0 && !str.equals("*"))
                            {    //string contains data and is not wildcard string
                                try
                                {       //convert string to integer:
                                    val = Integer.parseInt(str);
                                }
                                catch(NumberFormatException ex)
                                {       //error converting; set error code and message
                                    setErr(paramCount+1,
                                            "Error in 'year' parameter (" + str + ")");
                                    return false;
                                }
                                //create calender object with year value:
                                beginCalObj = Calendar.getInstance(
                                        UtilFns.GMT_TIME_ZONE_OBJ);
                                beginCalObj.clear();
                                beginCalObj.set(Calendar.YEAR,val);
                            }
                            break;
                        case 3:          //julian day
                            if(str.length() > 0 && !str.equals("*"))
                            {    //string contains data and is not wildcard string
                                try
                                {       //convert string to integer:
                                    val = Integer.parseInt(str);
                                }
                                catch(NumberFormatException ex)
                                {       //error converting; set error code and message
                                    setErr(paramCount+1,
                                            "Error in 'julianDay' parameter (" + str + ")");
                                    return false;
                                }
                                //if calender object OK then enter j-day value:
                                if(beginCalObj != null)
                                    beginCalObj.set(Calendar.DAY_OF_YEAR,val);
                            }
                            break;
                        case 4:          //minimum frequency
                            if(str.length() > 0)
                            {    //string contains data
                                try
                                {       //convert string to double:
                                    minFreqValue = Double.parseDouble(str);
                                }
                                catch(NumberFormatException ex)
                                {       //error converting; set error code and message
                                    setErr(paramCount+1,
                                            "Error in 'minFreq' parameter (" + str + ")");
                                    return false;
                                }
                                minimumFrequencyFlag = true;       //indicate minimum frequency entered
                            }
                            break;
                        case 5:          //maximum frequency
                            if(str.length() > 0 && minimumFrequencyFlag)
                            {    //string contains data and minimum frequency entered
                                try
                                {       //convert string to double:
                                    maxFreqValue = Double.parseDouble(str);
                                }
                                catch(NumberFormatException ex)
                                {       //error converting; set error code and message
                                    setErr(paramCount+1,
                                            "Error in 'maxFreq' parameter (" + str + ")");
                                    return false;
                                }
                            }
                            break;
                        case 6:          //number of frequencies
                            if(str.length() > 0 && minimumFrequencyFlag)
                            {    //string contains data and minimum frequency entered
                                try
                                {       //convert string to integer:
                                    if((numberFreqs=Integer.parseInt(str)) < 1)
                                    {     //value out of range
                                        setErr(paramCount+1,"Value of 'numFreqs' parameter (" +
                                                numberFreqs + ") out of range");
                                        return false;
                                    }
                                }
                                catch(NumberFormatException ex)
                                {       //error converting; set error code and message
                                    setErr(paramCount+1,
                                            "Error in 'numFreqs' parameter (" + str + ")");
                                    return false;
                                }
                            }
                            break;
                        default:
                            if(str.length() > 0 &&
                                    (str.charAt(0) == CfgPropItem.SWITCH1_CHAR ||
                                            str.charAt(0) == CfgPropItem.SWITCH2_CHAR))
                            {         //extra parameter is a switch parameter
                                setErr(1,"Illegal parameter (\"" + str + "\")");
                            }
                            else      //extra parameter is not a switch parameter
                                setErr(paramCount+1,"Too many parameters (\"" + str + "\")");
                            return false;
                    }
                    ++paramCount;                //increment parameter count
                }
            }
        }
        return true;
    }

    /**
     * Processes the switch parameters defined by the properties object.
     * @return true if successful, false if a parameter error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     */
    protected boolean processSwitchParams()
    {
        //convert output units conversion param string to index value
        // (convert array to list to do 'indexOf()' search):
        if((outUnitsConvIdx=Arrays.asList(UNIT_CONV_STRS).indexOf(
                outUnitsConvProp.stringValue().toLowerCase())) < 0)
        {    //units conversion value not matched; set error code and message
            setErr(11,"Illegal output units conversion parameter (\"" +
                    outUnitsConvProp.stringValue() + "\")");
            return false;
        }
        //process time of day parameter:
        if(timeOfDayProp.stringValue().length() > 0 && beginCalObj != null)
        {    //time of day and 'year' parameters were specified
            //process time of day ("HH:MM:SS") string:
            if(!addTimeToCalendar(beginCalObj,timeOfDayProp.stringValue()))
            {        //error processing string; set error code and message
                setErr(12,"Unable to interpret time of day parameter (\"" +
                        timeOfDayProp.stringValue() + "\")");
                return false;
            }
        }
        int val;
        //process type of spacing parameter:
        if((val=Arrays.asList(TYPE_SPACE_STRS).indexOf(
                typeOfSpacingProp.stringValue().toLowerCase())) < 0)
        {    //type of spacing value not matched; set error code and message
            setErr(13,"Illegal type of spacing parameter (\"" +
                    typeOfSpacingProp.stringValue() + "\")");
            return false;
        }              //set flag true if 'log' spacing selected:
        logSpacingFlag = (val == 0);
        //process response type parameter:
        if((respTypeIndex=Arrays.asList(RESP_TYPE_STRS).indexOf(
                responseTypeProp.stringValue().toLowerCase())) < 0)
        {    //response type value not matched; set error code and message
            setErr(13,"Illegal response type parameter (\"" +
                    responseTypeProp.stringValue() + "\")");
            return false;
        }
        //process network ID parameter (support list of names):
        if(networkIdProp.stringValue().length() > 0 && (netNamesArray=
                listToStringArray(networkIdProp.stringValue())) == null)
        {    //error occurred; set error code and message
            setErr(14,"Error in network ID parameter (\"" +
                    networkIdProp.stringValue() + "\")");
            return false;
        }
        //process location/site ID parameter (support list of names):
        if(locationIdProp.stringValue().length() > 0 && (siteNamesArray=
                listToStringArray(locationIdProp.stringValue())) == null)
        {    //error occurred; set error code and message
            setErr(14,"Error in location ID parameter (\"" +
                    locationIdProp.stringValue() + "\")");
            return false;
        }
        //pull start & stop stage numbers out of parameter item object:
        Integer [] intArr;
        final Object obj;
        if((obj=stageNumbersProp.getValue()) instanceof Integer[] &&
                (intArr=(Integer [])obj).length >= 2)
        {    //parameter item object contains Integer array with >= 2 elements
            //enter start/stop values from parameter array:
            enterStartStopStageNums(intArr[0],intArr[1]);
        }
        useDelayFlag = useDelayFlagProp.booleanValue();   //true if 'useDelay'
        showInputFlag = showInputFlagProp.booleanValue(); //true if 'showInput'
        stdioFlag = stdioFlagProp.booleanValue();    //set true if 'stdio' mode
        listInterpOutFlag = interpListFlagProp.booleanValue();
        listInterpInFlag = interpInputFlagProp.booleanValue();
        listInterpTension = interpTensionProp.doubleValue();
        unwrapPhaseFlag = unwrapPhaseFlagProp.booleanValue();
        totalSensitFlag = totalSensitFlagProp.booleanValue();
        multiOutFlag = multiOutFlagProp.booleanValue();   //true if multi-output
        multiSvrFlag = multiSvrFlagProp.booleanValue();   //true if multi-servers
        headerFlag = headerFlagProp.booleanValue();  //true for header in output
        debugFlag = debugFlagProp.booleanValue();    //true if debug msgs enabled
        b62XValue = b62XValueProp.doubleValue();
        if(b62XValueProp.getCmdLnLoadedFlag() && b62XValue <= 0.0)
        {  //b62_x parameter value was specified and is not positive; set error
            setErr(21,"Illegal 'b62_x' parameter value (\"" +
                    b62XValueProp.stringValue() + "\"); must be > 0");
            return false;
        }
        //process year value for end of time range to be matched:
        if((val=endYearProp.intValue()) > 0)
        {    //end-year value was given; create calender object
            endCalObj = Calendar.getInstance(UtilFns.GMT_TIME_ZONE_OBJ);
            endCalObj.clear();                    //clear current value
            endCalObj.set(Calendar.YEAR,val);     //enter year value
        }
        //process julian day value for end of time range to be matched:
        if((val=endDayProp.intValue()) > 0)
        {    //end-day value was given
            if(endCalObj != null)                      //if calender object OK
                endCalObj.set(Calendar.DAY_OF_YEAR,val); // then enter j-day value
        }
        //process time-of-day value for end of time range to be matched:
        if(endTimeProp.stringValue().length() > 0 && endCalObj != null)
        {    //time of day and 'year' for end-time were specified
            //process time of day ("HH:MM:SS") string:
            if(!addTimeToCalendar(endCalObj,endTimeProp.stringValue()))
            {        //error processing string; set error code and message
                setErr(14,"Unable to interpret time-of-day for end of time range " +
                        "parameter (\"" + endTimeProp.stringValue() + "\")");
                return false;
            }
        }
        verboseFlag = verboseFlagProp.booleanValue();     //true if verbose mode
        //save 'filename' parameter:
        fileNameString = fileNameProp.stringValue().trim();
        //save 'propsFile' parameter:
        propsFileString = (propsFileProp != null) ?
                propsFileProp.stringValue().trim() : UtilFns.EMPTY_STRING;
        if(NetVersionFlag.value)
        {    //"network" version of program
            if(propsFileString.length() > 0)
            {  //'propsFile' parameter given
                if(fileNameString.length() > 0)
                {  //'filename' parameter also given; set error code and message
                    setErr(16,"Cannot specify both \"fileName\" ('f') and " +
                            "\"propsFile\" ('p') parameters");
                    return false;
                }
            }
        }

        //get output directory text
        String outputDirectoryText = outputDirectoryProp.stringValue().trim();
        if (outputDirectoryText.length() > 0)
        {
            File od = new File(outputDirectoryText);  //create output directory file
            if (!od.exists())  //if the directory does not exist
            {
                setErr(17,"Output directory does not exist: " +
                        outputDirectoryProp.stringValue());
            }
            else
            {
                outputDirectory = od;  //save the output directory
            }
        }
        return true;
    }

    /**
     * Generates responses from input files.
     * @return true if successful, false if an error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     * @param respCallBackObj a 'RespCallback' object whose 'responseInfo()'
     * method will be called to report on each response found.
     */
    protected boolean generateResponses(RespCallback respCallBackObj)
    {
        if(verboseFlag)     //if verbose mode then show message
            getConsoleOut().println("<< " + REVISION_STR + " Response Output >>");
        //if Calendar object exists then create begin-Date object:
        final Date beginDateObj = (beginCalObj != null) ?
                beginCalObj.getTime() : null;
        //if Calendar object exists then create end-Date object:
        final Date endDateObj = (endCalObj != null) ?
                endCalObj.getTime() : null;
        //create response processor object:
        final RespProcessor respProcObj = new RespProcessor(
                multiOutFlag,headerFlag,outputDirectory);
        respCallBackObj.setRespProcObj(respProcObj);      //set object to use
        //find responses (each one is processed and written via
        // callback through the 'RespCallback' object):
        if(!respProcObj.findResponses(staNamesArray,chaNamesArray,netNamesArray,
                siteNamesArray,beginDateObj,endDateObj,fileNameString,
                stdioFlag,respCallBackObj))
        {    //error finding or processing responses; set error code and message
            setErr(19,respProcObj.getErrorMessage());
            return false;
        }
        //if exit status code is zero and errors occurred
        // then setup a non-zero exit status code:
        if(exitStatusValue == 0 && respProcObj.getNumberErrors() > 0)
            exitStatusValue = 20;
        return true;             //return OK code (no error message)
    }

    /**
     * Generates responses via CORBA connection to a FISSURES server.
     * @return true if successful, false if an error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     * @param respCallBackObj a 'RespCallback' object whose 'responseInfo()'
     * method will be called to report on each response found.
     */
    protected boolean generateNetResponses(RespCallback respCallBackObj)
    {
        //load properites object with system properties:
        final Properties propsObj = System.getProperties();
        final InputStream inStm;
        try
        {         //open input stream to properites file:
            inStm = new BufferedInputStream(new FileInputStream(propsFileString));
        }
        catch(Exception ex)
        {              //error opening input stream to properites file
            setErr(16,"Unable to open server property file \"" +
                    propsFileString + "\":  " + ex);
            return false;
        }
        try
        {         //load data from properties file:
            propsObj.load(inStm);
        }
        catch(Exception ex)
        {              //error loading data from properties file
            setErr(16,"Error loading server property file \"" +
                    propsFileString + "\":  " + ex);
            return false;
        }
        try { inStm.close(); }        //close properties file
        catch(Exception ex) {}        //ignore any exceptions on close
        //if CORBA properties not specified in loaded properites file
        // then put in values for ORBacus ORB:
        RespUtils.enterDefaultPropValue(propsObj,
                "org.omg.CORBA.ORBClass","com.ooc.CORBA.ORB");
        RespUtils.enterDefaultPropValue(propsObj,
                "org.omg.CORBA.ORBSingletonClass","com.ooc.CORBA.ORBSingleton");
        //create "network" response processor object:
        final RespNetProc respNetProcObj = new RespNetProc(propsObj,
                multiOutFlag,headerFlag,debugFlag,outputDirectory);
        if(respNetProcObj.getErrorFlag())
        {    //error creating "network" response processor object
            setErr(16,respNetProcObj.getErrorMessage());
            respNetProcObj.destroyORB();          //make sure ORB is deallocated
            return false;
        }
        respCallBackObj.setRespProcObj(respNetProcObj);   //set object to use
        if(verboseFlag)     //if verbose mode then show message
            getConsoleOut().println("<< " + REVISION_STR + " Response Output >>");
        //if Calendar object exists then create begin-Date object:
        final Date beginDateObj = (beginCalObj != null) ?
                beginCalObj.getTime() : null;
        //if Calendar object exists then create end-Date object:
        final Date endDateObj = (endCalObj != null) ?
                endCalObj.getTime() : null;
        //find responses (each one is processed and written via
        // callback through the 'RespCallback' object):
        if(!respNetProcObj.findNetResponses(staNamesArray,chaNamesArray,
                netNamesArray,siteNamesArray,beginDateObj,
                endDateObj,verboseFlag,respCallBackObj))
        {    //error finding or processing responses; set error code and message
            setErr(16,respNetProcObj.getErrorMessage());
            respNetProcObj.destroyORB();     //deallocate ORB resources
            return false;
        }
        respNetProcObj.destroyORB();       //deallocate ORB resources
        //if exit status code is zero and errors were flagged
        // then setup a non-zero exit status code:
        if(exitStatusValue == 0 && respNetProcObj.getErrorFlag())
            exitStatusValue = 20;
        return true;             //return OK code (no error message)
    }

    /**
     * Generates responses via a web-services server.
     * @return true if successful, false if an error was detected
     * (in which case the 'getErrorMessage()' and 'getExitStatusValue()'
     *  methods may be used to fetch information about the error.)
     * @param respCallBackObj a 'RespCallback' object whose 'responseInfo()'
     * method will be called to report on each response found.
     */
    protected boolean generateWebResponses(RespCallback respCallBackObj)
    {
        //create web-services response processor object:
        final RespWebProc respWebProcObj = new RespWebProc(propsFileString,
                multiOutFlag,headerFlag,outputDirectory,multiSvrFlag);
        respCallBackObj.setRespProcObj(respWebProcObj);   //set object to use
        if(verboseFlag)     //if verbose mode then show message
            getConsoleOut().println("<< " + REVISION_STR + " Response Output >>");
        //if Calendar object exists then create begin-Date object:
        final Date beginDateObj = (beginCalObj != null) ?
                beginCalObj.getTime() : null;
        //if Calendar object exists then create end-Date object:
        final Date endDateObj = (endCalObj != null) ? endCalObj.getTime() : null;
        //find responses (each one is processed and written via
        // callback through the 'RespCallback' object):
        if(!respWebProcObj.findWebResponses(staNamesArray,chaNamesArray,
                netNamesArray,siteNamesArray,beginDateObj,
                endDateObj,verboseFlag,respCallBackObj))
        {    //error finding or processing responses; set error code and message
            setErr(16,respWebProcObj.getErrorMessage());
            return false;
        }
        //if exit status code is zero and errors were flagged
        // then setup a non-zero exit status code:
        if(exitStatusValue == 0 && respWebProcObj.getErrorFlag())
            exitStatusValue = 20;
        return true;             //return OK code (no error message)
    }

    /**
     * Enters the given start/stop stage numbers.  If only the start-stage
     * value is given then both start/stop numbers are set to that value.
     * @param startIntObj Integer object holding the start-stage value, or
     * null or -1 for none.
     * @param stopIntObj Integer object holding the stop-stage value, or
     * null or -1 for none.
     */
    protected void enterStartStopStageNums(Integer startIntObj,
                                           Integer stopIntObj)
    {                //if start-stage value given then use value,
        // otherwise make it zero:
        if(startIntObj == null || (startStageNum=startIntObj.intValue()) < 0)
            startStageNum = 0;
        //if stop-stage value given then use value,
        // otherwise make it same as start-stage value:
        if(stopIntObj == null || (stopStageNum=stopIntObj.intValue()) < 0)
            stopStageNum = startStageNum;
    }

    /**
     * Enters error message (if none previously entered).
     * @param str error message string
     */
    protected void setErrorMessage(String str)
    {
        if(errorMessage == null)      //if no previous error then
            errorMessage = str;         //set error message
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
        setErrorMessage(errMsgStr);
    }

    /**
     * @return the exit status value for the program.
     */
    public int getExitStatusValue()
    {
        return exitStatusValue;
    }

    /**
     * Converts the given comma-separated String of items to an array of
     * Strings with each element containing one item.
     * @param str string
     * @return An array of strings, or null if an error occurred.
     */
    public static String [] listToStringArray(String str)
    {
        try
        {
            final Vector vec;      //convert String to Vector of Strings:
            if((vec=UtilFns.listStringToVector(str,',',false)) != null)
            {       //Vector OK, convert to array and return
                return (String [])(vec.toArray(new String[vec.size()]));
            }
        }
        catch(Exception ex) {}
        return null;        //if any error then return null
    }

    /**
     * Converts the given array of options strings to a displayable string.
     * @param strArr array of strings
     * @return display string
     */
    public static String optionsArrToString(String [] strArr)
    {
        final StringBuffer buff = new StringBuffer();
        if(strArr != null)
        {
            int i=0;
            while(true)
            {       //add each option string
                buff.append(strArr[i]);
                if(++i >= strArr.length)
                    break;
                buff.append("|");    //if not last then add separator
            }
        }
        return buff.toString();
    }

    /**
     * Enters the interpreted value of the given 'time' string into the
     * given Calendar object.  Any current hours/minutes/seconds values
     * in the Calendar object are overwritten.
     * @param timeStr a time string value in "HH:MM:SS", "HH:MM" or
     * "HH" format.
     * @param calObj a Calendar object to be modified.
     * @return true if successful, false if error in 'timeStr' format or
     * any null parameters.
     */
    public static boolean addTimeToCalendar(Calendar calObj,String timeStr)
    {
        if(calObj == null || timeStr == null)
            return false;          //if null parameter then return error
        int p;
        if((p=timeStr.indexOf(':')) < 0)        //if only hour value then
            timeStr += ":00:00.000";              //add ':MM:SS.SSS' before parsing
        else if(timeStr.indexOf(':',p+1) < 0)   //if only 'HH:MM' value then
            timeStr += ":00.000";                 //add ':SS.SSS' before parsing
        else if(timeStr.indexOf('.',p+1) < 0)   //if only 'HH:MM:SS' value then
            timeStr += ".000";                    //add '.SSS' before parsing
        Date dateObj;
        try
        {    //parse time string into a Date object:
            dateObj = timeFmtObj.parse(timeStr);
        }
        catch(ParseException ex)
        {
            dateObj = null;
        }
        if(dateObj == null)
            return false;     //if error parsing time string then return error
        //create local Calendar object with time value in it:
        final Calendar dateCalObj = Calendar.getInstance(
                UtilFns.GMT_TIME_ZONE_OBJ);
        dateCalObj.setTime(dateObj);       //enter parsed time value
        //put new time value into given Calendar object:
        calObj.set(Calendar.HOUR_OF_DAY,dateCalObj.get(Calendar.HOUR_OF_DAY));
        calObj.set(Calendar.MINUTE,dateCalObj.get(Calendar.MINUTE));
        calObj.set(Calendar.SECOND,dateCalObj.get(Calendar.SECOND));
        return true;
    }

    /**
     * @return a string of information about the given unit.  The
     * information includes the unit's name and, if applicable, the
     * 'UNIT_CONV_STRS[]' string for the unit.
     * @param unitObj unit object
     */
    protected static String unitInfoStr(Unit unitObj)
    {
        if(unitObj == null)      //if null handle then
            return "\"???\"";      //return null indicator string
        //create unit name string; convert 'Unit' to 'UnitImpl' object
        // to make sure that 'UnitImpl.toString()' method is used:
        final String unitNameStr = "\"" +
                RespUtils.unitToUnitImpl(unitObj) + "\"";
        final String unitConvStr;     //create unit conversion value string
        if((unitConvStr=OutputGenerator.toUnitConvString(unitObj)) != null &&
                unitConvStr.length() > 0)
        {    //unit conversion value string exists; return with name
            return unitNameStr + "(" + unitConvStr + ")";
        }
        return unitNameStr;           //return unit name
    }

    /**
     * Program entry point; creates a 'Run' object, performs operations
     * and exits with a status code (non-zero means error).
     * @param args program arguments
     */
    public static void main(String [] args)
    {
        //create 'Run' object, perform operations, get exit status value:
        final int statusVal = (new Run(args)).getExitStatusValue();
        System.exit(statusVal);       //exit program with status value
    }

}
