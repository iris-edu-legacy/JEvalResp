//ComplexBlk.java:  Holds a complex number.
//
//   11/7/2001 -- [ET]
//

package com.isti.jevalresp;

/**
 * Class ComplexBlk holds a complex number.
 */
public class ComplexBlk
{
  public double real,imag;

  public ComplexBlk(double real,double imag)
  {
    this.real = real;
    this.imag = imag;
  }

    /**
     * Performs complex multiplication; complex version of this *= val.
     * @param val value
     */
  public void zMultiply(ComplexBlk val)
  {
    final double r = real*val.real - imag*val.imag;
    final double i = imag*val.real + real*val.imag;
    real = r;
    imag = i;
  }

    /**
     * Performs complex multiplication; complex version of
     * this *= (realVal,imagVal).
     * @param realVal real value
     * @param imagVal imaginary value
     */
  public void zMultiply(double realVal,double imagVal)
  {
    final double r = real*realVal - imag*imagVal;
    final double i = imag*realVal + real*imagVal;
    real = r;
    imag = i;
  }

  public String toString()
  {
    return "real=" + real + ", imag=" + imag;
  }
}
