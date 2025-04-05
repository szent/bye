package app.atom.runner;


import app.atom.*;

/**
 * Control functions.
 *
 */
public class ControlFunctions implements Functions
{

  @Override
  public boolean parse( Runner runner, Atom ret, String op, AtomList args )
  {
    int argc = args.size();

    if ( "=".equals( op ) )
    {
      if ( argc == 2 )
      {
        runner.vars.make( args.second().toString() );
      }
      else if ( argc == 3 )
      {
        Atom val = runner.eval( args.third() );
        runner.vars.set( args.second().toString(), val );
        ret.setAs( val );
      }
    }
    else if ( "rep".equals( op ) )
    {
      int n = runner.eval(args.second()).intValue();
      Atom loop = args.third();
      if ( argc > 3 )
      {
        runner.eval( loop );
        loop = args.get( 3 );
      }
      for(int i=0; i<n; i++)
      {
        runner.eval( loop );
      }
    }
    else if ( "if".equals( op ) )
    {
      boolean cond = runner.eval( args.get(1) ).isTrue();
      if ( cond )
      {
        if ( argc > 1 )
          ret.setAs( runner.eval( args.get(2) ) );
      }
      else
      {
        if ( argc > 2 )
          ret.setAs( runner.eval( args.get(3) ) );
      }
    }
    else
      return false;
    return true;
  }

  @Override
  public Atom eval(String arg)
  {
    return null;
  }
}
