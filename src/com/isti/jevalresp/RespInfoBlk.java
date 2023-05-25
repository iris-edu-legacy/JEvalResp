//RespInfoBlk.java:  Holds a block of data describing an evaluated
//                   response.
//
//  12/17/2001 -- [ET]  Initial version.
//   4/21/2005 -- [ET]  Changed "@returns" to "@return".
//

package com.isti.jevalresp;

/**
 * Class RespInfoBlk holds a block of data describing an evaluated
 * response.  (Used by 'RunBlks.java'.)
 */
public class RespInfoBlk
{
         //station, channel, network and site name strings:
  public final String stationName,channelName,networkName,siteName;
  public final ComplexBlk [] cSpectraArray;      //array of response values
  public final double [] freqArr;                //array of frequencies
  public final String fileName;                  //source filename

    /**
     * Constructs a block of data describing an evaluated response.
     * @param stationName station name.
     * @param channelName channel name.
     * @param networkName network name.
     * @param siteName site/location name.
     * @param cSpectraArray array of response values.
     * @param freqArr array of frequencies.
     * @param fileName the name of the file that the response came from.
     */
  public RespInfoBlk(String stationName,String channelName,
             String networkName,String siteName,ComplexBlk [] cSpectraArray,
                                          double [] freqArr,String fileName)
  {
    this.stationName = stationName;
    this.channelName = channelName;
    this.networkName = networkName;
    this.siteName = siteName;
    this.cSpectraArray = cSpectraArray;
    this.freqArr = freqArr;
    this.fileName = fileName;
  }

    /**
     * Constructs a block of data describing an evaluated response.
     * @param stationName station name.
     * @param channelName channel name.
     * @param networkName network name.
     * @param siteName site/location name.
     * @param cSpectraArray array of response values.
     * @param freqArr array of frequencies.
     */
  public RespInfoBlk(String stationName,String channelName,
             String networkName,String siteName,ComplexBlk [] cSpectraArray,
                                                          double [] freqArr)
  {
    this(stationName,channelName,networkName,siteName,cSpectraArray,
                                                                freqArr,"");
  }

    /**
     * Returns a string containing the station, channel, network and
     * site names.
     * @return a string containing the station, channel, network and
     * site names.
     */
  public String toString()
  {
    return "sta=" + stationName + ", cha=" + channelName + ", net=" +
               networkName + ", site=" + siteName;
  }
}
