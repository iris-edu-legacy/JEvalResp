//RespNetProc.java:  Extension of high-level processing functions for
//                   'JEvalResp' that adds methods for finding and
//                   processing responses via FISSURES interface.
//
//    3/1/2002 -- [ET]  Initial release version.
//   5/22/2002 -- [ET]  Added new verbose messages to indicate number of
//                      items retrieved from FISSURES server and to
//                      indicate when instrumentation for channel object
//                      not found on server.
//   7/11/2002 -- [ET]  Changed 'apOutputFlag' to 'respTypeIndex'; added
//                      parameter 'multiOutputFlag' and support to allow
//                      multiple outputs with same "net.sta.loc.cha" code.
//   7/15/2002 -- [ET]  Changed find/proc-response methods to have
//                      'beginDateObj' and 'endDateObj' parameters;
//                      added 'debugFlag' to constructor.
//    8/6/2002 -- [ET]  Changed so that the response's end-date is reported
//                      via the 'responseInfo()' call; added 'logSpacingFlag'
//                      parameter to 'findAndOutputNetResponses()' method;
//                      added 'headerFlag' parameter to constructor.
//   3/26/2003 -- [KF]  Added 'outputDirectory' parameter.
//    5/6/2003 -- [ET]  Added support for using a Network DataCenter object
//                      via a path (like "edu/iris/dmc/IRIS_NetworkDC");
//                      implemented using an iterator when fetching all
//                      channel-IDs for a network.
//   2/25/2005 -- [ET]  Modified how "dummy" ORBacus objects are referenced;
//                      modified to use 'UtilFns.createDateFormatObj()'.
//    3/7/2005 -- [ET]  Added optional 'useDelayFlag' parameter to method
//                      'findAndOutputNetResponses()'.
//    4/1/2005 -- [ET]  Added optional 'showInputFlag' parameter to method
//                      'findAndOutputNetResponses()'.
//  10/25/2005 -- [ET]  Added optional List-blockette interpolation
//                      parameters to 'findAndOutputNetResponses()' method.
//   5/20/2010 -- [ET]  Added optional parameters 'unwrapPhaseFlag' and
//                      'totalSensitFlag' to 'findAndOutputNetResponses()'
//                      method.
//  10/22/2013 -- [ET]  Added optional 'b62XValue' parameter to method
//                      'findAndOutputNetResponses()'.
//

package com.isti.jevalresp;

import java.util.*;
import java.text.DateFormat;
import java.io.File;
import gnu.regexp.RE;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkFinderHelper;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelIdIter;
import edu.iris.Fissures.IfNetwork.ChannelIdIterHolder;
import edu.iris.Fissures.IfNetwork.ChannelIdSeqHolder;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.AllVTFactory;
import com.isti.util.UtilFns;
import com.isti.util.FifoHashtable;

/**
 * Class RespNetProc is an extension of high-level processing functions
 * for 'JEvalResp' that adds methods for finding and processing responses
 * via FISSURES interface.
 */
public class RespNetProc extends RespProcessor
{
    /** Property name for Network DataCenter ("fissures.networkDCname"). */
  public static final String NET_DC_PATHNAMEKEY = "fissures.networkDCname";
    /** Property name for Network Finder ("fissures.networkfindername"). */
  public static final String NET_FINDER_NAMEKEY =
                                               "fissures.networkfindername";
    /** Default Network DataCenter path ("edu/iris/dmc"). */
  public static final String DEF_NETDC_PATHSTR = "edu/iris/dmc";
    /** Default Network DataCenter object name ("IRIS_NetworkDC"). */
  public static final String DEF_NETDC_OBJNMSTR = "IRIS_NetworkDC";
    /** Default Network Finder name ("IRIS_NetworkFinder"). */
  public static final String DEF_NETFNDR_NAMESTR = "IRIS_NetworkFinder";
    /** Property name for Name Service ("ooc.orb.service.NameService"). */
  public static final String NAME_SERVICE_KEY =
                                              "ooc.orb.service.NameService";
                        //define String containing asterisk:
  private static final String asteriskString = "*";
  private static final String [] asteriskArray =      //define "*" array
                                           new String [] { asteriskString };
                        //setup date formatter for FISSURES calls:
  private static final DateFormat fDateFormatter =
                          UtilFns.createDateFormatObj("yyyyMMddHHmmss.SSSz",
                                                 UtilFns.GMT_TIME_ZONE_OBJ);
  private final Properties propertiesObj;
  private final boolean debugFlag;
  private final org.omg.CORBA_2_3.ORB orb;
  private NamingContextExt nameContextObj = null;
  private NetworkFinder networkFinderObj = null;
  private String resolveNetFinderResultStr = null;
  private NetworkAccess [] allNetworksArray = null;
              //hashtable with 'NetworkAccess' objects for keys and
              // 'ChannelId' array objects for values:
  private final FifoHashtable networkChannelIdsTable = new FifoHashtable();

              //reference ORBacus classes to force dependencies
              // to get classes into jar via JBuilder's "Archive Builder":
  private static final com.ooc.CORBA.ORB dummyOocOrbObj = null;
  private static final com.ooc.CORBA.ORBSingleton dummyOocSingObj = null;


    /**
     * Constructs object and initializes CORBA ORB used for finding and
     * processing responses via FISSURES interface.  If error then the
     * 'getErrorFlag()' method will return true and the 'getErrorMessage()'
     * method will return the description.
     * @param propsObj a 'Properties' objects holding the properties
     * to be used to initialize the CORBA ORB and find services.
     * @param multiOutputFlag true to allow multiple response outputs with
     * the same "net.sta.loc.cha" code.
     * @param headerFlag true to enable header information in the output
     * file; false for no header information.
     * @param debugFlag true to send debug messages to 'stderr'.
     * @param outputDirectory output directory.
     */
  public RespNetProc(Properties propsObj,boolean multiOutputFlag,
                  boolean headerFlag,boolean debugFlag,File outputDirectory)
  {
                                       //pass flags to parent constructor:
    super(multiOutputFlag,headerFlag,outputDirectory);
    propertiesObj = propsObj;          //save handle to properties object
    this.debugFlag = debugFlag;        //save debug flag value
    org.omg.CORBA.ORB orb0Obj;
    org.omg.CORBA_2_3.ORB orb23Obj;
    try
    {         //create CORBA ORB:
      orb0Obj = org.omg.CORBA.ORB.init(new String[0],propsObj);
      orb23Obj = (org.omg.CORBA_2_3.ORB)orb0Obj; //set handle to v2.3 ORB
    }
    catch(Exception ex)
    {         //exception error; set error message
      setErrorMessage("Error creating CORBA ORB:  " + ex);
      orb23Obj = null;
    }
    orb = orb23Obj;          //set final handle to ORB
    try
    {         //register factories for valuetype variables:
      (new AllVTFactory()).register(orb);
    }
    catch(Exception ex)
    {         //exception error; set error message
      setErrorMessage("Error registering valuetype factories:  " + ex);
    }
  }

