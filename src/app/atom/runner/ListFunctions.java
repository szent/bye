/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package app.atom.runner;

import app.atom.Atom;
import app.atom.AtomList;

/**
 *
 * @author brown
 */
public class ListFunctions implements Functions
{

  public Atom eval(String arg)
  {
    return null;
  }

  public boolean parse(Runner runner, Atom ret, String op, AtomList args)
  {
    int argc = args.size();

    if ( "ls".equals( op ) )
    {
      if ( argc > 1 )
      {
        AtomList ls = args.second().listValue();
        runner.print( ls );
      }
      else
      {
        runner.print( runner.vars.toString() );
      }
    }
    else if ( "lsc".equals( op ) )
    {
      int size = 0;
      if ( argc > 0 )
      {
        Atom arg = args.get(1);
        if ( arg.isList() )
        {
          size = arg.listValue().size();
        }
      }
      ret.setInt( size );
    }
    else if ( "isls".equals( op ) )
    {
      if ( argc > 0 )
      {
        Atom arg = args.get(1);
        if ( arg.isList() )
          ret.setAs( Atom.one );
      }
    }
    else
      return false;
    return true;

  }

}
