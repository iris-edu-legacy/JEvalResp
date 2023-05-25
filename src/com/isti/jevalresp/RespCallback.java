//RespCallback.java:  Specifies a callback method.
//
//  12/10/2001 -- [ET]  Initial release version.
//    5/2/2002 -- [ET]  Added 'setRespProcObj()' method.
//    6/5/2002 -- [ET]  Added 'showInfoMessage()' method.
//   7/29/2002 -- [ET]  Added 'respEndDateObj' parameter to 'responseInfo()'.
//

package com.isti.jevalresp;

import java.util.Date;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Response;
import java.io.File;

/**
 * Interface RespCallback specifies a callback method that provides
 * information about a response.
 */
public interface RespCallback
{
    /**
     * Sets the 'RespProcessor' object to be used.
     * @param respProcObj response processing object
     */
  public void setRespProcObj(RespProcessor respProcObj);

    /**
     * Provides information about a response that has been located and
     * fetched.
     * @param fileName the name of source file for the response.
     * @param channelIdObj the channel ID associated with the response, or
     * null if a channel ID was not found.
     * @param respEndDateObj end date for channel ID, or null if a channel ID
     * was not found.
     * @param channelIdFName a string version of the channel ID associated
     * with the response, or null if a channel ID was not found.
     * @param respObj the response information, or null if a channel ID
     * was not found (error message in 'errMsgStr').
     * @param errMsgStr if 'channelIdObj' or 'respObj' is null then
     * 'errMsgStr' contains an error message string; otherwise null.
     * @return true if the response was processed successfully, false if
     * an error occurred.
     */
  public boolean responseInfo(String fileName,ChannelId channelIdObj,
                                  Date respEndDateObj,String channelIdFName,
                                         Response respObj,String errMsgStr);

    /**
     * Shows the given informational message.
     * @param msgStr message string
     */
  public void showInfoMessage(String msgStr);
}