    /**
     * Frees the resources associated with the CORBA ORB.
     */
  public void destroyORB()
  {
    try
    {         //destroy CORBA ORB:
      orb.destroy();
    }
    catch(Exception ex)
    {         //exception error; set error message
      setErrorMessage("Error destroying CORBA ORB:  " + ex);
    }
  }

    /**
     * Resolves the initial "NameService" reference.  The "nameContextObj"
     * field is set to the associated 'NamingContextExt' object.
     * @return true if sucessful, false if error (in which case
     * 'getErrorMessage()' may be used to see information about the error).
     */
  public boolean resolveNameService()
  {
    org.omg.CORBA.Object cObj;
    try
    {                   //resolve name service:
      if((cObj=orb.resolve_initial_references("NameService")) == null)
      {       //error resolving name service
        setErrorMessage(               //set error message
                  "Unable to resolve initial reference to \"NameService\"");
        return false;
      }
                        //narrow to 'NamingContextExt' object:
      nameContextObj = NamingContextExtHelper.narrow(cObj);
      return true;
    }
    catch(Exception ex)
    {
      setErrorMessage(
            "Error resolving initial reference to \"NameService\":  " + ex);
      return false;
    }
  }

    /**
     * Resolves the reference to the FISSURES 'NetworkFinder' object.
     * The "networkFinderObj" handle is set to the object.
     * @return true if sucessful, false if error (in which case
     * 'getErrorMessage()' may be used to see information about the error).
     */
  public boolean resolveNetworkFinder()
  {
    if(nameContextObj == null)
    {    //name service not yet resolved
      if(!resolveNameService())        //resolve name service
        return false;                  //if error then return
    }
        //attempt to pull Network DataCenter location from properties:
    final String pathStr,objNameStr;
    String propStr;
    boolean propGivenFlag = false;     //set true if networkDC prop given
    if(propertiesObj != null &&
          (propStr=propertiesObj.getProperty(NET_DC_PATHNAMEKEY)) != null &&
                                      (propStr=propStr.trim()).length() > 0)
    {    //property value for "fissures.networkDCname" found
      propGivenFlag = true;            //indicate property was given
      final int len;         //if surrounding quotes found then remove them:
      if((len=propStr.length()) > 1 && propStr.charAt(0) == '\"' &&
                                              propStr.charAt(len-1) == '\"')
      propStr = propStr.substring(1,len-1);
      int lastSepPos;        //separate out path and object-name parts:
      if((lastSepPos=propStr.lastIndexOf('/')) >= 0)
      {  //last path separator char found; setup path and object name
        pathStr = propStr.substring(0,lastSepPos);
        objNameStr = propStr.substring(lastSepPos+1);
      }
      else
      {  //no path separator char found
        pathStr = "";                       //no path
        objNameStr = propStr;               //use property as object name
      }
    }
    else
    {    //property value for "fissures.networkDCname" not found
      pathStr = DEF_NETDC_PATHSTR;          //use default path
      objNameStr = DEF_NETDC_OBJNMSTR;      //use default object name
    }

    NetworkDC networkDC = null;
    try
    {    //resolve Network DataCenter object on server:
      networkDC = FissuresNamingUtils.getNetworkDC(
                                         nameContextObj,pathStr,objNameStr);

      try
      {       //fetch NetworkFinder object from DataCenter:
        networkFinderObj = networkDC.a_finder();
        resolveNetFinderResultStr =         //setup result-message string
                       "NetworkFinder object from DataCenter \"" + pathStr +
                                 "/" + objNameStr + "\" resolved on server";
        return true;
      }
      catch(Exception ex)
      {
        setErrorMessage("Error fetching NetworkFinder from DataCenter \"" +
                                 pathStr + "/" + objNameStr + "\":  " + ex);
      }
    }
    catch(Exception ex)
    {
      setErrorMessage("Error finding Network DataCenter \"" + pathStr +
                                           "/" + objNameStr + "\":  " + ex);
    }

         //see if entry for "fissures.networkfindername" in properties;
         // (if so then try using it to find the Network Finder):
    if(propertiesObj == null ||
          (propStr=propertiesObj.getProperty(NET_FINDER_NAMEKEY)) == null ||
                                     (propStr=propStr.trim()).length() <= 0)
    {    //property value for "fissures.networkfindername" not found
      return false;          //return error flag
    }
    if(!propGivenFlag)       //if "fissures.networkDCname" not given then
      clearErrorMessage();   //clear any error messages generated so far
    org.omg.CORBA.Object cObj;
    try
    {              //create array for name component objects:
      final NameComponent[] nameCompArr = new NameComponent[1];
      nameCompArr[0] = new NameComponent();      //create single element
      nameCompArr[0].id = propStr;               //set name string
      nameCompArr[0].kind = "";
      if((cObj=nameContextObj.resolve(nameCompArr)) == null ||
                (networkFinderObj=NetworkFinderHelper.narrow(cObj)) == null)
      {       //resolve or narrow returned null; set error message
        setErrorMessage("Unable to narrow 'NetworkFinder' name \"" +
                                                            propStr + "\"");
        return false;
      }
      resolveNetFinderResultStr =           //setup result-message string
              "NetworkFinder object \"" + propStr + "\" resolved on server";
      return true;
    }
    catch(Exception ex)
    {
      setErrorMessage("Error narrowing 'NetworkFinder' name \"" +
                                                    propStr + "\":  " + ex);
      return false;
    }
  }

