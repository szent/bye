package app.atom.runner;

import java.io.*;

/**
 */
public class Main
{
  public static void main( String[] args )
    throws Exception
  {
    Main main = new Main();
    String name = "auto.atom";
    if ( args.length > 0 )
      name = args[0];
    try
    {
      String p = loadString( name );
      main.runner.parse( p );
      main.runner.print( name + " invoked\n" );
    }
    catch( Exception any )
    {
      main.runner.print( name + " not invoked\n" );
    }
    main.run();
  }


  public static String loadString( String filename )
   throws IOException
  {
    return loadString( new FileInputStream( filename ), true );
  }
  
  public static String loadString( InputStream in )
    throws IOException
  {
    return loadString( in, false );
  }

  public static String loadString( InputStream in, boolean endl )
    throws IOException
  {
    BufferedReader reader = new BufferedReader
    (
      new InputStreamReader( in )
    );
    return loadString( reader, endl );
  }

  public static String loadString( BufferedReader reader, boolean endl )
   throws IOException
  {
    StringBuffer b = new StringBuffer();
    String line;
    for(;;)
    {
      line = reader.readLine();
      if ( line == null )
        break;
      b.append( line );
      if ( endl )
        b.append( '\n' );
    }
    reader.close();
    return b.toString();
  }


  boolean alive;
  Runner runner;

  Main ()
  {
    alive = true;
    runner = new Runner();
    runner.setToploop( new SystemToploop() );
  }

  public void run()
  {
    runner.print( "Atom parser\n enter q to quit\n" );
    while( alive )
    {
      String line = runner.top.readLine();
      if ( line == null )
        break;
      if ( line.equals( "q" ) )
	break;
      runner.parse( line );
    }
  }

}
