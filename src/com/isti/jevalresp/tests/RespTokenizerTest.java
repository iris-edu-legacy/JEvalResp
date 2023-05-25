//RespTokenizerTest.java - Tests JEvalRespTokenizer functionality.
//
//  12/3/2001 -- [ET]
//

package com.isti.jevalresp.tests;

import java.io.*;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import com.isti.jevalresp.RespTokenizer;

/**
 * Class RespTokenizerTest tests JEvalRespTokenizer functionality.
 */
public class RespTokenizerTest
{
  public static void main(String [] args)
  {


    FileInputStream inStm;
    BufferedReader rdr;
    try
    {
      rdr = new BufferedReader(new FileReader("RESP.UW.ALST.ELE"));
      (new RespTokenizer(rdr)).dumpTokens(System.out);

//      RespFileParser test = RespFileParser.parseChannelId(
//                                  new RespTokenizer(rdr),new ChannelId(
//         new NetworkId("NET",null),"STA","LOC","CHA",new Time("",-1)),null);
      rdr.close();
    }
    catch(Exception ex)
    {
      ex.printStackTrace(System.err);
      return;
    }
  }
}

