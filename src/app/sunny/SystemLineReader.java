package app.sunny;

import java.io.*;

class SystemLineReader
{
  String prompt = "> ";

  BufferedReader reader;

  SystemLineReader()
  {
    reader = new BufferedReader( new InputStreamReader(System.in) );
  }

  String readLine()
  {
    return readLine( prompt );
  }

  String readLine( String p )
  {
    String line = null;
    try
    {
      System.out.print( p );
      line = reader.readLine();
      if ( line == null )
        line = "";
    }
    catch( IOException ex )
    {
      line = "";
    }
    return line;
  }
}
