//ChanIdHldr.java:  Channel-ID holder that contains handles for a
//                  'ChannelId' object and an end-Date object.
//
//  7/29/2002 -- [ET]
//

package com.isti.jevalresp;

import java.util.Date;
import edu.iris.Fissures.IfNetwork.ChannelId;

/**
 * Class ChanIdHldr is a channel-ID holder that contains handles for a
 * 'ChannelId' object and an end-Date object.
 */
public class ChanIdHldr
{
  public final ChannelId channelIdObj;
  public final Date respEndDateObj;

  public ChanIdHldr(ChannelId channelIdObj,Date respEndDateObj)
  {
    this.channelIdObj = channelIdObj;
    this.respEndDateObj = respEndDateObj;
  }
}
