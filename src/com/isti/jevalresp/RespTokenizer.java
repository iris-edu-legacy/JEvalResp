//RespTokenizer.java:  Extends StreamTokenizer, sets up for
//                     'rdseed' ASCII file parsing.
//
//   9/18/2001 -- [ET]
//

package com.isti.jevalresp;

import java.io.*;

/**
 * Class RespTokenizer extends StreamTokenizer and sets up for
 * 'rdseed' ASCII file parsing.
 */
public class RespTokenizer extends StreamTokenizer
{
  private static final char COMMENT_CHAR = '#';  //define comment character
  private String tokenString=null;          //to hold token string data

    /**
     * Creates an object which reads from the given input stream.
     * @param rdr the input stream to read from.
     */
  public RespTokenizer(Reader rdr)
  {
    super(rdr);                        //call original constructor
    resetSyntax();                     //clear defaults
    whitespaceChars(0,(int)' ');       //define whitespace chars
    wordChars((int)' '+1,127);         //all printable chars for words
    commentChar((int)COMMENT_CHAR);    //define comment character
    eolIsSignificant(true);            //track end-of-lines
  }

    /**
     * Parses the next token from the input stream.
     * @return 'StreamTokenizer.TT_WORD' if a word or a quoted string
     * token was found (retrieve token with 'getTokenString()');
     * 'StreamTokenizer.TT_NUMBER' if a numeric value was parsed
     * (retrieve token with 'getTokenString()'); or 'StreamTokenizer.TT_EOF'
     * if the end of the input stream was reached.
     * @exception IOException if an I/O error occurs.
     */
  public int nextToken() throws IOException
  {
    int tType;

    if((tType=super.nextToken()) != StreamTokenizer.TT_EOF)
    {    //next token is not EOF
      switch(tType)
      {
        case StreamTokenizer.TT_WORD:                 //word token
          tokenString = sval;                         //store it
          break;
        case StreamTokenizer.TT_NUMBER:               //numeric token
          tokenString = Double.toString(nval);        //convert to string
          break;
        default:
      }
    }
    return tType;       //return token type value
  }

    /**
     * Returns the token string fetched by 'nextToken()'.
     * @return the token string, or null if none available.
     */
  public String getTokenString()
  { return tokenString; }

    /**
     * Returns the token string fetched by 'nextToken()'.
     * @return the token string, or an empty string if none available.
     */
  public String getNonNullTokenString()
  { return (tokenString != null) ? tokenString : ""; }

    /**
     * Sends display of tokens to the given print stream.
     * @param out output print stream
     * @exception IOException if an I/O error occurs.
     */
  public void dumpTokens(PrintStream out) throws IOException
  {
    int tType;

    while((tType=super.nextToken()) != StreamTokenizer.TT_EOF)
    {
      switch(tType)
      {
        case StreamTokenizer.TT_WORD:
          out.println("word = " + sval);
          break;
        case StreamTokenizer.TT_NUMBER:
          out.println("number = " + nval);
          break;
        case StreamTokenizer.TT_EOL:
          out.println("end of line");
          break;
        default:
          out.println("char = '" + (char)tType + "'");
      }
    }
  }
}