    /**
     * Finds responses (via FISSURES interface) with matching channel IDs,
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
  public boolean findAndOutputNetResponses(String [] staArr,String [] chaArr,
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
    if(!findNetResponses(staArr,chaArr,netArr,siteArr,beginDateObj,
                                                     endDateObj,verboseFlag,
                  new CallbackProcWrite(this,outUnitsConvIdx,freqArr,
                                   logSpacingFlag,verboseFlag,startStageNum,
                                    stopStageNum,useDelayFlag,showInputFlag,
                                         listInterpOutFlag,listInterpInFlag,
                                          listInterpTension,unwrapPhaseFlag,
                                    totalSensitFlag,b62XValue,respTypeIndex,
                                                     stdioFlag,System.err)))
    {    //error finding or processing responses; display error message
      System.err.println(getErrorMessage());
      return false;
    }
    return true;
  }

    /**
     * Finds responses (via FISSURES interface) with matching channel IDs,
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
  public boolean findAndOutputNetResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
                           double listInterpTension,boolean unwrapPhaseFlag,
                boolean totalSensitFlag,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputNetResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                        verboseFlag,startStageNum,stopStageNum,useDelayFlag,
                           showInputFlag,listInterpOutFlag,listInterpInFlag,
                      listInterpTension,unwrapPhaseFlag,totalSensitFlag,0.0,
                                                   respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses (via FISSURES interface) with matching channel IDs,
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
  public boolean findAndOutputNetResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                         boolean listInterpOutFlag,boolean listInterpInFlag,
               double listInterpTension,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputNetResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                        verboseFlag,startStageNum,stopStageNum,useDelayFlag,
                           showInputFlag,listInterpOutFlag,listInterpInFlag,
                 listInterpTension,false,false,0.0,respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses (via FISSURES interface) with matching channel IDs,
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
  public boolean findAndOutputNetResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                int stopStageNum,boolean useDelayFlag,boolean showInputFlag,
                                        int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputNetResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                                     verboseFlag,startStageNum,stopStageNum,
                                 useDelayFlag,showInputFlag,false,false,0.0,
                                                   respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses (via FISSURES interface) with matching channel IDs,
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
  public boolean findAndOutputNetResponses(String [] staArr,String [] chaArr,
                       String [] netArr,String [] siteArr,Date beginDateObj,
                      Date endDateObj,int outUnitsConvIdx,double [] freqArr,
               boolean logSpacingFlag,boolean verboseFlag,int startStageNum,
                       int stopStageNum,int respTypeIndex,boolean stdioFlag)
  {
    return findAndOutputNetResponses(staArr,chaArr,netArr,siteArr,
             beginDateObj,endDateObj,outUnitsConvIdx,freqArr,logSpacingFlag,
                                     verboseFlag,startStageNum,stopStageNum,
                                       false,false,respTypeIndex,stdioFlag);
  }

    /**
     * Finds responses (via FISSURES interface) with matching channel IDs.
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
  public boolean findNetResponses(String [] staArr,String [] chaArr,
       String [] netArr,String [] siteArr,Date beginDateObj,Date endDateObj,
                           boolean verboseFlag,RespCallback respCallbackObj)
  {
    if(staArr == null && chaArr == null)
    {    //both station and channel not given; set error message
      setErrorMessage("No station or channel parameters given");
      return false;
    }
    numRespFound = 0;        //clear responses found count
    if(networkFinderObj == null)
    {    //connection to server not yet initialized; do it now
      if(verboseFlag)
      {       //verbose messages enabled; show status message
                   //get corbaloc for server; take out usual leading
                   // and trailing parts (if found):
        String svrStr = propertiesObj.getProperty(NAME_SERVICE_KEY,"");
        final String startStr = "corbaloc:iiop:";
        final String endStr = "/NameService";
        if(svrStr.startsWith(startStr))
          svrStr = svrStr.substring(startStr.length());
        if(svrStr.endsWith(endStr))
          svrStr = svrStr.substring(0,svrStr.length()-endStr.length());
        System.err.println("Connecting to server:  " + svrStr);
      }
      if(!resolveNameService())                       //resolve name service
      {  //error resolving NameService; add helpful error message
        errorMessage += UtilFns.newline + "Check \"" + NAME_SERVICE_KEY +
                                            "\" setting in properties file";
        return false;
      }
      if(verboseFlag)
        System.err.println("Name service resolved on server");
      if(!resolveNetworkFinder())           //resolve net finder
      {  //error resolving 'NetworkFinder' object; add helpful error message
        errorMessage += UtilFns.newline + "Check " +
                    "\"fissures.networkDCname\" setting in properties file";
        return false;
      }
      if(verboseFlag && resolveNetFinderResultStr != null)
      {  //verbose flag set; send status message
        System.err.println(resolveNetFinderResultStr);
      }
    }
         //fetch matching 'NetworkAccess' objects from server:
    final NetworkAccess [] netAccArr;
    if((netAccArr=findNetworks(netArr,beginDateObj,endDateObj,
                                                      verboseFlag)) == null)
    {    //error finding matching network object
      return false;          //return error flag
    }
    if(netAccArr.length <= 0)
    {    //no matches were found; set error message
      setErrorMessage("No matching networks found on server");
      return false;
    }
    if(debugFlag)
    {
      System.err.println("DEBUG:  Matching networks found on server (" +
                                                    netAccArr.length + ")");
    }
    if(!procNetResponses(netAccArr,staArr,chaArr,siteArr,beginDateObj,
                                    endDateObj,verboseFlag,respCallbackObj))
    {    //error finding and processing responses
      return false;          //return error flag
    }
    if(numRespFound <= 0)
    {    //no channel-IDs were found; set error message
      setErrorMessage("No matching channel-IDs found");
      return false;
    }
    if(debugFlag)
    {
      System.err.println("DEBUG:  Matching responses found on server (" +
                                                        numRespFound + ")");
    }
    return true;
  }

    /**
     * Finds responses (via FISSURES interface) with matching channel IDs.
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
  public boolean findNetResponses(String staListStr,String chaListStr,
                     String netListStr,String siteListStr,Date beginDateObj,
           Date endDateObj,boolean verboseFlag,RespCallback respCallbackObj)
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
                                         "in 'findNetResponses()':  " + ex);
      return false;
    }
    return findNetResponses(staArr,chaArr,netArr,siteArr,beginDateObj,
                                    endDateObj,verboseFlag,respCallbackObj);
  }

    /**
     * Queries server for matching 'NetworkAccess' objects.  Each network
     * name is tried with 'NetworkFinder.retrieve_by_code()'; if no matches
     * and the network name contains wildcard characters then all network
     * objects are fetched (via 'NetworkFinder.retrieve_all()') and
     * compared.
     * @param netArr an array of network names to search for.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched networks.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * date range for the network (or null for all dates).
     * @param verboseFlag if true then status messages are sent to 'stderr'.
     * @return An array of 'NetworkAccess' objects, or null if an error
     * occurred (in which case 'getErrorMessage()' may be used to see
     * information about the error).
     */
  public NetworkAccess [] findNetworks(String [] netArr,
                      Date beginDateObj,Date endDateObj,boolean verboseFlag)
  {
    if(netArr == null || netArr.length <= 0)
      netArr = asteriskArray;     //if no data then set to asterisk string
                                  //table of 'NetworkAccess' objects:
    final FifoHashtable netAccessTbl = new FifoHashtable();
    int i;
    String netStr,str;
    NetworkAccess [] netAccessArr = null;
    RE regExPatternObj;
    NetworkAccess nAccObj;
    NetworkAttr nAttribObj;
    for(int netIdx=0; netIdx<netArr.length; ++netIdx)
    {    //for each element in array of network names; set str to net name
      if((netStr=netArr[netIdx]) == null || netStr.length() <= 0)
        netStr = asteriskString;       //if no data then use asterisk
      if(!netStr.equals(asteriskString))
      {  //network name is not '*', try fetching by name
        try
        {               //attempt to find network-access objects for name:
          netAccessArr = networkFinderObj.retrieve_by_code(netStr);
        }
        catch(NetworkNotFound ex)
        {               //network not found exception
          netAccessArr = null;    //indicate no result
        }
        catch(Exception ex)
        {               //exception error; set error message
          setErrorMessage(
                "Error calling 'NetworkFinder.retrieve_by_code()':  " + ex);
          return null;    //return error
        }
      }
      if(netAccessArr != null && netAccessArr.length > 0)
      {  //at least one network-access object returned; add to table
        for(i=0; i<netAccessArr.length; ++i)
        {     //for each network-access object in array
          if((nAccObj=netAccessArr[i]) != null &&
                            (nAttribObj=nAccObj.get_attributes()) != null &&
                         RespUtils.datesInTimeRange(beginDateObj,endDateObj,
                                                 nAttribObj.effective_time))
          {   //object OK and dates are within network time range
                   //create unique ID string for network for use as
                   // table key; if network not already in table then add:
            if(!netAccessTbl.containsKey(str=makeNetAttrString(nAttribObj)))
              netAccessTbl.put(str,nAccObj);
          }
        }
      }
      else
      {  //no network-access objects returned by 'retrieve_by_code()'
        if(RespUtils.containsGlobChars(netStr))
        {     //network name contains at least one wildcard character
          if(allNetworksArray == null)
          {        //array of 'NetworkAccess' objects not yet created
            if(verboseFlag)
            {      //verbose-output flag set
              System.err.println(      //send status message
                       "Retrieving all network-access objects from server");
            }
            try
            {      //fetch 'NetworkAccess' objs for all networks on server:
              if((allNetworksArray=networkFinderObj.retrieve_all()) ==
                                       null || allNetworksArray.length <= 0)
              {    //no 'NetworkAccess' elements in array
                setErrorMessage(       //set error message
                      "No data returned by 'NetworkFinder.retrieve_all()'");
                return null;      //return error
              }
            }
            catch(Exception ex)
            {                //exception error; set error message
              setErrorMessage(
                    "Error calling 'NetworkFinder.retrieve_all()':  " + ex);
                        //setup empty array to prevent reload attempts:
              allNetworksArray = new NetworkAccess[0];
              return null;        //return error
            }
            if(verboseFlag)
            {      //verbose-output flag set; send status message
              System.err.println(allNetworksArray.length +
                                                  " network-access object" +
                                     ((allNetworksArray.length!=1)?"s":"") +
                                                  " retrieved from server");
            }
          }
          try
          {        //compile regular expression pattern string:
            regExPatternObj = new RE(RespUtils.globToRegExString(netStr));
          }
          catch(Exception ex)
          {        //error compile pattern string; set message
            setErrorMessage("Error parsing search pattern \"" + netStr +
                                                              "\":  " + ex);
            return null;     //return error
          }
          for(i=0; i<allNetworksArray.length; ++i)
          {   //for each 'NetworkAccess' object in array
            if((nAccObj=allNetworksArray[i]) != null &&
                            (nAttribObj=nAccObj.get_attributes()) != null &&
                           regExPatternObj.isMatch(nAttribObj.get_code()) &&
                         RespUtils.datesInTimeRange(beginDateObj,endDateObj,
                                                 nAttribObj.effective_time))
            {      //network and attributes objects OK, name matches
                   // and date object null or within network time range
                        //create unique ID string for network for use as
                        // table key; if not already in table then add:
              if(!netAccessTbl.containsKey(
                                         str=makeNetAttrString(nAttribObj)))
              {
                netAccessTbl.put(str,nAccObj);
              }
            }
          }
        }
      }
    }
    if(debugFlag)
    {
      final int vecSize = netAccessTbl.size();
      System.err.println("DEBUG:  " + vecSize +
                                         " matching network-access object" +
                          ((vecSize!=1)?"s":"") + " retrieved from server");
    }
    try
    {         //convert table to array of 'NetworkAccess' objects:
      return (NetworkAccess [])netAccessTbl.getValuesVector().toArray(
                                    new NetworkAccess[netAccessTbl.size()]);
    }
    catch(Exception ex)
    {         //error converting array (shouldn't happen); set message
      setErrorMessage("Error converting table of 'NetworkAccess' " +
                                                "objects to array:  " + ex);
    }
    return null;
  }

