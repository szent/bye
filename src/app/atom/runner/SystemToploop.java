package app.atom.runner;

import java.io.*;

/**
 * This interface provide a minimal System related functions for
 * the Runner interpreter.
 */
public class SystemToploop implements Toploop
{
  String lastLine;
  BufferedReader reader;

  public SystemToploop()
  {
    reader = new BufferedReader( new InputStreamReader(System.in) );
  }

  public String readLine()
  {
    try
    {
      String line = reader.readLine();
      if ( line == null )
        return "";
      if ( line.length() != 0 )
        lastLine = line;
      return lastLine;
    }
    catch( IOException ex )
    {
      return "";
    }
  }

  public void print( String s )
  {
    System.out.print( s );
  }

}
