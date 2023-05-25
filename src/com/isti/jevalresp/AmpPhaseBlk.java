//AmpPhaseBlk.java:  Holds an amplitude and a phase value.
//
//  11/20/2001 -- [ET]  Initial version.
//   3/24/2005 -- [ET]  Made variables be declared "final"; added doc.
//

package com.isti.jevalresp;

/**
 * Class AmpPhaseBlk holds amplitude and a phase value.
 */
public class AmpPhaseBlk
{
    /** Amplitude value. */
  public final double amp;
    /** Phase value. */
  public final double phase;

  /**
   * Creates a block holding an amplitude and a phase value.
   * @param amp amplitude value.
   * @param phase phase value.
   */
  public AmpPhaseBlk(double amp,double phase)
  {
    this.amp = amp;
    this.phase = phase;
  }

  /**
   * Returns a string representation of the values held by this block.
   * @return A string representation of the values held by this block.
   */
  public String toString()
  {
    return "amp=" + amp + ", phase=" + phase;
  }
}