    /**
     * Queries server for responses that match-up to the
     * given names, and then processes the responses.  For each network
     * that was found via 'findNetworks()', each set of names is first
     * tried with 'NetworkAccess.retrieve_channels_by_code()'; if no
     * matches and any of the names are empty or contain wildcard
     * characters then all the channel-ID objects for the network are
     * fetched (via 'NetworkAccess.retrieve_all_channels()') and compared.
     * @param netAccArr an array of 'NetworkAccess' objects found via
     * 'findNetworks()'.
     * @param staArr an array of station name patterns to search for,
     * or a null or empty array to accept all station names.
     * @param chaArr an array of channel name patterns to search for,
     * or a null or empty array to accept all channel names.
     * @param siteArr an array of site name patterns to search for,
     * or a null or empty array to accept all site names.
     * @param beginDateObj the beginning of a date range to search for, or
     * null for no begin date.  If no end-date is given then this becomes a
     * single date that must be within the date-range of matched responses.
     * @param endDateObj the end of a date range to search for, or
     * null for no end date.
     * @param verboseFlag if true then status messages are sent to 'stderr'.
     * @param respCallbackObj a 'RespCallback' object whose 'responseInfo()'
     * method will be called to report on each response found.
     * @return true if successful, false if an error occurred (in which
     * case 'getErrorMessage()' may be used to see information about the
     * error).
     */
  public boolean procNetResponses(NetworkAccess [] netAccArr,
      String [] staArr,String [] chaArr,String [] siteArr,Date beginDateObj,
           Date endDateObj,boolean verboseFlag,RespCallback respCallbackObj)
  {
    final Date dateObj = beginDateObj;

    if(staArr == null || staArr.length <= 0)
      staArr = asteriskArray;     //if no data then set to asterisk string
    if(chaArr == null || chaArr.length <= 0)
      chaArr = asteriskArray;     //if no data then set to asterisk string
    if(siteArr == null || siteArr.length <= 0)
      siteArr = new String [] { "" };  //if no site then set to empty string
                        //if only begin-date given then create
                        // 'Time' object version of given date:
    final Time givenDateTimeObj = (beginDateObj != null &&
                                                       endDateObj == null) ?
                    new Time(fDateFormatter.format(beginDateObj),-1) : null;
                             //table of 'ChannelId' objects found:
    final FifoHashtable channelIdTable = new FifoHashtable();
    final Vector chanIdFNameVec = new Vector();  //Vector of chan ID Strings
                                  //get number of 'NetworkAccess' elements:
    final int netArrLen = (netAccArr != null) ? netAccArr.length : 0;
    int staIdx,chaIdx,siteIdx,netIdx,i,vecSize,numAhead,aheadCount;
    String staStr,chaStr,siteStr,str,exceptionStr,dispFName,chIdFName;
    boolean netChIdsFlag,chFoundFlag,rechkDatesFlag;
    NetworkAccess netAccObj;
    Channel [] chanArr;
    ChannelId [] chanIdArr;
    ChannelId chIdObj;
    Instrumentation instObj;
    Vector vec;
    NetworkAttr nAttribObj;
    RE staPatternObj,chaPatternObj,sitePatternObj;
    Object obj;
    for(staIdx=0; staIdx<staArr.length; ++staIdx)
    {    //for each station name
      if((staStr=staArr[staIdx]) == null || staStr.length() <= 0)
        staStr = asteriskString;       //if no data then use asterisk
      staPatternObj = null;            //indicate no pattern yet
      for(chaIdx=0; chaIdx<chaArr.length; ++chaIdx)
      {  //for each channel name
        if((chaStr=chaArr[chaIdx]) == null || chaStr.length() <= 0)
          chaStr = asteriskString;     //if no data then use asterisk
        chaPatternObj = null;          //indicate no pattern yet
        for(siteIdx=0; siteIdx<siteArr.length; ++siteIdx)
        {     //for each site/location name
          if((siteStr=siteArr[siteIdx]) == null || siteStr.length() <= 0)
            siteStr = "";              //if no data then use empty string
          sitePatternObj = null;       //indicate no pattern yet
          if(asteriskString.equals(staStr) &&
                                            asteriskString.equals(chaStr) &&
                                             asteriskString.equals(siteStr))
          {   //station/channel/location all set to wildcard "*"
            setErrorMessage(      //set message
                  "Requesting all stations/channels/locations not allowed");
            return false;         //return error
          }
          for(netIdx=0; netIdx<netArrLen; ++netIdx)
          {   //for each network to be checked
            channelIdTable.clear();    //clear any previous entries
            rechkDatesFlag = false;    //init recheck-dates flag
            if((netAccObj=netAccArr[netIdx]) != null)
            {      //network-access object fetched OK
              if(debugFlag)
              {
                System.err.println("DEBUG:  Processing:  " + staStr + "," +
                                                              chaStr + "," +
                           (((nAttribObj=netAccObj.get_attributes())!=null)?
                               nAttribObj.get_code():"??") + "," + siteStr);
              }
                        //init String that will hold name of current
                        // method in case exception occurrs:
              exceptionStr = null;
              try
              {    //fill 'channelIdTable' with matching channel-IDs:
                chFoundFlag = false;        //init channels-found flag
                   //set flag true if chan-IDs already loaded for network:
                if(!(netChIdsFlag=
                             networkChannelIdsTable.containsKey(netAccObj)))
                {  //array of channel-IDs not yet loaded for network
                        //set name of current method (in case of exception):
                  exceptionStr = "retrieve_channels_by_code()";
                             //attempt with names as-is:
                  if((chanArr=retrieveChannelsByCode(netAccObj,
                      staStr,siteStr,chaStr)) != null && chanArr.length > 0)
                  {     //channel objects returned
                    addChanArrToTable(channelIdTable,chanArr,beginDateObj,
                                                                endDateObj);
                    chFoundFlag = true;     //indicate channels found
                  }          //attempt with "  " for empty site string:
                  else if(siteStr.trim().length() <= 0 &&
                                                    !siteStr.equals("  ") &&
                                  (chanArr=retrieveChannelsByCode(netAccObj,
                         staStr,"  ",chaStr)) != null && chanArr.length > 0)
                  {     //channel objects returned
                    addChanArrToTable(channelIdTable,chanArr,beginDateObj,
                                                                endDateObj);
                    chFoundFlag = true;     //indicate channels found
                  }          //attempt with " " for empty site string:
                  else if(siteStr.trim().length() <= 0 &&
                                                        !siteStr.equals(" ")
                               && (chanArr=retrieveChannelsByCode(netAccObj,
                          staStr," ",chaStr)) != null && chanArr.length > 0)
                  {     //channel objects returned
                    addChanArrToTable(channelIdTable,chanArr,beginDateObj,
                                                                endDateObj);
                    chFoundFlag = true;     //indicate channels found
                  }
                      //clear name of current method (in case of exception):
                  exceptionStr = null;
                }
                if(!chFoundFlag && (RespUtils.containsGlobChars(staStr) ||
                                      RespUtils.containsGlobChars(chaStr) ||
                                      RespUtils.containsGlobChars(siteStr)))
                {  //channel not yet found & names contain wildcard chars
                  str = "";       //initialize string for possible error msg
                  try   //compile regular expression pattern strings
                  {     // (set 'str' to current string being attempted):
                    if(staPatternObj == null)
                    {   //pattern object not yet setup
                      staPatternObj =
                            new RE(RespUtils.globToRegExString(str=staStr));
                    }
                    if(chaPatternObj == null)
                    {   //pattern object not yet setup
                      chaPatternObj =
                            new RE(RespUtils.globToRegExString(str=chaStr));
                    }
                    if(sitePatternObj == null)
                    {   //pattern object not yet setup
                             //trim leading/trailing spaces from location
                             // string to facilitate loc="  " cases:
                      sitePatternObj = new RE(
                           RespUtils.globToRegExString(str=siteStr.trim()));
                    }
                  }
                  catch(Exception ex)
                  {        //error compile pattern string; set message
                    setErrorMessage("Error parsing search pattern \"" +
                                                        str + "\":  " + ex);
                    return false;      //return error
                  }
                  if(!netChIdsFlag)
                  {     //channel-ID array not yet loaded for network
                    if(verboseFlag)
                    {   //verbose flag set; send status message
                      System.err.println(
                                   "Retrieving channel-IDs for network \"" +
                           (((nAttribObj=netAccObj.get_attributes())!=null)?
                            nAttribObj.get_code():"??") + "\" from server");
                    }
                        //set name of current method (in case of exception):
                    exceptionStr = "retrieve_all_channels()";
                             //retrieve all channel-IDs for network:
                    chanIdArr = retrieveAllChannelIds(netAccObj,verboseFlag);
//                    if((chanIdArr=netAccObj.retrieve_all_channels(999999,
//                                        new ChannelIdIterHolder())) == null)
//                    {   //null returned; change to empty array
//                      chanIdArr = new ChannelId[0];
//                    }
                      //clear name of current method (in case of exception):
                    exceptionStr = null;
                    if(verboseFlag)
                    {   //verbose messages enabled
                      if(chanIdArr.length <= 0)
                      {      //no channel-IDs received
                        System.err.println(        //show warning message
                                       "No channel-IDs received for network");
                      }
                      else
                      {      //channel-IDs received; send status message
                        System.err.println(chanIdArr.length +
                                                             " channel-ID" +
                                            ((chanIdArr.length!=1)?"s":"") +
                                                         " for network \"" +
                           (((nAttribObj=netAccObj.get_attributes())!=null)?
                                               nAttribObj.get_code():"??") +
                                                "\" retrieved from server");
                      }
                    }
                        //add channel-ID array for network to table:
                    networkChannelIdsTable.put(netAccObj,chanIdArr);
                  }
                  else  //channel-ID array already loaded for network
                  {          //get channel-ID array for network
                             // (empty array if null or not found):
                    chanIdArr = ((obj=networkChannelIdsTable.get(netAccObj))
                              instanceof ChannelId []) ? (ChannelId [])obj :
                                                           new ChannelId[0];
                  }
                  for(i=0; i<chanIdArr.length; ++i)
                  {     //for each channel-ID object in array
                    if((chIdObj=chanIdArr[i]) != null &&
                              staPatternObj.isMatch(chIdObj.station_code) &&
                              chaPatternObj.isMatch(chIdObj.channel_code) &&
                           sitePatternObj.isMatch(chIdObj.site_code.trim()))
                    {   //channel-ID object OK & station/channel/site match
                      if((endDateObj == null && (beginDateObj == null ||
                                       !RespUtils.isBeforeTime(beginDateObj,
                                                    chIdObj.begin_time))) ||
                             (endDateObj != null && !RespUtils.isBeforeTime(
                                            endDateObj,chIdObj.begin_time)))
                      {      //dates not given or date(s) >= chan begin date
                        if(!channelIdTable.containsKey(
                                            str=makeChannelIdString(chIdObj)))
                        {      //channel-ID not already in table
                          channelIdTable.put(str,chIdObj);   //add to table
                          if(debugFlag)
                          {
                            System.err.println("DEBUG:  Matching object " +
                                  "found in channel-IDs set for " + staStr +
                                             "," + chaStr + ", " + siteStr +
                                                        ", IDstr=\"" + str);
                          }
                        }
                        else if(debugFlag)
                        {
                          System.err.println("DEBUG:  Duplicate matching " +
                                  "object ignored in channel-IDs set for " +
                                    staStr + "," + chaStr + ", " + siteStr +
                                                        ", IDstr=\"" + str);
                        }
                      }
                    }
                  }
                        //set recheck-dates flag if end-date was given
                        // because time-range still needs to be checked:
                  rechkDatesFlag = (endDateObj != null);
                }
                else if(debugFlag && !chFoundFlag)
                {
                  System.err.println(
                       "DEBUG:  No matching channel-ID objects found for " +
                                     staStr + "," + chaStr + "," + siteStr);
                }
                   //processes channel-IDs in 'channelIdTable':
                                  //get Vector of 'ChannelId' objects found:
                vec = channelIdTable.getValuesVector();
                vecSize = vec.size();
                if(verboseFlag)
                {  //verbose messages enabled; note # of matching IDs
                  System.err.println(vecSize + " channel-ID" +
                                          ((vecSize!=1)?"s":"") + " match" +
                                                    ((vecSize!=1)?"":"es"));
                  if(vecSize <= 20)
                  {     //not too many in Vector; display a string for each
                    for(i=0; i<vecSize; ++i)
                    {  //for each 'ChannelId' object in Vector; show string
                      if((obj=vec.elementAt(i)) instanceof ChannelId)
                      {      //'ChannelId' object fetched OK
                        System.err.println("  " +
                             RespUtils.channelIdToEvString((ChannelId)obj));
                      }
                    }
                  }
                }
                for(i=0; i<vecSize; ++i)
                {  //for each 'ChannelId' object in Vector
                  if((obj=vec.elementAt(i)) instanceof ChannelId)
                  {     //'ChannelId' object fetched OK
                    chIdObj = (ChannelId)obj;    //set handle to object
                        //generate display "filename" from channel ID info
                        // (don't include date/time code):
                    dispFName = RespUtils.channelIdToFName(chIdObj,false);
                    if(multiOutputFlag ||
                                        !chanIdFNameVec.contains(dispFName))
                    {   //multi-output or channel-ID not yet processed
                             //check if next channel-ID has the same
                             // station, channel and site codes
                             // (last one wants to be processed first):
                      if(i < vecSize-1 &&
                            (obj=vec.elementAt(i+1)) instanceof ChannelId &&
                               staChaSiteEqualLater(chIdObj,(ChannelId)obj))
                      {      //not at end of Vector and codes match
                        chIdObj = (ChannelId)obj;  //set handle to next elem
                        numAhead = 1;              //indicate moved ahead 1
                        while(++i < vecSize-1 &&
                            (obj=vec.elementAt(i+1)) instanceof ChannelId &&
                               staChaSiteEqualLater(chIdObj,(ChannelId)obj))
                        {    //for each channel-ID with same codes
                          chIdObj = (ChannelId)obj;   //set to next elem
                          ++numAhead;       //increment number moved ahead
                        }
                      }
                      else   //no following channel-IDs with matching codes
                        numAhead = 0;  //indicate not moved ahead
                      aheadCount = 0;       //init counter for # ahead
                             //generate "filename" from channel ID info
                             // (if allowing multiple outputs with same
                             // net.sta.loc.cha then include date/time code):
                      chIdFName = RespUtils.channelIdToFName(chIdObj,
                                                           multiOutputFlag);
                      while(true)
                      { //(loop if needed to go backward after ahead items)
                        if(!multiOutputFlag ||
                                        !chanIdFNameVec.contains(chIdFName))
                        {    //not multi-output or ch-ID not yet processed
                          if(debugFlag)
                          {
                            System.err.println("DEBUG:  Calling " +
                                           "'retrieve_instrumentation(chID=" +
                                    makeChannelIdString(chIdObj) + ", time=" +
                                                    ((givenDateTimeObj!=null)?
                              givenDateTimeObj:chIdObj.begin_time).date_time +
                                                                        ")'");
                          }
                          try       //retrieve 'Instrumentation' object
                          {         // (use given date if not null, otherwise
                                    // use begin-date from channel-ID):
                            instObj = netAccObj.retrieve_instrumentation(
                                            chIdObj,((givenDateTimeObj!=null)?
                                        givenDateTimeObj:chIdObj.begin_time));
                          }
                          catch(ChannelNotFound ex)
                          {         //unable to retrieve object
                            instObj = null;   //indicate failure; set msg
                            respCallbackObj.responseInfo(dispFName,chIdObj,
                                                        null,chIdFName,null,
                                      ("Instrumentation object not found " +
                                             "on server for channel-ID \"" +
                                      makeChannelIdString(chIdObj) + "\""));
                          }
                          catch(Exception ex)
                          {         //error retrieving object; set message
                            instObj = null;   //indicate failure
                            respCallbackObj.responseInfo(dispFName,chIdObj,
                                                        null,chIdFName,null,
                                                  ("Error calling method " +
                                   "'retrieve_instrumentation()':  " + ex));
                          }
                          if(instObj != null && instObj.the_response != null
                                                      && (!rechkDatesFlag ||
                                    RespUtils.datesInTimeRange(beginDateObj,
                                        endDateObj,instObj.effective_time)))
                          {  //'Instrumentation' and 'Response' objects OK
                             // and not rechecking dates or dates are OK
                            if(debugFlag && rechkDatesFlag)
                            {     //debug enabled and recheck done; show msg
                              System.err.println("DEBUG:  Recheck_dates=" +
                                "'yes' on \"" + makeChannelIdString(chIdObj)
                                                                  + "\" (" +
                                 instObj.effective_time.start_time.date_time
                                                                   + " - " +
                                 instObj.effective_time.end_time.date_time +
                                                                       ")");
                            }
                            if(respCallbackObj.responseInfo(dispFName,
                                           chIdObj,RespUtils.fissTimeToDate(
                                           instObj.effective_time.end_time),
                                       chIdFName,instObj.the_response,null))
                            {  //response processed OK;
                                    //add channel-ID to list of processed
                                    // (if multi-out use name with date):
                              chanIdFNameVec.add(multiOutputFlag ?
                                                     chIdFName : dispFName);
                              ++numRespFound;    //inc responses found count
                              if(!multiOutputFlag)    //if not multi-out then
                                break;                //exit "ahead" loop
                            }
                          }
                          else if(debugFlag && rechkDatesFlag &&
                            instObj != null && instObj.the_response != null)
                          {  //debug enabled and recheck done; show msg
                            System.err.println("DEBUG:  Recheck_dates=" +
                                 "'no' on \"" + makeChannelIdString(chIdObj)
                                                                  + "\" (" +
                                 instObj.effective_time.start_time.date_time
                                                                   + " - " +
                                 instObj.effective_time.end_time.date_time +
                                                                       ")");
                          }
                        }
                        else if(debugFlag)
                        {    //debug, multi-output & ch-ID already processed
                          System.err.println("DEBUG:  Ignoring duplicate " +
                            "channel-ID:  " + makeChannelIdString(chIdObj));
                        }
                        if(++aheadCount > numAhead)   //if no more ahead
                          break;                      // then exit loop
                                            //fetch previous channel-ID:
                        if((obj=vec.elementAt(i-aheadCount)) instanceof
                                                                  ChannelId)
                        {    //previous channel-ID object fetched OK
                          chIdObj = (ChannelId)obj;   //set handle to object
                        }
                        else
                        {    //error fetching previous channel-ID object
                          if(debugFlag)
                          {   //debug enabled; show error message
                            System.err.println("DEBUG:  Error fetching " +
                                              "previous channel-ID object");
                          }
                          break;
                        }
                        if(multiOutputFlag)
                        {    //multiple outputs for same net.sta.loc.cha OK
                                  //generate new "filename" from channel
                                  // ID info (include date/time code):
                          chIdFName = RespUtils.channelIdToFName(chIdObj,
                                                           multiOutputFlag);
                        }
                      }
                    }
                    else if(debugFlag)
                    {   //debug, not multi-output & ch-ID already processed
                      System.err.println("DEBUG:  Ignoring duplicate " +
                            "channel-ID:  " + makeChannelIdString(chIdObj));
                    }
                  }
                }
              }
              catch(Exception ex)
              {         //exception error; set message
                setErrorMessage("Error processing responses" +
                    ((exceptionStr!=null)?(" in method '"+exceptionStr+"'"):
                                                          "") + ":  " + ex);
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }

    //Calls 'netAccObj.retrieve_channels_by_code()' and returns an array of
    // retrieved 'Channel' objects, trapping the 'ChannelNotFound' exception
    // and returning an empty array if it occurs.
  private Channel [] retrieveChannelsByCode(NetworkAccess netAccObj,
                                 String staStr,String siteStr,String chaStr)
  {
    Channel [] retArr;
    try
    {                   //call retrieve method
      retArr = netAccObj.retrieve_channels_by_code(staStr,siteStr,chaStr);
    }
    catch(ChannelNotFound ex)
    {                             //if not-found exception then
      retArr = new Channel[0];    //setup to return empty array
    }
    return retArr;
  }

    //Fetches all 'ChannelId' objects for a network.
  private ChannelId [] retrieveAllChannelIds(NetworkAccess netAccObj,
                                                        boolean verboseFlag)
  {
    final int SEQ_INIT = 10;      //initial # of IDs to fetch
    final int SEQ_MAX = 500;      //maximum # of IDs to fetch per call
    final ChannelIdIterHolder iterHolderObj = new ChannelIdIterHolder();
    ChannelId [] chanIdArr;
    if((chanIdArr=netAccObj.retrieve_all_channels(
                                           SEQ_INIT,iterHolderObj)) != null)
    {    //array of 'ChannelId' objects returned OK
      final ChannelIdIter iterObj;
      if((iterObj=iterHolderObj.value) != null)
      {  //Channel-ID iterator returned OK
        final int remainCount;
        if((remainCount=iterObj.how_many_remain()) > 0)
        {     //more IDs remaining
          final long totalCount = (long)(chanIdArr.length) + remainCount;
          if(verboseFlag)
          {   //verbose messages enabled; show status message
            System.err.println("Receiving " + totalCount +
                                                " channel-IDs from server");
          }
                   //convert array to Vector of 'ChannelId' objects:
          final Vector chanIdVec = new Vector(Arrays.asList(chanIdArr));
          final ChannelIdSeqHolder seqHolderObj = new ChannelIdSeqHolder();
                   //fetch next set of IDs; save "more" flag:
          boolean moreFlag = iterObj.next_n(SEQ_MAX-SEQ_INIT,seqHolderObj);
          while(true)   //for each set of IDs fetched
          {
            if((chanIdArr=seqHolderObj.value) == null ||
                                                      chanIdArr.length <= 0)
            {      //no IDs returned
//              System.out.println("retrieveAllChannelIds():  " +
//                         "no channels returned by next_n():  " + chanIdArr);
              break;         //exit loop
            }
                   //convert array to List and add to Vector of IDs:
            chanIdVec.addAll(Arrays.asList(chanIdArr));
            if(verboseFlag && moreFlag)
            {   //verbose messages enabled; show status message
              System.err.println("Received " + chanIdVec.size() + " of " +
                                   totalCount + " channel-IDs from server");
            }
            if(!moreFlag)         //if no more IDs left then
              break;              //exit loop
                   //fetch next set of IDs:
            moreFlag = iterObj.next_n(SEQ_MAX,seqHolderObj);
          }
                   //convert Vector to array of all 'ChannelId' objects:
          chanIdArr = (ChannelId [])(chanIdVec.toArray(
                                          new ChannelId[chanIdVec.size()]));
        }
//        else
//          System.out.println("retrieveAllChannelIds():  all " +
//                     chanIdArr.length + " channels returned on first call");
        try { iterObj.destroy(); }     //release iterator
        catch(Exception ex) {}         //ignore any errors during release
      }
//      else
//        System.out.println("retrieveAllChannelIds():  iter==null; all " +
//                     chanIdArr.length + " channels returned on first call");
    }
    else
    {   //null array object returned
      chanIdArr = new ChannelId[0];         //change to empty array
//      System.out.println("retrieveAllChannelIds():  null returned");
    }
    return chanIdArr;
  }


    //Adds the 'ChannelId' objects from the given 'Channel' array to the
    // given hashtable, leaving out ones that are in range of the given
    // dates (if not null) or were previously added.
  private void addChanArrToTable(FifoHashtable tableObj,Channel [] chanArr,
                                          Date beginDateObj,Date endDateObj)
  {
    Channel chanObj;
    ChannelId chIdObj;
    String str;
    if(debugFlag)
    {    //debug enabled; show info messages
      System.err.println("DEBUG:  Matching channel object(s) " +
                                  "found via retrieve_channels_by_code():");
      boolean dateFlag,putFlag;
      for(int i=0; i<chanArr.length; ++i)
      {  //for each 'Channel' object in array
        if((chanObj=chanArr[i]) != null &&
                                         (chIdObj=chanObj.get_id()) != null)
        {
          dateFlag = RespUtils.datesInTimeRange(beginDateObj,endDateObj,
                                                    chanObj.effective_time);
          putFlag = !tableObj.containsKey(str=makeChannelIdString(chIdObj));
          System.err.println("DEBUG:  " + str + ", inDateRange=" +
                                         dateFlag + ", putFlag=" + putFlag);
        }
        else
          System.err.println("***Null handle***");
      }
    }
    for(int i=0; i<chanArr.length; ++i)
    {    //for each 'Channel' object in array
      if((chanObj=chanArr[i]) != null &&
                                       (chIdObj=chanObj.get_id()) != null &&
                         RespUtils.datesInTimeRange(beginDateObj,endDateObj,
                                                    chanObj.effective_time))
      {       //'Channel' object OK, check if already in table
        if(!tableObj.containsKey(str=makeChannelIdString(chIdObj)))
          tableObj.put(str,chIdObj);        //add to table
      }
    }
  }

    //Makes a string containing information that uniquely identifies the
    // given 'NetworkAttr' object.  Used for hashtable keys.
  private String makeNetAttrString(NetworkAttr netAttrObj)
  {
    if(netAttrObj == null)
      return "(null)";
    String retStr;
    NetworkId netIdObj;      //put in name-code and begin-time from net-ID:
    if((netIdObj=netAttrObj.get_id()) != null)
      retStr = netIdObj.network_code + " " + timeToStr(netIdObj.begin_time);
    else
      retStr = "(null) (null)";
    TimeRange timeRangeObj;  //put in start and end time value:
    if((timeRangeObj=netAttrObj.effective_time) != null)
    {
      retStr += " " + timeToStr(timeRangeObj.start_time) +
                                           timeToStr(timeRangeObj.end_time);
    }
    else
      retStr += " (null) (null)";
    return retStr;
  }

    //Makes a string containing information that uniquely identifies the
    // given 'ChannelId' object.  Used for hashtable keys.
  private String makeChannelIdString(ChannelId chIdObj)
  {
    if(chIdObj == null)
      return "(null)";
                   //put in station and channel codes:
    String retStr = chIdObj.station_code + " " + chIdObj.channel_code;
    NetworkId netIdObj;      //put in name-code and begin-time from net-ID:
    if((netIdObj=chIdObj.network_id) != null)
    {    //network-ID object OK
      retStr += " " + netIdObj.network_code + " " +   //add site code & time
                  timeToStr(netIdObj.begin_time) + " " + chIdObj.site_code +
                                        " " + timeToStr(chIdObj.begin_time);
    }
    else      //network-ID object was null; add site code & time
      retStr += " (null) (null) " + chIdObj.site_code +
                                        " " + timeToStr(chIdObj.begin_time);
    return retStr;
  }

    //Returns String version of time object (with null check).
  private String timeToStr(Time timeObj)
  {
    return (timeObj != null) ? timeObj.date_time : "(null)";
  }

    //Returns true if station, channel and site codes of given channel-ID
    // objects are equal and if begin-time on 'id2Obj' is later or equal
    // to 'id2Obj'.
  private boolean staChaSiteEqualLater(ChannelId id1Obj,ChannelId id2Obj)
  {
    return id1Obj.station_code.equals(id2Obj.station_code) &&
                          id1Obj.channel_code.equals(id2Obj.channel_code) &&
                                id1Obj.site_code.equals(id2Obj.site_code) &&
           RespUtils.compareTimes(id1Obj.begin_time,id2Obj.begin_time) <= 0;
  }
}
