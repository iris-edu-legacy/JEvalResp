//RespUnitsTest.java - Tests units processing.
//
//  11/28/2001 -- [ET]
//

package com.isti.jevalresp.tests;

import java.io.*;
import edu.iris.Fissures.Unit;
import com.isti.util.UtilFns;
import com.isti.jevalresp.*;

/**
 * Class RespUnitsTest tests units processing.
 */
public class RespUnitsTest
{
  public static void main(String [] args)
  {
    final String [] unitConvNames = new String[] {"default","displacement",
                                                 "velocity","acceleration"};
    String nameStr;
    Unit unitObj;
    int idx;
    while(true)
    {
      System.out.print("Enter unit name (<Enter> to exit): ");
      if((nameStr=UtilFns.getUserConsoleString()) == null)
      {
        System.out.println("Error reading input");
        return;
      }
      if(nameStr.length() <= 0)
        return;
      if((unitObj=RespUtils.respStrToUnit(nameStr)) != null)
      {
        System.out.println("Unit:  " + unitObj);
        System.out.print("toUnitsArray(): ");
        final Unit [] unitsArr = RespUtils.toUnitsArray(unitObj);
        for(int i=0; i<unitsArr.length; ++i)
          System.out.print(" " + unitsArr[i]);
        System.out.println();
        System.out.print("toUnitConvIndex() = " +
                            (idx=OutputGenerator.toUnitConvIndex(unitObj)));
        if(idx >= 0 && idx < unitConvNames.length)
          System.out.println(" [" + unitConvNames[idx] + "]");
        else
          System.out.println();
        System.out.println("getFirstUnitPower() = " +
                                       RespUtils.toFirstUnitPower(unitObj));
      }
      else
        System.out.println("Unable to match unit name");

    }
  }
}
