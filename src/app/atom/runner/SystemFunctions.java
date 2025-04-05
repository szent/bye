package app.atom.runner;


import app.atom.*;

/**
 * Functions for system.
 *
 */
public class SystemFunctions implements Functions
{

  public boolean parse( Runner runner, Atom ret, String op, AtomList args )
  {
    int argc = args.size();

    if ( "exit".equals( op ) )
    {
      int exitCode = 0;
      if ( argc > 1 )
        exitCode = runner.eval(args.second()).intValue();
      System.exit( exitCode );
    }
    else if ( "pl".equals( op )  )
    {
      StringBuffer b = new StringBuffer();
      int n = argc-1;
      int si = 1;
      for(int i=0; i<n; i++)
      {
	Atom val = runner.eval( args.get(si++) );
	b.append( val.toString() );
      }
      b.append( '\n' );
      runner.print( b );
    }
    else if ( "input".equals( op ) )
    {
      if ( argc > 0 )
	runner.print( args.first() );
      String line = runner.top.readLine();
      ret.setValue( line );
    }
    else
      return false;
    return true;
  }

  public Atom eval(String arg)
  {
    if ( arg.startsWith( "sys." ) )
    {
      String val = System.getProperty( arg.substring( 4 ) );
      return new Atom(val);
    }
    return null;
  }
}
