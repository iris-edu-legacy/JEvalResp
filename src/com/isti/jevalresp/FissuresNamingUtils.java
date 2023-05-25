//FissuresNamingUtils.java:  Defines a set of static methods used to find
//                           a given Network DataCenter object on a
//                           Fissures server.  Uses code lifted from the
//                           'FissuresNamingServiceImpl' class in the
//                           'FissuresUtil' module.
//
//   5/5/2003 -- [ET]
//

package com.isti.jevalresp;

import java.util.StringTokenizer;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkDCHelper;

/**
 * Class FissuresNamingUtils defines a set of static methods used to find
 * a given Network DataCenter object on a Fissures server.  Uses code
 * lifted from the 'FissuresNamingServiceImpl' class in the 'FissuresUtil'
 * module.
 */
public class FissuresNamingUtils
{

    //private constructor so that no object instances may be created
    // (static access only)
  private FissuresNamingUtils()
  {
  }


  /**
   * Returns the NeworkDC object reference in the namingService.
   * @param namingContextObj the <code>NamingContextExt</code> object
   * to use.
   * @param dns a <code>String</code> value.
   * @param objectname a <code>String</code> value.
   * @return a <code>NetworkDC</code> value.
   * @exception NotFound if an error occurs.
   * @exception CannotProceed if an error occurs.
   * @exception org.omg.CosNaming.NamingContextPackage.InvalidName if an
   * error occurs.
   * @exception org.omg.CORBA.ORBPackage.InvalidName if an error occurs.
   */
  public static NetworkDC getNetworkDC(NamingContextExt namingContextObj,
                                               String dns,String objectname)
                                              throws NotFound,CannotProceed,
                         org.omg.CosNaming.NamingContextPackage.InvalidName,
                                        org.omg.CORBA.ORBPackage.InvalidName
  {
    return NetworkDCHelper.narrow(resolve(
                              namingContextObj,dns,"NetworkDC",objectname));
  }


  /**
   * Resolves a CORBA object with the name objectname.
   * @param namingContextObj the <code>NamingContextExt</code> object
   * to use.
   * @param dns a <code>String</code> value.
   * @param interfacename a <code>String</code> value.
   * @param objectname a <code>String</code> value.
   * @return an <code>org.omg.CORBA.Object</code> value.
   * @exception NotFound if an error occurs.
   * @exception CannotProceed if an error occurs.
   * @exception org.omg.CosNaming.NamingContextPackage.InvalidName if an
   * error occurs.
   * @exception org.omg.CORBA.ORBPackage.InvalidName if an error occurs.
   */
  public static org.omg.CORBA.Object resolve(
                               NamingContextExt namingContextObj,String dns,
                                     String interfacename,String objectname)
                                              throws NotFound,CannotProceed,
                         org.omg.CosNaming.NamingContextPackage.InvalidName,
                                        org.omg.CORBA.ORBPackage.InvalidName
  {
    dns = appendKindNames(dns);

    if(interfacename != null && interfacename.length() != 0)
        dns = dns + "/" + interfacename + ".interface";
    if(objectname != null && objectname.length() != 0)
    {
      objectname = objectname;
      dns = dns + "/" + objectname + ".object" + getVersion();
    }
//    logger.info("the final dns resolved is "+dns);
    return namingContextObj.resolve(namingContextObj.to_name(dns));
  }


  /**
   * Appends "kind" names to the various components of given path string.
   * @param dns the input string.
   * @return A new version of the input string with "kind" names added.
   */
  public static String appendKindNames(String dns)
  {
    dns = "Fissures/" + dns + "/";

    StringTokenizer tokenizer = new StringTokenizer(dns, "/");
    String rtnValue = new String();

    while( tokenizer.hasMoreElements() )
    {
      String temp = (String) tokenizer.nextElement();
      temp = temp + ".dns/";
      rtnValue = rtnValue + temp;
    }

    return rtnValue.substring(0, rtnValue.length()-1);
//    rtnValue = rtnValue.substring(0, rtnValue.length()-1);
//    logger.info("The String returned is "+rtnValue);
//    return rtnValue;
  }


  /**
   * Returns a string representing the Fissures version.
   * @return A string representing the Fissures version.
   */
  public static String getVersion()
  {
    String version = edu.iris.Fissures.VERSION.value;
    String rtnValue = new String();
    String prefix = new String("_FVer");

    StringTokenizer tokenizer = new StringTokenizer(version, ".");

    while( tokenizer.hasMoreElements() )
    {
      String temp = (String) tokenizer.nextElement();
      temp = temp + "\\.";
      rtnValue = rtnValue + temp;
    }
    return prefix + rtnValue.substring(0, rtnValue.length() - 2);
  }
}
