package app.atom.runner;


import app.atom.*;

/**
 * Mathematical functions.
 *
 */
public class CalcFunctions implements Functions
{

  @Override
  public boolean parse( Runner runner, Atom ret, String op, AtomList args )
  {
    int argc = args.size();

    if ( "++".equals( op ) )
    {
      Atom val = runner.vars.get( args.second().toString() );
      val.setInt( val.intValue() + 1 );
    }
    else if ( "+".equals( op ) )
    {
      int n = argc-1;
      int si = 1;
      int sum = 0;
      for(int i=0; i<n; i++)
      {
        Atom val = runner.eval( args.get( si++ ) );
        sum += val.intValue();
      }
      ret.setInt( sum );
    }
    else
      return false;
    return true;
  }

  public Atom eval(String arg)
  {
    try
    {
      int i = Integer.parseInt( arg );
      return new Atom( new Integer(i) );
    }
    catch( Exception any )
    {
    }
    return null;
  }
}
